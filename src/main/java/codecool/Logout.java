package codecool;

import DAO.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.HttpCookie;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Logout implements HttpHandler {

    private Database database;
    private CookieHelper cookieHelper;

    public Logout(){
        this.database = new Database();
        this.cookieHelper = new CookieHelper();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException{
        String response = "";
        String method = httpExchange.getRequestMethod();

        if(method.equals("GET")){
            String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
            HttpCookie cookie = HttpCookie.parse(cookieStr).get(0);;

            JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
            JtwigModel model = JtwigModel.newModel();

            model.with("username", cookie.getValue());

            response = template.render(model);
        }

        if(method.equals("POST")){

            String header = httpExchange.getRequestHeaders().getFirst("Cookie");
            List<HttpCookie> cookies = HttpCookie.parse(header);

            Optional<HttpCookie> getCookieSessionId = getSessionIdCookie(httpExchange);

            String username = "";
            try{
                for(HttpCookie cookie: cookies){
                    if(cookie.getName().equals("username")){
                        username = cookie.getValue();

                        cookie.setValue("");
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
                    }
                }
                if(getCookieSessionId.get().getName().equals("sessionId")){
                    getCookieSessionId.get().setValue("");
                    getCookieSessionId.get().setPath("/");
                    getCookieSessionId.get().setMaxAge(0);

                    httpExchange.getResponseHeaders().add("Set-Cookie", getCookieSessionId.get().toString());

                    database.setSessionId(null, username);
                }
            }catch(SQLException e){
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }

            httpExchange.getResponseHeaders().add("Location", "/form");
            httpExchange.sendResponseHeaders(303, 0);
        }

        httpExchange.sendResponseHeaders(200, 0);// response.getBytes("UTF-32").length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    private Optional<HttpCookie> getSessionIdCookie(HttpExchange httpExchange){
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        List<HttpCookie> cookies = cookieHelper.parseCookies(cookieStr);
        return cookieHelper.findCookieByName("sessionId", cookies);
    }
}
