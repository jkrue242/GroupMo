import java.io.*;
import java.net.Socket;
import java.sql.SQLException;


/**
 * Handles all communication per client connected. This class is created every time a client is connected  to the server.
 *
 */
public class ClientConnectionHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean stopped;

    private Client client;
    private final DatabaseConnector db;

    /**
     * Initializes a connection handler between the server and the client.
     * @param clientSocket The socket as to which the client is connected
     * constuctor for the client to connection handler
>
     */
    public ClientConnectionHandler(Socket clientSocket) {
        this.socket = clientSocket;
        this.db = new DatabaseConnector();
    }

    /**
     * The overriden runnable method that handles all flags sent from the client and determines the correct protocol to follow
     * runs threading
     */
    @Override
    public void run() {
        try{
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            String buffer = in.readObject().toString();

            System.out.println(socket + ">  " + buffer);
            if(buffer.startsWith("flag:signup")){
                User temp = (User) in.readObject();
                handleSignup(temp);

            } else if(buffer.startsWith("flag:signin")){
                User temp = (User) in.readObject();
                handleSignIn(temp);

            } else if(buffer.startsWith("flag:transaction")){
                Transaction newTrans =  (Transaction) in.readObject();
                db.insertRow(newTrans);

            } else if(buffer.startsWith("flag:createGroup")) {
                Group newGroup = (Group) in.readObject();
                db.insertRow(newGroup);

            } else if (buffer.startsWith("flag:shutdown")){
                System.out.println("Connection handler closing for " + client.getClientID());
                stop();

            } else if(buffer.startsWith("flag:deposit")){
                User user = (User) in.readObject();
                double depositAmount = (double) in.readObject();
                handleDeposit(user, depositAmount);

            } else if(buffer.startsWith("flag:withdraw")){
                User user = (User) in.readObject();
                double depositAmount = (double) in.readObject();
                handleWithdrawal(user, depositAmount);

            } else if(buffer.startsWith("flag:addFriend")){
                User user = (User) in.readObject();
                String friend = in.readObject().toString();
                handleAddFriend(user, friend);

            } else if(buffer.startsWith("flag:removeFriend")){
                User user = (User) in.readObject();
                String friend = in.readObject().toString();
                handleRemoveFriend(user, friend);
            }

        } catch(Exception e ){
            //#TODO handle exception
            e.printStackTrace();
        }
    }

    /**
<<<<<<< HEAD
     * Once the flag:removeFriend is found perform the necessary actions to remove the friend from the callers list
     * and also remove the friend from the friends list. reinsert the two users and return a flag:sucess if sucesceful
     * @param user The user that called the flag
     * @param friend the friend to be removed
     * handles removes friend
     */
    private void handleRemoveFriend(User user, String friend) {
        System.out.println("Handling remove friend");
        String flag;
        try {
            if (user.getFriends().contains(friend)) { // check if the friend entered is a friend of the current user
                user.removeFriend(friend);
                db.insertRow(user); // update the user.
                User userFriend = db.getUserData(friend);   // now update the friends list of the other user
                if(userFriend.getFriends().contains(user.user_id)){
                    userFriend.removeFriend(user.user_id);
                    db.insertRow(userFriend);
                } else {
                    flag = "flag:error"; // if for some reason the user doesn't exist, throw error
                }
                flag = "flag:removedFriend"; // success
            }  else {
                flag = "flag:nullFriend"; // friend doesn't exist.
            }

            out.writeObject(flag);
            out.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Once the flag:addFriend was found, perform the necessary protocol to add the friend to both users lists.
     * @param user The user that calls the add Friend
     * @param friend the friend to be added
     * handles adding the friend

     */
    private void handleAddFriend(User user, String friend) {
        String flag;
        try {
            if(db.checkIfUserExists(friend)){
                user.addFriend(friend);
                System.out.println(user.getFriends());
                db.insertRow(user); // update the user.
                User newFriend = db.getUserData(friend);
                newFriend.addFriend(user.user_id);
                db.insertRow( newFriend ); // update the user.
                flag = "flag:addedFriend";
            } else {
                flag = "flag:nullFriend";
            }

            System.out.println(flag);
            out.writeObject(flag);
            out.flush();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Once the flag;withdrawal is found the user is then decremented the amoutn of money they requested to withdrawal.
     * then performs the necesary protocol to update the database.
     * @param user The user to withdrawal money from
     * @param withdrawalAmount the amount of money to withdrawal
     */
    private void handleWithdrawal(User user, double withdrawalAmount)  {
        System.out.println("handling Withdrawal");
        String flag;
        try {
            user.setBalance(user.getBalance() - withdrawalAmount);
            System.out.println(user.user_id);
            System.out.println(user.getBalance());
            db.insertRow(user);
            flag = "flag:withdrawalSuccess";

        } catch(SQLException e){
            e.printStackTrace();
            flag = "flag:withdrawalFail";

        }
        System.out.println(flag);

        try {
            out.writeObject(flag);
            out.writeObject(user);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * once the flag:deposit is called, perform the necessary protocol to update the users balance and save it in the database.
     * @param user The user to deposit money into
     * @param depositAmount the amount of money to deposit
     * handles the deposit
     */
    private void handleDeposit(User user, double depositAmount) {
        System.out.println("handling Deposit");
        String flag;
        try {
            user.setBalance(user.getBalance() + depositAmount);
            db.insertRow(user);
            flag = "flag:depositSuccess";
        } catch(SQLException e){
            e.printStackTrace();
            flag = "flag:sqlErrors";
        }

        System.out.println(flag);

        try {
            out.writeObject(flag);
            out.writeObject(user);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
<<<<<<< HEAD
     * IS the stop condiition is called, stop the inpout and out put streams and stop the thread.
=======
     * stops
>>>>>>> b397f823ef5ee8188c7a13225ccacbacebde1ecb
     */
    public void stop(){
        this.stopped = true;
        try {
            this.out.close();
            this.in.close();
        } catch(IOException e){
            //#TODO handle exception
        }
    }

    /**
     * once the flag:signup is recieved perform the necesary protocol to add the user to the database.
     * @param newUser The new user to add to the database
     * @throws Exception exception handling
     */
    private void handleSignup(User newUser) throws Exception{
        String username = newUser.user_id;
        String password = newUser.getPassword();
        String flag;
        System.out.println("Handling: Signup");
        if( db.checkIfUserExists(username) )
        {
            flag = "flag:userExists";
        }
        else if (username.equals("") || password.length() ==0)
        {
                flag = "flag:emptyUserPass";
        }
        else
        {
            flag = "flag:success";

            db.insertRow(newUser);
        }

        out.writeObject(flag);
        out.flush();
    }

    /**
     * Handle signin is called when the flag:siginin is recieved perfrom the necessary protocol to check if the user is able to sign in.
     * @param newUser a new user contains just a username and password to check against
     * @throws Exception throws an exception
     */
    private void handleSignIn(User newUser) throws Exception{
        String username = newUser.user_id;
        String password = newUser.getPassword();
        System.out.println("Handling Sign in");
        String flag;

        if(db.checkIfUserExists(newUser.user_id)) {
            System.out.println("User Exists");
            if(!db.checkPassword(username, password))
            {
                flag = "flag:incorrectPswd";
                out.writeObject(flag);
            } else {
                flag = "flag:loginComplete";
                System.out.println(flag);

                out.writeObject(flag);
                out.flush();
                out.writeObject(db.getUserData(newUser.user_id));   // sends the users information such as groups it belongs to, its balance, its friends.
            }

        } else if (username.equals("") || password.length() == 0) {
            // print notification
            flag = "flag:emptyUserPass";
            out.writeObject(flag);
        } else {
            flag = "flag:unhandled";
            out.writeObject(flag);
        }
        System.out.println(flag);
        out.flush();
    }
}
