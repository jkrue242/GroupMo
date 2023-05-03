import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls payment page
 */
public class PaymentPageController {

    /**
     * initializes the payment page
     * @throws SQLException exception handling
     */
    @FXML
    public void initialize() throws SQLException {
        updateFeed();
        receiverTextField.setStyle("-fx-text-box-border: #000000");
        amountTextField.setStyle("-fx-text-box-border: #000000");
        payRadioButton.setSelected(true);
        singleUserRadioButton.setSelected(true);
        privacyCheckbox.setSelected(false);
        updateBalanceDisplay();
    }

    /**
     * Pay Radio Button clicked
     */
    @FXML
    private void payRadioButtonChecked()
    {
        requestRadioButton.setSelected(false);
        payRadioButton.setSelected(true);
    }

    /**
     * Request Radio Button Clicked
     */
    @FXML
    private void requestRadioButtonChecked()
    {
        payRadioButton.setSelected(false);
        requestRadioButton.setSelected(true);
    }

    /**
     * single user radio Button Clicked
     */
    @FXML
    private void singleUserRadioButtonChecked() {
        groupRadioButton.setSelected(false);
        singleUserRadioButton.setSelected(true);
        receiverTextField.setPromptText("User Name");
    }

    /**
     * group radio button clicked
     */
    @FXML
    private void groupRadioButtonChecked()
    {
        singleUserRadioButton.setSelected(false);
        groupRadioButton.setSelected(true);
        receiverTextField.setPromptText("Group Id");
    }

    /**
     * When submit is clicked
     * @throws SQLException exception handling
     */
    @FXML
    private void paySubmitButtonPressed() throws SQLException {
        //                receiverTextField.setStyle("-fx-text-box-border: #FF0000");
        List<String> comments = new ArrayList<>();
        comments.add(commentTextArea.getText());
        double amount;

        // make sure it is a valid amount
        try
        {
            amount = Double.parseDouble(amountTextField.getText());
            if (amount <= 0)
            {
                amountTextField.setStyle("-fx-text-box-border: #FF0000");
                bottomNotification.setText("Amount can't be less than $0.00");
                return;
            }
        }
        catch (Exception e)
        {
            amountTextField.setStyle("-fx-text-box-border: #FF0000");
            bottomNotification.setText("Invalid amount");
            return;
        }

        boolean success = false;

        // if we are paying to a single user
        if (payRadioButton.isSelected() && singleUserRadioButton.isSelected())
        {
            User currentUser = App.getCurrentClient().getUser();
            success = payUser(receiverTextField.getText(), amount);

            if (success)
            {
                User receiver = App.databaseConnector.getUserData(receiverTextField.getText());

                // withdraw from sender, deposit to receiver
                currentUser.withdraw(amount);
                receiver.deposit(amount);

                App.databaseConnector.insertRow(currentUser);
                App.databaseConnector.insertRow(receiver);
            }

        }

        // if we are paying to a group
        else if (payRadioButton.isSelected() && groupRadioButton.isSelected())
        {
            try
            {
                int group_id = Integer.parseInt(receiverTextField.getText());
                success = payGroup(group_id, amount);

                if (success)
                {
                    User currentUser = App.getCurrentClient().getUser();
                    Group groupToPay = App.databaseConnector.getGroupData(group_id);

                    // withdraw from sender, deposit to receiver
                    currentUser.withdraw(amount);
                    groupToPay.addToBalance(amount);

                    App.databaseConnector.insertRow(currentUser);
                    App.databaseConnector.insertRow(groupToPay);
                }
            }
            catch (Exception e)
            {
                receiverTextField.setStyle("-fx-text-box-border: #FF0000");
                bottomNotification.setText("Invalid Group Id");
                return;
            }
        }

        // if we are requesting from a user
        else if (requestRadioButton.isSelected() && singleUserRadioButton.isSelected())
        {
            String receiver = receiverTextField.getText();
            success = requestFromUser(receiver, amount);
            if (!success)
            {
                bottomNotification.setStyle("-fx-text-box-border: #FF0000");
                bottomNotification.setText("Request failed.");
            }
        }

        // if we are requesting from a group
        else if (requestRadioButton.isSelected() && groupRadioButton.isSelected())
        {
             int receiver_id = Integer.parseInt(receiverTextField.getText());
             success = requestGroup(receiver_id, amount);
             if (!success)
             {
                 bottomNotification.setStyle("-fx-text-box-border: #FF0000");
                 bottomNotification.setText("Request failed.");
             }
        }

        // if payment went through
        if (success) {
            updateBalanceDisplay();
            ProfilePageController profilePage = App.profileLoader.getController();
            profilePage.updateBalance();
            updateFeed();
            clearEntries();
            amountTextField.setStyle("-fx-text-box-border: #000000");
            receiverTextField.setStyle("-fx-text-box-border: #000000");

            HomepageController homepageController = App.homeLoader.getController();
            homepageController.updatePendingRequests();
            App.setScreen("Home");
        }
        else
        {
            bottomNotification.setText("Transaction failed");
        }
    }

    /**
     * Pays a user
     * @param username username
     * @param amount amount
     * @return true/false if successful
     * @throws SQLException exception handling
     */
    private boolean payUser(String username, double amount) throws SQLException {

        // check that user exists and is not yourself
        if (App.databaseConnector.checkIfUserExists(username) && !username.equals(App.getCurrentClient().getUser().user_id))
        {
            // check that the amount we want to pay is less than our balance
            if (amount <= App.databaseConnector.getUserData(App.getCurrentClient().getUser().user_id).getBalance())
            {
                // create Transaction object
                List<String> comments = new ArrayList<>();
                comments.add(commentTextArea.getText());
                Transaction payment = new Transaction(privacyCheckbox.isSelected(), App.getCurrentClient().getUser().user_id, username, amount,
                        true, false, comments);

                // check if paying back a request, if so, remove the request
                if (currentRequest != null)
                {
                    if (username.equals(currentRequest.getSender()) && amount == currentRequest.getAmount())
                    {
                        System.out.println("Removing transaction");
                        App.databaseConnector.setTransactionToPaid(currentRequest);
                        currentRequest = new Transaction();
                    }
                }

                // add to db
                App.databaseConnector.insertRow(payment);
                return true;
            }
            else
            {
                amountTextField.setStyle("-fx-text-box-border: #FF0000");
                bottomNotification.setText("Amount can't be more than your balance");
                return false;
            }
        }
        else
        {
            receiverTextField.setStyle("-fx-text-box-border: #FF0000");
            bottomNotification.setText("Invalid recipient");
            return false;
        }
    }

    /**
     * Requests money from user
     * @param username username
     * @param amount amount
     * @return true/false if complete
     * @throws SQLException exception handling
     */
    private boolean requestFromUser(String username, double amount) throws SQLException {

        // check that user exists and is not yourself
        if (App.databaseConnector.checkIfUserExists(username) && !username.equals(App.getCurrentClient().getUser().user_id))
        {
            List<String> comments = new ArrayList<>();
            comments.add(commentTextArea.getText());
            Transaction payment = new Transaction(privacyCheckbox.isSelected(), App.getCurrentClient().getUser().user_id, username, amount,
                    false, true, comments);

            // add to db
            App.databaseConnector.insertRow(payment);
            return true;
        }
        else
        {
            receiverTextField.setStyle("-fx-text-box-border: #FF0000");
            bottomNotification.setText("Invalid recipient");
            return false;
        }
    }

    /**
     * Pays a group, adds money to group balance
     * @param group_id group id to add to
     * @param amount amount to add
     * @return true if successful, false otherwise
     * @throws SQLException exception handling
     */
    private boolean payGroup(int group_id, double amount) throws SQLException {

        // check if user belongs to group
        if (App.databaseConnector.checkIfUserBelongsToGroup(group_id, App.getCurrentClient().getUser().user_id))
        {
            List<String> comments = new ArrayList<>();
            comments.add(commentTextArea.getText());
            Transaction payment = new Transaction(privacyCheckbox.isSelected(), Integer.toString(group_id), App.getCurrentClient().getUser().user_id, amount,
                    true, false, comments);

            // add to db
            App.databaseConnector.insertRow(payment);

            Group current_group = App.databaseConnector.getGroupData(group_id);
            current_group.addToBalance(amount);
            App.databaseConnector.insertRow(current_group);
            return true;
        }
        else
        {
            bottomNotification.setText("Invalid recipient");
        }
        return false;
    }


    /**
     * Request amount from a group, depletes balance of group
     * @param group_id group id
     * @param amount amount
     * @return true/false if was successful
     * @throws SQLException exception handling
     */
    private boolean requestGroup(int group_id, double amount) throws SQLException
    {
        // check if user belongs to group
        if (App.databaseConnector.checkIfUserBelongsToGroup(group_id, App.getCurrentClient().getUser().user_id))
        {
            List<String> comments = new ArrayList<>();
            comments.add(commentTextArea.getText());
            Transaction payment = new Transaction(privacyCheckbox.isSelected(), App.getCurrentClient().getUser().user_id,Integer.toString(group_id), amount,
                    true, true, comments);

            // add to db
            App.databaseConnector.insertRow(payment);

            Group current_group = App.databaseConnector.getGroupData(group_id);
            current_group.removeFromBalance(amount);
            App.databaseConnector.insertRow(current_group);

            User current_user = App.getCurrentClient().getUser();
            current_user.deposit(amount);
            App.databaseConnector.insertRow(current_user);
            return true;
        }
        else
        {
            bottomNotification.setText("Invalid recipient");
        }
        return false;
    }

    /**
     * Returns home from payment screen
     * @throws SQLException exception handling
     */
    @FXML
    private void payHomeButtonPressed() throws SQLException {
        clearEntries();
        updateFeed();
        HomepageController homeController = App.homeLoader.getController();
        homeController.updateFeed();
        App.setScreen("Home");
    }

    /**
     * Tells homepage to update feed
     */
    private void updateFeed() throws SQLException {
        HomepageController homeController = App.homeLoader.getController();
        homeController.updateFeed();
    }

    /**
     * Clears all entries
     */
    private void clearEntries()
    {
        payRadioButton.setSelected(false);
        requestRadioButton.setSelected(false);
        singleUserRadioButton.setSelected(false);
        groupRadioButton.setSelected(false);
        receiverTextField.clear();
        amountTextField.clear();
        userText.setText("");
        amountText.setText("");
        privacyCheckbox.setSelected(false);
        commentTextArea.clear();
        bottomNotification.setText("");
    }

    /**
     * Updates the balance displayed
     */
    public void updateBalanceDisplay() {
        User user = App.getCurrentClient().getUser();
        balanceText.setText("$" +new DecimalFormat("#.##").format(user.getBalance()));
    }

    /**
     * Responds to a requested amount
     * @param t transaction requested
     */
    public void respondToRequest(Transaction t)
    {
        // set up gui
        payRadioButton.setSelected(true);
        requestRadioButton.setSelected(false);
        receiverTextField.setText(t.getSender());
        amountTextField.setText(String.valueOf(t.getAmount()));
        singleUserRadioButton.setSelected(true);
        groupRadioButton.setSelected(false);
        currentRequest = t;
    }

    @FXML
    RadioButton payRadioButton;
    @FXML
    RadioButton requestRadioButton;
    @FXML
    RadioButton singleUserRadioButton;
    @FXML
    RadioButton groupRadioButton;
    @FXML
    TextField receiverTextField;
    @FXML
    TextField amountTextField;
    @FXML
    Button paySubmitButton;
    @FXML
    Button payHomeButton;
    @FXML
    CheckBox privacyCheckbox;
    @FXML
    TextArea commentTextArea;
    @FXML
    Text userText;
    @FXML
    Text amountText;
    @FXML
    Text balanceText;

    @FXML
    Text bottomNotification;

    private Transaction currentRequest;
}
