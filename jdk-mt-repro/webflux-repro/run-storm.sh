#!/usr/bin/env bash
#
# Cold-start storm: reproduce the Blackbird MethodHandle WrongMethodTypeException
# (FasterXML/jackson-modules-base#142).
#
# Hypothesis: the trigger is a real WebFlux cold start where many DISTINCT nested DTO types are
# first-serialized/deserialized CONCURRENTLY on the small Netty event-loop pool during the first
# request burst, overlapped with heap ramp-up under Shenandoah. So per iteration we:
#   1. launch the app under the field flags (Shenandoah, -Xms32m -Xmx6g)
#   2. wait until it is listening
#   3. fire a CONCURRENT burst hitting ALL ~100 distinct types at once, GET (serialize) + POST
#      (deserialize), as the very first traffic the app ever sees
#   4. scan the app log for WrongMethodTypeException / 5xx
#   5. shut the app down
# Loop many times, because the signal is per-cold-start. On a hit, the app log is preserved and the
# script stops.
#
# Everything is configurable via env vars (see DEFAULTS below).
set -u

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ---- DEFAULTS (override via env) -------------------------------------------------------------
JAVA="${JAVA:-/usr/lib/jvm/java-21-openjdk-fastdebug/bin/java}"
JAR="${JAR:-$HERE/target/webflux-blackbird-repro-1.0.0.jar}"
ITERATIONS="${ITERATIONS:-200}"
PORT="${PORT:-8080}"
PARALLEL="${PARALLEL:-100}"          # concurrency of the first-request burst
NDTO="${NDTO:-100}"                  # number of distinct DTO ids (matches DtoRegistry.COUNT)
GC_FLAGS="${GC_FLAGS:--XX:+UseShenandoahGC}"
HEAP_FLAGS="${HEAP_FLAGS:--Xms32m -Xmx6g}"
EXTRA_JVM_FLAGS="${EXTRA_JVM_FLAGS:-}"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-60}"   # seconds to wait for the port to open
OUTDIR="${OUTDIR:-$HERE/storm-out}"
# ---------------------------------------------------------------------------------------------

mkdir -p "$OUTDIR"

if [[ ! -f "$JAR" ]]; then
  echo "JAR not found: $JAR  (run: mvn -q package -DskipTests)" >&2
  exit 2
fi
if [[ ! -x "$JAVA" ]]; then
  echo "JAVA not executable: $JAVA" >&2
  exit 2
fi

echo "JAVA=$JAVA"
"$JAVA" -version 2>&1 | sed 's/^/  /'
echo "JAR=$JAR"
echo "ITERATIONS=$ITERATIONS  PARALLEL=$PARALLEL  NDTO=$NDTO  PORT=$PORT"
echo "FLAGS: $GC_FLAGS $HEAP_FLAGS $EXTRA_JVM_FLAGS"
echo

# POST bodies (canonical JSON per type) are captured ONCE in a dedicated throwaway launch before
# the loop, and reused for every iteration. This matters: capturing them from inside a storm
# iteration would first-touch the serialize path for all types and defeat the cold-start race we
# are trying to provoke. Capturing once up front keeps each iteration's burst the true first traffic.
BODIES_DIR="$OUTDIR/bodies"

fire_burst() {
  # $1 = app log file (for context only). Fires all ids concurrently: GET then POST.
  local seq_ids
  seq_ids="$(seq 0 $((NDTO - 1)))"

  # GET burst (serialize path)
  echo "$seq_ids" | xargs -P "$PARALLEL" -I{} \
    curl -s -o /dev/null -w "GET %{http_code} {}\n" "http://127.0.0.1:$PORT/obj/{}" \
    > "$OUTDIR/get-codes.txt" 2>&1 &
  local get_pid=$!

  # POST burst (direct readValue deserialize path). Body for id N is the canonical JSON in BODIES_DIR.
  echo "$seq_ids" | xargs -P "$PARALLEL" -I{} \
    curl -s -o /dev/null -w "POST %{http_code} {}\n" \
      -H 'Content-Type: application/json' \
      --data-binary "@$BODIES_DIR/{}.json" \
      "http://127.0.0.1:$PORT/obj/{}" \
    > "$OUTDIR/post-codes.txt" 2>&1 &
  local post_pid=$!

  # Reactive POST burst (/robj/N). Body is taken as Mono<TopDtoN>, so deserialization runs through
  # WebFlux's Jackson2JsonDecoder on the Netty event loop — the Blackbird CreatorOptimizer
  # first-link-on-event-loop path. Same canonical bodies as the direct POST burst.
  echo "$seq_ids" | xargs -P "$PARALLEL" -I{} \
    curl -s -o /dev/null -w "RPOST %{http_code} {}\n" \
      -H 'Content-Type: application/json' \
      --data-binary "@$BODIES_DIR/{}.json" \
      "http://127.0.0.1:$PORT/robj/{}" \
    > "$OUTDIR/rpost-codes.txt" 2>&1 &
  local rpost_pid=$!

  wait "$get_pid" "$post_pid" "$rpost_pid"
}

capture_bodies_once() {
  # Capture canonical JSON for each type from a dedicated throwaway app launch, before the loop.
  if [[ -d "$BODIES_DIR" && -f "$BODIES_DIR/$((NDTO - 1)).json" ]]; then
    echo "Reusing existing POST bodies in $BODIES_DIR"
    return 0
  fi
  mkdir -p "$BODIES_DIR"
  echo "Capturing POST bodies via a throwaway launch..."
  # shellcheck disable=SC2086
  "$JAVA" $GC_FLAGS $HEAP_FLAGS $EXTRA_JVM_FLAGS \
      -jar "$JAR" --server.port="$PORT" > "$OUTDIR/bodies-launch.log" 2>&1 &
  local pid=$!
  if ! wait_for_port; then
    echo "throwaway launch failed to start; see $OUTDIR/bodies-launch.log" >&2
    kill "$pid" 2>/dev/null; wait "$pid" 2>/dev/null
    exit 2
  fi
  for id in $(seq 0 $((NDTO - 1))); do
    curl -s "http://127.0.0.1:$PORT/sample/$id" -o "$BODIES_DIR/$id.json"
  done
  kill "$pid" 2>/dev/null; wait "$pid" 2>/dev/null
  echo "Captured $NDTO POST bodies."
}

wait_for_port() {
  local deadline=$(( $(date +%s) + STARTUP_TIMEOUT ))
  while (( $(date +%s) < deadline )); do
    # /ready returns a plain String and does not first-touch any DTO serialize path.
    if curl -s -o /dev/null --fail "http://127.0.0.1:$PORT/ready"; then return 0; fi
    sleep 0.3
  done
  return 1
}

# Patterns that constitute a hit.
HIT_REGEX='WrongMethodTypeException|handle.s method type|found .*Function'

capture_bodies_once

for i in $(seq 1 "$ITERATIONS"); do
  LOG="$OUTDIR/app-$i.log"
  rm -f "$LOG"

  # Launch the app under the field flags.
  # shellcheck disable=SC2086
  "$JAVA" $GC_FLAGS $HEAP_FLAGS $EXTRA_JVM_FLAGS \
      -jar "$JAR" --server.port="$PORT" > "$LOG" 2>&1 &
  APP_PID=$!

  if ! wait_for_port; then
    echo "[$i/$ITERATIONS] app failed to start within ${STARTUP_TIMEOUT}s; see $LOG" >&2
    # Check the log anyway in case the failure IS the bug at boot.
    if grep -Eq "$HIT_REGEX" "$LOG"; then
      echo "!!! HIT during startup in iteration $i. Log preserved: $LOG"
      kill "$APP_PID" 2>/dev/null; wait "$APP_PID" 2>/dev/null
      exit 0
    fi
    kill "$APP_PID" 2>/dev/null; wait "$APP_PID" 2>/dev/null
    continue
  fi

  fire_burst "$LOG"

  # Decide hit: server-side exception in log, or any 5xx response code.
  HIT=0
  if grep -Eq "$HIT_REGEX" "$LOG"; then HIT=1; fi
  if grep -Eq '^(GET|POST|RPOST) 5[0-9][0-9] ' \
      "$OUTDIR/get-codes.txt" "$OUTDIR/post-codes.txt" "$OUTDIR/rpost-codes.txt" 2>/dev/null; then HIT=1; fi

  if (( HIT )); then
    echo "!!! HIT in iteration $i."
    grep -E "$HIT_REGEX" "$LOG" | head -5
    grep -E '^(GET|POST|RPOST) 5' "$OUTDIR"/*-codes.txt 2>/dev/null | head -10
    SAVED="$OUTDIR/HIT-app-$i.log"
    cp "$LOG" "$SAVED"
    cp "$OUTDIR/get-codes.txt" "$OUTDIR/HIT-get-codes-$i.txt" 2>/dev/null
    cp "$OUTDIR/post-codes.txt" "$OUTDIR/HIT-post-codes-$i.txt" 2>/dev/null
    cp "$OUTDIR/rpost-codes.txt" "$OUTDIR/HIT-rpost-codes-$i.txt" 2>/dev/null
    echo "App log preserved at: $SAVED"
    kill "$APP_PID" 2>/dev/null; wait "$APP_PID" 2>/dev/null
    exit 0
  fi

  echo "[$i/$ITERATIONS] no hit (burst: GET+POST+RPOST x$NDTO). Shutting down."
  kill "$APP_PID" 2>/dev/null; wait "$APP_PID" 2>/dev/null
  # Keep only the last few logs to bound disk use.
  rm -f "$LOG"
done

echo "Completed $ITERATIONS iterations with no reproduction."
exit 1
