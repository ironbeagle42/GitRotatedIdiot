import java.io.File;

public class Git {
    public static void main(String[] args) {
        File a = new File("git/");
        a.mkdir();
        File b = new File("git/objects/");
        b.mkdir();
        File c = new File("git/index");
        c.mkdir();
        File d = new File("git/HEAD");
        d.mkdir();
    }
}
