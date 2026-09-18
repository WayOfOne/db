import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.ZipFile;

/**
 * Layer-3 finisher: replays 4cs.5 offline from runtime-captured material.
 *
 * Usage: SdnFinish <payload> <base64 key> <salt long> <class a:J long> <out.jar> [transformation]
 * Default transformation: AES/CBC/PKCS5Padding (16-byte IV). Captured values
 * come from the capture agent (SKILL.md §5): key string, salt, and the
 * declaring class's a:J (printed by Probe4cs).
 */
public class SdnFinish {
    public static void main(String[] args) throws Exception {
        String payloadPath = args[0];
        String key64 = args[1];
        long salt = Long.parseLong(args[2]);
        long aJ = Long.parseLong(args[3]);
        String outPath = args[4];
        String transformation = args.length > 5 ? args[5] : "AES/CBC/PKCS5Padding";

        long l3 = aJ ^ salt;
        System.out.printf("a:J=0x%016x salt=0x%016x l3=0x%016x%n", aJ, salt, l3);

        byte[] data = Files.readAllBytes(Paths.get(payloadPath));
        System.out.println("payload bytes = " + data.length + " (iv 16 + body " + (data.length - 16) + ")");

        byte[] iv = new byte[16];
        System.arraycopy(data, 0, iv, 0, 16);
        byte[] body = new byte[data.length - 16];
        System.arraycopy(data, 16, body, 0, body.length);
        byte[] key = Base64.getDecoder().decode(key64);
        System.out.println("key bytes = " + key.length);

        Cipher c = Cipher.getInstance(transformation);
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
        byte[] plain = c.doFinal(body);

        StringBuilder head = new StringBuilder();
        for (int i = 0; i < 8; i++) head.append(String.format("%02x ", plain[i]));
        String md5 = md5(plain);
        System.out.println("plain bytes = " + plain.length + " head = " + head);
        System.out.println("plain MD5   = " + md5);

        boolean zip = plain[0] == 'P' && plain[1] == 'K';
        String fileName = Paths.get(payloadPath).getFileName().toString();
        boolean nameMatch = md5.equalsIgnoreCase(fileName);
        System.out.println("zip magic   = " + zip + "   md5 == filename (" + fileName + "): " + nameMatch);
        if (!zip) {
            System.out.println("NOT a jar — wrong key/transformation; do not write output");
            return;
        }
        Files.write(Paths.get(outPath), plain);
        try (ZipFile zf = new ZipFile(outPath)) {
            System.out.println("jar entries = " + zf.size());
        }
        System.out.println("wrote " + outPath);
    }

    static String md5(byte[] b) throws Exception {
        byte[] d = MessageDigest.getInstance("MD5").digest(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : d) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}
