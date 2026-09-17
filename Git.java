import java.io.File;
import java.io.IOException;

public class Git {
    public static void main(String[] args) {
        init();
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
}
