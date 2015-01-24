import java.io.FileInputStream;
import java.io.InputStream;


public interface IDropboxUpload {

	void upload(String filenameIncludingPath, long size, InputStream data);

}
