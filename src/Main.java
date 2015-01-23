import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Gson gson = new Gson();
        Map<String, Long> outer = new LinkedHashMap<String, Long>();
        outer.put("total-file-count", (long) all.size());
        return gson.toJson(outer);
    }

}
