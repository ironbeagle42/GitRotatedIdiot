import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.io.FileWriter;

public class Git {
    public static void main(String[] args) {
        init();
        blob("helloworld.txt");
    }

    public static void init() {
        File a = new File("git/");
        File b = new File("git/objects/");
        File c = new File("git/INDEX");
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

    public static void blob(String filePath) {
        try {
            FileReader blobReader = new FileReader(filePath);
            String fileContents = "";
            int c;
            while ((c = blobReader.read()) != -1) {
                fileContents = fileContents + (char) c;
            }
            String hashedFile = hash(fileContents);
            FileWriter blobWriter = new FileWriter("git/objects/" + hashedFile);
            blobWriter.write(fileContents);
            FileWriter indexWriter = new FileWriter("git/INDEX");
            indexWriter.write(hashedFile + " " + filePath);
            indexWriter.close();
            blobWriter.close();
            blobReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
