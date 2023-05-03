import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds Group information
 */
public class Group implements Serializable {
    @Serial
    private static final long serialVersionUID = 3067007269134650675L;
    private List<String> users;
    private String userAdmin;
    private String groupName;
    private int groupID;
    private double balance;

    /**
     * This is for when we don't know the id
     * @param groupName group name
     * @param host host
     * @param users members
     * @param balance balance
     */
    public Group(String groupName, String host, ArrayList<String> users, double balance)
    {
        this.groupName = groupName;
        this.userAdmin = host;
        this.users = users;
        this.balance = balance;
        generateID();
    }

    /**
     * This is for when we know the id
     * @param id id
     * @param groupName name
     * @param host host
     * @param users members
     * @param balance balance
     */
    public Group(int id, String groupName, String host, List<String> users, double balance)
    {
        this.groupID = id;
        this.groupName = groupName;
        this.userAdmin = host;
        this.users = users;
        this.balance = balance;
    }

    /**
     * Returns group balance
     * @return balance
     */
    public double getBalance(){
        return balance;
    }

    /**
     * Returns group name
     * @return group name
     */
    public String getGroupName(){
        return groupName;
    }

    /**
     * Returns users
     * @return users
     */
    public List<String> getUsers(){
        return users;
    }

    /**
     * Adds a user to the group
     * @param user user to add
     */
    public void addUser(String user)
    {
        this.users= new ArrayList<>(this.users);

        users.add(user);
    }

    /**
     * Removes a user
     * @param user user to remove
     * @throws SQLException exception handling
     */
    public void removeUser(String user) throws SQLException {
        users.remove(user);
    }

    /**
     * returns the id
     * @return id
     */
    public int getID() {
        return groupID;
    }

    /**
     * Returns group admin
     * @return admin
     */
    public String getUserAdmin(){return userAdmin;}

    /**
     * Generates the id of a group using a combination of group name hash and group admin hash
     */
    private void generateID()
    {
        groupID = Math.abs(groupName.hashCode() + userAdmin.hashCode());
    }

    /**
     * Adds amount to balance
     * @param amount amount to add
     */
    public void addToBalance(double amount)
    {
        balance += amount;
    }

    /**
     * Removes amount from balance
     * @param amount amount to remove
     */
    public void removeFromBalance(double amount)
    {
        balance -= amount;
    }

}
