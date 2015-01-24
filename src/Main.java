import com.dropbox.core.DbxException;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static final int CRON_DELAY_SECONDS = 10;

    private static IDropboxService dropbox;



    public static void main(String[] args) throws Exception {
        dropbox = new DropboxService();

        System.out.println("Manifest Backend - Team 25");

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("HTTP service endpoint running on port " + port);


        while (true) {
             try {
                System.out.println("\n------ STARTING RUN -----");
                runProcess();
                System.out.println("\n------ RUN COMPLETE -----");
            } catch (Throwable t) {
                System.out.println("COLLECTION failed.  Will try again later.");
                t.printStackTrace();
            }
            sleep();

        }
    }

    private static void runProcess() throws IOException, DbxException {
        System.out.println("Running process");
        List<String> tokens = readTokens();
        if (tokens.isEmpty()) {
            System.out.println("There are no registered tokens.  Nothing to do.");
        }
        for (String token : tokens) {
            System.out.println("COLLECTION on token " + token);

            List<IDropboxFile> allFiles = dropbox.getFileList(token);

            allFiles = removeReportFiles(allFiles);

            IDropboxStats stats = dropbox.getQuotaStats(token);

            String json = OldMain.makeJson(allFiles, stats);
            byte[] data = json.getBytes();
            InputStream is = new ByteArrayInputStream(data);
            System.out.println("Uploading json report (" + data.length + " bytes)");
            String jsonReportFilename = jsonReportFilename();
            dropbox.upload(token, jsonReportFilename, data.length, is);
            System.out.println("Uploaded file to dropbox: " + jsonReportFilename);
        }
    }

    private static List<IDropboxFile> removeReportFiles(List<IDropboxFile> allFiles) {
        List out = new ArrayList<IDropboxFile>();
        for (IDropboxFile f : allFiles) {
            if (!f.fullPath().startsWith("/Apps/Manifest")) {
                out.add(f);
            }
        }
        System.out.println("removeReportFiles: Examining " + out.size() + " files out of " + allFiles.size());
        return out;
    }

    private static String jsonReportFilename() {
        SimpleDateFormat format=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS");
        String date=format.format(new Date());

        return "/Apps/Manifest/Manifest_Report_" + date + ".json";
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(com.sun.net.httpserver.HttpExchange t) throws IOException {
            System.out.println("Handling incoming request");
            InputStream is = t.getRequestBody();
            String response;
            int code;
            try {
                User u = read(is);
                System.out.println("New access token: " + u.accessToken);
                List<String> tokens = readTokens();
                tokens.add(u.accessToken);
                writeTokens(tokens);
                response = "ok";
                code = 200;
                System.out.println("Request handled");
            } catch (Throwable f) {
                System.out.println(f);
                response = "failed";
                code = 500;
            }
            t.sendResponseHeaders(code, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private static void writeTokens(List<String> tokens) throws IOException {
            tokens = stripQuotes(tokens);
            tokens = new ArrayList(new HashSet(tokens)); // dedupe
            FileUtils.writeLines(new File("tokens"), tokens);
            System.out.println("Updated tokens file with " + tokens.size() + " tokens");
        }

        private static List<String> stripQuotes(List<String> tokens) {
            List<String> out = new ArrayList<String>();
            for (String t : tokens) {
                if (t.startsWith("\"")) {
                    t = t.substring(1);
                }
                if (t.endsWith("\"")) {
                    t = t.substring(0, t.length() - 1);
                }
                out.add(t);
            }
            return out;
        }

    }

    private static List<String> readTokens() throws IOException {
        try {
            List<String> s =  FileUtils.readLines(new File("tokens"));
            System.out.println("Read " + s.size() + " access tokens from tokens file");
            return s;
        } catch (FileNotFoundException e) {
            return new ArrayList<String>();
        }
    }


    private static User read(InputStream is) {
        User u = new User();
        JsonElement parse = Streams.parse(new JsonReader(new InputStreamReader(is)));
        for (Map.Entry<String, JsonElement> e : parse.getAsJsonObject().entrySet()) {
            System.out.println(e.getKey() + "     " + e.getValue().toString());

            if ("accessToken".equals(e.getKey())) {
                u.accessToken = e.getValue().toString();
            }
        }
        if (u.accessToken == null || u.accessToken.length() == 0) {
            throw new RuntimeException("Cannot find access token in request");
        }
        return u;
    }

    private static void sleep() throws InterruptedException {
        for (int i = CRON_DELAY_SECONDS; i > 0; i--) {
            System.out.print("COLLECTION will run in " + i + " seconds       \r");
            Thread.sleep(1000);
        }
        System.out.println("COLLECTION Running                               ");
    }

}

class User {
    String accessToken;
}

/*

post to http://tims-laptop:8000/userlogin

{
       accessToken: 'geuyheuheuehiehuiehe'
}

response:

ok (200)

or

failed (500)


 */