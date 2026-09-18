import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Prints the layer-1 key long(s) of many classes in ONE JVM (faster than one
 * DumpClassTables per class). Only triggers each <clinit> — verify every
 * target <clinit> is PRNG+DES-only via javap BEFORE running (see results/
 * deob-remap-2026-09-18.md for the safety procedure).
 *
 * Usage: KeyDump <dreambot-client.jar> <class1> [<class2> ...]
 * Prints: <class> <field>=<value> ... for static long/Long fields.
 * Classpath must include stub/ (Logger shadow).
 */
public class KeyDump {
    public static void main(String[] args) throws Exception {
        URLClassLoader cl = new URLClassLoader(
                new URL[]{new File(args[0]).toURI().toURL()}, KeyDump.class.getClassLoader());
        for (int k = 1; k < args.length; k++) {
            try {
                Class<?> c = Class.forName(args[k], true, cl);
                StringBuilder sb = new StringBuilder(args[k]);
                for (Field f : c.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    Object v = f.get(null);
                    if (v instanceof Long) sb.append(' ').append(f.getName()).append('=').append(v);
                }
                System.out.println(sb);
            } catch (Throwable t) {
                System.out.println(args[k] + " !! " + t);
            }
        }
    }
}
