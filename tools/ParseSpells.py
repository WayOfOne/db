"""Extract (book, name, child, level, maxhit, autocast) spell rows.

DreamBot book enums (Normal/Ancient/Lunar/Arceuus) construct each spell as
(name, ordinal, child, level, xp, maxhit, autocast, runes...) where xp is a
double (ldc2_w, skipped) and runes ride decrypted name tables (skipped).
Single-arg blocks (teleport group headers) yield (child) only.

Usage: python tools/ParseSpells.py results/javap-c-db-spells.txt
"""
import re
import sys

path = sys.argv[1]
lines = open(path, encoding="utf-8", errors="replace").read().splitlines()

CLASS = re.compile(r"^public final class ([\w.]+)")
PUTSTATIC = re.compile(r"putstatic\s+#\d+\s+// Field (\w+):")
PUSH = re.compile(r"(?:sipush|bipush)\s+(-?\d+)|iconst_(\d)|iconst_m1")

book = "?"
in_block = False
nums = []
computed = False
out = []


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


for ln in lines:
    s = ln.strip()
    m = CLASS.match(s)
    if m:
        book = m.group(1).split(".")[-1]
        continue
    if re.search(r"new\s+#\d+\s+// class ", s) and "newarray" not in s \
            and "String" not in s:
        in_block = True
        nums = []
        computed = False
        continue
    if not in_block:
        continue
    v = push_of(s)
    if v is not None:
        nums.append(v)
    if "laload" in s or "invokedynamic" in s:
        computed = True
    if "invokespecial" in s and "<init>" in s:
        continue
    m = PUTSTATIC.search(s)
    if m and in_block:
        name = m.group(1)
        # nums = [nameIdx, ordinal, child, level?, ...]; single-arg = [ni, ord, child]
        if len(nums) >= 4:
            out.append((book, name, nums[2], nums[3], computed))
        elif len(nums) == 3:
            out.append((book, name, nums[2], -1, computed))
        in_block = False

for book, name, child, level, computed in out:
    flag = " COMPUTED" if computed else ""
    print(f"{book} {name} child={child} level={level}{flag}")
print("total:", len(out))
