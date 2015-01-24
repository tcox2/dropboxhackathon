import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dropbox.core.DbxEntry;

public class DropboxFile implements IDropboxFile {

	final DbxEntry.File dropboxFile;

	public DropboxFile(DbxEntry.File dropboxFile) {
		this.dropboxFile = dropboxFile;
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
		List<String> filePathSegments = new ArrayList<String>();
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
	public String rev() {
		return dropboxFile.rev;
	}

	@Override
	public String fullPath() {
		return dropboxFile.path;
	}

}
