"""Parse DreamBot quest-book enums -> (name, grandchild, qp, config, varbit, settings).

Ctor: (String, ordinal, grandchild, qp, configId, varbitId, int[] settings,
req, req). The int[] is built inline as: <len-push> newarray int
(<idx-push> <val-push> iastore)*. Only int pushes that are structural
(len/idx/val around newarray/iastore) are consumed as the table; everything
else before them is scalar args in order.
"""
import re
import sys

path = sys.argv[1]
lines = open(path, encoding="utf-8", errors="replace").read().splitlines()

CLASS = re.compile(r"^public (?:final )?class ([\w.]+)")
PUTSTATIC = re.compile(r"putstatic\s+#\d+\s+// Field (\w+):")
PUSH = re.compile(r"(?:sipush|bipush)\s+(-?\d+)|iconst_(\d)|iconst_m1")


def push_of(s):
    m = PUSH.search(s)
    if not m:
        return None
    if "iconst_m1" in s:
        return -1
    for g in (m.group(1), m.group(2)):
        if g is not None:
            return int(g)
    return None


cur_class = "?"
in_block = False
scalars = []
arr = None
arr_len = None
out = []

for ln in lines:
    s = ln.strip()
    m = CLASS.match(s)
    if m:
        cur_class = m.group(1).split(".")[-1]
        continue
    if re.search(r"new\s+#\d+\s+// class ", s) and "newarray" not in s \
            and "String" not in s and "Requirement" not in s \
            and "Quest" in s:
        in_block = True
        scalars = []
        arr = None
        arr_len = None
        pending_idx = None
        continue
    if not in_block:
        continue
    if "newarray" in s and "int" in s and arr is None and scalars:
        arr_len = scalars.pop()
        arr = {}
        pending_idx = None
        continue
    v = push_of(s)
    if v is not None:
        if arr is not None and arr_len is not None:
            if pending_idx is None:
                pending_idx = v
            else:
                arr[pending_idx] = v
                pending_idx = None
        else:
            scalars.append(v)
        continue
    if "invokespecial" in s and "<init>" in s:
        continue
    m = PUTSTATIC.search(s)
    if m and in_block:
        name = m.group(1)
        # Free/Paid: [nameIdx, ord, gc, qp, config, varbit] + settings.
        # Mini:      [nameIdx, ord, gc, extra, config, varbit] + settings
        #            (qp is constant 0; `extra` meaning unknown, unused).
        if cur_class == "MiniQuest" and len(scalars) >= 6:
            settings = [arr[i] for i in sorted(arr)] if arr else []
            out.append((cur_class, name, scalars[2], 0,
                        scalars[4], scalars[5], settings))
        elif len(scalars) >= 6:
            settings = [arr[i] for i in sorted(arr)] if arr else []
            out.append((cur_class, name, scalars[2], scalars[3],
                        scalars[4], scalars[5], settings))
        in_block = False

for cls, name, gc, qp, cfg, vb, settings in out:
    print(f"{cls} {name} gc={gc} qp={qp} config={cfg} varbit={vb} settings={settings}")
print("total:", len(out))
