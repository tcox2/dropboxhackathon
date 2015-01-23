import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OldBigFiles {

    public static Object find(List<IDropboxFile> all) {
        List<OldBigFile> old = new ArrayList<OldBigFile>();

        long now = new Date().getTime();
        long oneHourAgo = now - (60 * 60 * 1000);  /* mins seconds micros */

        // find old files (>1 hr old)
        for (IDropboxFile f : all) {
            if (f.lastModified() < oneHourAgo) {
                OldBigFile x = new OldBigFile();
                x.filename = f.filename();
                x.path = f.path();
                x.size = f.size();
                x.ageInMillis = now - f.lastModified();
                old.add(x);
                if (old.size() >= 5) {
                    return old;
                }
            }
        }

        return old;
    }


}

class OldBigFile {
    String filename;
    String path;
    long size;
    long ageInMillis;
}
