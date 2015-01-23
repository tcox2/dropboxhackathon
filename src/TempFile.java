
import java.util.Date;
import java.util.Random;

// team 25


public class TempFile implements DropboxFile {

    static Random random = new Random();

    private final long lastModified;
    private final String filename;
    private final String path;
    private final long size;

    TempFile() {
        this.lastModified = Math.abs(random.nextLong() % new Date().getTime());
        this.filename = "filename_" + Math.random() + "_" + Math.random() + anExtension();
        this.path = aPath();
        this.size = Math.abs(random.nextInt() % 10000000);
    }

    private String anExtension() {
        String[] extensions = {".avi", ".mkv", ".mp3", ".mp4", ".txt", ".bmp", ".png", ".gif",
                ".jpg"};
        return extensions[random.nextInt(extensions.length)];
    }

    private String aPath() {
        String[] paths = {
                "/foo",
                "/foo/bar",
                "/monkey"
        };
        return paths[random.nextInt(paths.length)];
    }


    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public String filename() {
        return filename;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public String toString() {
        return "TempFile{" +
                "lastModified=" + lastModified +
                ", filename='" + filename + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                '}';
    }
}
