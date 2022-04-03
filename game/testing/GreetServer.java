import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;


public class GreetServer {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void start(int port) {
        try {
            clientSocket = new Socket("localhost", 25565);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println("msg");

        }
        catch(Exception e) {


        }
    }


    public static void main(String[] args) {
        GreetServer server = new GreetServer();
        server.start(6000);

    }
}
