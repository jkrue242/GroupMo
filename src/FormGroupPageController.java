import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FormGroupPageController {

    /**
     * Creates a new group based on the ID input by the user
     * @param event
     * @throws IOException
     * @throws SQLException
     */
    @FXML
    private void setEnterIDButtonPressed(ActionEvent event) throws IOException, SQLException {
        String groupName = groupNameTextField.getText();
        try{
            int groupID= Integer.parseInt(groupIDTextField.getText());
            GroupPageController groupPageController= App.groupLoader.getController();
            boolean existingGroup =App.databaseConnector.checkIfGroupExists(groupID);

            // check the database to see if the groupID already exists
            // need to add this to the logic
            if(!existingGroup){
                groupPageController.addMenuButton(groupID, groupName);
            }
            else{
                notifications.setText("Group Name or ID already Exist");
            }
        }
        catch(NumberFormatException e){
            System.out.println("Error: " + e.getMessage());

        }

    }

    /**
     * Button to return to the group page
     * @throws SQLException
     */
    @FXML
    private void returnButtonClicked() throws SQLException {
        App.setScreen("Group");
    }

    /**
     * button that confirms the closing of the group
     * @throws SQLException
     * @throws IOException
     */
    @FXML
    private void closeGroupButtonPressed() throws SQLException, IOException {

        try {
            // enter the logic that closes the group in here
            int closeGroupID= Integer.parseInt(closeGroupTextField.getText());
            // get the Menuitem from the database
            //groupPageController.removeOption(removeGroup);
            boolean existingGroup =App.databaseConnector.checkIfGroupExists(closeGroupID);
            // check the database to see if the groupID already exists
            // need to add this to the logic
            if(!existingGroup){
                notifications.setText("Group Name or ID doesn't exist");
            }
            else{
                Group temp= App.databaseConnector.getGroupData(closeGroupID);
                String admin= temp.getUserAdmin();
                // check if the user is admin as well
                if(Objects.equals(admin, App.getCurrentClient().getUser().user_id)){
                    App.databaseConnector.deleteGroup(closeGroupID);
                    notifications.setText("The group with ID :"+ closeGroupID+ " closed.");
                }
                else{
                    notifications.setText("You aren't the admin of this group. Group wasn't closed.");
                }

            }

        } catch (NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * button for joining a group
     * @throws SQLException
     * @throws IOException
     */
    @FXML
    private void joinGroupButtonClicked() throws SQLException, IOException {
        // enter the logic that closes the group in here
        try{
            int joinGroupID= Integer.parseInt(joinGroupText.getText());
            GroupPageController groupPageController= App.groupLoader.getController();

            boolean existingGroup =App.databaseConnector.checkIfGroupExists(joinGroupID);

            if(!existingGroup){
                notifications.setText("Group Name or ID doesn't exist");
            }
            else{
                List<String> temp = new ArrayList<>();
                String currUser = App.getCurrentClient().getUser().user_id;
                temp.add(currUser);

                if(App.databaseConnector.checkIfUserBelongsToGroup(joinGroupID, currUser)){
                    notifications.setText("You are already in the group.");
                }
                else{
                    //Add a user to the group
                    App.databaseConnector.addUserToGroup(currUser, joinGroupID);
                }

            }
        }catch(NumberFormatException e){
            System.out.println("Error: " + e.getMessage());
        }

    }


    @FXML
    Text signUpNotification;
    @FXML
    TextField groupIDTextField;
    @FXML
    TextField groupNameTextField;
    @FXML
    TextField joinGroupText;
    @FXML
    TextField closeGroupTextField;
    @FXML
    Button groupNameButton;
    @FXML
    Button joinGroupButton;
    @FXML
    Text notifications;
    @FXML
    Button returnButton;
    @FXML
    Button closeGroup;

}

