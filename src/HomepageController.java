import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controls the home page
 */
public class HomepageController {

    /**
     * Initializes feed
     * @throws SQLException exception handling
     */
    public void initialize() throws SQLException {
        privateFeed = false;
        System.out.println(App.getCurrentClient());
        updateFeed();
        updatePendingRequests();
    }

    /**
     * Adds transaction data to feed
     * @param transaction Transaction object
     */
    private void addTransactionToFeed(Transaction transaction)
    {
        CustomFeedWidget feedWidget = new CustomFeedWidget(transaction);
        feedVBox.getChildren().add(0, feedWidget);
    }

    /**
     * When Pay/Request clicked
     */
    @FXML
    private void payButtonClicked() throws SQLException {
        PaymentPageController paymentPageController = App.payLoader.getController();
        paymentPageController.updateBalanceDisplay();
        App.setScreen("Pay/Request");
        ((PaymentPageController) App.payLoader.getController()).updateBalanceDisplay();
    }

    /**
     * when profile button is clicked
     * @throws SQLException exception handling
     */
    @FXML
    private void profileButtonClicked() throws SQLException {
        ProfilePageController profile = App.profileLoader.getController();
        profile.updateBalance();
        profile.updateFriendsList();
        profile.updatePendingRequests();
        App.setScreen("Profile");
        ((ProfilePageController) App.profileLoader.getController()).updatePendingRequests();
    }

    /**
     * when groups is clicked
     * @throws SQLException exception handling
     */
    @FXML
    private void groupsButtonClicked() throws SQLException {
        GroupPageController groupPageController= App.groupLoader.getController();
        App.setScreen("Group");
        groupPageController.updateMenuButton();
    }

    /**
     * If we only select to view transactions with user
     * @throws SQLException exception handling
     */
    @FXML
    private void onlyUserTransactionsOptionChosen() throws SQLException {
        privateFeed = true;
        updateFeed();
    }

    /**
     * if we select to view all transactions
     * @throws SQLException exception handling
     */
    @FXML
    private void allTransactionsOptionChosen() throws SQLException {
        privateFeed = false;
        updateFeed();
    }

    /**
     * Clears feed
     */
    public void clearFeed()
    {

        feedVBox.getChildren().clear();
    }

    /**
     * Updates feed
     * @throws SQLException exception handling
     */
    public void updateFeed() throws SQLException {
        clearFeed();

        // we only want transactions
        for (Transaction t : App.databaseConnector.getTransactionList())
        {
            // if they selected to only view their transactions
            if (privateFeed)
            {
                if (t.getSender().equals(App.getCurrentClient().getUser().user_id) || t.getReceiver().equals(App.getCurrentClient().getUser().user_id))
                {
                    addTransactionToFeed(t);
                }
            }
            else
            {
                // public transactions
                if (!t.is_private || (t.getSender().equals(App.getCurrentClient().getUser().user_id) || (t.getReceiver().equals(App.getCurrentClient().getUser().user_id))))
                {
                    addTransactionToFeed(t);
                }
                else
                {
                    // private but current user transactions
                    if (t.getSender().equals(App.getCurrentClient().getUser().user_id) && t.getReceiver().equals(App.getCurrentClient().getUser().user_id))
                    {
                        addTransactionToFeed(t);
                    }
                }

            }
        }
    }

    /**
     * Updates the pending requests
     * @throws SQLException exception handling
     */
    public void updatePendingRequests() throws SQLException {
        System.out.println("Current user: "+ App.getCurrentClient().getUser().user_id);
        pendingRequests = App.databaseConnector.getPendingRequests();
        if (pendingRequests.size() > 0)
        {
            profileButton.setStyle("-fx-text-box-border: #000FFF");
        }
    }

    @FXML
    Button groupsButton;
    @FXML
    Button payButton;
    @FXML
    Button profileButton;
    @FXML
    MenuItem onlyUserTransactionsOption;
    @FXML
    MenuItem allTransactionsOption;
    @FXML
    VBox feedVBox;
    @FXML
    ScrollPane feedScrollPane;
    @FXML
    Text notification;

    public static boolean privateFeed;

    private HashMap<String, List<Transaction>> pendingRequests;

    /**
     * Returns the pending requests
     * @return pending requests
     * @throws SQLException exception handling
     */
    public HashMap<String, List<Transaction>> getPendingRequests() throws SQLException {

        pendingRequests = App.databaseConnector.getPendingRequests();
        return pendingRequests;
    }
}
