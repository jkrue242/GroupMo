import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the main server that handles all communication between all clients connected to the server.
 */
public class Server implements Runnable {
    private int port;
    private ServerSocket serverSocket;
    private boolean stopped = false;

    private ArrayList<ClientConnectionHandler> clients;
    private ExecutorService threadPool;
    public Server(int port){
        this.port = port;
        clients = new ArrayList<>();
    }

    /**
     * The server is the main executor for all client handlers. It executes each client handler every time a client connects as to allow for a multi-client connection
     * server scoket accept is a blocking call that awaits a client connection before adding it to the client list and starting the nwe thread.
     */
    @Override
    public void run() {
        try{
            System.out.println("Starting server on port: " + port);
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newCachedThreadPool();
            System.out.println(serverSocket.getLocalSocketAddress());
            while(!stopped) {
                System.out.println("waiting for connection ");
                Socket cSocket = serverSocket.accept();
                ClientConnectionHandler client = new ClientConnectionHandler(cSocket);
                System.out.println("Client connected" + client);
                clients.add(client);
                System.out.println(clients);
                threadPool.execute(client);
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * stops the server from running
     */
    public void stop(){
        this.stopped = true;
    }

    /**
     * The main function that starts up the server.
     * @param args system arguments
     */
    public static void main(String[] args) {
        Server server = new Server(23507);
        server.run();
    }
}
