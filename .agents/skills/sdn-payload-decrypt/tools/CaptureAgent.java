import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SDN payload capture agent (premain). Build-agnostic: instruments every
 * org/dreambot/* class containing the layer-3 payload decryptor shape — a
 * static byte[] <name>(Object[]) method whose body calls
 * Base64$Decoder.decode + SecretKeySpec.<init> + Cipher.doFinal — and injects
 * an entry hook CaptureHook.capture(Object[]) that dumps the arguments
 * (payload bytes, base64 key string, salt Long) to ~/sdn-capture/.
 *
 * Usage: -javaagent:sdn-capture-agent.jar[=<class prefix filter, e.g. org.dreambot>]
 * Two passes per class: (1) tree-API shape scan, (2) rewrite with injection.
 */
public class CaptureAgent implements ClassFileTransformer {
    static final String HOOK = "CaptureHook";
    static String prefix = "org/dreambot/";

    public static void premain(String args, Instrumentation inst) {
        if (args != null && !args.trim().isEmpty()) {
            prefix = args.trim().replace('.', '/') + "/";
        }
        inst.addTransformer(new CaptureAgent(), true);
        System.out.println("[sdn-capture] agent installed, instrumenting classes under " + prefix);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> being,
                            ProtectionDomain pd, byte[] bytes) {
        if (bytes == null || className == null || !className.startsWith(prefix)) return null;
        if (className.endsWith("CaptureHook") || className.endsWith("CaptureAgent")) return null;
        try {
            // pass 1: shape scan
            ClassNode cn = new ClassNode();
            new ClassReader(bytes).accept(cn, 0);
            Set<String> hit = new HashSet<>();
            for (MethodNode mn : (List<MethodNode>) cn.methods) {
                if (matches(mn)) hit.add(mn.name + mn.desc);
            }
            if (hit.isEmpty()) return null;

            // pass 2: rewrite with entry hook
            final Set<String> targets = hit;
            ClassWriter cw = new ClassWriter(new ClassReader(bytes), 0);
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM8, cw) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exc) {
                    MethodVisitor mv = super.visitMethod(access, name, desc, sig, exc);
                    if (targets.contains(name + desc)) {
                        System.out.println("[sdn-capture] hooked " + className + "." + name);
                        mv.visitCode();
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, HOOK, "capture",
                                "([Ljava/lang/Object;)V", false);
                    }
                    return mv;
                }
            }, 0);
            return cw.toByteArray();
        } catch (Throwable t) {
            System.out.println("[sdn-capture] skip " + className + ": " + t);
            return null;
        }
    }

    static boolean matches(MethodNode mn) {
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
