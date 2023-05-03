import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

/**
 * This handles the widgets that are displayed in the feed
 */
public class CustomFeedWidget extends VBox {

    /**
     * Constructor
     * @param transaction Transaction to be modeled
     */
    public CustomFeedWidget(Transaction transaction)
    {
        Line line = new Line(0, 0, 100, 0);
        getChildren().add(line);

        String sender = transaction.getSender();
        String receiver = transaction.getReceiver();

        String action = " paid ";
        if (transaction.getRequest())
        {
            action = " requested payment from ";
        }
        String transactionText = sender + action + receiver +"\n\nComments:\n";

        for (String comment : transaction.getComments())
        {
            transactionText = transactionText + comment + "\n";
        }
        TextArea transactionLabel = new TextArea(transactionText);
        transactionLabel.setEditable(false);
        getChildren().add(transactionLabel);
    }
}
