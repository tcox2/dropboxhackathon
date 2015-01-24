import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.dropbox.core.DbxAccountInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxDelta;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWriteMode;
import org.apache.commons.io.FileUtils;


public class DropboxService implements IDropboxService {

	private  final String ACCESS_TOKEN = "";

	private String lastDeltaCursor = "";

	@Override
	public List<IDropboxFile> getFileList(final String accessToken) throws DbxException {

		final List<IDropboxFile> allFilesInDropbox = traverseHierarchy(getDbxClient(accessToken), "/");

		return allFilesInDropbox;
	}

	public DbxDelta<DbxEntry> getDelta(final String accessToken) throws DbxException {
		final DbxDelta<DbxEntry> delta = getDbxClient(accessToken).getDelta(lastDeltaCursor.isEmpty() ? null : lastDeltaCursor);
		lastDeltaCursor = delta.cursor;

		if (delta.hasMore)
		{
			getDelta(accessToken);
		}

		System.out.println("delta result:");
		System.out.println(delta);

		return delta;
	}

	@Override
	public void upload(final String accessToken, final String filenameIncludingPath, final long size, final InputStream data)
			throws DbxException, IOException {
		getDbxClient(accessToken).uploadFile(filenameIncludingPath, DbxWriteMode.force(), size, data);
	}

	@Override
	public String getFileHash(String accessToken, IDropboxFile file) throws DbxException {

		return hashFile(getDbxClient(accessToken), file);
	}

	@Override
	public IDropboxStats getQuotaStats(String accessToken) throws DbxException {
		final DbxAccountInfo.Quota quotaInfo = getAccountInfo(getDbxClient(accessToken)).quota;

		return new DropboxStats(quotaInfo);
	}

	private DbxClient getDbxClient(final String accessToken) throws DbxException {

		final DbxRequestConfig config = new DbxRequestConfig(
				"ContentAnalyzer/1.0", Locale.getDefault().toString());

		return new DbxClient(config, accessToken);
	}

	private  DbxAccountInfo getAccountInfo(final DbxClient client) throws DbxException {
		final DbxAccountInfo accountInfo = client.getAccountInfo();

		return accountInfo;
	}

	private List<IDropboxFile> traverseHierarchy(final DbxClient client, final String parentPath)
			throws DbxException {

		final List<IDropboxFile> files = new ArrayList<IDropboxFile>();

		final DbxEntry.WithChildren listing = client.getMetadataWithChildren(parentPath);

		for (final DbxEntry child : listing.children) {

			if (child.isFile()) {
				final DbxEntry.File dropboxFile = child.asFile();
				files.add(new DropboxFile(dropboxFile));
			}
			else if (child.isFolder()) {
				files.addAll(traverseHierarchy(client, child.asFolder().path));
			}
		}

		return files;
	}

	private File downloadFile(final DbxClient client, final IDropboxFile file)
			throws DbxException {

		File tempFile = null;

		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), file.filename());
			final FileOutputStream target = new FileOutputStream(tempFile);

			try {
				client.getFile(file.fullPath(), file.rev(), target);
			}
			finally {
				target.close();
			}
		} catch (final IOException e) {
			System.out.println("Error creating temp file:");
			e.printStackTrace();
		}

		return tempFile;
	}

	private String hashFile(final DbxClient client, final IDropboxFile file) throws DbxException {

		final File fileToHash = downloadFile(client, file);

		final String myHash = "MD5";
		MessageDigest complete;
		String result = "";

		try {
			complete = MessageDigest.getInstance(myHash);

			final byte[] b = complete.digest(FileUtils.readFileToByteArray(fileToHash));

			for (int i=0; i < b.length; i++) {
				result +=
						Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
			}
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error generating file hash:");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error generating file hash:");
			e.printStackTrace();
		}

		return result;
	}
}
