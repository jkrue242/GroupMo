import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
<<<<<<< HEAD
 * The sign in controller is in charge of all elements in the sign in window controller
=======
 * Controls the sign in window
>>>>>>> b397f823ef5ee8188c7a13225ccacbacebde1ecb
 */
public class SignInWindowController {

    private final Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {   // executes a submitted task, in a backgroud low priority thread.
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "signin-controller-thread");
            t.setDaemon(true);
            return t;
        }
    });
    /**
     * Sign up button pressed
     * @param event
     */
    private Client client = App.getCurrentClient();

    /**
     * The signup handles the signup button being pressed. This button needs to communicate with the server to receive a flag.
     * These flag values determine the outcome of the signup process. Since we are interfacing with a server that has blocking operations as
     * well as a GUI, we need to use a thread to handle the blocking operations. This is done by creating a new thread and passing it a task.
     * On the success of the task, the thread will update the GUI in the background.
     *
     * @param event event
     */
    @FXML
    private void signUpButtonPressed(ActionEvent event) throws IOException, SQLException {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        SignupTask task = new SignupTask(client, username, password, "flag:signup");

        handleTaskSuccess(task);

        executor.execute(task);     // start the task.
        App.loadPages();
    }

    /**
     * Sign in button pressed
     * @param event event
     */
    @FXML
    private void signInButtonPressed(ActionEvent event) throws IOException, SQLException {
        // grab text from field
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        SignupTask task = new SignupTask(client, username, password, "flag:signin");

        handleTaskSuccess(task);
        executor.execute(task);     // start the task.

        App.loadPages();
        HomepageController homepageController = App.homeLoader.getController();
        homepageController.updatePendingRequests();

        PaymentPageController paymentPageController = App.payLoader.getController();
        paymentPageController.updateBalanceDisplay();
    }

    /**
     * Once the task is handled and was successful .perform the desired tasks to run on the GUI
     * @param task the task that was called in the executor
     */
    private void handleTaskSuccess(SignupTask task) {
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent wse) {  // handles the tasks value and performs the necessary function to display on the GUI.
                String res = task.getValue();
                System.out.println("task completed: > " + res);
                if (res.startsWith("flag:success")) {
                    Platform.runLater(() -> {
                        try {
                            App.setScreen("Home");
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        signUpNotification.setTextAlignment(TextAlignment.CENTER);
                        signUpNotification.setText(res);
                    });
                }
            }
        });
    }

    /**
     * CLear all entries in the signin page
     * Clears entries in the sign in window
     */
    public void clearEntries()
    {
        usernameTextField.clear();
        passwordTextField.clear();
    }
    @FXML
    Text signUpNotification;
    @FXML
    TextField usernameTextField;
    @FXML
    TextField passwordTextField;
    @FXML
    Button signInButton;
    @FXML
    Button signUpButton;

}
