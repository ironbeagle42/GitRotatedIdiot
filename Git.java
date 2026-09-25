import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
            String fileContents = getTextOfFile(filePath);
            String hashedFile = hash(fileContents);
            FileWriter blobWriter = new FileWriter("git/objects/" + hashedFile);
            blobWriter.write(fileContents);
            indexFile(filePath);
            blobWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getTextOfFile(String filePath) {
        FileReader blobReader;
        try {
            blobReader = new FileReader(filePath);
            String fileContents = "";
            int c;
            while ((c = blobReader.read()) != -1) {
                fileContents = fileContents + (char) c;
            }
            blobReader.close();
            return fileContents;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    public static void indexFile(String filePath) {
        try {
            BufferedReader indexReader = new BufferedReader(new FileReader("git/INDEX"));
            String line;
            List<String> indexLines = new ArrayList<>();
            while ((line = indexReader.readLine()) != null) {
                if (line.contains(" " + filePath)) {
                    String hashedFile = hash(getTextOfFile(filePath));
                    indexLines.add(hashedFile + " " + filePath);
                } else {
                    indexLines.add(line);
                }
            }
            FileWriter indexWriter = new FileWriter("git/INDEX");
            for (String i : indexLines) {
                indexWriter.write(i + "\n");
            }
            indexWriter.close();
            indexReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
