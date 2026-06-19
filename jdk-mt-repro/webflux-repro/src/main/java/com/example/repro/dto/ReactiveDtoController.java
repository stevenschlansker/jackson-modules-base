package com.example.repro.dto;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Generated. One typed reactive POST handler per distinct DTO type.
 *
 * Each handler takes its body as {@code Mono<TopDtoN>}, which forces WebFlux's
 * Jackson2JsonDecoder (built from the Blackbird-enabled primary ObjectMapper) to
 * deserialize the concrete type reactively on the Netty event loop. This is the
 * Blackbird CreatorOptimizer first-link-on-event-loop path the direct readValue()
 * POST in DtoController does not exercise. The handler logs the executing thread
 * once per type so the event-loop thread can be confirmed, then echoes the object.
 */
@RestController
public class ReactiveDtoController {

    private static final Logger log = LoggerFactory.getLogger(ReactiveDtoController.class);

    // Log the decode thread at most once per type to keep the cold-start storm log readable.
    private final AtomicBoolean[] logged = new AtomicBoolean[100];
    { for (int i = 0; i < logged.length; i++) logged[i] = new AtomicBoolean(); }

    @PostMapping(value = "/robj/0",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto0> post0(@RequestBody Mono<TopDto0> body) {
        return body.doOnNext(v -> {
            if (logged[0].compareAndSet(false, true)) {
                log.info("robj/0 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/1",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto1> post1(@RequestBody Mono<TopDto1> body) {
        return body.doOnNext(v -> {
            if (logged[1].compareAndSet(false, true)) {
                log.info("robj/1 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/2",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto2> post2(@RequestBody Mono<TopDto2> body) {
        return body.doOnNext(v -> {
            if (logged[2].compareAndSet(false, true)) {
                log.info("robj/2 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/3",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto3> post3(@RequestBody Mono<TopDto3> body) {
        return body.doOnNext(v -> {
            if (logged[3].compareAndSet(false, true)) {
                log.info("robj/3 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/4",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto4> post4(@RequestBody Mono<TopDto4> body) {
        return body.doOnNext(v -> {
            if (logged[4].compareAndSet(false, true)) {
                log.info("robj/4 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/5",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto5> post5(@RequestBody Mono<TopDto5> body) {
        return body.doOnNext(v -> {
            if (logged[5].compareAndSet(false, true)) {
                log.info("robj/5 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/6",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto6> post6(@RequestBody Mono<TopDto6> body) {
        return body.doOnNext(v -> {
            if (logged[6].compareAndSet(false, true)) {
                log.info("robj/6 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/7",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto7> post7(@RequestBody Mono<TopDto7> body) {
        return body.doOnNext(v -> {
            if (logged[7].compareAndSet(false, true)) {
                log.info("robj/7 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/8",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto8> post8(@RequestBody Mono<TopDto8> body) {
        return body.doOnNext(v -> {
            if (logged[8].compareAndSet(false, true)) {
                log.info("robj/8 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/9",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto9> post9(@RequestBody Mono<TopDto9> body) {
        return body.doOnNext(v -> {
            if (logged[9].compareAndSet(false, true)) {
                log.info("robj/9 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/10",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto10> post10(@RequestBody Mono<TopDto10> body) {
        return body.doOnNext(v -> {
            if (logged[10].compareAndSet(false, true)) {
                log.info("robj/10 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/11",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto11> post11(@RequestBody Mono<TopDto11> body) {
        return body.doOnNext(v -> {
            if (logged[11].compareAndSet(false, true)) {
                log.info("robj/11 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/12",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto12> post12(@RequestBody Mono<TopDto12> body) {
        return body.doOnNext(v -> {
            if (logged[12].compareAndSet(false, true)) {
                log.info("robj/12 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/13",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto13> post13(@RequestBody Mono<TopDto13> body) {
        return body.doOnNext(v -> {
            if (logged[13].compareAndSet(false, true)) {
                log.info("robj/13 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/14",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto14> post14(@RequestBody Mono<TopDto14> body) {
        return body.doOnNext(v -> {
            if (logged[14].compareAndSet(false, true)) {
                log.info("robj/14 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/15",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto15> post15(@RequestBody Mono<TopDto15> body) {
        return body.doOnNext(v -> {
            if (logged[15].compareAndSet(false, true)) {
                log.info("robj/15 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/16",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto16> post16(@RequestBody Mono<TopDto16> body) {
        return body.doOnNext(v -> {
            if (logged[16].compareAndSet(false, true)) {
                log.info("robj/16 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/17",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto17> post17(@RequestBody Mono<TopDto17> body) {
        return body.doOnNext(v -> {
            if (logged[17].compareAndSet(false, true)) {
                log.info("robj/17 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/18",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto18> post18(@RequestBody Mono<TopDto18> body) {
        return body.doOnNext(v -> {
            if (logged[18].compareAndSet(false, true)) {
                log.info("robj/18 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/19",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto19> post19(@RequestBody Mono<TopDto19> body) {
        return body.doOnNext(v -> {
            if (logged[19].compareAndSet(false, true)) {
                log.info("robj/19 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/20",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto20> post20(@RequestBody Mono<TopDto20> body) {
        return body.doOnNext(v -> {
            if (logged[20].compareAndSet(false, true)) {
                log.info("robj/20 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/21",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto21> post21(@RequestBody Mono<TopDto21> body) {
        return body.doOnNext(v -> {
            if (logged[21].compareAndSet(false, true)) {
                log.info("robj/21 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/22",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto22> post22(@RequestBody Mono<TopDto22> body) {
        return body.doOnNext(v -> {
            if (logged[22].compareAndSet(false, true)) {
                log.info("robj/22 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/23",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto23> post23(@RequestBody Mono<TopDto23> body) {
        return body.doOnNext(v -> {
            if (logged[23].compareAndSet(false, true)) {
                log.info("robj/23 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/24",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto24> post24(@RequestBody Mono<TopDto24> body) {
        return body.doOnNext(v -> {
            if (logged[24].compareAndSet(false, true)) {
                log.info("robj/24 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/25",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto25> post25(@RequestBody Mono<TopDto25> body) {
        return body.doOnNext(v -> {
            if (logged[25].compareAndSet(false, true)) {
                log.info("robj/25 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/26",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto26> post26(@RequestBody Mono<TopDto26> body) {
        return body.doOnNext(v -> {
            if (logged[26].compareAndSet(false, true)) {
                log.info("robj/26 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/27",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto27> post27(@RequestBody Mono<TopDto27> body) {
        return body.doOnNext(v -> {
            if (logged[27].compareAndSet(false, true)) {
                log.info("robj/27 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/28",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto28> post28(@RequestBody Mono<TopDto28> body) {
        return body.doOnNext(v -> {
            if (logged[28].compareAndSet(false, true)) {
                log.info("robj/28 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/29",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto29> post29(@RequestBody Mono<TopDto29> body) {
        return body.doOnNext(v -> {
            if (logged[29].compareAndSet(false, true)) {
                log.info("robj/29 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/30",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto30> post30(@RequestBody Mono<TopDto30> body) {
        return body.doOnNext(v -> {
            if (logged[30].compareAndSet(false, true)) {
                log.info("robj/30 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/31",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto31> post31(@RequestBody Mono<TopDto31> body) {
        return body.doOnNext(v -> {
            if (logged[31].compareAndSet(false, true)) {
                log.info("robj/31 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/32",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto32> post32(@RequestBody Mono<TopDto32> body) {
        return body.doOnNext(v -> {
            if (logged[32].compareAndSet(false, true)) {
                log.info("robj/32 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/33",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto33> post33(@RequestBody Mono<TopDto33> body) {
        return body.doOnNext(v -> {
            if (logged[33].compareAndSet(false, true)) {
                log.info("robj/33 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/34",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto34> post34(@RequestBody Mono<TopDto34> body) {
        return body.doOnNext(v -> {
            if (logged[34].compareAndSet(false, true)) {
                log.info("robj/34 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/35",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto35> post35(@RequestBody Mono<TopDto35> body) {
        return body.doOnNext(v -> {
            if (logged[35].compareAndSet(false, true)) {
                log.info("robj/35 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/36",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto36> post36(@RequestBody Mono<TopDto36> body) {
        return body.doOnNext(v -> {
            if (logged[36].compareAndSet(false, true)) {
                log.info("robj/36 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/37",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto37> post37(@RequestBody Mono<TopDto37> body) {
        return body.doOnNext(v -> {
            if (logged[37].compareAndSet(false, true)) {
                log.info("robj/37 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/38",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto38> post38(@RequestBody Mono<TopDto38> body) {
        return body.doOnNext(v -> {
            if (logged[38].compareAndSet(false, true)) {
                log.info("robj/38 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/39",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto39> post39(@RequestBody Mono<TopDto39> body) {
        return body.doOnNext(v -> {
            if (logged[39].compareAndSet(false, true)) {
                log.info("robj/39 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/40",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto40> post40(@RequestBody Mono<TopDto40> body) {
        return body.doOnNext(v -> {
            if (logged[40].compareAndSet(false, true)) {
                log.info("robj/40 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/41",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto41> post41(@RequestBody Mono<TopDto41> body) {
        return body.doOnNext(v -> {
            if (logged[41].compareAndSet(false, true)) {
                log.info("robj/41 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/42",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto42> post42(@RequestBody Mono<TopDto42> body) {
        return body.doOnNext(v -> {
            if (logged[42].compareAndSet(false, true)) {
                log.info("robj/42 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/43",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto43> post43(@RequestBody Mono<TopDto43> body) {
        return body.doOnNext(v -> {
            if (logged[43].compareAndSet(false, true)) {
                log.info("robj/43 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/44",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto44> post44(@RequestBody Mono<TopDto44> body) {
        return body.doOnNext(v -> {
            if (logged[44].compareAndSet(false, true)) {
                log.info("robj/44 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/45",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto45> post45(@RequestBody Mono<TopDto45> body) {
        return body.doOnNext(v -> {
            if (logged[45].compareAndSet(false, true)) {
                log.info("robj/45 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/46",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto46> post46(@RequestBody Mono<TopDto46> body) {
        return body.doOnNext(v -> {
            if (logged[46].compareAndSet(false, true)) {
                log.info("robj/46 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/47",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto47> post47(@RequestBody Mono<TopDto47> body) {
        return body.doOnNext(v -> {
            if (logged[47].compareAndSet(false, true)) {
                log.info("robj/47 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/48",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto48> post48(@RequestBody Mono<TopDto48> body) {
        return body.doOnNext(v -> {
            if (logged[48].compareAndSet(false, true)) {
                log.info("robj/48 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/49",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto49> post49(@RequestBody Mono<TopDto49> body) {
        return body.doOnNext(v -> {
            if (logged[49].compareAndSet(false, true)) {
                log.info("robj/49 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/50",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto50> post50(@RequestBody Mono<TopDto50> body) {
        return body.doOnNext(v -> {
            if (logged[50].compareAndSet(false, true)) {
                log.info("robj/50 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/51",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto51> post51(@RequestBody Mono<TopDto51> body) {
        return body.doOnNext(v -> {
            if (logged[51].compareAndSet(false, true)) {
                log.info("robj/51 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/52",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto52> post52(@RequestBody Mono<TopDto52> body) {
        return body.doOnNext(v -> {
            if (logged[52].compareAndSet(false, true)) {
                log.info("robj/52 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/53",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto53> post53(@RequestBody Mono<TopDto53> body) {
        return body.doOnNext(v -> {
            if (logged[53].compareAndSet(false, true)) {
                log.info("robj/53 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/54",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto54> post54(@RequestBody Mono<TopDto54> body) {
        return body.doOnNext(v -> {
            if (logged[54].compareAndSet(false, true)) {
                log.info("robj/54 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/55",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto55> post55(@RequestBody Mono<TopDto55> body) {
        return body.doOnNext(v -> {
            if (logged[55].compareAndSet(false, true)) {
                log.info("robj/55 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/56",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto56> post56(@RequestBody Mono<TopDto56> body) {
        return body.doOnNext(v -> {
            if (logged[56].compareAndSet(false, true)) {
                log.info("robj/56 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/57",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto57> post57(@RequestBody Mono<TopDto57> body) {
        return body.doOnNext(v -> {
            if (logged[57].compareAndSet(false, true)) {
                log.info("robj/57 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/58",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto58> post58(@RequestBody Mono<TopDto58> body) {
        return body.doOnNext(v -> {
            if (logged[58].compareAndSet(false, true)) {
                log.info("robj/58 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/59",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto59> post59(@RequestBody Mono<TopDto59> body) {
        return body.doOnNext(v -> {
            if (logged[59].compareAndSet(false, true)) {
                log.info("robj/59 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/60",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto60> post60(@RequestBody Mono<TopDto60> body) {
        return body.doOnNext(v -> {
            if (logged[60].compareAndSet(false, true)) {
                log.info("robj/60 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/61",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto61> post61(@RequestBody Mono<TopDto61> body) {
        return body.doOnNext(v -> {
            if (logged[61].compareAndSet(false, true)) {
                log.info("robj/61 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/62",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto62> post62(@RequestBody Mono<TopDto62> body) {
        return body.doOnNext(v -> {
            if (logged[62].compareAndSet(false, true)) {
                log.info("robj/62 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/63",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto63> post63(@RequestBody Mono<TopDto63> body) {
        return body.doOnNext(v -> {
            if (logged[63].compareAndSet(false, true)) {
                log.info("robj/63 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/64",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto64> post64(@RequestBody Mono<TopDto64> body) {
        return body.doOnNext(v -> {
            if (logged[64].compareAndSet(false, true)) {
                log.info("robj/64 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/65",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto65> post65(@RequestBody Mono<TopDto65> body) {
        return body.doOnNext(v -> {
            if (logged[65].compareAndSet(false, true)) {
                log.info("robj/65 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/66",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto66> post66(@RequestBody Mono<TopDto66> body) {
        return body.doOnNext(v -> {
            if (logged[66].compareAndSet(false, true)) {
                log.info("robj/66 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/67",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto67> post67(@RequestBody Mono<TopDto67> body) {
        return body.doOnNext(v -> {
            if (logged[67].compareAndSet(false, true)) {
                log.info("robj/67 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/68",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto68> post68(@RequestBody Mono<TopDto68> body) {
        return body.doOnNext(v -> {
            if (logged[68].compareAndSet(false, true)) {
                log.info("robj/68 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/69",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto69> post69(@RequestBody Mono<TopDto69> body) {
        return body.doOnNext(v -> {
            if (logged[69].compareAndSet(false, true)) {
                log.info("robj/69 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/70",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto70> post70(@RequestBody Mono<TopDto70> body) {
        return body.doOnNext(v -> {
            if (logged[70].compareAndSet(false, true)) {
                log.info("robj/70 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/71",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto71> post71(@RequestBody Mono<TopDto71> body) {
        return body.doOnNext(v -> {
            if (logged[71].compareAndSet(false, true)) {
                log.info("robj/71 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/72",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto72> post72(@RequestBody Mono<TopDto72> body) {
        return body.doOnNext(v -> {
            if (logged[72].compareAndSet(false, true)) {
                log.info("robj/72 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/73",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto73> post73(@RequestBody Mono<TopDto73> body) {
        return body.doOnNext(v -> {
            if (logged[73].compareAndSet(false, true)) {
                log.info("robj/73 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/74",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto74> post74(@RequestBody Mono<TopDto74> body) {
        return body.doOnNext(v -> {
            if (logged[74].compareAndSet(false, true)) {
                log.info("robj/74 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/75",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto75> post75(@RequestBody Mono<TopDto75> body) {
        return body.doOnNext(v -> {
            if (logged[75].compareAndSet(false, true)) {
                log.info("robj/75 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/76",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto76> post76(@RequestBody Mono<TopDto76> body) {
        return body.doOnNext(v -> {
            if (logged[76].compareAndSet(false, true)) {
                log.info("robj/76 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/77",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto77> post77(@RequestBody Mono<TopDto77> body) {
        return body.doOnNext(v -> {
            if (logged[77].compareAndSet(false, true)) {
                log.info("robj/77 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/78",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto78> post78(@RequestBody Mono<TopDto78> body) {
        return body.doOnNext(v -> {
            if (logged[78].compareAndSet(false, true)) {
                log.info("robj/78 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/79",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto79> post79(@RequestBody Mono<TopDto79> body) {
        return body.doOnNext(v -> {
            if (logged[79].compareAndSet(false, true)) {
                log.info("robj/79 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/80",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto80> post80(@RequestBody Mono<TopDto80> body) {
        return body.doOnNext(v -> {
            if (logged[80].compareAndSet(false, true)) {
                log.info("robj/80 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/81",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto81> post81(@RequestBody Mono<TopDto81> body) {
        return body.doOnNext(v -> {
            if (logged[81].compareAndSet(false, true)) {
                log.info("robj/81 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/82",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto82> post82(@RequestBody Mono<TopDto82> body) {
        return body.doOnNext(v -> {
            if (logged[82].compareAndSet(false, true)) {
                log.info("robj/82 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/83",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto83> post83(@RequestBody Mono<TopDto83> body) {
        return body.doOnNext(v -> {
            if (logged[83].compareAndSet(false, true)) {
                log.info("robj/83 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/84",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto84> post84(@RequestBody Mono<TopDto84> body) {
        return body.doOnNext(v -> {
            if (logged[84].compareAndSet(false, true)) {
                log.info("robj/84 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/85",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto85> post85(@RequestBody Mono<TopDto85> body) {
        return body.doOnNext(v -> {
            if (logged[85].compareAndSet(false, true)) {
                log.info("robj/85 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/86",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto86> post86(@RequestBody Mono<TopDto86> body) {
        return body.doOnNext(v -> {
            if (logged[86].compareAndSet(false, true)) {
                log.info("robj/86 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/87",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto87> post87(@RequestBody Mono<TopDto87> body) {
        return body.doOnNext(v -> {
            if (logged[87].compareAndSet(false, true)) {
                log.info("robj/87 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/88",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto88> post88(@RequestBody Mono<TopDto88> body) {
        return body.doOnNext(v -> {
            if (logged[88].compareAndSet(false, true)) {
                log.info("robj/88 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/89",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto89> post89(@RequestBody Mono<TopDto89> body) {
        return body.doOnNext(v -> {
            if (logged[89].compareAndSet(false, true)) {
                log.info("robj/89 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/90",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto90> post90(@RequestBody Mono<TopDto90> body) {
        return body.doOnNext(v -> {
            if (logged[90].compareAndSet(false, true)) {
                log.info("robj/90 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/91",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto91> post91(@RequestBody Mono<TopDto91> body) {
        return body.doOnNext(v -> {
            if (logged[91].compareAndSet(false, true)) {
                log.info("robj/91 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/92",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto92> post92(@RequestBody Mono<TopDto92> body) {
        return body.doOnNext(v -> {
            if (logged[92].compareAndSet(false, true)) {
                log.info("robj/92 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/93",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto93> post93(@RequestBody Mono<TopDto93> body) {
        return body.doOnNext(v -> {
            if (logged[93].compareAndSet(false, true)) {
                log.info("robj/93 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/94",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto94> post94(@RequestBody Mono<TopDto94> body) {
        return body.doOnNext(v -> {
            if (logged[94].compareAndSet(false, true)) {
                log.info("robj/94 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/95",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto95> post95(@RequestBody Mono<TopDto95> body) {
        return body.doOnNext(v -> {
            if (logged[95].compareAndSet(false, true)) {
                log.info("robj/95 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/96",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto96> post96(@RequestBody Mono<TopDto96> body) {
        return body.doOnNext(v -> {
            if (logged[96].compareAndSet(false, true)) {
                log.info("robj/96 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/97",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto97> post97(@RequestBody Mono<TopDto97> body) {
        return body.doOnNext(v -> {
            if (logged[97].compareAndSet(false, true)) {
                log.info("robj/97 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/98",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto98> post98(@RequestBody Mono<TopDto98> body) {
        return body.doOnNext(v -> {
            if (logged[98].compareAndSet(false, true)) {
                log.info("robj/98 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

    @PostMapping(value = "/robj/99",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TopDto99> post99(@RequestBody Mono<TopDto99> body) {
        return body.doOnNext(v -> {
            if (logged[99].compareAndSet(false, true)) {
                log.info("robj/99 decoded on thread {}", Thread.currentThread().getName());
            }
        });
    }

}
