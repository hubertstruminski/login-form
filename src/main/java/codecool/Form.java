package codecool;

import DAO.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.*;

public class Form implements HttpHandler {

    private Database database;
    private CookieHelper cookieHelper;
    private Generator generator;

    public Form(){
        this.database = new Database();
        this.cookieHelper = new CookieHelper();
        this.generator = new Generator();
    }

    public String renderTemplate(String templateName, HttpCookie cookie, String cookieName){
        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/" + templateName);
        JtwigModel model = JtwigModel.newModel();

        if(cookie != null && cookieName != null){
            model.with(cookieName, cookie.getValue());
        }

        return template.render(model);
    }

    public void redirect(HttpExchange httpExchange, String pageName) throws IOException {
        httpExchange.getResponseHeaders().add("Location", pageName);
        httpExchange.sendResponseHeaders(303, 0);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        String method = httpExchange.getRequestMethod();


        if(method.equals("GET")){

            String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
            Optional<HttpCookie> getCookieSessionId = getSessionIdCookie(httpExchange);
            List<HttpCookie> cookies = null;

            if(cookieStr != null){
                cookies = HttpCookie.parse(cookieStr);
            }else{
                response = renderTemplate("index.twig", null, null);
            }

            try{
                for(HttpCookie cookie: cookies){

                    String sessionId = getCookieSessionId.get().getValue().substring(1, getCookieSessionId.get().getValue().length()-1);

                    if (database.checkUsernameCookie(cookie.getValue(), sessionId)) {
                        cookie = HttpCookie.parse(cookieStr).get(0);

                        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
                        JtwigModel model = JtwigModel.newModel();

                        model.with("username", cookie.getValue());

                        response = template.render(model);

                        redirect(httpExchange, "/logout");

                    }else if(cookie.getName().equals("username") && cookie.getValue().equals("")){

                        response = renderTemplate("index.twig", null, null);
                    }
                }
            }catch(Exception e){
                System.err.println(e.getClass().getName()+ ": " + e.getMessage());
            }


        }

        if(method.equals("POST")){
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();

            System.out.println(formData);
            Map inputs = parseFormData(formData);

            try{
                if(database.selectAllData(inputs.get("name").toString(), inputs.get("password").toString())){
                    // cookie - username
                    HttpCookie cookie = new HttpCookie("username", inputs.get("name").toString());
                    cookie.setMaxAge(2*60);
                    httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
                    // end

                    // cookie - sessionID
                    String randomSessionId = generator.generateUniqueSessionID();
                    Optional<HttpCookie> cookieSessionId = Optional.of(new HttpCookie("sessionId", randomSessionId));
                    cookieSessionId.get().setMaxAge(2*60);
                    httpExchange.getResponseHeaders().add("Set-Cookie", cookieSessionId.get().toString());
                            //update sessionID in DB
                    database.setSessionId(randomSessionId, inputs.get("name").toString());
                    // end

                    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
                    JtwigModel model = JtwigModel.newModel();

                    model.with("username", cookie.getValue());

                    response = template.render(model);

                    httpExchange.getResponseHeaders().add("Location", "/logout");
                    httpExchange.sendResponseHeaders(303, 0);
                }else{

                    response = renderTemplate("index.twig", null, null);
                }
            }catch(SQLException e){
                System.err.println(e.getClass().getName()+ ": " + e.getMessage());
            }
        }


        httpExchange.sendResponseHeaders(200, 0);//response.getBytes("UTF-32").length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for(String pair : pairs){
            String[] keyValue = pair.split("=");
            // We have to decode the value because it's urlencoded. see: https://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }
        return map;
    }

    private Optional<HttpCookie> getSessionIdCookie(HttpExchange httpExchange){
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
        List<HttpCookie> cookies = cookieHelper.parseCookies(cookieStr);
        return cookieHelper.findCookieByName("sessionId", cookies);
    }
}
