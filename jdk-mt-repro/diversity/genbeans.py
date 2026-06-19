#!/usr/bin/env python3
"""
Generates a LARGE population of DISTINCT, NESTED bean classes for the
concurrent-first-link diversity harness (jackson-modules-base#142).

WHY this shape: Blackbird builds a serializer (and interns a MethodType) per
property-per-type lazily on FIRST encounter, and a creator (CreatorOptimizer ->
invokeExact, type (MethodHandle)Function) per @JsonCreator on first deserialize.
The field bug is a durable COLD-START failure, so the trigger we want to hit is
the concurrent FIRST-TIME linkage of a large, diverse population of accessor and
creator MethodTypes. Distinct classes + distinct properties = distinct first-time
linkage events. We therefore emit hundreds of distinct top-level types, each with
nested bean fields (2-3 levels deep) so a single top-level first-link cascades into
many nested first-links, and with primitive boolean fields (the field report's
failing accessor was a primitive boolean -> ()ToBooleanFunction).

The generator emits a single GenBeans.java file (one public top-level class with
many public static nested classes + a registry). All beans are round-trippable:
each has a multi-arg @JsonCreator constructor AND a default ctor + setters, so we
exercise both the serializer-accessor path and the CreatorOptimizer deser path.

Output is deterministic: same seed => same classes, so a rebuild is reproducible.
"""
import sys

N = int(sys.argv[1]) if len(sys.argv) > 1 else 320  # distinct top-level types
SEED = 0x9E3779B9

# Leaf field types we rotate through. boolean is over-weighted because the field
# report's failing accessor was a primitive boolean (()ToBooleanFunction).
LEAF_TYPES = [
    "boolean", "boolean", "boolean",   # over-weight the field-report shape
    "int", "long", "double",
    "String", "Object",
]
ENUMS = ["Color", "Size", "Mode"]


def lcg(state):
    while True:
        state = (state * 1103515245 + 12345) & 0x7FFFFFFF
        yield state


rng = lcg(SEED)


def pick(lst):
    return lst[next(rng) % len(lst)]


def leaf_default(t, salt):
    if t == "boolean":
        return "true" if (salt & 1) else "false"
    if t == "int":
        return str(salt % 1000)
    if t == "long":
        return str(salt % 100000) + "L"
    if t == "double":
        return str((salt % 1000) / 10.0)
    if t == "String":
        return f'"s{salt}"'
    if t == "Object":
        return f'Integer.valueOf({salt % 1000})'
    return "null"


def getter_name(t, field):
    cap = field[0].upper() + field[1:]
    return ("is" if t == "boolean" else "get") + cap


# We build a pool of "child" bean class names first (the deepest level), then mid
# beans that reference children, then top beans that reference mids. This gives
# real 2-3 level nesting where one top-level first-link cascades into many.
classes = []          # list of (name, level) emitted in dependency order
child_names = []
mid_names = []
top_names = []

n_child = max(8, N // 4)
n_mid = max(8, N // 3)
n_top = N


def emit_fields(idx, level, child_pool):
    """Return (field_decls, ctor_params, ctor_assigns, setters, getters, json_pairs)
    for a bean. level 0 = child (leaves only), 1 = mid (leaves + child refs),
    2 = top (leaves + mid refs + List<child>)."""
    n_leaf = 3 + (next(rng) % 4)   # 3..6 leaf fields
    fields = []   # (java_type, name, is_bean, is_list)
    for f in range(n_leaf):
        t = pick(LEAF_TYPES)
        # occasionally use an enum as a leaf
        if next(rng) % 7 == 0:
            t = pick(ENUMS)
        fields.append((t, f"f{f}", False, False))
    if level >= 1 and child_pool:
        # one or two nested bean fields
        for _ in range(1 + (next(rng) % 2)):
            cn = child_pool[next(rng) % len(child_pool)]
            fields.append((cn, f"nested{len(fields)}", True, False))
    if level >= 2 and child_pool:
        # a List<ChildBean> to exercise collection-of-bean accessors
        cn = child_pool[next(rng) % len(child_pool)]
        fields.append((f"java.util.List<{cn}>", f"list{len(fields)}", False, True))
    return fields


def render_bean(name, level, child_pool):
    fields = emit_fields(name, level, child_pool)
    decls, params, assigns, setters, getters, jprops = [], [], [], [], [], []
    for (jt, fn, is_bean, is_list) in fields:
        decls.append(f"        private {jt} {fn};")
        params.append(f'@JsonProperty("{fn}") {jt} {fn}')
        assigns.append(f"            this.{fn} = {fn};")
        cap = fn[0].upper() + fn[1:]
        gname = ("is" if jt == "boolean" else "get") + cap
        getters.append(f"        public {jt} {gname}() {{ return {fn}; }}")
        setters.append(f"        public void set{cap}({jt} {fn}) {{ this.{fn} = {fn}; }}")
    body = []
    body.append(f"    public static final class {name} {{")
    body.extend(decls)
    body.append("")
    # default ctor (setter-based deser path)
    body.append(f"        public {name}() {{}}")
    # multi-arg @JsonCreator (CreatorOptimizer path)
    body.append("        @JsonCreator")
    body.append(f"        public {name}({', '.join(params)}) {{")
    body.extend(assigns)
    body.append("        }")
    body.extend(getters)
    body.extend(setters)
    body.append("    }")
    return "\n".join(body), fields


# Build a deterministic sample-construction expression for a bean so we can make
# a populated instance for serialization (the registry's make()).
def sample_expr(name, fields, depth, child_sample):
    args = []
    for (jt, fn, is_bean, is_list) in fields:
        if is_list:
            inner = jt[jt.index("<") + 1: jt.rindex(">")]
            if depth > 0 and inner in child_sample:
                args.append(f"java.util.List.of({child_sample[inner]}, {child_sample[inner]})")
            else:
                args.append(f"java.util.<{inner}>of()" if False else "java.util.List.of()")
        elif is_bean:
            if depth > 0 and jt in child_sample:
                args.append(child_sample[jt])
            else:
                args.append("null")
        elif jt in ENUMS:
            args.append(f"{jt}.values()[{next(rng) % 3}]")
        else:
            args.append(leaf_default(jt, next(rng)))
    return f"new {name}({', '.join(args)})"


out = []
out.append("import com.fasterxml.jackson.annotation.JsonCreator;")
out.append("import com.fasterxml.jackson.annotation.JsonProperty;")
out.append("")
out.append("/**")
out.append(" * GENERATED by genbeans.py -- do not edit by hand.")
out.append(" *")
out.append(" * A large population of distinct, nested bean classes. Each distinct class and")
out.append(" * each distinct property forces a distinct FIRST-TIME Blackbird accessor/creator")
out.append(" * MethodType linkage -- the concurrent burst of which is what this harness targets")
out.append(" * (jackson-modules-base#142 is a cold-start, first-link failure).")
out.append(" */")
out.append("public final class GenBeans {")
out.append("    public enum Color { RED, GREEN, BLUE }")
out.append("    public enum Size { S, M, L }")
out.append("    public enum Mode { A, B, C }")
out.append("")

# fields per class, kept to build sample exprs
bean_fields = {}

# children (level 0)
for i in range(n_child):
    name = f"C{i}"
    txt, flds = render_bean(name, 0, [])
    out.append(txt)
    out.append("")
    child_names.append(name)
    bean_fields[name] = flds

# mids (level 1) reference children
for i in range(n_mid):
    name = f"M{i}"
    txt, flds = render_bean(name, 1, child_names)
    out.append(txt)
    out.append("")
    mid_names.append(name)
    bean_fields[name] = flds

# tops (level 2) reference mids and a List of children
for i in range(n_top):
    name = f"T{i}"
    txt, flds = render_bean(name, 2, mid_names + child_names)
    out.append(txt)
    out.append("")
    top_names.append(name)
    bean_fields[name] = flds

# child sample expressions (leaves only, no recursion needed -> depth 0 ok)
child_sample = {}
for name in child_names:
    child_sample[name] = sample_expr(name, bean_fields[name], 0, {})
mid_sample = {}
for name in mid_names:
    mid_sample[name] = sample_expr(name, bean_fields[name], 1, child_sample)

# combined pool visible to top sample exprs (mids reference children which are
# already concrete expressions; we inline child exprs into mid exprs above)
all_child_for_top = dict(child_sample)
all_child_for_top.update(mid_sample)

# Registry: COUNT, make(k) -> populated top instance, type(k) -> Class.
out.append("    // ---- registry over the TOP-LEVEL types (the harness unit of work) ----")
out.append(f"    public static final int COUNT = {n_top};")
out.append("")
out.append("    public static Class<?> type(int k) {")
out.append("        switch (k) {")
for i, name in enumerate(top_names):
    out.append(f"            case {i}: return {name}.class;")
out.append("            default: throw new IllegalArgumentException(\"k=\" + k);")
out.append("        }")
out.append("    }")
out.append("")
out.append("    public static Object make(int k) {")
out.append("        switch (k) {")
for i, name in enumerate(top_names):
    expr = sample_expr(name, bean_fields[name], 2, all_child_for_top)
    out.append(f"            case {i}: return {expr};")
out.append("            default: throw new IllegalArgumentException(\"k=\" + k);")
out.append("        }")
out.append("    }")
out.append("}")

with open("GenBeans.java", "w") as fh:
    fh.write("\n".join(out) + "\n")

print(f"GenBeans.java: top={n_top} mid={n_mid} child={n_child} "
      f"total={n_top + n_mid + n_child} nested classes")
