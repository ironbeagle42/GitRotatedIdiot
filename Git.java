import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.io.FileWriter;

public class Git {
    public static void main(String[] args) {
        init();
        System.out.println((hash("hello world")));
    }

    public static void init() {
        File a = new File("git/");
        File b = new File("git/objects/");
        File c = new File("git/index");
        File d = new File("git/HEAD");
        if (!a.exists() && !b.exists() && !c.exists() && !d.exists()) {
            System.out.println("Git Repository Already Exists");
        } else {
            System.out.println("Git Repository Created");
            a.mkdir();
            b.mkdir();
            try {
                c.createNewFile();
                d.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static String hash(String input) {
        byte[] input_bytes = input.getBytes();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input_bytes);
            byte[] hashed_bytes = md.digest();
            return Base64.getEncoder().encodeToString(hashed_bytes);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    public static void blob(String file) {
        String hashedFile = hash(file);
        try {
            FileWriter blobWriter = new FileWriter("git/objects/" + hashedFile);
            blobWriter.write(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
