import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Controls the group page
 */
public class GroupPageController {

    /**
     * When Pay/Request clicked
     */
    @FXML
    private void homeButtonClicked() throws SQLException {
        notifications.setText("");
        App.setScreen("Home");
    }

    /**
     * when form group is clicked
     * @throws SQLException
     */
    @FXML
    private void FormGroupButtonClicked() throws SQLException {
        notifications.setText("");
        App.setScreen("FormGroup");
    }

    /**
     * initializes the group page
     * @throws SQLException
     */
    @FXML
    private void initialize() throws SQLException {
        System.out.println("initializing");
        leaveGroupButton.setVisible(false);
        leaveGroupButton.isDisabled();
        updateMenuButton();
    }

    /**
     * adds a menu button based on th
     * @param groupID id of the group
     * @param fromField string from field
     * @throws SQLException excpetion
     */
    public void addMenuButton(int groupID, String fromField) throws SQLException {
        String groupName = fromField;
        System.out.println(groupName);

        List<String> temp = new ArrayList<>();
        String currUser = App.getCurrentClient().getUser().user_id;
        temp.add(currUser);
        Group newGroup = new Group(groupID, groupName, currUser, temp, 0.0);
        User currUserUser = App.getCurrentClient().getUser();
        currUserUser.addGroup(newGroup);
        App.databaseConnector.insertRow(currUserUser);
        App.getCurrentClient().getUser().addGroup(newGroup);
        App.databaseConnector.insertRow(newGroup);

        updateMenuButton();
    }

    /**
     * updates the groups properties
     * @throws SQLException
     */
    public void updateMenuButton() throws SQLException {
        chooseGroupButton.getItems().clear();
        // get the list of groups
        // iterate through them to see if the user is a part of them
        User user = App.databaseConnector.getUserData(App.getCurrentClient().getUser().user_id);
        List<Group> groupList= user.getGroups();
        List<MenuItem> menuItemsList = new ArrayList<>();

        for (Group g : groupList)
        {
            System.out.println(g.getID());
            if(g.getID()<0){

            }
            else{
                String groupName = g.getGroupName();
                MenuItem tempMenuitem= new MenuItem(groupName);
                tempMenuitem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        groupMembersText.setText("Members: "+g.getUsers().toString().replace("[","").replace("]",""));
                        balanceText.setText(String.valueOf("Balance: "+g.getBalance()));
                        groupIDText.setText(String.valueOf("Id: "+g.getID()));
                        groupNameText.setText("Group Name: "+g.getGroupName());
                        setCurrentGroupID= String.valueOf(g.getID());
                        notifications.setText("");
                    }
                });
                menuItemsList.add(tempMenuitem);
            }

        }
        chooseGroupButton.getItems().addAll(menuItemsList);
    }

    /**
     * leave group button is clicked
     * @throws SQLException exception
     */
    public void leaveButtonGroupClicked() throws SQLException {
        User user = App.databaseConnector.getUserData(App.getCurrentClient().getUser().user_id);
        String currUser= App.getCurrentClient().getUser().user_id;
        int leaveGroupID = Integer.parseInt(setCurrentGroupID);

        Group temp= App.databaseConnector.getGroupData(Integer.parseInt(setCurrentGroupID));// change to the current group ID when the button was clicked
        String admin= temp.getUserAdmin();
        if(Objects.equals(admin, currUser)){
            notifications.setText("You are the Admin, you cannot leave the group.");
        }
        else{
            notifications.setText("You left from the group");
            // add leaving the group option if there is time
            App.databaseConnector.removeUserToGroup(currUser, leaveGroupID);
            updateMenuButton();
        }
    }
    public void removeOption(MenuItem removeGroup){
        filter.remove(removeGroup);
    }

    private HashMap<Integer, String> menuItemNames= new HashMap<>();
    private HashMap<Integer, MenuItem> menuItems= new HashMap<>(); // store the
    String setCurrentGroupID;
    @FXML
    private MenuButton chooseGroupButton;
    @FXML
    Button groupsButton;
    @FXML
    private Text notifications;
    @FXML
    Button homeButton;
    @FXML
    Text groupNameText;
    @FXML
    Text groupIDText;
    @FXML
    Text groupMembersText;
    @FXML
    Text balanceText;
    @FXML
    Button leaveGroupButton;



    @FXML
    ScrollPane feedScrollPane;
    private ObservableList<MenuItem> filter= FXCollections.observableArrayList();
}
