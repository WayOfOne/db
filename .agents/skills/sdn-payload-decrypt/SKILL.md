---
name: sdn-payload-decrypt
description: Decrypt and extract DreamBot SDN script payloads (.cache/bin, e.g. a purchased "P2P Master AI" jar) — the three-layer crypto map (DES string framework, Tink AEAD list transport, AES-CBC script payload), offline probe tooling, and the runtime-capture agent that recovers the per-script AES key. Use when a .cache/bin payload needs to be opened, when SDN script crypto must be re-derived after a client update, or when a capture must be replayed offline.
---

# Decrypting DreamBot SDN script payloads (.cache/bin)

You are recovering a script jar from an encrypted DreamBot SDN cache payload.
Everything here targets the user's **own installed client and the user's own
purchased scripts** for clean-room interop documentation, per `AGENTS.md`.
Derived from the `dreambot-client.jar` bytecode on the macOS install
(`~/DreamBot/...`, equivalent to `%USERPROFILE%\DreamBot\...` on Windows),
session 2026-09-17. Full evidence trail: `results/sdn-payload-decrypt-2026-09-17.md`.

## 0. Safety rules (inherited + session-specific)

- Never execute a decrypted script payload. Decrypt → inspect bytecode → document. That is all.
- Never copy `accounts.db`, `account.dat`, `session.dat`, credentials, or tokens.
- The Tink Aead extracted in step 3 also encrypts/decrypts `accounts.db` —
  use it **only** on SDN list data and script payloads, never on account files.
- Stop conditions from `dreambot-deob` apply: the per-script AES key is not on
  disk; it arrives inside the authenticated SDN list response during a normal
  client session. Offline work stops at the keyset; the sanctioned way past it
  is the runtime-capture agent on the user's own normal session (step 5).

## 1. The three crypto layers (verified 2026-09-17 build)

```text
Layer 1  per-class string encryption (every org.dreambot.* class)
         x/t/b long-seeded PRNG -> class key long -> DES/CBC/PKCS5Padding,
         zero IV, big-endian key bytes; ciphertexts in String[] b/c;
         invokedynamic callsites (int,long) dispatch via MutableCallSite;
         method args packed as Object[] and dispatched reflectively.
         -> protects constant strings only (filenames, "AES", suffixes...).

Layer 2  Tink AEAD (org.dreambot.4_W this build)
         cleartext keyset byte[] embedded in <clinit> -> CleartextKeysetHandle
         -> static Aead. Decrypts the SDN script-list JSON with associated
         data = 4-byte big-endian int. Also wraps accounts.db (HANDS OFF).
         -> protects the script catalog in transit (name/id/md5/revision +
            the per-script base64 AES key of layer 3).

Layer 3  script payload (org.dreambot.4cs this build)
         payload = [16-byte IV][AES-CBC/PKCS5Padding body]
         key = Base64.decode(per-script key string from layer 2 list)
         salt = per-script long from the same list, used only to decrypt
         the class's own layer-1 strings at runtime.
         4cs.<init>(long salt, byte[] jarBytes) parses the DECRYPTED jar with
         JarInputStream and serves classes via defineClass.
```

Obfuscated class names (`4_M`, `4_W`, `4cs`, `x`, `t`, `b`) are **per-build**.
Re-derive them with the anchor chain in §2 after any DreamBot update. The
shapes are stable: the loader class holds `URLClassLoader` + Tink `Hex.encode`,
the crypto class holds a `com.google.crypto.tink.Aead` static field, the
payload class holds `JarInputStream` + `defineClass` + `HashMap<String,byte[]>`.

## 2. Anchor chain (how each class was found — reproducible)

Work from the jar, never from extracted trees (case-collapsing names like
`4cs`/`4Cs` on macOS/Windows corrupt them). `jar tf` and
`javap -classpath <jar>` are the only readers.

```bash
JAR=~/DreamBot/BotData/repository2/dreambot-client.jar

# 1. Script framework entry point (stable API names)
javap -classpath $JAR -c -p org.dreambot.api.script.ScriptManager
#    -> static {} holds DES/CBC/PKCS5Padding + a multi-KB encrypted string
#    -> reveals the layer-1 pattern: x.a(JJ,Object).a(J) ^ const = key long

# 2. Layer-1 PRNG classes (pure math, no I/O — safe to run offline)
javap -classpath $JAR -c -p org.dreambot.x   # long-seeded PRNG, a(long) advance
javap -classpath $JAR -c -p org.dreambot.b   # interface: a(J)J, merge ops

# 3. The script loader — intersect constant-pool anchors:
zipgrep -l "java/security/MessageDigest" $JAR   # MD5 hex cache filenames
zipgrep -l "javax/crypto/Cipher"         $JAR   # string framework everywhere (noisy)
zipgrep -l "java/net/URLClassLoader"     $JAR   # script classloaders
#    MessageDigest ∩ Cipher ∩ URLClassLoader -> loader class (4_M on 2026-09-17)
#    shape check: fields URL[] + List<URLClassLoader> + Map<Class,String>,
#    method (Object[])URLClassLoader, Tink Hex.encode for MD5->hex

# 4. The Tink Aead holder
zipgrep -l "com/google/crypto/tink" $JAR        # -> loader, crypto class, one game class
#    the class with a static com.google.crypto.tink.Aead field = list/accounts
#    crypto (4_W on 2026-09-17). Its <clinit>:
#      AeadConfig.register(); BinaryKeysetReader.withBytes(<embedded byte[]>);
#      CleartextKeysetHandle.read(...).getPrimitive(Aead.class)

# 5. The payload class — the strongest anchor in the client:
zipgrep -l "java/util/jar/JarInputStream" $JAR  # -> exactly one class (4cs)
zipgrep -l "defineClass"                  $JAR  # -> same class
#    shape: ctor (long, byte[]) wraps bytes in JarInputStream,
#    HashMap<String,byte[]> entry cache, private static byte[] (Object[])
#    decryptor using Base64.getDecoder + SecretKeySpec + Cipher.doFinal
```

## 3. Offline probe workflow (Layer 1 + Layer 2 extraction)

The framework classes are safe to run offline: verify with
`grep -cE "java/net/|Runtime|System.exit|java/io/File" *.javap` → 0 for the
PRNG trio and for the target `<clinit>` ranges before loading anything.
A stub Logger is required because client classes log during init.

```bash
# one-time stub that shadows org.dreambot.api.utilities.Logger (tools/stubsrc/)
javac stubsrc/org/dreambot/api/utilities/Logger.java -d stub

R=~/DreamBot/BotData/repository2
CP=".:stub:$R/asm-8.0.1.jar:$R/asm-tree-8.0.1.jar:$R/guava-23.2-jre.jar:$R/tink-1.5.0.jar:$R/protobuf-java-3.21.1.jar:$R/gson-2.10.1.jar:$R/okhttp-3.14.9.jar:$R/okio-1.17.2.jar"

# 3a. Derive a per-build class key long (Probe4cs prints 4cs.a:J = 131601469286517 on 2026-09-17)
java -cp "$CP" Probe4cs $JAR

# 3b. Dump decrypted static string tables of any framework class
java -cp "$CP" DumpClassTables $JAR org.dreambot.4_M
#    4_M decrypted fragments this build: "-script.tmp" "libs" "scripts.path"
#    ".jar" "SHA-1" ".class" "db-" "Failed to load jar: "

# 3c. Extract the Tink keyset + Aead, sweep associated-data candidates
java -cp "$CP" DecryptWithTink $JAR ~/DreamBot/BotData/.cache/bin/<hash> out.jar
#    -> keyset.bin (108 bytes, md5 720af1534e8d19d3fabc70bcf001b9f0 this build)
#    -> Aead.decrypt(payload, AD) sweep: ints 0..100 BE, name strings: all fail
#       (expected — the payload is layer 3, not layer 2)
```

What this proves per build: the layer-1 key constants, the decrypted
loader strings, and the layer-2 keyset. It cannot produce the layer-3 AES
key — that value exists only inside the layer-2-protected list response,
which the server serves to authenticated sessions at runtime.

## 4. Payload format (layer 3, confirmed static)

`4cs.5(Object[]{byte[] data, String base64Key, Long salt})` — private static,
called reflectively (Object[]-arg dispatch), swallows its own exceptions:

```java
long l3   = 4cs.a:J ^ salt;                      // a:J = 131601469286517 (2026-09-17)
byte[] iv = Arrays.copyOfRange(data, 0, 16);     // FIRST 16 BYTES = IV
byte[] body = Arrays.copyOfRange(data, 16, data.length);
byte[] key  = Base64.getDecoder().decode(base64Key);
Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");  // INDY-decrypted; 16-byte IV => AES
c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
byte[] jar = c.doFinal(body);                    // -> new 4cs(salt, jar)
```

Payload triage for `~/DreamBot/BotData/.cache/bin/838b43e2b04f367b132c939dc2ae8abf`
(2026-09-17): 7,237,888 bytes = 16-byte IV + 7,237,872 body (mod 16 = 0 ✓),
sha256 `d9a5971e0b0d76a2968bd7136f1f1146eea45107ec4c6a24ed5960de959a5e70`,
md5(ciphertext) `c07f83712b0672f8238e96a7165beb99` ≠ filename. The SAME
filename exists on the Windows install with a DIFFERENT size (6,702,000 B,
2026-09-09 fingerprint), so the filename is revision-stable → derived from a
server-side script identity (SDN id most likely), NOT a content hash.
Disproven: MD5(ciphertext), MD5(plaintext), MD5(name variants
"P2P Master AI"/"P2P_Master_AI"/etc.). Status: Unknown (identity input).
Re-derive sizes/hashes per payload — never quote these as live for other
builds.

## 5. Runtime capture (the sanctioned step past the stop condition)

The per-script base64 key + salt arrive only during a normal, authenticated
client session. Capture them from the user's own session with a local agent —
no server interaction is faked, no auth is bypassed.
**HISTORICAL NOTE: the SDN died 2026-08-31 (~22:07), so live capture is no
longer possible — use §5b artifact recovery instead. Kept here for
reproducibility on archived/air-gapped installs where the service still
exists.**

```bash
cd tools && ./build_agent.sh          # shades ASM into sdn-capture-agent.jar

# instrument ANY DreamBot JVM (Launcher and/or client) via the env var:
export JAVA_TOOL_OPTIONS=-javaagent:$PWD/sdn-capture-agent.jar
# then start DreamBot normally and run/load the target script once.
unset JAVA_TOOL_OPTIONS               # after capture
```

The agent (shape-based, build-agnostic) instruments every `org/dreambot/*`
class whose shape matches the layer-3 decryptor (`static byte[] (Object[])`
body containing `Base64.getDecoder` + `SecretKeySpec` + `Cipher.doFinal`) and
dumps each call's arguments to `~/sdn-capture/`:
`payload bytes` (IV+ciphertext), `base64 key string`, `salt Long`,
declaring-class `a:J`, caller stack, timestamp. Also works after client
updates — no obfuscated names baked in (class prefix overridable via agent
arg). All captured artifacts stay local; nothing is uploaded.

## 5b. SDN end-of-life (2026-08-31) — offline artifact recovery

**The SDN is dead.** Mac client log (`~/DreamBot/Logs/DreamBot/`) timestamps
the death: last successful list refresh `2026-08-31 22:05:34`
("Successfully refreshed scripts!"), then "Unable to connect to server"
cascade from `22:07` onward, forever. Section §5 (runtime capture) therefore
only applies to captures made while the service lived. With the server gone
the AES key can no longer be delivered — but the DECRYPTED JAR hit the
filesystem during every past run, and copies may survive elsewhere.

### Why %TEMP% was a lead — and why it's mostly dead (corrected 2026-09-17)

Two separate script-load paths exist in the loader class:
- **LOCAL scripts**: `4_M.2g(File, Long)` → `File.createTempFile("db-", "-script.tmp")`
  → `402` URLClassLoader. Evidence: the offline `Dump4M` run logged
  `"Failed to load jar: RSFirstScript.jar"` — the local `Scripts/` scan.
  This path writes plaintext temp jars, but only for LOCAL jars, which are
  plaintext on disk anyway.
- **SDN scripts**: payload bytes → `4cs.5` (AES) → `4cs(long, byte[])`
  constructor → **in-memory** `JarInputStream` + `defineClass`. No temp file
  is written on this path (no file-write ops between decrypt and ctor).

So the decrypted SDN jar likely never hit disk. Empiricals: Windows
`%TEMP%\db-*` search came back empty (user, 2026-09-17), and macOS `$TMPDIR`
was already purged. Treat %TEMP%/VSS recovery as near-dead for SDN scripts;
it only recovers local-script copies.

### Mac exhaustion ledger (all ruled out 2026-09-17)

`$TMPDIR`/`/private/var/folders` (3-day purge), `/tmp`, `/var/tmp`,
`backups/` (accounts.db copies only), `Scripts/.cache` (paint images only),
okhttp cache (jagex `jav_config.ws` only — no dreambot.org entries), logs
(timeline evidence only), Time Machine (none configured), APFS snapshots
(OS-update only), **TinkSweep of every non-credential `.cache` file**
(`scripts.dat`, `props`, `latest.sha256`, `launcher-metadata.json`,
`db-bootstrap.json`, `prices.json`, `nodes-latest.db`, the payload itself)
with AD = ints 0–50 + empty: **zero hits** — no local encrypted list cache
exists. `4_W`'s `4t_` Path reader + `"accounts.db"` constant identify it as
the accounts reader (off-limits); the SDN list was network-fetched and kept
in memory only, never persisted.

### Windows checklist — what's still worth one pass (in order)

Target machine: the Windows install from AGENTS.md (user `thewa`; older
`D_Jok` profile may exist). Odds are low after the corrections above — run
these once to close them out:

```powershell
# 1. okhttp cache: any dreambot-domain response cached there? (The list was
#    likely fetched via java.net.http with no disk cache, so expect nothing —
#    but a cached copy would decrypt offline with the extracted keyset.)
Select-String -Path "C:\Users\*\DreamBot\BotData\.cache\okhttp\*.0" -Pattern "dreambot" -List

# 2. Confirm whether P2P Master AI ever RAN on Windows (decides if any
#    in-memory artifact could ever have existed there):
Select-String -Path "C:\Users\*\DreamBot\Logs\DreamBot\*.log*" -Pattern "P2P Master" -List

# 3. %TEMP% direct check (local-script copies only, per the correction):
Get-ChildItem "C:\Users\*\AppData\Local\Temp" -Recurse -File -Filter "db-*" -ErrorAction SilentlyContinue |
  Select-Object FullName, Length, LastWriteTime

# 4. Shadow copies dated Aug 27–Sep 9 (admin) — last resort:
vssadmin list shadows
```

### Verdict if all of the above is negative

The payload is **unrecoverable from these machines**: AES-128 key existed
only in the SDN list response, the service is dead (2026-08-31), no local
ciphertext cache, no plaintext jar on disk, and the list was never written
to any file. Remaining theoretical sources, all outside these disks: another
machine/disk where the client ran while the SDN lived (apply §5's agent
there only if the service still exists on that install — it won't), a full
disk image from the Aug 27–31 window, or a re-release by the script author.
Do not attempt brute force (AES-128) or auth spoofing — both violate the
stop conditions.

## 6. Finish offline after a capture (SdnFinish)

```bash
java -cp "$CP" SdnFinish \
    ~/DreamBot/BotData/.cache/bin/838b43e2b04f367b132c939dc2ae8abf \
    "<captured base64 key>" <captured salt> <captured a:J> ./p2p-master-ai.jar
# SdnFinish replicates 4cs.5 (IV split, AES/CBC/PKCS5), verifies:
#   1. PK\x03\x04 magic           2. MD5(plain) == cache filename
# then `unzip` the jar and read the ScriptManifest + bytecode.
# Evidence discipline: record all values + hashes in results/, treat the
# decrypted jar as read-only reference material for the clean-room fork —
# never ship DreamBot bytecode or the captured key in the repo.
```

## 7. Status ledger (2026-09-17)

| Item | Status |
|---|---|
| Layer-1 scheme (PRNG chain, DES/CBC/PKCS5, zero IV, BE key bytes) | Confirmed static + Confirmed runtime (offline probes ran `<clinit>`s) |
| Derived class key longs: ScriptManager 89269067584905, 4_M a:J 50324244270421, 4cs a:J 131601469286517 | Confirmed runtime, this build only |
| Tink keyset extraction (108 B, md5 720af153…) + list AD = 4-byte BE int | Confirmed static + runtime |
| Payload = 16-byte IV ‖ AES-CBC/PKCS5 body; key/salt from SDN list | Confirmed static (bytecode), cipher name Strong inference (16-byte IV) |
| Cache filename = MD5(decrypted jar) | DISPROVEN (same filename, different sizes on Mac 7.2 MB vs Windows 6.7 MB). Revision-stable → server-side identity input; Unknown which |
| Decrypted jar written to `%TEMP%` as `db-*-script.tmp` during every script load | Confirmed static (createTempFile strings in 4_M); survival depends on exit cleanliness + OS cleanup |
| SDN service | DEAD since 2026-08-31 ~22:07 (mac log evidence); last successful refresh 22:05:34 |
| Mac recovery avenues | ALL EXHAUSTED 2026-09-17 (§5b ledger) |
| Windows `%TEMP%` / okhttp / logs / VSS | Pending inspection (§5b checklist) |
| Per-script AES key/salt values | Unrecoverable while the service is dead, unless a §5b artifact surfaces |
| `.cache/bin` payload count/hash inventory | Re-derive every session (AGENTS.md rule) |
