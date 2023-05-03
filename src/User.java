import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles User data
 */
public class User implements Serializable
{
    @Serial
    private static final long serialVersionUID = 8985996076625392040L;

    /**
     * Constructor
     */
    public User(){
        this.user_id = "Default";
        this.password = "Default Password";
        this.balance = 0;
        this.groups = new ArrayList<>();
        this.friends = new ArrayList<>();
    }

    /**
     * Constructor
     * @param id username
     * @param password password
     */
    public User(String id, String password)
    {
        user_id = id;
        this.password = password;
        balance = 0;
        groups = new ArrayList<Group>();
        friends = new ArrayList<>();
    }

    /**
     * Constructors
     * @param id username
     * @param password password
     * @param balance balance
     * @param groups groups
     * @param friends friends
     */
    public User(String id, String password, double balance, List<Group> groups, List<String> friends)
    {
        user_id = id;
        this.password = password;
        this.balance = balance;
        this.groups = groups;
        this.friends = friends;
    }

    /**
     * Adds a group to group list
     * @param group group to add
     * @throws SQLException exception handling
     */
    public void addGroup(Group group) throws SQLException {
        groups.add(group);
    }

    /**
     * sets the group list
     * @param curr groups
     */
    public void setGroups(ArrayList<Group> curr){
        this.groups= curr;
    }

    /**
     * removes a group from the list
     * @param group group to remove
     * @throws SQLException exception handling
     */
    public void removeGroup(Group group) throws SQLException {
        groups.remove(group);
    }

    /**
     * adds a friend
     * @param friend friend to add
     */
    public void addFriend(String friend)
    {
        this.friends = new ArrayList<>(this.friends);

        friends.add(friend);
    }

    /**
     * removes a friend
     * @param friend
     */
    public void removeFriend(String friend)
    {
        friends.remove(friend);
    }

    /**
     * returns the balance
     * @return balance to return
     */
    public double getBalance(){
        return balance;
    }

    /**
     * gets the group list
     * @return list of groups
     */
    public List<Group> getGroups(){return groups;}

    /**
     * gets the friend list
     * @return list of friends
     */
    public List<String> getFriends(){return friends;}

    /**
     * Gets the password
     * @return password
     */
    public String getPassword(){return password;}
    public String user_id;
    private double balance;
    private List<Group> groups;
    private List<String> friends;
    private String password;

    /**
     * Sets balance
     * @param newBalance balance to set
     */
    public void setBalance(Double newBalance) {
        this.balance = newBalance;
    }

    /**
     * Deposits money to balance
     * @param balance amount to deposit
     * @throws SQLException exception handling
     */
    public void deposit(Double balance) throws SQLException {
        this.balance += balance;
    }

    /**
     * Withdraws money from balance
     * @param balance money to withdraw
     * @throws SQLException exception handling
     */
    public void withdraw(Double balance) throws SQLException {
        this.balance -= balance;
    }
}


