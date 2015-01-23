import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
        return "";
    }

}
