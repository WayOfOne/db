#!/usr/bin/env python3
"""ClassByteSearch.py -- UTF-8 anchor scan over class-file bytes, case-safe.

Python port of the helper specified in bot-client/dreambot/deob/README.md.
Reads jar/zip entries directly (no extraction), so obfuscated names that
differ only by case (4cs vs 4cS, 40o vs 40O) can never collapse.

Usage:
  python tools/ClassByteSearch.py <jar> <anchor> [<anchor> ...]
  python tools/ClassByteSearch.py %USERPROFILE%/DreamBot/BotData/repository2/dreambot-client.jar randomUUID SecureRandom

Output: one line per (anchor, entry) hit:  <anchor> :: <entry>
Binary grep tools are noisy on .class files; string constants live in the
constant pool as plain UTF-8, so a byte-substring scan is sufficient to find
which classes mention a literal, API name, superclass, interface, or
annotation type. Confirm every hit with `javap -c -p` before recording it:
presence of a literal is not proof of the data flow.
"""
import sys
import zipfile


def main() -> int:
    if len(sys.argv) < 3:
        print(__doc__)
        return 2
    jar_path = sys.argv[1]
    anchors = [(a, a.encode("utf-8")) for a in sys.argv[2:]]
    with zipfile.ZipFile(jar_path) as zf:
        for info in zf.infolist():
            if not info.filename.endswith(".class"):
                continue
            try:
                data = zf.read(info.filename)
            except KeyError:
                continue
            for text, raw in anchors:
                if raw in data:
                    print(f"{text} :: {info.filename}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
