"""Extract diary tier varbits from javap dumps.

Each area diary has easyFinished/mediumFinished/hardFinished/eliteFinished
methods shaped as: <decrypt> <istore> sipush <varbit> getBitValue <compare> 1.
Prints: CLASS easy medium hard elite (varbit ints, -1 when absent).
"""
import re
import sys

path = sys.argv[1]
lines = open(path, encoding="utf-8", errors="replace").read().splitlines()

CLASS = re.compile(r"^(?:public|final|public final).*class ([\w.]+)")
METHOD = re.compile(r"^  public boolean (easy|medium|hard|elite)Finished\(\);")
PUSH = re.compile(r"(?:sipush|bipush)\s+(-?\d+)|iconst_(\d)")

cur_class = "?"
cur_method = None
pending_push = None
rows = {}


def push_of(s):
    m = PUSH.search(s)
    if not m:
        return None
    if m.group(1) is not None:
        return int(m.group(1))
    return int(m.group(2))


for ln in lines:
    m = CLASS.match(ln.strip())
    if m:
        cur_class = m.group(1)
        rows.setdefault(cur_class, {})
        continue
    m = METHOD.match(ln)
    if m:
        cur_method = m.group(1)
        pending_push = None
        continue
    if cur_method is not None:
        v = push_of(ln)
        if v is not None:
            pending_push = v
        if "getBitValue" in ln and pending_push is not None:
            rows[cur_class][cur_method] = pending_push
            cur_method = None

for cls in sorted(rows):
    r = rows[cls]
    print(cls.split(".")[-1],
          r.get("easy", -1), r.get("medium", -1),
          r.get("hard", -1), r.get("elite", -1))
