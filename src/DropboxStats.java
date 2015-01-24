import com.dropbox.core.DbxAccountInfo;


public class DropboxStats implements IDropboxStats {

	final DbxAccountInfo.Quota quotaStats;

	public DropboxStats (final DbxAccountInfo.Quota quotaStats) {
		this.quotaStats = quotaStats;
	}

	@Override
	public long totalSizeInBytes() {
		return quotaStats.total;
	}

	@Override
	public long spaceAvailableInBytes() {
		return totalSizeInBytes() - spaceUsedInBytes();
	}

	@Override
	public long spaceUsedInBytes() {
		return quotaStats.total - quotaStats.normal - quotaStats.shared;
	}

	@Override
	public long spaceUsedSharedInBytes() {
		return quotaStats.shared;
	}

}
