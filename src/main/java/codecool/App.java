package codecool;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class App 
{
    public static void main( String[] args ) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/form", new Form());
        server.createContext("/logout", new Logout());
        server.setExecutor(null);

        server.start();
    }
}
