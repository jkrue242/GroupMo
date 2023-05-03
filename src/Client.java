import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable {
    private int port;

    private User user;
    private boolean stopped = false;
    private static int totalClients = 0;
    private int clientID;

    private ObjectInputStream in;
    private ObjectOutputStream out;

    /**
     * constructor for Client
     * @param port
     */
    public Client(int port){
        this.clientID = ++totalClients;
        this.user = new User();
        this.port = port;
    }

    /**
     * gets the client ID
     * @return
     */
    public int getClientID() {
        return clientID;
    }

    /**
     * sets the user for user class
     * @param user
     */
    public void setUser(User user){
        this.user = user;
    }

    /**
     * runs
     */
    @Override
    public void run() {
        try{
            Socket client = new Socket("0.0.0.0", port);
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());

            System.out.println("Starting client{" + clientID  + "}");

            String input;
            while(!stopped) {

            }

        } catch (Exception e) {
            System.out.println(e);
        }
        stop();
        System.out.println("closing client{" + clientID+"}");
    }

    /**
     * sends packet to the server
     * @param user
     * @param flag
     */
    public void sendPacketToServer(User user, String flag){
        try {
            out.writeObject(flag);
            out.writeObject(user);
            out.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * sends packet to the server
     * @param flag
     */
    public void sendPacketToServer(String flag){
        try {
            out.writeObject(flag);
            out.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets the user
     * @return
     */
    public User getUser(){
        return this.user;
    }

    /**
     * receives the packet
     * @return
     */
    public Object receivePacket() {
        try {
            return in.readObject();

        }catch(Exception e){
            e.printStackTrace();
        }
        return "flag:noPacket";
    }

    /**
     * stops
     */
    private void stop(){
        this.stopped = true;
        try{
            this.in.close();
            this.out.close();

        } catch(Exception e ){
            //# todo handle exception
        }
    }

    /**
     * send Packet to the server
     * @param amount
     */
    public void sendPacketToServer(double amount) {
        try {
            out.writeObject(amount);
            out.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
