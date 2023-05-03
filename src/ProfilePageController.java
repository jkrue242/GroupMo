import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javax.swing.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static javax.swing.JList.HORIZONTAL_WRAP;

/**
 * Profile Page Controller used to add and remove users from your friends list. Also view your friends as well.
 */
public class ProfilePageController {
    ObservableList<String> usersFriends = FXCollections.observableArrayList();
    // creates a new thread for handling the client requests and server inputs.
    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "profile-controller-thread"); // an Executor with a new threa of name
        t.setDaemon(true); // signify this thread performs low priority background operations,
        return t; // this thread handles all background operations.
    });


    /**
     * Initialize the page with empty fields and loads in all pending payment requests
     * @throws SQLException throws an exeption if theres an SQL error
     */
    public void initialize() throws SQLException {
        clearEntries();
        updatePendingRequests();
        updateBalance();
        usersFriends = FXCollections.observableArrayList(App.getCurrentClient().getUser().getFriends());
        friendsListView.setPrefWidth(200);
        friendsListView.setPrefHeight(200);
        friendsListView.setItems(usersFriends);
    }

    /**
     * Clears all the entries in the controller
     */
    public void clearEntries()
    {
        withdrawTextField.clear();
        depositTextField.clear();
        addFriendTextField.clear();
        removeFriendTextField.clear();
        bottomNotification.setText("");
    }

    /**
     * This function displays the new balance created after talking to the server
     */
    public void updateBalance() {
        User user = App.getCurrentClient().getUser();
        amountText.setText(new DecimalFormat("#.##").format(user.getBalance()));
    }

    /**
     * Deposit money to users account. checks if the deposit amount is valid, then creates the task on a low prio thread for execution
     */
    @FXML
    private void depositButtonPressed()
    {
        try
        {
            double depositAmount = Double.parseDouble(depositTextField.getText());
            if (depositAmount <= 0)
            {
                depositTextField.setStyle("-fx-text-box-border: #FF0000");
                bottomNotification.setText("Amount can't be less than $0.00");
                return;
            }

            depositTextField.setStyle("-fx-text-box-border: #000000");

            depositTextField.clear();

            // Create task to send deposit
            //  start task
            // execute task
            // creates a profile task that handles the communication with the server this allows the user to perform many operations in between button presses.
            ProfileTask task = new ProfileTask(App.getCurrentClient(), depositAmount, "flag:deposit");
            handleTaskSuccess(task);
            executor.execute(task);

            PaymentPageController paymentPageController = App.payLoader.getController();
            paymentPageController.updateBalanceDisplay();
        }
        catch (Exception e)
        {
            depositTextField.setStyle("-fx-text-box-border: #FF0000");
            bottomNotification.setText("Invalid amount");
            return;
        }
    }

    /**
     * Withdraws a certain amount of money from the users account, checks if the amount is valid for the account. then
     * communicates with the server to perform the task.
     */
    @FXML
    private void withdrawButtonPressed()
    {
        try
        {
            double withdrawAmount = Double.parseDouble(withdrawTextField.getText());
            User currentUser = App.getCurrentClient().getUser();
            if (withdrawAmount <= 0 || withdrawAmount > currentUser.getBalance())
            {
                withdrawTextField.setStyle("-fx-text-box-border: #FF0000");
                if (withdrawAmount <= 0)
                {
                    bottomNotification.setText("Amount can't be less than $0.00");
                }
                else
                {
                    bottomNotification.setText("Not enough available funds to withdraw");
                }
                return;
            }
            withdrawTextField.setStyle("-fx-text-box-border: #000000");
            withdrawTextField.clear();

            // Create task to send deposit
            //  start task
            // execute task
            // creates a task to handle withdrawing money from the user.
            ProfileTask task = new ProfileTask(App.getCurrentClient(), withdrawAmount, "flag:withdraw");
            handleTaskSuccess(task);
            executor.execute(task);

            PaymentPageController paymentPageController = App.payLoader.getController();
            paymentPageController.updateBalanceDisplay();
        }

        catch (Exception e)
        {
            withdrawTextField.setStyle("-fx-text-box-border: #FF0000");
            bottomNotification.setText("Invalid amount");
            return;
        }
    }

    /**
     * Adds a friend from the given field addfriendtextfield. creates a low level thread for the GUI updating
     */
    @FXML
    private void addFriendButtonPressed() {

        String newFriend = addFriendTextField.getText();
        if (newFriend.length()> 0) {
            ProfileTask task = new ProfileTask(App.getCurrentClient(), newFriend, "flag:addFriend");
            handleFriendsTaskSuccess(task);
            executor.execute(task);
        } else {
            addFriendTextField.clear();
            bottomNotification.setText("You cannot add an empty user as a friend.");
        }
    }

    /**
     * removes a friend from your list if and onyl if they exist inside your friends list.
     */
    @FXML
    private void removeFriendButtonPressed()
    {
        String newFriend = removeFriendTextField.getText();
        if(newFriend.length() >0) {
            ProfileTask task = new ProfileTask(App.getCurrentClient(), newFriend, "flag:removeFriend");
            handleFriendsTaskSuccess(task);
            executor.execute(task);
        } else {
            removeFriendTextField.clear();
            bottomNotification.setText("You cannot remove an empty user as a friend.");
        }

    }

    /**
     * Swithces the screen to the pay/request screen.
     * @throws SQLException exception handling
     */
    @FXML
    private void payButtonPressed() throws SQLException {
        App.setScreen("Pay/Request");
        ((PaymentPageController)App.payLoader.getController()).updateBalanceDisplay();
    }

    /**
     * SWithces the scene to the home page.
     * @throws SQLException exception handling
     */
    @FXML
    private void homeButtonPressed() throws SQLException {
        HomepageController homeController = App.homeLoader.getController();
        homeController.updateFeed();
        App.setScreen("Home");
    }

    /**
     *
     * @param requests gets the list of requests available to the user
     */
    public void updateNotificationTab(HashMap<String, List<Transaction>> requests)
    {
        notificationsDropDown.getItems().clear();
        for (String member : requests.keySet())
        {
            for (Transaction t : requests.get(member))
            {
                MenuItem newGroupMenuItem= new MenuItem(member + " requests "+ t.getAmount());
                newGroupMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        PaymentPageController paymentPageController = App.payLoader.getController();
                        paymentPageController.updateBalanceDisplay();
                        paymentPageController.respondToRequest(t);
                        try {
                            App.setScreen("Pay/Request");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                notificationsDropDown.getItems().add(newGroupMenuItem);
            }
        }
    }

    /**
     * Updates the GUI of the friends list called everytime the user enters the profile page.
     */
    public void updateFriendsList(){
        usersFriends.clear();
        List<String> friends = App.getCurrentClient().getUser().getFriends();
        System.out.println(friends);
        usersFriends.addAll(friends);
        friendsListView.setItems(usersFriends);
    }
    /**
     * Updates the notification tab each time the page is loaded in.
     * @throws SQLException exceptions
     */
    public void updatePendingRequests() throws SQLException {
        HomepageController homepageController = App.homeLoader.getController();
        HashMap<String, List<Transaction>> pendingRequests = homepageController.getPendingRequests();
        updateNotificationTab(pendingRequests);
    }

    /**
     * Signs the user out of the app, and resets the current user of the client to a new user, so that the next user to log in is then represented.
     * @throws SQLException exception handling
     */
    @FXML
    private void signOutButtonPressed() throws SQLException {
        // clear everything
        HomepageController homepageController = App.homeLoader.getController();
        homepageController.clearFeed();

        SignInWindowController signInWindowController = App.signInLoader.getController();
        signInWindowController.clearEntries();
        clearEntries();

        amountText.setText("");
        App.getCurrentClient().setUser(new User());
        App.setScreen("Sign In");
    }

    @FXML
    Text amountText;

    @FXML
    MenuButton notificationsDropDown;

    @FXML
    Button depositButton;

    @FXML
    Button withdrawButton;

    @FXML
    ScrollPane friendsListPane;

    @FXML
    Button addFriendButton;

    @FXML
    Button removeFriendButton;

    @FXML
    Button payButton;

    @FXML
    Button homeButton;

    @FXML
    TextField addFriendTextField;

    @FXML
    TextField removeFriendTextField;

    @FXML
    TextField depositTextField;

    @FXML
    TextField withdrawTextField;

    @FXML
    Text bottomNotification;

    @FXML
    Button signOutButton;

    @FXML
    ListView<String> friendsListView = new ListView<>();

    /**
     * This checks if the task was completed successfully then craetes an event handler to handle updating the GUI contents.
     * @param task the task that was called
     */
    private void handleTaskSuccess(ProfileTask task) {
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent wse) {
                String resp = task.getValue();

                Platform.runLater(() -> {
                    ProfilePageController.this.updateBalance();
                    bottomNotification.setTextAlignment(TextAlignment.CENTER);
                    bottomNotification.setText(resp);
                });
            }
        });
    }

    /**
     * Once a task is ran successfully update the GUI with the responce from the task.
     * @param task the task that was called
     */
    private void handleFriendsTaskSuccess(ProfileTask task){
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent wse) {
                String resp = task.getValue();
                for (String user :
                        App.getCurrentClient().getUser().getFriends()) {
                    System.out.println(user);
                }
                Platform.runLater(() -> {
                    addFriendTextField.clear();
                    bottomNotification.setTextAlignment(TextAlignment.CENTER);
                    bottomNotification.setText(resp);
                    updateFriendsList();
                });
            }
        });
    }

    /**
     * this class handles the communication between the client and the server. Task is a low level background threaded operation
     * that can be instatiated to be called on a thread. the logic is then handled on a low level thread until completeion where a workerstate e
     * event handles the successful task output.
     */
    private class ProfileTask extends Task<String> {
        private final Client client;
        private final double amount;
        private final String flag;
        private final String friend;

        /**
         * A class that is used for any event handling created by  the buttons in charge of transfering money to the account or away from the account
         * @param client the client that called the task
         * @param amount the amount of money to be relayed to the server
         * @param flag the flag that the button is assgined
         */
        public ProfileTask(Client client, double amount, String flag){
            this.client = client;
            this.amount = amount;
            this.friend = "";
            this.flag = flag;
        }

        /**
         * @param client
         * @param friend
         * @param flag
         */
        public ProfileTask(Client client, String friend, String flag){
            this.client = client;
            this.flag = flag;
            this.friend = friend;
            this.amount = 0;
        }
        private String invokeFriendTask(){
            client.sendPacketToServer(client.getUser(), flag);
            client.sendPacketToServer(friend);
            System.out.println("Friend to send:"+ friend);

            String resp = client.receivePacket().toString();
            System.out.println("task completed > " + resp);
            String text = "";
            if(resp.equals("flag:nullFriend")){
                text = "User: "+ friend + " doesn't exist.";
            } else if(resp.equals("flag:removedFriend")){
                text = "You successfully removed " + friend + " as a friend.";
            } else if(resp.equals("flag:addedFriend")){
                text = "You successfully added " + friend + " as a friend";
            } else {
                text = "SQL error";
            }
            return text;

        }

        /**
         * The invoke account task handles the logic required to withdrawal and deposit money into a users account.
         * @return the text to display to the screen
         */
        private String invokeAccountTask(){
            String text = "";
            if(client.getUser().getBalance() < amount && flag.equals("flag:withdraw")){
                return "Balance is too low to withdraw $" + amount +".";
            }

            client.sendPacketToServer(client.getUser(), flag);
            client.sendPacketToServer(amount);

            String resp = client.receivePacket().toString();
            System.out.println("task completed: > " + resp);

            if(resp.equals("flag:withdrawalSuccess")){  // if the withdrawal was a success forward the flag to update the GUI.
                client.setUser((User) client.receivePacket());
                text = "You successfully withdrew $"+ amount + ".";
            } else if(resp.equals("flag:depositSuccess")){
                client.setUser((User) client.receivePacket());
                text = "You successfully deposited $" + amount+ ".";
            }
            else if(resp.equals("flag:withdrawalFail") || resp.equals("flag:depositFail")){
                text = "SQL error";
            } else {
                text = "unhandled error";
            }


            return text;
        }

        /**
         * The overriden method of the task object. Used to handle low level background tasks and in this case to update the display.
         * @return the result of the tasks completion
         */
        @Override
        protected String call() {

            if(flag.equals("flag:addFriend") || flag.equals("flag:removeFriend")){
                return invokeFriendTask();
            } else if(flag.equals("flag:withdraw") || flag.equals("flag:deposit")) {
                return invokeAccountTask();
            } else {
                return "flag:error";
            }

        }
    }
}
