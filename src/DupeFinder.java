

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DupeFinder {

    public static Object find(List<DropboxFile> all) {
        // pair is <hash, size>
        Map<Pair<String,Long>, DuplicatedFile> foo = new HashMap<Pair<String, Long>, DuplicatedFile>();

        for (DropboxFile f : all) {
            Pair<String, Long> key = new ImmutablePair<String, Long>(f.hash(), f.size());

            if (!foo.containsKey(key)) {
                DuplicatedFile value = new DuplicatedFile();
                value.filename = f.filename();
                value.path = f.path();
                value.fileSize = f.size();
                foo.put(key, value);

            }
            foo.get(key).incDupes();
        }

        removeDupes(foo);

        return foo;
    }

    private static void removeDupes(Map<Pair<String, Long>, DuplicatedFile> foo) {
        List<Pair<String, Long>> keysToRemove = new ArrayList<Pair<String, Long>>();
        for (Map.Entry<Pair<String, Long>, DuplicatedFile> e : foo.entrySet()) {
            if (e.getValue().numberofdupes() < 2) {
                keysToRemove.add(e.getKey());
            }
        }
        for (Pair<String, Long> r : keysToRemove) {
            foo.put(r, null);
        }
    }

    // top five:
    // (one of the) filenames/path, num dupes, space wasted
    //
    // total wasted space


}
 class DuplicatedFile {
    private long numberOfCopies = 0;
    String filename;
    String path;
    long fileSize;
     long totalWastedSpace = 0;

     public long numberofdupes() {
         return numberOfCopies;
     }

     public void incDupes() {
         numberOfCopies++;
         totalWastedSpace = (numberOfCopies -1) * fileSize;
     }
 }