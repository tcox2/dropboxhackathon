import com.dropbox.core.DbxException;
import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
            if (token.startsWith("latest ")) {
                token = token.replace("latest ", "");
            }
            System.out.println("COLLECTION on token [" + token + "]");

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
                Request u = read(is);
                if (u.command == null) {
                    System.out.println("New access token: " + u.accessToken);
                    List<String> tokens = readTokens();
                    tokens.add(u.accessToken);
                    writeTokens(tokens);
                    response = "ok";
                    code = 200;
                    System.out.println("Request handled");
                } else if ("get_latest_json".equals(u.command)) {
                    System.out.println("getting latest report for token " + u.accessToken);
                    String latestJson = dropbox.getLatestReport(u.accessToken);
                    System.out.println("latest json: " + latestJson);
                    response = latestJson;
                    code = 200;
                    System.out.printf("Front end requested latest report");
                } else {
                    response = "bad request";
                    code = 400;
                }
            } catch (Throwable f) {
                System.out.println(f);
                f.printStackTrace();
                response = "failed";
                code = 500;
            }
            List<String> fff = new ArrayList<String>();
            fff.add("application/json");
            t.getResponseHeaders().put("Content-Type",fff);
            t.sendResponseHeaders(code, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println("http request closed");
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


    private static Request read(InputStream is) throws IOException {
        Request u = new Request();
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        String theString = writer.toString();
        System.out.println("received: " + theString);

        String token = theString.trim();
        token = token.replace("latest ", "");
        System.out.println("token: " + token);

        List<String> t = readTokens();
        t.add(token.trim());
        writeTokens(t);

        u.command = "get_latest_json";

        u.accessToken = token;
        System.out.println("uat=[" + u.accessToken + "]");
        return u;
    }

    private static void sleep() throws InterruptedException {
        for (int i = CRON_DELAY_SECONDS; i > 0; i--) {
            System.out.print("COLLECTION will run in " + i + " seconds       \r");
            Thread.sleep(1000);
        }
        System.out.println("COLLECTION Running                               ");
    }

    private static void writeTokens(List<String> tokens) throws IOException {
        tokens = new ArrayList(new HashSet(tokens)); // dedupe
        FileUtils.writeLines(new File("tokens"), tokens);
        System.out.println("Updated tokens file with " + tokens.size() + " tokens");
    }

}

class Request {
    String accessToken;
    String command;
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