package codecool;

import DAO.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class Logout implements HttpHandler {

    private Database database;
    private CookieHelper cookieHelper;

    Logout(){
        this.database = new Database();
        this.cookieHelper = new CookieHelper();
    }

    private String renderTemplate(HttpCookie cookie, String cookieName){
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
        JtwigModel model = JtwigModel.newModel();

        if(cookie != null && cookieName != null){
            model.with(cookieName, cookie.getValue());
        }

        return template.render(model);
    }

    private void redirect(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Location", "/form");
        httpExchange.sendResponseHeaders(303, 0);
    }

    private void deleteCookie(HttpCookie cookie, HttpExchange httpExchange){
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
    }

    private void deleteSessionId(Optional<HttpCookie> getCookieSessionId, HttpExchange httpExchange, String username) throws SQLException {
        if(getCookieSessionId.isPresent()){
            getCookieSessionId.get().setValue("");
            getCookieSessionId.get().setPath("/");
            getCookieSessionId.get().setMaxAge(0);

            httpExchange.getResponseHeaders().add("Set-Cookie", getCookieSessionId.get().toString());
        }

        database.setSessionId(null, username);
    }

    private boolean checkUserNameCookieExists(HttpCookie cookie){
        return cookie.getName().equals("username");
    }

    private boolean checkSessionIdCookieExists(Optional<HttpCookie> getCookieSessionId){
        if(getCookieSessionId.isPresent()){
            return getCookieSessionId.get().getName().equals("sessionId");
        }
        return false;
    }

    private List<HttpCookie> parseCookieToList(HttpExchange httpExchange){
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        return HttpCookie.parse(cookieStr);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException{
        String response = "";
        String method = httpExchange.getRequestMethod();

        if(method.equals("GET")){
            List<HttpCookie> cookies = parseCookieToList(httpExchange);
            response = renderTemplate(cookies.get(0), "username");
        }

        if(method.equals("POST")){
            List<HttpCookie> cookies = parseCookieToList(httpExchange);
            Optional<HttpCookie> getCookieSessionId = getSessionIdCookie(httpExchange);

            String username = "";
            try{
                for(HttpCookie cookie: cookies){
                    if(checkUserNameCookieExists(cookie)){
                        username = cookie.getValue();
                        deleteCookie(cookie, httpExchange);
                    }
                }
                if(checkSessionIdCookieExists(getCookieSessionId)){
                    deleteSessionId(getCookieSessionId, httpExchange, username);
                }
            }catch(SQLException e){
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            redirect(httpExchange);
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
