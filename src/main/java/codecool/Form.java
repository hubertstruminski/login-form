package codecool;

import DAO.Database;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.*;

public class Form implements HttpHandler {

    private Database database = new Database();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "";
        String method = httpExchange.getRequestMethod();


        // Send a form if it wasn't submitted yet.
        if(method.equals("GET")){

            String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie");
//            HttpCookie cookie;


            if(cookieStr == null){
                JtwigTemplate template = JtwigTemplate.classpathTemplate("templates/index.twig");
                JtwigModel model = JtwigModel.newModel();

                response = template.render(model);
            }else{
                List<HttpCookie> cookies = HttpCookie.parse(cookieStr);
                for(HttpCookie cookie: cookies){
                    if (cookieStr != null && cookie.getName().equals("username") && !cookie.getValue().equals("")) {
                        cookie = HttpCookie.parse(cookieStr).get(0);

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
                }
            }

        }

//         If the form was submitted, retrieve it's content.
        if(method.equals("POST")){
            InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();

            System.out.println(formData);
            Map inputs = parseFormData(formData);


            if(database.selectAllData(inputs.get("name").toString(), inputs.get("password").toString())){
                HttpCookie cookie = new HttpCookie("username", inputs.get("name").toString());
                cookie.setMaxAge(2*60);
                httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());

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
        }


        httpExchange.sendResponseHeaders(200, response.getBytes("UTF-32").length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * Form data is sent as a urlencoded string. Thus we have to parse this string to get data that we want.
     * See: https://en.wikipedia.org/wiki/POST_(HTTP)
//     */
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

    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
