import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.dropbox.core.DbxException;


public interface IDropboxService {

	void upload(String accessToken, String filenameIncludingPath, long size, InputStream data) throws DbxException, IOException;

	List<IDropboxFile> getFileList(String accessToken) throws DbxException;

	String getFileHash(String accessToken, IDropboxFile file) throws DbxException;

	IDropboxStats getQuotaStats(String accessToken) throws DbxException;

}
