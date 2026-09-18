import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * Offline dump of a framework class's decrypted static tables. Loads the class
 * (static init runs the PRNG chain + DES/CBC/PKCS5 string decryption — verify
 * the <clinit> has no I/O first, see SKILL.md §2/§3), then reflects every
 * static field: String[] entries (ciphertext arrays b/c — some already
 * decrypted), long[]/Long[]/Integer[] key tables, Maps, and long scalars.
 *
 * Usage: DumpClassTables <dreambot-client.jar> <binary class name, e.g. org.dreambot.4_M>
 * Classpath must include: stub/ (Logger shadow), asm, asm-tree, guava, tink,
 * protobuf, gson, okhttp, okio — see SKILL.md §3.
 */
public class DumpClassTables {
    public static void main(String[] args) throws Exception {
        URLClassLoader cl = new URLClassLoader(
                new URL[]{new java.io.File(args[0]).toURI().toURL()}, DumpClassTables.class.getClassLoader());
        Class<?> c = Class.forName(args[1], true, cl);
        System.out.println("loaded " + c + " (static init ran)");
        for (Field f : c.getDeclaredFields()) {
            f.setAccessible(true);
            if (!Modifier.isStatic(f.getModifiers())) continue;
            Object v = f.get(null);
            String name = f.getName();
            if (v instanceof String[]) {
                String[] arr = (String[]) v;
                System.out.println("field " + name + " String[" + arr.length + "]:");
                for (int k = 0; k < arr.length; k++)
                    if (arr[k] != null) System.out.printf("  [%d] %s%n", k, arr[k]);
            } else if (v instanceof long[]) {
                long[] arr = (long[]) v;
                StringBuilder sb = new StringBuilder("field " + name + " long[" + arr.length + "]: ");
                for (long x : arr) sb.append(x).append(' ');
                System.out.println(sb);
            } else if (v instanceof Long[]) {
                Long[] arr = (Long[]) v;
                StringBuilder sb = new StringBuilder("field " + name + " Long[" + arr.length + "]: ");
                for (Long x : arr) sb.append(x == null ? "null " : x + " ");
                System.out.println(sb);
            } else if (v instanceof Integer[]) {
                Integer[] arr = (Integer[]) v;
                StringBuilder sb = new StringBuilder("field " + name + " Integer[" + arr.length + "]: ");
                for (Integer x : arr) sb.append(x == null ? "null " : x + " ");
                System.out.println(sb);
            } else if (v instanceof Map) {
                System.out.println("field " + name + " Map[" + ((Map) v).size() + "]:");
                for (Object e : ((Map) v).entrySet()) {
                    Map.Entry en = (Map.Entry) e;
                    System.out.printf("  %s -> %s (%s -> %s)%n", en.getKey(), en.getValue(),
                            en.getKey() == null ? "?" : en.getKey().getClass().getSimpleName(),
                            en.getValue() == null ? "?" : en.getValue().getClass().getSimpleName());
                }
            } else if (v instanceof Long) {
                System.out.printf("field %s long = %d (0x%016x)%n", name, (Long) v, (Long) v);
            } else if (!(v instanceof java.util.List || v instanceof java.util.concurrent.locks.Lock)) {
                System.out.println("field " + name + " = " + v);
            }
        }
    }
}
