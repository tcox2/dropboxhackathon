import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("Manifest Backend - Team 25");

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
        server.createContext("/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("HTTP service endpoint running on port " + port);


        // while (true) {
        //    sleep();
        //}
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
            tokens = new ArrayList(new HashSet(tokens)); // dedupe
            FileUtils.writeLines(new File("tokens"), tokens);
            System.out.println("Updated tokens file with " + tokens.size() + " tokens");
        }

    }

    private static List<String> readTokens() throws IOException {
        try {
            List<String> s =  FileUtils.readLines(new File("tokens"));
            System.out.println("Read " + s.size() + " access tokens from tokens file");
            return s;
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
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
        for (int i = 60; i > 0; i--) {
            System.out.print("Will run in " + i + " seconds       \r");
            Thread.sleep(1000);
        }
        System.out.println("Running                               ");
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