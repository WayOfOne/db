# DreamBot Client Mappings (ported from bot-client)

Carried over from `bot-client/docs/dreambot-random-dat-hwid-findings.md`,
`bot-client/docs/dreambot-sdn-script-findings.md`, and
`bot-client/dreambot/deob/README.md`, then **re-verified 2026-09-09 against
the live jars** (see `results/phase1-reverification.md` for the evidence log).
Rows marked [DRIFT] differ from the bot-client docs on this build.
Obfuscated names are per-build shapes, not constants.

> Staleness note (2026-09-09): bot-client docs reference
> `C:\Users\D_Jok\DreamBot\...` and five `.cache\bin` payloads. On
> `C:\Users\thewa\DreamBot\...` the catalog still names `P2P Master AI` but
> `.cache\bin` holds one payload (`838b43e2b04f367b132c939dc2ae8abf`,
> 6702000 bytes, SHA-256 `439bd4d5…8ecca1`, magic `41 7E 5F AB`, not `PK`).
> Quote current inventory, not the old hashes.

## 1. Injected-client map (`injected-client-1.12.36.jar`)

| Class | Members | Meaning (behavior-assigned) | Status |
|---|---|---|---|
| `me` | `ao(String,String,String,int,int)` + variants, static `aw:Laac;` (plus sibling `al`, `aj`) | Field owner; `ao` still exists but no longer touches `aw` | Confirmed static |
| `cl` | `az(String,String,String,IIB)` | [DRIFT] Cache setup now lives here: `new File(ie.ap,"random.dat")`, legacy `separatorChar+"random.dat"` loop, `RandomAccessFile` read/seek/write/seek/close fallback; 3× `putstatic me.aw`; called 6× from `client` | Confirmed static |
| `aar` | `(File,"rw",25L)` wrapper | Random-access file wrapper around `random.dat` | Confirmed static |
| `aac` | seek/read (`ay`, `ag`) | Buffered wrapper; `new aac(new aar(f,"rw",25L),24,0)` | Confirmed static |
| `jl` | `ab(byte):[B` | Reads 24 bytes via `me.aw` @0; all-zero -> `IOException` -> 24x `0xff` | Confirmed static |
| `bz` | `ag(xy,int,int)` | Copies 24 bytes from `me.aw` into existing `xy` buffer @offset | Confirmed static |
| `mr` | `gf(xy,int,int)` | Wrapper: `ck.gu(buf.aj,off,…)` then `bz.ag(buf,off,…)` | Confirmed static |
| `ck` | `gu(byte[],int,int)` | Only visible `client.lx` writer; null -> `new byte[24]`, copies 24 | Confirmed static |
| `client` | `lx:[B` | Process-global 24-byte fallback, not per-account in visible bytecode | Confirmed static |
| `dd`, `gy` | account-request builders | `client.lx != null ? write(lx) : write(jl.ab(0))`, then `xy.dl` RSA; form fields `data2=` / `&dest=`, URL `m=accountappeal/login.ws`, `dest=passwordchoice.ws`, server markers `OFFLINE`/`WRONG`/`RELOAD` (+ social-network refusal); triplicated `ab/ag/ae(long,String)` builder variants | Confirmed static (flow + framing vocabulary, not live wire bytes) |
| `xy` | `ie(...)` writers, `dl(BigInteger,BigInteger,int)` | Packet buffer + RSA encrypt | Confirmed static |
| `vt` | `re/ac` (OS cmd), `kg(int)` (process/JVM), `az(xy,int)` (serialize, marker 9) | Platform/HWID object: `wmic csproduct get UUID` / `cat /etc/machine-id` / `system_profiler …`, `ProcessHandle`, JVM args; Linux fallback `12345678-0000-0000-0000-123456789012` | Confirmed static |
| `cl`, `pj` | — | `cl` is on the setup path (`random.dat`/`RandomAccessFile`/`jagex_cl` ldc); `pj` holds the `random.dat` literal only as an unread `static final String ac` constant — no data-flow role | Confirmed static |
| `dg` / `eb.pt` / `ea` / `cp` / `pw` | `client.pn.az(…) -> vt`, `vt.kg(-1)`, field updates | `vt` init path | Confirmed static |
| `me.aw` users | constant-pool refs | `bz`, `cl`, `jl`, `lw`, `me` (ignore `bb`/`qr` `me.aw(…)` method refs — different member) | Confirmed static |
| `jl.ab(byte)` callers | constant-pool refs | `dd`, `gy`, **and `client` (3 sites, same `lx`-branch shape)** (`dj` `jl.ab(I)V` is a different overload) | Confirmed static |
| `client.lx` refs | constant-pool refs | `ck`, `dd`, `gy` only | Confirmed static |

`random.dat` lookup shape (`cl.az`, Confirmed static): try
`<cacheRoot>/random.dat` ([DRIFT] was `me.ao`) -> legacy bases (`c:/rscache/`, `/rscache/,
`c:/windows/`, `c:/winnt/`, `c:/`, home, `/tmp/`, …) + legacy cache dirs ->
else create/open target (`read` one byte, `seek(0)`, `write`, `seek(0)`,
`close`) and wrap. Path derives from cache roots/user-home, never from
username/account-id/token — cache-level, not per-account.

## 2. String / constant anchors

Injected: `random.dat`, `jagex_cl`, `java/io/RandomAccessFile`,
`wmic csproduct get UUID`, `cat /etc/machine-id`, `system_profiler`,
`ProcessHandle`, `me.aw`, `jl.ab`, `client.lx`.
DreamBot jar: `ClassLoader`, `defineClass`, `JarInputStream`, `Cipher`,
`org/objectweb/asm/tree/ClassNode`, `java/util/UUID randomUUID`,
`SecureRandom`, `MessageDigest`, `java/net/http/WebSocket`, `Gson toJson`,
`getDeclaredField`, `setAccessible`.
`client-1.12.36.jar` sweep found no `random.dat` anchor; `dreambot-client.jar`
sweep found no plaintext `random.dat`/`me.aw`/`jl.ab` (only unrelated
`RandomAccessFile` use) — both Confirmed static at doc time.

## 3. Runtime loader / transformer map (`dreambot-client.jar`)

| Class | Meaning | Status |
|---|---|---|
| `org.dreambot.4cs` (case-sensitive; cf. `4cS`) | `ClassLoader` ctor `(long,byte[])`; `JarInputStream` collect; Base64/cipher decrypt; `findClass` -> `defineClass` | Confirmed static |
| `org.dreambot.82` | ASM registry; Guava `ClassPath` scan for `@0L`; decrypts metadata; dispatches `ClassNode`; directly constructs `4Xb`, `40p`, `4XB` | Confirmed static |
| `org.dreambot.4Xi` + `40p`, `4XB`, `4Xb` | Registry match predicates over `java.lang.reflect.Method` (annotations, generic signatures, names): `40p` stringless/structural, `4XB`/`4Xb` string-guided; [DRIFT] `40P` does **not** extend `4Xi` — it is the authenticator-code generator, see below | Confirmed static (roles Strong inference) |
| `org.dreambot.0u` family (44 direct subclasses + `36` via abstract `3G`; was 43) | `ClassNode.methods`/`InsnList` iteration; `MethodInsnNode` owner/name match vs decrypted strings; `InsnList` rewrite insert. `0u(a,b)` stores a→field `3`, b→field `0`. FULL registration table decrypted offline 2026-09-18 (`results/deob-module-census-2026-09-18.md`): mixin points across `Client/Actor/Model/Region/Menu/Projectile/Animation/Widget/Mouse` + `*listener` event fan-out (`projectilespawnlistener`, `loginlistener`, `hitsplatslistener`, `animationlistener`, `actionlistener`, `fieldsetlistener` via `3G`/`36`); single-constraint modules use literal `"null"` first arg. Method-time match comparands need the dispatcher's runtime salt (Unknown, runtime-gated) | Confirmed static + runtime (registrations; bodies per-module pending except `0Y`/`30`) |
| `org.dreambot.0Y` | Constructor/call rewrite module (`extends 0u`, name `cache`): matches `TypeInsnNode.desc` + `MethodInsnNode.owner/name`, inserts `new TypeInsnNode(406)` + `MethodInsnNode(owner 406, …)` (two plaintext `ldc class 406`). The `aar → 406` redirector at module level | Confirmed static (module + target; runtime before/after still open) |
| `org.dreambot.30` | Frame-hook injector (`extends 0u`, name `Client.fps`/`fps`): MethodInsn matching + `JumpInsnNode`/`TypeInsnNode` insertion referencing `org/dreambot/4cs` (via `8n.4()` template) at the client's fps path | Confirmed static (registration + shape; exact inserted semantics Strong inference) |
| `org.dreambot.406` | `extends RandomAccessFile`; `(String,mode)` calls **`40o.2(String)` directly** (`invokestatic`, not reflective dispatch — corrects prior map); `(File,mode)` -> `5(file)` (identity) -> `super` | Confirmed static |
| `org.dreambot.4zF` | `extends java.io.File`, sibling redirector for File objects: `(String parent, String child)` -> `File(40o.7(parent,child), child)`; `(File, String)` -> parent if `40o.7(parentAbs,child)` equals parent else `new File(mapped)`; `(String)` -> `40o.7(path,"")` (identity by construction); `(URI)` identity; `listFiles()` unmapped passthrough | Confirmed static |
| `org.dreambot.40o` / `2(String)` / `7(String,String)` | Path-map rules, DECRYPTED offline 2026-09-18 (`40o.a` = 54924725345123; `results/deob-remap-2026-09-18.md`): `2` maps endsWith `xtea` -> `<6x.7 base>/cache/xtea` (mkdirs parents, exists-guarded), else identity; `7` maps `"random.dat".equalsIgnoreCase(name)` or `name.contains("preferences")` -> rebase of the `dreambot.path` property value onto the `6x.7` base, else identity. `█ → 7V[]` fetch is a null-guard only (fail-open). Callers: `2` ← `406` only; `7` ← `4zF` only | Confirmed static + runtime (offline decrypt verified) |
| `org.dreambot.6x` / `7(Object[])` | Cache-home resolver: `3N` = `getProperty("user.home")` + sep + `UserHomeCache`; segments `jagexcache`/`oldschool`/`LIVE`; sanitizer `[^A-Za-z0-9]`; keys `JX_CHARACTER_ID`; names `FreshStarts`, `tmpDB`; `-userhome` override + default-location warning strings (13 strings decrypted offline). Return flow read 2026-09-18: env/`UID`-derived override or prior `3N`, warn+log, mkdirs `3N` and `3N/jagexcache/oldschool/LIVE`, return `3N` — so the `40o.2` xtea replacement is exactly `3N/​cache/​xtea`. Account-lookup tail (`5I`/`core/9`) Strong inference — not executed (would load account-state classes) | Confirmed static + runtime (strings + return flow; tail Strong inference) |
| `org.dreambot.40P` | Authenticator-code generator (NOT a transformer): `1(Object[]{6n account, Long salt})` -> `4cx(account)` SecretKey -> `1r.8(...)` + `Instant.now()` -> int code, -1 on `InvalidKeyException`. `1r` = TOTP/HOTP core (Duration steps, SecretKey); `4cx implements SecretKey` over `core/6n`. Callers: `LoginUtility` (next to `RSLoginResponse.BAD_AUTH_CODE`), `LoginSolver` | Confirmed static (TOTP label Strong inference) |
| `org.dreambot.l` | Shared INDY resolver + runtime linker: bootstraps all `█:(JJ)` callsites (`7V[]` session context, null-guard semantics verified); exposes `Class b(long,long)`, `Method d(long,long)`, `Field c(long,long)` — `Class.forName` + member match by (name, type/return/params) with superclass/interface walk | Confirmed static (mechanism; full call-graph semantics Unknown, reference only) |
| `org.dreambot.0L` / `4Sl` / `7s` | Transformer annotations; encrypted `value()` (+ boolean `2` / `3`) | Confirmed static |
| `aar -> 406` rewrite of `me.aw` path | Module `0Y` (name `cache`) confirmed targeting `org/dreambot/406` for constructor/call rewrite; `406` maps `(String,mode)` opens through `40o.2`. Remaining: runtime before/after `aar` ClassNode + observed path pair | Confirmed static (module + target; end-to-end NOT VERIFIED) |

## 4. DreamBot identity / telemetry map

| Class | Meaning | Status |
|---|---|---|
| `org.dreambot.4ky` (lowercase; [DRIFT] uppercase `4kY` twin exists but has no UUID logic) | Persistent-id helper: file under `System.getProperty(<enc>)/<enc>`; exists -> `Files.readAllLines().get(0)`; else `UUID.randomUUID() + SecureRandom.getInstanceStrong().nextInt()`, `Files.write`, return. Decrypted 2026-09-18 (`4ky.a` = 56498337406173): inputs are `user.home`-rooted with a `.jid` name; `os.name`/`win` branches; `wmic csproduct get UUID` probe; 4 fallback UUID constants. Static `7` = build-constant `8584252e-…-1046430731` (identical across JVM loads — never a live id). Exact subdir + filename need the dispatcher's runtime salt (no static referrer exists) | Confirmed static (shape + fragments + constant); exact path Unknown (runtime-gated) |
| `AccountManager.getAccountHash()` | `String` account string -> `MessageDigest` -> Base64; null/empty -> `""`; no `byte[24]`, no `random.dat`/`me.aw`/`jl.ab`/`client.lx` | Confirmed static |
| `org.dreambot.4z_` | Telemetry/session object: UUID (`9J."2"`), account hash (`"8"`), legacy flag, timing, session objs, map copy | Confirmed static |
| `org.dreambot.404` | `HttpClient` + `WebSocket` + `BlockingQueue<String>` + `Gson`; `9x` envelope -> `toJson` -> `offer` -> `take` -> `sendText`; `buildAsync` with config headers | Confirmed static |
| `4z_` vs `random.dat` | Separate flows: `4z_`->`9x`->JSON->DreamBot WebSocket vs `me.aw`->`jl.ab`->`dd/gy`->RSA->client web request | Confirmed static |

## 5. Script-loader / SDN map (`dreambot-client.jar` + `.cache`)

Re-derived 2026-09-17 against the macOS install (`~/DreamBot/...`; this build
has no `BotData\client.jar` — the SDN loader lives in `dreambot-client.jar`,
sha256 `66036CB5…0882A`). Full evidence: `results/sdn-payload-decrypt-2026-09-17.md`;
procedure: `.agents/skills/sdn-payload-decrypt/SKILL.md`. Names `4_M`, `4_W`,
`4cs` are per-build — re-derive via the skill's anchor chain.

| Item | Meaning | Status |
|---|---|---|
| `scripts.dat` | Line-separated catalog; contains `P2P Master AI` (48 names on 2026-09-17); plaintext, no keys | Confirmed static |
| `.cache/bin/<hash>` | Per-script encrypted payload: `[16-byte IV][AES-CBC/PKCS5Padding body]`; one file on 2026-09-17: `838b43e2…` 7,237,888 B, sha256 `d9a5971e…` | Confirmed static (format) |
| Layer 1 string crypto | Per-class: `a:J = x.a(JJ,lookupClass).a(J)`; key long = `a:J ^ const`; DES/CBC/PKCS5, zero IV, BE key bytes; ciphertext in `String[] b/c`; INDY `(int,long)` callsites; `Object[]`-packed reflectively dispatched methods | Confirmed static + runtime |
| Derived key longs (this build) | ScriptManager `89269067584905`, `4_M` a:J `50324244270421`, `4cs` a:J `131601469286517` | Confirmed runtime |
| `4_M` ([DRIFT] supersedes `8Q`/`9O9` for this install) | Script loader: MD5 file hashes (Tink `Hex.encode`), decrypted strings `-script.tmp`/`libs`/`scripts.path`/`.jar`/`SHA-1`/`.class`/`db-`, `402` URLClassLoader, local jar scan | Confirmed static |
| `4_W` | SDN list/accounts crypto: embedded **cleartext Tink keyset** (108 B, md5 `720af153…`) → static `Aead`; list JSON decrypted with AD = 4-byte BE int; also wraps `accounts.db` (do not touch) | Confirmed static + runtime |
| `4cs` | In-memory script classloader: `4cs(long salt, byte[] jarBytes)` wraps decrypted bytes in `JarInputStream`; `HashMap<String,byte[]>` entries; `defineClass` | Confirmed static |
| `4cs.5` | Payload decryptor: `l3 = a:J ^ salt`; IV = `data[0:16]`; key = `Base64.decode(keyString)`; `AES/CBC/PKCS5Padding` (16-byte IV; name INDY-decrypted); reflective dispatch, exceptions swallowed | Confirmed static (cipher name Strong inference) |
| Per-script AES key + salt | Delivered only inside the authenticated SDN list response (layer 2); **not on disk**. Capture via `sdn-capture-agent` during a normal session, then `SdnFinish` replays offline | Unknown offline / Confirmed runtime (agent) |
| Filename vs payload | Same 32-hex filename on Mac (7.2 MB, Aug 27) and Windows (6.7 MB, Sep 9) with different content → revision-stable, derived from server-side script identity; disproven: MD5(ciphertext), MD5(plaintext), MD5(name variants) | Unknown (identity input) |
| Decrypted jar temp file | `4_M.2g` writes plaintext jar via `File.createTempFile("db-", "-script.tmp")` to `java.io.tmpdir` on every script load; survives crash/kill, removed on clean exit + macOS 3-day purge (Mac copies gone; Windows `%TEMP%` unchecked) | Confirmed static |
| SDN service | DEAD since 2026-08-31 ~22:07 per mac client log (last list refresh 22:05:34); no future key delivery or live capture | Confirmed (log evidence) |
| `P2P Master AI` payload hash | `.cache/bin/838b43e2b04f367b132c939dc2ae8abf` is the only payload and predates this analysis; script↔payload mapping needs a capture-time correlation | Strong inference |

## 6. Data-flow summary (do not merge branches without observed copies)

```text
me cache setup -> random.dat path -> aar/aac -> me.aw
  -> jl.ab() | bz.ag() 24 bytes -> dd/gy (+client.lx branch) -> xy.dl RSA -> account web request
DreamBot account activation -> getAccountHash() + persistent UUID -> 4z_ -> 9x/Gson -> 404 WebSocket
packed bytes -> 4cs -> 82/4Xi/0u transforms -> (406/40o.2 ?) -> same me.aw reader path
vt probes -> vt.az -> xy buffer (separate source from random.dat)
```

Fallbacks (NOT a GUID pool): `jl.ab` failure -> 24x `0xff`; `ck.gu` init ->
24x `0x00`; `vt` Linux unexpected output ->
`12345678-0000-0000-0000-123456789012`.
