package dropboxhackathon;

public interface DropboxFile {

    long lastModified();

    String filename();

    String path();

}
