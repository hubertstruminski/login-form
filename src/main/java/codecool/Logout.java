package codecool;

import DAO.Database;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Logout implements HttpHandler {

    private Database database = new Database();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        String method = httpExchange.getRequestMethod();


        // Send a form if it wasn't submitted yet.
        if(method.equals("GET")){


        }

//         If the form was submitted, retrieve it's content.
        if(method.equals("POST")){

            String header = httpExchange.getRequestHeaders().getFirst("Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(header);

            for(HttpCookie cookie: cookies){
                if(cookie.getName().equals("username")){
                    cookie.setMaxAge(0);
                }
            }

        }


        httpExchange.sendResponseHeaders(200, response.getBytes("UTF-32").length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
