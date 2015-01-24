import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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


public class DropboxService implements IDropboxService {

	private  final String ACCESS_TOKEN = "";

	private String lastDeltaCursor = "";

	@Override
	public List<IDropboxFile> getFileList(final String accessToken) throws DbxException {

		final List<IDropboxFile> allFilesInDropbox = traverseHierarchy(accessToken, "/");

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

	private DbxClient getDbxClient(final String accessToken) throws DbxException {

		final DbxRequestConfig config = new DbxRequestConfig(
				"ContentAnalyzer/1.0", Locale.getDefault().toString());

		return new DbxClient(config, accessToken);
	}

	private  DbxAccountInfo getAccountInfo(final DbxClient client) throws DbxException {
		final DbxAccountInfo accountInfo = client.getAccountInfo();

		return accountInfo;
	}

	private List<IDropboxFile> traverseHierarchy(final String accessToken, final String parentPath)
			throws DbxException {

		final List<IDropboxFile> files = new ArrayList<IDropboxFile>();

		final DbxEntry.WithChildren listing = getDbxClient(accessToken).getMetadataWithChildren(parentPath);

		for (final DbxEntry child : listing.children) {

			if (child.isFile()) {
				final DbxEntry.File dropboxFile = child.asFile();
				final DropboxFile file = new DropboxFile(dropboxFile);

				final File tempFile = downloadFile(getDbxClient(accessToken), dropboxFile, file);
				file.setHash(hashFile(tempFile));

				files.add(file);
			}
			else if (child.isFolder()) {
				files.addAll(traverseHierarchy(accessToken, child.asFolder().path));
			}
		}

		return files;
	}

	private File downloadFile(final DbxClient client, final DbxEntry.File dropboxFile, final DropboxFile file)
			throws DbxException {

		File tempFile = null;

		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), file.filename());
			final FileOutputStream target = new FileOutputStream(tempFile);

			try {

				client.getFile(dropboxFile.path, dropboxFile.rev, target);
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

	private String hashFile(final File tempFile) {

		final String myHash = "MD5";
		MessageDigest complete;
		String result = "";

		try {
			complete = MessageDigest.getInstance(myHash);

			final byte[] b = complete.digest(Files.readAllBytes(tempFile.toPath()));

			for (int i=0; i < b.length; i++) {
				result +=
						Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			System.out.println("Error generating file hash:");
			e.printStackTrace();
		}

		return result;
	}
}
