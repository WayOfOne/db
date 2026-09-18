import com.google.crypto.tink.Aead;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Layer-2 probe: loads the Tink-Aead holder class (static init builds the Aead
 * from the embedded cleartext keyset), dumps the keyset to keyset.bin, prints
 * the class's decrypted string fragments, then sweeps associated-data
 * candidates (4-byte BE ints + short strings) against a .cache/bin payload.
 * Expected outcome per SKILL.md: the sweep FAILS on layer-3 payloads — this
 * probe exists to extract the keyset (which protects the SDN list JSON) and to
 * prove the payload is NOT layer-2 protected.
 *
 * Usage: DecryptWithTink <dreambot-client.jar> <cryptoClassName, e.g. org.dreambot.4_W>
 *                        <payload> <out.jar>
 */
public class DecryptWithTink {
    public static void main(String[] args) throws Exception {
        String jar = args[0];
        String cryptoClass = args[1];
        String payloadPath = args[2];
        String outPath = args[3];

        URLClassLoader cl = new URLClassLoader(
                new URL[]{new File(jar).toURI().toURL()}, DecryptWithTink.class.getClassLoader());
        Class<?> w = Class.forName(cryptoClass, true, cl);

        Field f3 = w.getDeclaredField("3"); // embedded cleartext Tink keyset
        f3.setAccessible(true);
        byte[] keyset = (byte[]) f3.get(null);
        System.out.println("keyset bytes = " + keyset.length + " md5=" + md5(keyset));
        Files.write(Paths.get("keyset.bin"), keyset);

        Field f8 = w.getDeclaredField("8"); // static com.google.crypto.tink.Aead
        f8.setAccessible(true);
        Aead aead = (Aead) f8.get(null);
        System.out.println("aead = " + aead);

        for (String fn : new String[]{"9", "b", "c"}) { // decrypted string fragments
            try {
                Field f = w.getDeclaredField(fn);
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof String) {
                    System.out.println(cryptoClass + "." + fn + " = " + v);
                } else if (v instanceof String[]) {
                    String[] arr = (String[]) v;
                    System.out.println(cryptoClass + "." + fn + " String[" + arr.length + "]:");
                    for (int i = 0; i < arr.length; i++)
                        if (arr[i] != null) System.out.printf("  [%d] %s%n", i, arr[i]);
                }
            } catch (Exception e) {
                System.out.println(cryptoClass + "." + fn + " -> " + e);
            }
        }

        byte[] data = Files.readAllBytes(Paths.get(payloadPath));
        System.out.println("payload bytes = " + data.length);

        int maxAd = 100;
        for (int ad = 0; ad <= maxAd; ad++) {
            try {
                byte[] plain = aead.decrypt(data, java.nio.ByteBuffer.allocate(4).putInt(ad).array());
                System.out.printf("DECRYPTED with AD int %d! plain=%d head=%s md5=%s%n",
                        ad, plain.length, head(plain), md5(plain));
                Files.write(Paths.get(outPath), plain);
                return;
            } catch (Exception ignored) { }
        }
        for (String s : new String[]{"", "P2P Master AI", "script", "scripts", "sdn", "bin"}) {
            try {
                byte[] plain = aead.decrypt(data, s.getBytes("UTF-8"));
                System.out.printf("DECRYPTED with AD string \"%s\"! plain=%d head=%s%n", s, plain.length, head(plain));
                Files.write(Paths.get(outPath), plain);
                return;
            } catch (Exception ignored) { }
        }
        System.out.println("no candidate worked -> payload is layer 3 (per-script AES), as expected");
    }

    static String md5(byte[] b) throws Exception {
        byte[] d = MessageDigest.getInstance("MD5").digest(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : d) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static String head(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(8, b.length); i++) sb.append(String.format("%02x ", b[i]));
        return sb.toString();
    }
}
