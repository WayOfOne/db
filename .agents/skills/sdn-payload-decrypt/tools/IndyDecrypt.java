import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Offline layer-1 string decryption WITHOUT running client logic: invokes the
 * class's own private {@code a(int,long)} lazy decryptor (pure in-memory —
 * reads static b[], fills static c[] cache, allocates Ciphers only).
 *
 * The (int,long) pairs come from BootParse's bootstrap STATIC args for the
 * w:/d:/s: callsites of the same class.
 *
 * Safety: only triggers the target class's <clinit> (verify it is PRNG+DES
 * only via javap first, per SKILL.md §3). Never invokes the Object[]-
 * dispatched feature methods (those can touch FS/network).
 *
 * Usage: IndyDecrypt <dreambot-client.jar> <binary class> <int> <long> [<int> <long> ...]
 * Classpath must include stub/ (Logger shadow). Prints index + plaintext.
 */
public class IndyDecrypt {
    public static void main(String[] args) throws Exception {
        URLClassLoader cl = new URLClassLoader(
                new URL[]{new File(args[0]).toURI().toURL()}, IndyDecrypt.class.getClassLoader());
        Class<?> c = Class.forName(args[1], true, cl);
        Method m = null;
        for (Method k : c.getDeclaredMethods()) {
            if (k.getReturnType() == String.class) {
                Class<?>[] p = k.getParameterTypes();
                if (p.length == 2 && p[0] == int.class && p[1] == long.class) m = k;
            }
        }
        if (m == null) { System.out.println("no (int,long)->String in " + args[1]); return; }
        System.out.println("using " + args[1] + "." + m.getName() + "(int,long)");
        m.setAccessible(true);
        for (int i = 2; i + 1 < args.length; i += 2) {
            int iarg = Integer.decode(args[i]);
            long larg = Long.decode(args[i + 1]);
            try {
                String s = (String) m.invoke(null, iarg, larg);
                System.out.println("[" + iarg + "," + larg + "] -> "
                        + (s == null ? "<java-null>" : "\"" + s + "\""));
            } catch (Exception e) {
                System.out.println("[" + iarg + "," + larg + "] !! " +
                        (e.getCause() == null ? e : e.getCause()));
            }
        }
    }
}
