import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        List<DropboxFile> all = new ArrayList<DropboxFile>();
        for (int i = 0; i < 100; i++) {
            all.add(new TempFile());
        }

        String json = makeJson(all);
        System.out.println(json);
    }

    private static String makeJson(List<DropboxFile> all) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> outer = new LinkedHashMap<String, Object>();
        outer.put("total-file-count", (long) all.size());
        outer.put("file-extensions", extensions(all));
        outer.put("media-types", mediaTypes(extensions(all)));
        // last 24 hours
        // last week
        return gson.toJson(outer);
    }

    private static Map<String, Long> extensions(List<DropboxFile> all) {
        Map<String, Long> extensions = new HashMap<String, Long>();
        for (DropboxFile f : all) {
            String fn = f.filename();
            String[] parts = fn.split("\\.");
            String ext = "(none)";
            if (parts.length > 1) {
                ext = parts[parts.length - 1];
            }

            if (!extensions.containsKey(ext)) {
                extensions.put(ext, 0L);
            }
            long count = extensions.get(ext);
            count++;
            extensions.put(ext, count);
        }

        return extensions;
    }



    private static Object mediaTypes(Map<String, Long> extensions) {
        CounterThing c = new CounterThing();
        for (Map.Entry<String, Long> e : extensions.entrySet()) {
            c.inc(extToMediaType(e.getKey()), e.getValue());
        }
        return c.get();
    }

    private static String extToMediaType(String ext) {
        String picture = "picture";
        String movie = "movie";
        String text = "text";
        String audio = "audio";

        if ("png".equals(ext)) return picture;
        if ("txt".equals(ext)) return text;
        if ("mkv".equals(ext)) return movie;
        if ("jpg".equals(ext)) return picture;
        if ("mp4".equals(ext)) return movie;
        if ("mp3".equals(ext)) return audio;
        if ("avi".equals(ext)) return movie;
        if ("gif".equals(ext)) return picture;
        if ("bmp".equals(ext)) return picture;

        return "(unknown)";
    }


}
