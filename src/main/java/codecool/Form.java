package codecool;

import DAO.Database;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
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
                JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
                JtwigModel model = JtwigModel.newModel();

                response = template.render(model);
            }

            try{
                for(HttpCookie cookie: cookies){
                    if (cookieStr != null && cookie.getName().equals("username") && !cookie.getValue().equals("") && database.checkUsernameCookie(cookie.getValue()) && getCookieSessionId.isPresent()) {
                        cookie = HttpCookie.parse(cookieStr).get(0);

                        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
                        JtwigModel model = JtwigModel.newModel();

                        model.with("username", cookie.getValue());

                        response = template.render(model);

                        httpExchange.getResponseHeaders().add("Location", "/logout");
                        httpExchange.sendResponseHeaders(303, 0);

                    }else if(cookie.getName().equals("username") && cookie.getValue().equals("")){
                        JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
                        JtwigModel model = JtwigModel.newModel();

                        response = template.render(model);
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
                    String randomSessionId = generator.setSessionId();
                    Optional<HttpCookie> cookieSessionId = Optional.of(new HttpCookie("sessionId", randomSessionId));
                    httpExchange.getResponseHeaders().add("Set-Cookie", cookieSessionId.get().toString());
                    // end

                    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/logout.twig");
                    JtwigModel model = JtwigModel.newModel();

                    model.with("username", cookie.getValue());

                    response = template.render(model);

                    httpExchange.getResponseHeaders().add("Location", "/logout");
                    httpExchange.sendResponseHeaders(303, 0);
                }else{
                    JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
                    JtwigModel model = JtwigModel.newModel();

                    response = template.render(model);
                }
            }catch(Exception e){
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
