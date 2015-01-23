
public interface IDropboxFile {

    long lastModified();

    String filename();

    String path();

    long size();

    String hash();

}
