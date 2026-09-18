# SDN payload decrypt — session 2026-09-17 (macOS install)

Goal: extract the `P2P Master AI` script from `~/DreamBot/BotData/.cache/bin/`.
Outcome: crypto fully mapped across three layers; the per-script AES key is
**not on disk** — it arrives in the authenticated SDN list response at runtime.
Offline work stopped at the sanctioned stop condition; runtime capture agent
built + validated. Skill: `.agents/skills/sdn-payload-decrypt/SKILL.md`.

## Inputs (this machine, 2026-09-17)

| Item | Value |
|---|---|
| `dreambot-client.jar` | 9163575 B, sha256 `66036CB5246A9BD8014A93DCFC7A5AE49DDE0253B1D07DA4AD747EC0C700882A` (matches AGENTS.md fingerprint) |
| `.cache/bin/838b43e2b04f367b132c939dc2ae8abf` | 7,237,888 B, sha256 `d9a5971e0b0d76a2968bd7136f1f1146eea45107ec4c6a24ed5960de959a5e70`, md5 `c07f83712b0672f8238e96a7165beb99` |
| `scripts.dat` | 48 names incl. `P2P Master AI`; plaintext, no keys |
| NOTE | Mac install has no `BotData/client.jar`; `dreambot-client.jar` hosts the SDN loader. Client rev moved to 1.12.37 (`injected-client-1.12.37.jar` present) |

## Anchor walk (commands in SKILL.md §2)

```text
ScriptManager.<clinit> DES/CBC/PKCS5Padding + zero IV + big-endian key long
  -> layer-1 framework: org.dreambot.x (PRNG) / t (merge) / b (iface)
MessageDigest ∩ Cipher ∩ URLClassLoader -> org.dreambot.4_M  (script loader)
com/google/crypto/tink ∩ Aead static    -> org.dreambot.4_W  (list/accounts AEAD)
JarInputStream ∩ defineClass            -> org.dreambot.4cs  (payload classloader)
```

## Layer 1 — string framework (Confirmed static + runtime)

- `<clinit>` per class: `a:J = x.a(seedA, seedB, lookupClass).a(advance)`;
  key long(s) = `a:J ^ const`; DES key = 8 BE bytes of the long; IV = 8×00;
  `DES/CBC/PKCS5Padding`. Ciphertexts in `String[] b/c`; invokedynamic
  `(int,long)` callsites (`n`, `j`, `Û`, `s` bootstrap names); methods take
  packed `Object[]` args (reflective dispatch, exceptions swallowed).
- Offline `<clinit>` execution is safe: `grep -cE "java/net/|Runtime|System.exit|java/io/File"`
  = 0 for `x`, `t`, `b`, `4_M <clinit>`, `4_W <clinit>`. Requires a stub
  `org.dreambot.api.utilities.Logger` + client-bundled deps on the classpath.
- Derived `a:J` this build (Confirmed runtime):
  ScriptManager key long `89269067584905`; `4_M` `50324244270421`;
  `4cs` `131601469286517`.
- `4_M` decrypted fragments: `-script.tmp`, `libs`, `scripts.path`, `.jar`,
  `SHA-1`, `.class`, `db-`, `Failed to load jar: ` (cache layout + integrity).
- `4_M.2V` = MD5 of a file via `MessageDigest`, hex-encoded with
  `com.google.crypto.tink.subtle.Hex.encode` (cache filename source).

## Layer 2 — Tink AEAD (Confirmed static + runtime)

- `4_W.<clinit>`: `AeadConfig.register()` → `BinaryKeysetReader.withBytes(<embedded 108-byte keyset>)`
  → `CleartextKeysetHandle.read(...).getPrimitive(Aead.class)` → static Aead.
- Keyset extracted to `results/sdn-tink-keyset-2026-09-17.bin`
  (108 B, md5 `720af1534e8d19d3fabc70bcf001b9f0`). Cleartext keyset — no wrapping.
- `4_W.3l`: `Aead.decrypt(listBytes, ByteBuffer.allocate(4).putInt(n).array())`
  → Gson → script descriptors (name/id/md5/revision/**base64 AES key**/salt).
- Same Aead instance encrypts/decrypts `accounts.db` — never applied to it here.
- AD sweep against the payload (ints 0–100 BE, short strings): all fail →
  payload is NOT layer-2 protected (expected).

## Layer 3 — payload (Confirmed static; key Unknown offline)

`4cs.5(Object[]{byte[] data, String base64Key, Long salt})`, private static,
reflectively dispatched, swallows exceptions (returns null):

```java
long l3    = 4cs.a:J ^ salt;               // per-script
byte[] iv  = data[0:16];                   // first 16 bytes = IV
byte[] body= data[16:];
byte[] key = Base64.getDecoder().decode(base64Key);
Cipher c   = Cipher.getInstance(<INDY j 30787, 7833828681349606260 ^ l3>); // AES/CBC/PKCS5Padding (inference: 16-byte IV)
c.init(DECRYPT_MODE, new SecretKeySpec(key, <INDY j 24009, ...>), new IvParameterSpec(iv));
byte[] jar = c.doFinal(body);              // -> new 4cs(salt, jar) -> JarInputStream
```

- Payload layout matches: 7,237,888 = 16 (IV) + 7,237,872 (body, mod 16 = 0).
- md5(ciphertext) ≠ filename → filename plausibly MD5(decrypted jar)
  (**Strong inference**, confirm at capture).
- Per-script key/salt exist only in the layer-2-protected list response
  (server-delivered to authenticated sessions). Offline extraction stops here
  per the dreambot-deob stop conditions (needs live session; no auth bypass).

## Runtime capture (built + validated)

- `tools/build_agent.sh` → `sdn-capture-agent.jar` (shape-based premain,
  ASM shaded; instruments any `org/dreambot/*` static `byte[] (Object[])`
  method containing `Base64$Decoder` + `SecretKeySpec` + `Cipher.doFinal`).
- Offline smoke test with the agent attached:
  `[sdn-capture] hooked org/dreambot/4cs.5` and the hook dumped all three
  arguments (byte[] payload, String key, Long salt) + caller stack to
  `~/sdn-capture/<ts>/`. Test artifacts deleted (dummy data only).
- Next session: `JAVA_TOOL_OPTIONS=-javaagent:...` → run script once →
  `SdnFinish` replays AES-CBC offline; verify PK magic + MD5 == filename.

## Reproduce-after-update checklist

1. Re-fingerprint jars (AGENTS.md §0); re-run anchor walk (SKILL.md §2) —
   `4_M`/`4_W`/`4cs` names are per-build.
2. Re-derive `a:J` of the payload class (`Probe4cs`).
3. Re-extract keyset (`DecryptWithTink`) and diff against
   `sdn-tink-keyset-2026-09-17.bin`.
4. Rebuild agent (`build_agent.sh` — shape detection is build-agnostic),
   capture, finish with `SdnFinish` (transformation arg if IV length differs).

## Addendum (same session, post-analysis): SDN end-of-life + recovery sweep

**SDN death timestamped from client logs** (`~/DreamBot/Logs/DreamBot/`):
last successful list refresh `2026-08-31 22:05:34` ("Successfully refreshed
scripts!"), connection failure cascade from `22:07:02` to end of log. Last
successful `P2P Master AI` run: `17:46–17:47` same day ("Now loading…").
DreamBot web-nodes service already failing at `15:27`. No live capture is
possible anymore; §5 of the skill is historical.

**Payload filename hypothesis REVISED**: the identical filename
`838b43e2b04f367b132c939dc2ae8abf` exists on the Windows install with a
different size (6,702,000 B per 2026-09-09 AGENTS.md fingerprint) than the
Mac payload (7,237,888 B) → revision-stable name from server-side script
identity. Disproven: MD5(ciphertext), MD5(plaintext jar), MD5 of name
variants ("P2P Master AI" = bfcaddcc…, "p2p master ai" = 94a21fa0…,
"P2P_Master_AI" = 60450eba…, "P2P Master" = 1a2716f2…, "p2p master" =
b9f04f06… — none match). Status: Unknown.

**Mac exhaustion sweep (all negative, 2026-09-17)**:
- `$TMPDIR`, `/private/var/folders`, `/tmp`, `/var/tmp`: no `db-*` /
  `*-script.tmp` / stray PK-magic jars (macOS 3-day temp purge).
- `BotData/backups/`: 8 × accounts.db backups only (Jul 12–Aug 31; off-limits).
- `Scripts/.cache/Images/`: script paint PNGs only.
- okhttp cache: 100% `oldschool*.runescape.com/jav_config.ws`; zero dreambot
  entries → no cached list ciphertext on this Mac.
- `Scripts/P2P_Master_AI/`: script DATA dir (profiles .p2p, modeldata 14 MB,
  PNGs, stats, sailing9–12) — not the jar; `2k main.p2p` last written
  Aug 31 20:40, `Don OS.json` 22:01.
- Time Machine: no destinations. APFS local snapshots: OS-update only
  (system volume, not user Data).

**Remaining avenues (Windows machine, unchecked)** — full checklist in
`.agents/skills/sdn-payload-decrypt/SKILL.md` §5b:
1. `C:\Users\<user>\AppData\Local\Temp\db-*-script.tmp` — plaintext script
   jars from past runs (Windows does not purge %TEMP%; runs confirmed through
   2026-09-09; expect older 6.7 MB revision).
2. okhttp cache on Windows — a cached dreambot-domain list response decrypts
   offline with `sdn-tink-keyset-2026-09-17.bin` (same dreambot-client.jar
   build on both machines per AGENTS.md fingerprint).
3. Windows `DreamBot\Logs` for run/revision correlation.
4. `vssadmin list shadows` (admin) for Aug 27–Sep 9 shadow copies of %TEMP%.
