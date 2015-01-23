
public interface DropboxFile {

    long lastModified();

    String filename();

    String path();

    long size();
}
