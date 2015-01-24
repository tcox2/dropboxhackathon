
public interface IDropboxFile {

	long lastModified();

	String filename();

	String path();

	String fullPath();

	long size();

	String rev();

}
