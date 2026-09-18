import org.objectweb.asm.*;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Lists every invokedynamic callsite of one class with its BOOTSTRAP STATIC
 * ARGS (the true layer-1 decryption parameters — dynamic (I,J) args are
 * dropped by the bootstrap, see 40o.a(Lookup,MutableCallSite,String,Object[])).
 *
 * Usage: BootParse <dreambot-client.jar> <binary class, e.g. org.dreambot.40o> [indyNameFilter]
 * Needs asm-8.0.1.jar on the classpath. Pure parse — loads no client code.
 */
public class BootParse {
    public static void main(String[] args) throws Exception {
        String jar = args[0], cls = args[1].replace('.', '/') + ".class";
        String filter = args.length > 2 ? args[2] : null;
        try (ZipFile z = new ZipFile(jar)) {
            Enumeration<? extends ZipEntry> en = z.entries();
            ZipEntry hit = null;
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                if (e.getName().equals(cls)) { hit = e; break; }
            }
            if (hit == null) { System.out.println("not found: " + cls); return; }
            ClassReader cr;
            try (InputStream in = z.getInputStream(hit)) { cr = new ClassReader(in); }
            cr.accept(new ClassVisitor(Opcodes.ASM8) {
                String method = "?";
                @Override
                public MethodVisitor visitMethod(int acc, String name, String desc,
                        String sig, String[] exc) {
                    method = name + desc;
                    final String m = method;
                    return new MethodVisitor(Opcodes.ASM8) {
                        @Override
                        public void visitInvokeDynamicInsn(String name, String desc,
                                Handle bsm, Object... bsmArgs) {
                            if (filter != null && !name.equals(filter)) return;
                            StringBuilder sb = new StringBuilder();
                            sb.append(m).append("  indy '").append(name).append("' ").append(desc);
                            sb.append("  bsm=").append(bsm.getOwner()).append('.').append(bsm.getName());
                            sb.append("  staticArgs=[");
                            for (int i = 0; i < bsmArgs.length; i++) {
                                if (i > 0) sb.append(", ");
                                Object a = bsmArgs[i];
                                sb.append(a instanceof Long ? a + "L"
                                        : a instanceof Integer ? a + ""
                                        : a.getClass().getSimpleName() + ":" + a);
                            }
                            sb.append(']');
                            System.out.println(sb);
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }
}
