import java.io.IOException;
import java.io.InputStream;

import com.dropbox.core.DbxException;


public interface IDropboxUpload {

	void upload(String filenameIncludingPath, long size, InputStream data) throws DbxException, IOException;

}
