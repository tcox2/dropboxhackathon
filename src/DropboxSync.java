import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.dropbox.core.DbxAccountInfo;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxDelta;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;


public class DropboxSync implements IDropboxUpload {

	final static String APP_KEY = "5qrvnudv5gltoy8";
	final static String APP_SECRET = "ystltok87pzc6ue";
	final static String ACCESS_TOKEN = "";

	static String lastDeltaCursor = "";
	static DbxClient client = null;

	public static void main(final String[] args) throws DbxException {

		final DbxClient client = getDbxClient();

		final DbxAccountInfo accountInfo = getAccountInfo(client);
		System.out.println("Linked account: " + accountInfo.displayName);

		System.out.println("normal: " + accountInfo.quota.normal);
		System.out.println("shared: " + accountInfo.quota.shared);
		System.out.println("total: " + accountInfo.quota.total);

		getFileList();
		getDelta();
	}

	private static DbxClient getDbxClient() throws DbxException {

		if (client == null)
		{
			final DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

			final DbxRequestConfig config = new DbxRequestConfig(
					"ContentAnalyzer/1.0", Locale.getDefault().toString());
			String accessToken = ACCESS_TOKEN;

			if (accessToken.isEmpty())
			{
				final DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

				final String authorizeUrl = webAuth.start();
				System.out.println("1. Go to: " + authorizeUrl);
				System.out.println("2. Click \"Allow\" (you might have to log in first)");
				System.out.println("3. Copy the authorization code.");
				String code = "";

				try {
					code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
				} catch (IOException e) {
					System.out.println("Error reading user input:");
					e.printStackTrace();
				}

				final DbxAuthFinish authFinish = webAuth.finish(code);
				accessToken = authFinish.accessToken;
				System.out.println(accessToken);
			}

			client = new DbxClient(config, accessToken);
		}

		return client;
	}

	private static DbxAccountInfo getAccountInfo(final DbxClient client) throws DbxException {
		final DbxAccountInfo accountInfo = client.getAccountInfo();

		return accountInfo;
	}

	private static void getFileList() throws DbxException {

		final List<IDropboxFile> allFilesInDropbox = traverseHierarchy("/");
	}

	private static DbxDelta<DbxEntry> getDelta() throws DbxException {
		final DbxDelta<DbxEntry> delta = getDbxClient().getDelta(lastDeltaCursor.isEmpty() ? null : lastDeltaCursor);
		lastDeltaCursor = delta.cursor;

		if (delta.hasMore)
		{
			getDelta();
		}

		System.out.println("delta result:");
		System.out.println(delta);

		return delta;
	}

	private static List<IDropboxFile> traverseHierarchy(final String parentPath)
			throws DbxException {

		final List<IDropboxFile> files = new ArrayList<IDropboxFile>();

		final DbxEntry.WithChildren listing = getDbxClient().getMetadataWithChildren(parentPath);

		for (final DbxEntry child : listing.children) {

			if (child.isFile()) {
				final DbxEntry.File dropboxFile = child.asFile();
				final DropboxFile file = new DropboxFile(dropboxFile);

				final File tempFile = downloadFile(getDbxClient(), dropboxFile, file);
				file.setHash(hashFile(tempFile));

				files.add(file);
			}
			else if (child.isFolder()) {
				files.addAll(traverseHierarchy(child.asFolder().path));
			}
		}

		return files;
	}

	private static File downloadFile(final DbxClient client, final DbxEntry.File dropboxFile, final DropboxFile file)
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
		} catch (IOException e) {
			System.out.println("Error creating temp file:");
			e.printStackTrace();
		}

		return tempFile;
	}

	private static String hashFile(final File tempFile) {

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

	@Override
	public void upload(String filenameIncludingPath, long size, InputStream data) throws DbxException, IOException {
		getDbxClient().uploadFile(filenameIncludingPath, DbxWriteMode.force(), size, data);
	}
}
