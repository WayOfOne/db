import com.google.crypto.tink.Aead;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Layer-2 sweep: try the extracted Tink Aead against every candidate local
 * file (the SDN script list is read from a file per 4_W.3l -> 4t_.6). Skips
 * account/session files by rule (accounts.db, account.dat, accounts.dat,
 * session.dat). Success prints a sanitized head of the plaintext; a hit on
 * script-list JSON yields the per-script AES key + salt for SdnFinish.
 *
 * Usage: TinkSweep <dreambot-client.jar> <cryptoClassName> <file>...
 */
public class TinkSweep {
    public static void main(String[] args) throws Exception {
        URLClassLoader cl = new URLClassLoader(
                new URL[]{new File(args[0]).toURI().toURL()}, TinkSweep.class.getClassLoader());
        Class<?> w = Class.forName(args[1], true, cl);
        Field f8 = w.getDeclaredField("8");
        f8.setAccessible(true);
        Aead aead = (Aead) f8.get(null);

        byte[][] ads = new byte[52][];
        for (int i = 0; i <= 50; i++) ads[i] = java.nio.ByteBuffer.allocate(4).putInt(i).array();
        ads[51] = new byte[0];

        for (int a = 2; a < args.length; a++) {
            java.nio.file.Path p = Paths.get(args[a]);
            if (!Files.isRegularFile(p)) continue;
            byte[] data = Files.readAllBytes(p);
            for (byte[] ad : ads) {
                try {
                    byte[] plain = aead.decrypt(data, ad);
                    StringBuilder head = new StringBuilder();
                    for (int i = 0; i < Math.min(plain.length, 300); i++) {
                        char c = (char) (plain[i] & 0xff);
                        head.append(c >= 0x20 && c < 0x7f ? c : '.');
                    }
                    System.out.printf("HIT %s (ad=%d bytes) len=%d%n  %s%n",
                            p.getFileName(), ad.length, plain.length, head);
                    Files.write(Paths.get("sweep_" + p.getFileName()), plain);
                    break;
                } catch (Exception ignored) { }
            }
        }
        System.out.println("sweep done");
    }
}
