import java.io.FileInputStream;


public interface IDropboxUpload {

	long fileSize();

	FileInputStream fileInputStream();

}
