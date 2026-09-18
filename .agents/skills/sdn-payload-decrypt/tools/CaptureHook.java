import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Capture hook invoked (by CaptureAgent injection) at the entry of the SDN
 * payload decryptor with its Object[] arguments:
 *   [0] byte[] payload   = [16-byte IV][AES-CBC ciphertext]
 *   [1] String base64Key = per-script AES key, base64-encoded
 *   [2] Long salt        = per-script long from the SDN list
 * Writes each argument to ~/sdn-capture/<timestamp>/ and records the caller
 * stack plus the declaring class's static a:J key long when reachable.
 * Everything stays local; failures never propagate into the host app.
 */
public class CaptureHook {
    public static void capture(Object[] args) {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), "sdn-capture",
                    Long.toString(System.currentTimeMillis()));
            Files.createDirectories(dir);
            StringBuilder meta = new StringBuilder();
            meta.append("time = ").append(Instant.now()).append('\n');
            if (args != null) {
                meta.append("argc = ").append(args.length).append('\n');
                for (int i = 0; i < args.length; i++) {
                    Object a = args[i];
                    if (a instanceof byte[]) {
                        byte[] b = (byte[]) a;
                        Files.write(dir.resolve("arg" + i + ".bin"), b);
                        meta.append("arg").append(i).append(" = byte[").append(b.length)
                                .append("] md5=").append(md5(b))
                                .append(" head=").append(hex(b, 16)).append('\n');
                    } else if (a instanceof String) {
                        String s = (String) a;
                        Files.write(dir.resolve("arg" + i + ".txt"), s.getBytes("UTF-8"));
                        meta.append("arg").append(i).append(" = String len=").append(s.length())
                                .append(" value=").append(s).append('\n');
                    } else if (a instanceof Long) {
                        meta.append("arg").append(i).append(" = Long ").append(a)
                                .append(" (0x").append(Long.toHexString((Long) a)).append(")\n");
                    } else {
                        meta.append("arg").append(i).append(" = ")
                                .append(a == null ? "null" : a.getClass().getName() + " " + a).append('\n');
                    }
                }
            }
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            meta.append("stack:\n");
            for (int i = 2; i < Math.min(st.length, 14); i++) meta.append("  at ").append(st[i]).append('\n');
            try {
                if (st.length > 2) {
                    Class<?> caller = Class.forName(st[2].getClassName());
                    try {
                        java.lang.reflect.Field fa = caller.getDeclaredField("a");
                        fa.setAccessible(true);
                        meta.append("caller a:J = ").append(fa.getLong(null)).append('\n');
                    } catch (Exception ignored) { }
                }
            } catch (Throwable ignored) { }
            Files.write(dir.resolve("meta.txt"), meta.toString().getBytes("UTF-8"),
                    StandardOpenOption.CREATE);
            System.out.println("[sdn-capture] captured decryptor call -> " + dir);
        } catch (Throwable t) {
            System.out.println("[sdn-capture] hook error (ignored): " + t);
        }
    }

    static String md5(byte[] b) {
        try {
            byte[] d = MessageDigest.getInstance("MD5").digest(b);
            StringBuilder sb = new StringBuilder();
            for (byte x : d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            return "?";
        }
    }

    static String hex(byte[] b, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(n, b.length); i++) sb.append(String.format("%02x ", b[i]));
        return sb.toString();
    }
}
