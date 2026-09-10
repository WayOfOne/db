"""Parse DreamBot enum static{} blocks + static int[] tables from javap dumps.

Block mode (default): for each `new <class>` .. invokespecial <init> ..
putstatic NAME, print NAME, init-owner, ordinal, int-args, and whether any
arg came from a decrypted/computed source (laload array or invokedynamic)
rather than a plain push. Those values are Unknown, not constants.

Table mode (--table <file> --field <name>): extract a static int[] built
with the dup/iconst/iastore idiom and stored via putstatic <name>.

Usage:
  python tools/ParsePrayerEnum.py results/javap-c-db-prayer-enum.txt
  python tools/ParsePrayerEnum.py --table results/javap-c-db-X.txt --field 0
"""
import re
import sys

PUSH = re.compile(
    r"(?:sipush|bipush)\s+(-?\d+)|iconst_(\d)|iconst_m1"
    r"|ldc\s+#\d+\s+// int (-?\d+)"
)


def push_value(s):
    m = PUSH.search(s)
    if not m:
        return None
    if "iconst_m1" in s:
        return -1
    for g in (m.group(1), m.group(2), m.group(3)):
        if g is not None:
            return int(g)
    return None


def block_mode(path):
    lines = open(path, encoding="utf-8", errors="replace").read().splitlines()
    in_block = False
    nums = []
    computed = False
    owner = "?"
    out = []
    for ln in lines:
        s = ln.strip()
        if re.search(r"new\s+#\d+\s+// class ", s) and "newarray" not in s:
            in_block = True
            nums = []
            computed = False
            owner = "?"
            continue
        if not in_block:
            continue
        v = push_value(s)
        if v is not None:
            nums.append(v)
        if "laload" in s or "invokedynamic" in s:
            computed = True
        if "invokespecial" in s and "<init>" in s:
            m = re.search(r"Method\s+(?:(\S+)\.)?\"<init>\"", s)
            owner = m.group(1) if m and m.group(1) else "Prayer"
            # init descriptor arity check happens on next lines; keep scanning
            # until putstatic
            continue
        if "putstatic" in s and owner != "?":
            m = re.search(r"// Field (\w+):", s)
            name = m.group(1) if m else "?"
            out.append((name, owner, nums, computed))
            in_block = False
            owner = "?"
    for name, owner, nums, computed in out:
        flag = " COMPUTED-ARGS" if computed else ""
        print(f"{name} {owner} {nums}{flag}")
    print("total constants:", len(out))


def table_mode(path, field):
    lines = open(path, encoding="utf-8", errors="replace").read().splitlines()
    # find `putstatic ... Field <field>:` preceded by newarray/iastore fills
    vals = {}
    cur_fill = None
    for i, ln in enumerate(lines):
        s = ln.strip()
        if "newarray" in s and "int" in s:
            cur_fill = {}
            continue
        if cur_fill is not None:
            m = re.match(r"\d+: (iconst_\d|bipush\s+-?\d+|sipush\s+-?\d+)", s)
            # track index/value pairs around iastore
            pass
        m = re.search(r"putstatic\s+#\d+\s+// Field \"" + re.escape(field) + r"\"", s)
        if m:
            # walk backwards collecting iastore triples
            idx = None
            for j in range(i - 1, max(i - 200, -1), -1):
                t = lines[j].strip()
                if "putstatic" in t or "putfield" in t:
                    break
                v = push_value(t)
                if "iastore" in t:
                    idx = None
                    continue
                if v is not None:
                    if idx is None:
                        idx = v  # value comes after index in fill order; handled below
                    else:
                        vals[v] = idx
                        idx = None
            break
    # The backwards walk above pairs (value,index) reversed; redo forward parse
    # of the fill window properly.
    vals = {}
    # locate window: last newarray-int before the putstatic
    put_idx = None
    for i, ln in enumerate(lines):
        if re.search(r"putstatic\s+#\d+\s+// Field \"" + re.escape(field) + r"\"", ln):
            put_idx = i
            break
    if put_idx is None:
        print(f"field {field!r} not found")
        return
    new_idx = max(
        (i for i in range(put_idx) if "newarray" in lines[i] and "int" in lines[i]),
        default=None,
    )
    if new_idx is None:
        print("no int[] fill found")
        return
    pending = []
    for ln in lines[new_idx:put_idx]:
        s = ln.strip()
        v = push_value(s)
        if v is not None:
            pending.append(v)
        if "iastore" in s and len(pending) >= 2:
            vals[pending[-2]] = pending[-1]
            pending = []
    for k in sorted(vals):
        print(f"[{k}] = {vals[k]}")


if __name__ == "__main__":
    if len(sys.argv) >= 2 and sys.argv[1] == "--table":
        path = sys.argv[2]
        field = sys.argv[4] if len(sys.argv) > 4 else "0"
        table_mode(path, field)
    else:
        block_mode(sys.argv[1])
