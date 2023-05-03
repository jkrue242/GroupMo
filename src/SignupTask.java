import javafx.concurrent.Task;

/**
 * The sign in window task, is called when a user presses the sign in or sign up button, this then performs communication with the server
 */
public class SignupTask extends Task<String> {
    private final String username;
    private final String password;
    private final Client client;
    private final String flag;

    /**
     * A signup task constructor
     * @param client the client calling the function
     * @param username the username being sent over to the server for checking
     * @param password the password being sent over to the server for comparison
     * @param flag the protocol that needs to run server side
     */
    public SignupTask(Client client, String username, String password, String flag) {
        this.username = username;
        this.password = password;
        this.client = client;
        this.flag = flag;
    }

    /**
     * This invokes the signup task and receives a flag from the server specifying it's okay to log in or not
     * @return the text to be displayed on the GUI
     */
    private String invokeSignUpTask(){
        // grab text from field
        String text = "";

        client.sendPacketToServer(new User(this.username, password), flag);
        String resp = client.receivePacket().toString();
        System.out.println("resp 1:"+resp);

        if(resp.equals("flag:emptyUser")) {
            text = "Please enter a username.";
        }
        else if(resp.equals("flag:emptypswd"))
        {
            text = "Please enter a password.";
        }
        else if(resp.equals("flag:userExists"))
        {
            return "User \"" + username + "\" exists please log in with your password";
        } else if (resp.equals("flag:emptyUserPass")) {
            text = "Username and password fields are empty.";
        } else if(resp.equals("flag:success")){
            text = resp;
        }
        else if(resp.equals("flag:incorrectPswd")){
            text = "Incorrect Password. Please try again.";
        } else if(resp.equals("flag:loginComplete")) {
            App.getCurrentClient().setUser((User) client.receivePacket());  // set the user to the client
            System.out.println(client.getUser() + " is associated with client"+ client.getClientID() + ": user_id" + client.getUser().user_id);
            text = "flag:success";
        } else if(resp.equals("flag:unhandled")){
            text = "Unhandled flag";
            System.out.println(resp);
        }

        System.out.println(text);
        return text;
    }

    /**
     * The overriden function fromt he Task object, that is called to be placed into a background thread.
     * @return the result of the server communication
     * @throws Exception
     */
    @Override public String call() {

        String res = invokeSignUpTask();

        return res;
    }
}
