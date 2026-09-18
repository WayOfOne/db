import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Offline probe for the layer-1 framework: loads a client class (static init
 * runs the PRNG chain + DES string decryption), prints the derived class key
 * long `a:J`, then shape-detects the layer-3 payload decryptor
 * (`static byte[] <name>(Object[])` with Base64 + SecretKeySpec + doFinal in
 * the body) and probes it with dummy inputs to confirm the cipher strings
 * decrypt without a per-script salt problem. Swallowed exceptions inside the
 * decryptor make results null; use this mainly for the a:J value.
 *
 * Usage: Probe4cs <dreambot-client.jar> <binary class name, e.g. org.dreambot.4cs>
 */
public class Probe4cs {
    public static void main(String[] args) throws Exception {
        String jar = args[0];
        String className = args[1]; // dots, e.g. org.dreambot.4cs

        URLClassLoader cl = new URLClassLoader(
                new URL[]{new java.io.File(jar).toURI().toURL()}, Probe4cs.class.getClassLoader());
        Class<?> c = Class.forName(className, true, cl);
        System.out.println("loaded " + c);

        Field fa = c.getDeclaredField("a");
        fa.setAccessible(true);
        System.out.println(className + ".a:J = " + fa.getLong(null));

        // shape-detect the payload decryptor via ASM: static byte[] (Object[])
        // with Base64.getDecoder + SecretKeySpec + Cipher.doFinal in the body
        byte[] bytes;
        try (JarFile jf = new JarFile(jar);
             InputStream is = jf.getInputStream(jf.getJarEntry(className.replace('.', '/') + ".class"))) {
            bytes = is.readAllBytes();
        }
        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(cn, 0);

        for (MethodNode mn : (List<MethodNode>) cn.methods) {
            if (!isDecryptorShape(mn)) continue;
            System.out.printf("decryptor shape: %s%s%n", mn.name, mn.desc);
            try {
                Method m = c.getDeclaredMethod(mn.name, Object[].class);
                m.setAccessible(true);
                byte[] dummy = new byte[32]; // [16-byte IV][16-byte body]
                Object r = m.invoke(null, (Object) new Object[]{dummy,
                        "AAAAAAAAAAAAAAAAAAAAAA==", 0L});
                System.out.println("  dummy probe -> " + r + " (null = decryptor swallowed its exception; expected offline)");
            } catch (Exception e) {
                System.out.println("  dummy probe failed: " + e);
            }
        }
    }

    static boolean isDecryptorShape(MethodNode mn) {
        if (!java.lang.reflect.Modifier.isStatic(mn.access)) return false;
        if (!mn.desc.equals("([Ljava/lang/Object;)[B")) return false;
        boolean hasBase64 = false, hasKeySpec = false, hasDoFinal = false;
        for (AbstractInsnNode in : mn.instructions) {
            if (in instanceof MethodInsnNode) {
                MethodInsnNode mi = (MethodInsnNode) in;
                if (mi.owner.equals("java/util/Base64$Decoder")) hasBase64 = true;
                if (mi.owner.equals("javax/crypto/spec/SecretKeySpec")) hasKeySpec = true;
                if (mi.owner.equals("javax/crypto/Cipher") && mi.name.equals("doFinal")) hasDoFinal = true;
            }
        }
        return hasBase64 && hasKeySpec && hasDoFinal;
    }
}
