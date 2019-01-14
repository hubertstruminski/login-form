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
            String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
            HttpCookie cookie = HttpCookie.parse(cookieStr).get(0);;

            JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
            JtwigModel model = JtwigModel.newModel();

            model.with("username", cookie.getValue());

            response = template.render(model);

        }

//         If the form was submitted, retrieve it's content.
        if(method.equals("POST")){

            String header = httpExchange.getRequestHeaders().getFirst("Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(header);

            for(HttpCookie cookie: cookies){
                if(cookie.getName().equals("username")){
                    System.out.println("Username: " + cookie.getValue());
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());



//                    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
//                    JtwigModel model = JtwigModel.newModel();
//
//                    response = template.render(model);

                    httpExchange.getResponseHeaders().add("Location", "/form");
                    httpExchange.sendResponseHeaders(303, 0);
                }else{
//                    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
//                    JtwigModel model = JtwigModel.newModel();
//
//                    cookie.setMaxAge(0);
//
//                    response = template.render(model);

//                    httpExchange.getResponseHeaders().add("Location", "/form");
//                    httpExchange.sendResponseHeaders(303, 0);
                }
            }

        }


        httpExchange.sendResponseHeaders(200, response.getBytes("UTF-32").length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
