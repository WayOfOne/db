# sdn-payload-decrypt tools

Method + findings live in `../SKILL.md`. All probes are offline and load only
the client's own framework classes (I/O-free by javap inspection) — they never
execute script payloads or touch account files.

## Layout

| File | Job |
|---|---|
| `stubsrc/.../Logger.java` | Shadow `org.dreambot.api.utilities.Logger` so framework classes load without client context |
| `Probe4cs.java` | Load a framework class, print its derived `a:J` key long, shape-detect + dummy-probe the payload decryptor |
| `DumpClassTables.java` | Dump decrypted static string tables / key maps of any framework class |
| `DecryptWithTink.java` | Extract the embedded Tink keyset + Aead; sweep associated-data candidates against a payload |
| `SdnFinish.java` | Replay `4cs.5` offline from captured key/salt/a:J; verifies PK magic + MD5 vs filename |
| `CaptureAgent.java` / `CaptureHook.java` / `build_agent.sh` | Runtime capture of the per-script key during a normal client session |
| `sdn-tink-keyset-2026-09-17.bin` → in `results/` | Extracted 108-byte cleartext Tink keyset (build 2026-09-17) |

## Build & run (macOS; Windows uses %USERPROFILE% paths)

```bash
R="$HOME/DreamBot/BotData/repository2"
javac stubsrc/org/dreambot/api/utilities/Logger.java -d stub
CP=".:stub:$R/asm-8.0.1.jar:$R/asm-tree-8.0.1.jar:$R/guava-23.2-jre.jar:$R/tink-1.5.0.jar:$R/protobuf-java-3.21.1.jar:$R/gson-2.10.1.jar:$R/okhttp-3.14.9.jar:$R/okio-1.17.2.jar"

javac -cp "$CP" Probe4cs.java DumpClassTables.java DecryptWithTink.java SdnFinish.java

java -cp "$CP" Probe4cs        "$R/dreambot-client.jar" org.dreambot.4cs
java -cp "$CP" DumpClassTables "$R/dreambot-client.jar" org.dreambot.4_M
java -cp "$CP" DecryptWithTink "$R/dreambot-client.jar" org.dreambot.4_W \
    "$HOME/DreamBot/BotData/.cache/bin/<hash>" out.jar

./build_agent.sh
export JAVA_TOOL_OPTIONS=-javaagent:"$PWD/sdn-capture-agent.jar"
# start DreamBot normally, run the target script once, then unset JAVA_TOOL_OPTIONS

java -cp "$CP" SdnFinish "$HOME/DreamBot/BotData/.cache/bin/<hash>" \
    "<captured base64 key>" <salt> <a:J> ./script.jar
```

Re-derive class names (`4cs`, `4_W`, `4_M`) per build via the anchor chain in
`SKILL.md` §2 — never reuse the ones above against a newer client.
