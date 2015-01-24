import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.spi.http.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
                read(is);
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
    }

    private static void read(InputStream is) {
        JsonElement parse = Streams.parse(new JsonReader(new InputStreamReader(is)));
        System.out.println(parse);
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