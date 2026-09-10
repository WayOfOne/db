#!/usr/bin/env python3
"""ExtractWidgetIds.py -- pull (group, child) widget pairs out of javap output.

DreamBot addresses widgets as int arrays passed to Widgets.get(int[]), built
with the idiom::

    iconst_0
    sipush <group>        (or bipush / iconst_N / ldc // int N)
    iastore
    dup
    iconst_1
    sipush <child>
    iastore
    invokestatic ...Widgets.get:([I)...

Usage:
  javap -classpath <jar> -c -p <class>... > dump.txt
  python tools/ExtractWidgetIds.py dump.txt

Output: one line per call site:  <method> <- [group, child] (javap line N)

Only reports pairs built with the array idiom immediately feeding a
Widgets.get call. iconst_N decodes as N; ldc int constants are read from the
javap comment. Verify every pair against the enclosing method's semantics
before recording it — proximity is evidence, not proof.
"""
import re
import sys

PUSH = r"(?:(?:sipush|bipush)\s+(-?\d+)|iconst_(\d)|ldc\s+#\d+\s+// int (-?\d+))"
NUM = r"(?:sipush|bipush)\s+(-?\d+)"
HEADER = re.compile(r"^  (?:public|private|protected|static|final|synchronized|\s)+[\w<>\[\].$]+\s+(\w+)\(")
SITE = re.compile(
    r"iconst_0\s*\n"                       # index 0
    r"\s*\d+: " + PUSH + r"\s*\n"          # group value
    r"\s*\d+: iastore\s*\n"
    r"(?:\s*\d+: dup\s*\n)?"
    r"\s*\d+: iconst_1\s*\n"               # index 1
    r"\s*\d+: " + PUSH + r"\s*\n"          # child value
    r"\s*\d+: iastore\s*\n"
    r"(?:.*\n){0,6}?"
    r"\s*\d+: invokestatic\s+#\d+\s+// Method .*Widgets\.get:",
)


def value(groups):
    iconst, ldc, num = groups
    if num is not None:
        return int(num)
    if iconst is not None:
        return int(iconst)
    return int(ldc)


def main() -> int:
    if len(sys.argv) != 2:
        print(__doc__)
        return 2
    lines = open(sys.argv[1], encoding="utf-8", errors="replace").read().splitlines(keepends=True)
    text = "".join(lines)
    # line number lookup: char offset -> 1-based line
    offsets = [0]
    for ln in lines:
        offsets.append(offsets[-1] + len(ln))

    def lineno(pos):
        import bisect
        return bisect.bisect_right(offsets, pos)

    method_lines = []  # (1-based line, name)
    for i, ln in enumerate(lines):
        m = HEADER.match(ln)
        if m:
            method_lines.append((i + 1, m.group(1)))

    def enclosing(line):
        name = "<unknown>"
        for hline, hname in method_lines:
            if hline <= line:
                name = hname
            else:
                break
        return name

    for m in SITE.finditer(text):
        g = value((m.group(1), m.group(2), m.group(3)))
        c = value((m.group(4), m.group(5), m.group(6)))
        ln = lineno(m.start())
        print(f"{enclosing(ln)} <- [{g}, {c}] (javap line {ln})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
