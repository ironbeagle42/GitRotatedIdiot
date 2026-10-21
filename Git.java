import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class Git {
    public static void main(String[] args) throws IOException {
        init();

        blob("./testing/hello.txt");
        indexFile("./testing/hello.txt");

        blob("./testing/world.txt");
        indexFile("./testing/world.txt");

        blob("./testing/subfolder/xyz.txt");
        indexFile("./testing/subfolder/xyz.txt");

        System.out.println(treeIndex());

        rebuildIndex(commit("demir", "hello"));
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
            blobWriter.close();
            indexFile(filePath);
        } catch (IOException e) {
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
            e.printStackTrace();
            return "";
        }
    }

    @SuppressWarnings("resource")
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

            // added fix for consistent ordering; sort after finishing
            if (!tree.isEmpty()) {
                List<String> entries = new ArrayList<>(Arrays.asList(tree.split("\n")));
                java.util.Collections.sort(entries);
                tree = String.join("\n", entries);
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
            ArrayList<String> indexArray = new ArrayList<>(Arrays.asList(getTextOfFile("./git/index").split("\n")));

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

    public static String commit(String author, String message) throws IOException {
        Path head = Path.of("git/HEAD");
        String parent = Files.readString(head).strip();

        // initialize root tree index, using existing method
        String rootTree = treeIndex();
        if (rootTree == null) {
            throw new IOException("creating root tree failed");
        }

        // populate the commit BLOB
        String commitString = "tree: " + rootTree + "\n";
        if (!parent.isEmpty()) {
            commitString += "parent: " + parent + "\n";
        }
        commitString += "author: " + author + "\n";
        commitString += "date: " + Instant.now() + "\n";
        commitString += "message: " + message + "\n\n";

        String commitHash = hashString(commitString);

        // writing to HEAD
        try {
            Files.writeString(Path.of("git/objects", commitHash), commitString);
            Files.writeString(head, commitHash);
        } catch (IOException e) {
            Files.writeString(head, parent); // if it fails we can restore HEAD
            throw new IOException("commit file writing failed", e);
        }

        return commitHash;
    }

    public static void rebuildIndex(String commitHash) throws IOException {
        // read a recorded commit
        List<String> commitLines = Files.readAllLines(Path.of("git/objects", commitHash));

        // check if commit is proper
        if (commitLines.isEmpty() || !commitLines.get(0).startsWith("tree: ")) {
            throw new IOException("commit missing root tree");
        }

        // get commit root hash
        String rootTree = commitLines.get(0).substring(6).strip();

        List<String> indexLines = new ArrayList<>();
        loopThroughTreeAndCollectEntries(rootTree, "", indexLines);

        Files.writeString(Path.of("git/index"), String.join("\n", indexLines));
    }

    private static void loopThroughTreeAndCollectEntries(String treeHash, String directory,
            List<String> indexLines)
            throws IOException {

        List<String> treeLines = Files.readAllLines(Path.of("git/objects", treeHash));

        for (String line : treeLines) {
            // format: blob/tree hash name
            String[] lineParts = line.split(" ", 3);

            String type = lineParts[0];
            String hash = lineParts[1];
            String path = directory + lineParts[2];

            // if its blob, add, if its tree, run recursively
            if (type.equals("blob")) {
                indexLines.add(hash + " " + path);
            } else if (type.equals("tree")) {
                loopThroughTreeAndCollectEntries(hash, path + "/", indexLines);
            } else {
                throw new IOException("weird entry type" + type);
            }
        }

    }

    public static void checkout(String commitHash) throws IOException {
        // collect all files from past commit into indexLines
        List<String> commitLines = Files.readAllLines(Path.of("git/objects", commitHash));
        if (commitLines.isEmpty() || !commitLines.get(0).startsWith("tree: ")) {
            throw new IOException("commit missing root tree");
        }
        String rootTree = commitLines.get(0).substring(6).strip();
        List<String> indexLines = new ArrayList<>();
        loopThroughTreeAndCollectEntries(rootTree, "", indexLines);

        // remove staged files that are not from this commit
        // cross reference every line in old index with the new commit index
        for (String oldLine : Files.readAllLines(Path.of("git/index"))) {
            Path oldFilePath = Path.of(oldLine.split(" ", 2)[1]).normalize();
            boolean inCommit = false;
            for (String line : indexLines) {
                Path commitFilePath = Path.of(line.split(" ", 2)[1]).normalize();
                if (oldFilePath.equals(commitFilePath)) {
                    inCommit = true;
                    break;
                }
            }
            if (!inCommit) {
                Files.deleteIfExists(oldFilePath);
            }
        }

        // fill in all the files that are saved by that commit
        for (String line : indexLines) {
            // each entry = blobHash, relativePath
            // get the target file path
            String[] parts = line.split(" ", 2);
            String commitFilePath = parts[1];

            // get the target file contents from blob
            Path blobPath = Path.of("git/objects", parts[0]);
            byte[] contents = Files.readAllBytes(blobPath);

            // write target file
            File commitFile = new File(commitFilePath);
            if (commitFile.getParentFile() != null) {
                commitFile.getParentFile().mkdirs();
            }
            Files.write(Path.of(commitFilePath), contents);
        }

        // TODO delete empty folders
        // TODO make sure not staged files are protected

        // replace the index with this snapshot then record the checked out commit
        Files.writeString(Path.of("git/index"), String.join("\n", indexLines));
        Files.writeString(Path.of("git/HEAD"), commitHash);
    }
}
