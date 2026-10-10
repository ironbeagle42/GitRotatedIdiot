import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Path;

public class Git {
    public static void main(String[] args) {
        init();

        blob("./testing/Hello.txt");
        indexFile("./testing/Hello.txt");

        blob("./testing/World.txt");
        indexFile("./testing/World.txt");

        System.out.println(treeIndex());
    }

    public static void init() {
        File git = new File("git/");
        File obj = new File("git/objects/");
        File index = new File("git/index");
        File head = new File("git/HEAD");
        if (git.exists() && obj.exists() && index.exists() && head.exists()) {
            System.out.println("Git Repository Already Exists");
        } else {
            System.out.println("Git Repository Created");
            git.mkdir();
            obj.mkdir();
            try {
                index.createNewFile();
                head.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static String hashString(String input) {
        byte[] input_bytes = input.getBytes();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input_bytes);
            byte[] hashed_bytes = md.digest();
            return HexFormat.of().formatHex(hashed_bytes);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }

    public static void blob(String filePath) {
        try {
            String fileContents = getTextOfFile(filePath);
            String hashedFile = hashString(fileContents);
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
            BufferedReader indexReader = new BufferedReader(new FileReader("git/index"));
            String line;
            boolean hasAdded = false;

            // fixed storing file name correctly
            Path root = Path.of("").toAbsolutePath();
            Path file = Path.of(filePath).toAbsolutePath();

            if (!file.startsWith(root)) {
                throw new IllegalArgumentException("File is outside the repository");
            }

            String printName = root.relativize(file).toString().replace(File.separatorChar, '/');

            List<String> indexLines = new ArrayList<>();
            while ((line = indexReader.readLine()) != null) {
                if (line.contains(" " + printName)) {
                    String hashedFile = hashString(getTextOfFile(filePath));
                    indexLines.add(hashedFile + " " + printName);
                    hasAdded = true;
                } else {
                    indexLines.add(line);
                }
            }
            if (!hasAdded) {
                String hashedFile = hashString(getTextOfFile(filePath));
                indexLines.add(hashedFile + " " + printName);
            }
            FileWriter indexWriter = new FileWriter("git/index");
            for (int i = 0; i < indexLines.size() - 1; i++) {
                indexWriter.write(indexLines.get(i) + "\n");
            }
            indexWriter.write(indexLines.get(indexLines.size() - 1));
            indexWriter.close();
            indexReader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String createTree(String workingList, String dirPath) {
        try {
            String[] lines = workingList.split("\n");
            String tree = "";
            for (String line : lines) {
                String[] part = line.split(" ", 3);
                int slashPos = part[2].lastIndexOf('/');
                String dir;
                String name;
                if (slashPos == -1) {
                    dir = "";
                    name = part[2];
                } else {
                    dir = part[2].substring(0, slashPos);
                    name = part[2].substring(slashPos + 1);
                }
                if (dir.equals(dirPath)) {
                    tree += part[0] + " " + part[1] + " " + name + "\n";
                }
            }
            if (tree.endsWith("\n")) {
                tree = tree.substring(0, tree.length() - 1);
            }
            String treeHash = hashString(tree);
            FileWriter fw = new FileWriter("./git/objects/" + treeHash);
            fw.write(tree);
            fw.close();
            return treeHash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String treeIndex() {
        try {
            ArrayList<String> indexArray =
                    new ArrayList<>(Arrays.asList(getTextOfFile("./git/index").split("\n")));

            for (int i = 0; i < indexArray.size(); i++) {
                indexArray.set(i, "blob " + indexArray.get(i));
            }
            String index = "";
            String deepPath = "";

            while (true) {
                deepPath = "";
                for (String string : indexArray) {
                    if ((deepPath.length() - deepPath.replace("/", "").length()) < (string.length()
                            - string.replace("/", "").length())) {
                        deepPath = new File(
                                string.substring(string.indexOf(' ', string.indexOf(' ') + 1) + 1))
                                        .getParent();
                    }
                }
                ArrayList<Integer> remove = new ArrayList<>();
                for (int i = 0; i < indexArray.size(); i++) {
                    String path = indexArray.get(i).split(" ", 3)[2];

                    // extract just parent directory
                    int lastSlashIndex = path.lastIndexOf('/');
                    String parent;
                    if (lastSlashIndex < 0) {
                        parent = "";
                    } else {
                        parent = path.substring(0, lastSlashIndex);
                    }
                    if (parent.equals(deepPath)) {
                        remove.add(i);
                    }
                }
                index = String.join("\n", indexArray).strip();
                indexArray.add("tree " + createTree(index, deepPath) + " " + deepPath);
                for (int i = 0; i < remove.size(); i++) {
                    indexArray.remove((int) remove.get(i) - i);
                }

                // loop end condition
                if (deepPath.isEmpty()) {
                    break;
                }
            }
            return createTree(index, deepPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
