import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the Transactions
 */
public class Transaction
{

    /**
     * This constructor is used when we know the id
     * @param id id
     * @param is_private privacy
     * @param sender_id sender id
     * @param receiver_id receiver id
     * @param amount amount
     * @param is_paid is it paid
     * @param request is it a request
     * @param comments comments
     */
    public Transaction(int id, boolean is_private, String sender_id, String receiver_id, double amount,
                       boolean is_paid, boolean request, List<String> comments) {
        this.id = id;
        this.is_private = is_private;
        sender = sender_id;
        receiver = receiver_id;
        this.amount = amount;
        this.is_paid = is_paid;
        this.request = request;
        this.comments = comments;
    }

    /**
     * This constructor is for when we do not know the id
     * @param is_private privacy
     * @param sender_id sender id
     * @param receiver_id receiver id
     * @param amount amount
     * @param is_paid has it been paid yet
     * @param request was it a request
     * @param comments comments
     * @throws SQLException exception handling
     */
    public Transaction(boolean is_private, String sender_id, String receiver_id, double amount,
                       boolean is_paid, boolean request, List<String> comments) throws SQLException {
        this.is_private = is_private;
        sender = sender_id;
        receiver = receiver_id;
        this.amount = amount;
        this.is_paid = is_paid;
        this.request = request;
        this.comments = comments;
        generateID();
    }

    /**
     * Default constructor
     */
    public Transaction() {
        sender = "";
        receiver = "";
        request = false;
    }

    /**
     * Sets paid status of transaction
     * @param paid yes/no paid
     */
    public void setPaid(boolean paid)
    {
        is_paid = paid;
    }

    /**
     * returns the id
     * @return id
     */
    public int getId(){return id;}

    /**
     * Returns the sender
     * @return sender
     */
    public String getSender(){return sender;}

    /**
     * Returns the receiver
     * @return receiver
     */
    public String getReceiver(){return receiver;}

    /**
     * Returns paid status
     * @return true/false paid
     */
    public boolean getPaid(){return is_paid;}

    /**
     * Returns if it is a request
     * @return true/false request
     */
    public boolean getRequest(){return request;}

    /**
     * Returns the comments
     * @return comments
     */
    public List<String> getComments(){return comments;}

    /**
     * Returns the amount
     * @return amount
     */
    public double getAmount(){return amount;}
    private int id;
    public boolean is_private;
    private final String sender;
    private final String receiver;
    private double amount;
    private boolean is_paid;
    private final boolean request;
    private List<String> comments;

    /**
     * Generates the id of the transaction - corresponds to the number of transactions
     * @throws SQLException exception handling
     */
    private void generateID() throws SQLException {

        // get number of transactions
        DatabaseConnector db = new DatabaseConnector();
        int number = db.getNumberOfTransactions();

        id = number + 1;
    }
}

