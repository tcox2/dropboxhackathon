import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		List<String> filePathSegments = new ArrayList<>();
		filePathSegments.addAll(Arrays.asList(dropboxFile.path.split("/")));
		filePathSegments.remove(0);
		filePathSegments.remove(filePathSegments.size() - 1);
		
		if (filePathSegments.isEmpty()) {
			return "/";
		}
		
		for (String pathSegment : filePathSegments)
		{
			filePath += "/";
			filePath += pathSegment;
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
