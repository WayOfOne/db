import org.objectweb.asm.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Extracts ctor-time layer-1 call data for 0u modules (or any class): the key
 * derivation constant (GETSTATIC key; LDC2_W M; LXOR; LSTORE) and every
 * (INT, LONG, LLOAD, LXOR, INVOKEDYNAMIC) site with its indy name, plus plain
 * LDC string constants passed to the super ctor.
 *
 * Combine with KeyDump's key long: siteLong = longConst ^ (keyLong ^ M).
 * Feed (intArg, siteLong) to IndyDecrypt.
 *
 * Usage: CtorParse <dreambot-client.jar> <class1> [<class2> ...]
 * Needs asm-8.0.1.jar. Pure parse — loads no client code.
 */
public class CtorParse {
    static class IntInsn { int op, arg; IntInsn(int o, int a) { op = o; arg = a; } }
    static class LongInsn { long arg; LongInsn(long a) { arg = a; } }

    public static void main(String[] args) throws Exception {
        try (ZipFile z = new ZipFile(args[0])) {
            for (int c = 1; c < args.length; c++) {
                String name = args[c].replace('.', '/') + ".class";
                ZipEntry e = z.getEntry(name);
                if (e == null) { System.out.println(args[c] + " NOT-FOUND"); continue; }
                ClassReader cr;
                try (InputStream in = z.getInputStream(e)) { cr = new ClassReader(in); }
                System.out.println("== " + args[c]);
                cr.accept(new ClassVisitor(Opcodes.ASM8) {
                    @Override
                    public MethodVisitor visitMethod(int acc, String mname, String desc,
                            String sig, String[] exc) {
                        if (!mname.equals("<init>")) return null;
                        return new MethodVisitor(Opcodes.ASM8) {
                            List<Object> window = new ArrayList<>();
                            void push(Object o) { window.add(o); if (window.size() > 8) window.remove(0); }
                            @Override public void visitFieldInsn(int op, String o, String n, String d) {
                                push(op == Opcodes.GETSTATIC ? "GETSTATIC:" + n : "FIELD");
                            }
                            @Override public void visitIntInsn(int op, int a) { push(new IntInsn(op, a)); }
                            @Override public void visitLdcInsn(Object v) {
                                if (v instanceof Long) push(new LongInsn((Long) v));
                                else if (v instanceof Integer) push(new IntInsn(Opcodes.LDC, (Integer) v));
                                else push(v);
                            }
                            @Override public void visitVarInsn(int op, int v) {
                                if (op == Opcodes.LLOAD) push("LLOAD");
                                else if (op == Opcodes.LSTORE) {
                                    int sz = window.size();
                                    if (sz >= 3 && window.get(sz - 1).equals("LXOR")
                                            && window.get(sz - 2) instanceof LongInsn
                                            && (window.get(sz - 3) instanceof String
                                                && ((String) window.get(sz - 3)).startsWith("GETSTATIC:"))) {
                                        System.out.println("  KEYM field="
                                                + ((String) window.get(sz - 3)).substring(10)
                                                + " const=" + ((LongInsn) window.get(sz - 2)).arg);
                                    }
                                    push("LSTORE");
                                } else push("VAR");
                            }
                            @Override public void visitInsn(int op) {
                                if (op == Opcodes.LXOR) push("LXOR");
                                else if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5)
                                    push(new IntInsn(op, op - Opcodes.ICONST_0));
                                else push("OP" + op);
                            }
                            @Override public void visitInvokeDynamicInsn(String n, String d,
                                    Handle bsm, Object... bsmArgs) {
                                // look back: INT, LONG, LLOAD, LXOR
                                int sz = window.size();
                                if (sz >= 4
                                        && window.get(sz - 1).equals("LXOR")
                                        && window.get(sz - 2).equals("LLOAD")
                                        && window.get(sz - 3) instanceof LongInsn
                                        && window.get(sz - 4) instanceof IntInsn) {
                                    IntInsn ii = (IntInsn) window.get(sz - 4);
                                    LongInsn li = (LongInsn) window.get(sz - 3);
                                    System.out.println("  SITE indy=" + n + " int=" + ii.arg
                                            + " const=" + li.arg + " bsm=" + bsm.getOwner()
                                            + "." + bsm.getName());
                                } else {
                                    System.out.println("  SITE indy=" + n + " desc=" + d
                                            + " (nonstandard preamble)");
                                }
                                push("INDY");
                            }
                            @Override public void visitMethodInsn(int op, String o, String n,
                                    String d, boolean itf) { push("CALL"); }
                        };
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        }
    }
}
