import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Driver program
 */
public class App extends Application{

    private final int HEIGHT = 600;
    private final int WIDTH = 400;

    private static final String START_SCREEN = "Sign In";

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String args[]) {
        launch(args);
    }


    /**
     * Starts the app
     * @param stage stage
     * @throws IOException exception handling
     */
    @Override
    public void start(Stage stage) throws IOException {
        databaseConnector = new DatabaseConnector();
        root = new AnchorPane();
        client = new Client(23507);
        setCurrentClient(client);
        executorService.execute(client);
        currentUser = "";

        // each new screen needs an FXML loader object
        signInLoader = new FXMLLoader(getClass().getResource("SignInWindow.fxml"));
        homeLoader = new FXMLLoader(getClass().getResource("Homepage.fxml"));
        payLoader = new FXMLLoader(getClass().getResource("PaymentPage.fxml"));
        groupLoader= new FXMLLoader(getClass().getResource("GroupPage.fxml"));
        formGroup= new FXMLLoader(getClass().getResource("FormGroupPage.fxml"));
        profileLoader = new FXMLLoader(getClass().getResource("ProfilePage.fxml"));

        // each screen gets added to the windows HashMap which stores all the screens
        windows.put("Sign In", signInLoader.load());

        // set the initial screen to Sign in window
        root.getChildren().add(windows.get("Sign In"));
        Scene signInScene = new Scene(root);
        stage.setHeight(HEIGHT);
        stage.setWidth(WIDTH);
        stage.setScene(signInScene);
        stage.setResizable(false);

        // stop program on app close
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    if (!currentUser.equals(""))
                    {
                        User currentUserObject = databaseConnector.getUserData(currentUser);
                        if (currentUserObject != null)
                        {
                            client.sendPacketToServer("flag:shutdown");
                        }
                        else
                        {
                            System.out.println("No user found");
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                Platform.exit();
                System.exit(0);
            }
        });
        stage.show();
    }

    /**
     * Sets the visible screen
     * @param screenName screen name
     * @throws SQLException exception handling
     */
    public static void setScreen(String screenName) throws SQLException {
        root.getChildren().remove(windows.get(currentWindow));
        root.getChildren().add(windows.get(screenName));
        currentWindow = screenName;
    }

    /**
     * Sets current user
     * @param newClient the client that started the application
     */
    public void setCurrentClient(Client newClient)
    {
        this.client = newClient;
    }

    /**
     * Sets the current user
     * @param user User
     */
    public void setUser(User user) {
        this.client.setUser(user);// maps the client to the user that logged in.
    }

    /**
     * Return username of current user
     * @return username
     */
    public static Client getCurrentClient (){
        return client;
    }

    /**
     * Loads all of the pages
     * @throws IOException exception handling
     * @throws SQLException exception handling
     */
    public static void loadPages() throws IOException, SQLException {

        if (windows.size() <= 1)
        {
            windows.put("Home", homeLoader.load());
            windows.put("Pay/Request", payLoader.load());
            windows.put("Group", groupLoader.load());
            windows.put("FormGroup",formGroup.load());
            windows.put("Profile", profileLoader.load());
        }

        HomepageController homepageController = homeLoader.getController();
        homepageController.initialize();

        ProfilePageController profilePageController = profileLoader.getController();
        profilePageController.initialize();

        PaymentPageController paymentPageController = payLoader.getController();
        paymentPageController.initialize();
    }
    public static FXMLLoader signInLoader;
    public static FXMLLoader homeLoader;
    public static FXMLLoader payLoader;
    private static FXMLLoader formGroup;
    public static FXMLLoader groupLoader;
    public static FXMLLoader profileLoader;

    private static String currentUser;
    public static DatabaseConnector databaseConnector;
    public static AnchorPane root;
    public static HashMap<String, AnchorPane> windows = new HashMap<>();
    private static String currentWindow = START_SCREEN;
    private static Client client;
}

