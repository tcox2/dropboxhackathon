import com.dropbox.core.DbxEntry;

public class DropboxFile implements IDropboxFile {
	
	final DbxEntry.File dropboxFile;
	final String hash;
	
	public DropboxFile(DbxEntry.File dropboxFile, String hash) {
		this.dropboxFile = dropboxFile;
		this.hash = hash;
	}

	@Override
	public long lastModified() {
		return dropboxFile.lastModified.getTime();
	}

	@Override
	public String filename() {
		String[] filePathSegments = dropboxFile.path.split("/");
		
		return filePathSegments[filePathSegments.length - 1];
	}

	@Override
	public String path() {
		String filePath = "";
		String[] filePathSegments = dropboxFile.path.split("/");
		
		if (filePathSegments.length < 2) {
			return "/";
		}
		
		for (int i = 0; i < filePathSegments.length - 2; i++)
		{
			filePath += "/";
			filePath += filePathSegments[i];
		}
		
		return filePath;
	}

	@Override
	public long size() {
		return dropboxFile.numBytes;
	}

	@Override
	public String hash() {
		return hash;
	}

}
