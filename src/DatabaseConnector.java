import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Handles database operation
 */
public class DatabaseConnector {

    /**
     * Constructor
     */
    public DatabaseConnector()
    {
        connect();
    }

    /**
     * Connect to database
     */
    public void connect()
    {
        // database id
        String db_name = "swd_db007";
        String url = "jdbc:mysql://s-l112.engr.uiowa.edu/" + db_name + "?enabledTLSProtocols=TLSv1.2";

        try {

            // user/pass
            String username = "engr_class007";
            String password = "jbkrueger242";

            // connect
            connection = DriverManager.getConnection(url, username, password);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts row to UserTable
     * @param entry user data
     * @throws SQLException exception handling
     */
    public void insertRow(User entry) throws SQLException {

        // remove existing row if we are updating data
        PreparedStatement check = connection.prepareStatement(
                "DELETE FROM UserTable WHERE user_id = '"+entry.user_id+"'");
        check.executeUpdate();
        check.close();
        System.out.println("deleted old user");
        // turn group objects to string of list format
        String groups = "[";
        for (int i = 0; i < entry.getGroups().size(); i++)
        {
            groups += entry.getGroups().get(i).getID();
            if (i < entry.getGroups().size() -1)
            {
                groups += ",";
            }
        }
        groups += "]";
        // store friends as string
        String friends = entry.getFriends().toString();

        // sql insert
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO UserTable VALUES('"+entry.user_id+"', " +
                        "'"+entry.getPassword()+"',"+
                        "'"+entry.getBalance()+"', '" +
                        ""+groups+"', " +
                        "'"+friends+"')");
        statement.executeUpdate();
    }

    /**
     * Inserts row to TransactionTable
     * @param entry transaction data
     * @throws SQLException exception handling
     */
    public void insertRow(Transaction entry) throws SQLException
    {

        // this is so we do not have duplicate rows
        removeRow(entry);

        // sql insert
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO TransactionTable VALUES(" +
                        "'"+entry.getId()+"', " +
                        entry.is_private+"," +
                        "'"+entry.getSender()+"'," +
                        "'"+entry.getReceiver()+ "'," +
                        "'"+entry.getAmount()+"'," +
                        entry.getPaid()+"," +
                        entry.getRequest()+","+
                        "'"+entry.getComments().toString()+"')");
        statement.executeUpdate();
    }

    /**
     * Inserts row to GroupTable
     * @param entry group data
     * @throws SQLException exception handling
     */
    public void insertRow(Group entry) throws SQLException {

        System.out.println(entry.getID());
        System.out.println(entry.getUsers());
        System.out.println(entry.getUserAdmin());
        System.out.println(entry.getGroupName());
        System.out.println(entry.getBalance());

        // we don't want the same group id repeated
        PreparedStatement check = connection.prepareStatement(
                "DELETE FROM GroupsTable WHERE group_id = '"+entry.getID()+"'");
        check.executeUpdate();

        // sql insert
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO GroupsTable VALUES('"+ entry.getID() +"'," +
                        "'" + entry.getGroupName()+"',"+
                        "'" + entry.getUserAdmin()+"',"+
                        "'" + entry.getUsers().toString() + "',"+
                        "'" + entry.getBalance() +"')");
        statement.executeUpdate();
    }

    /**
     * Removes row from TransactionTable
     * @param entry transaction data
     * @throws SQLException exception handling
     */
    public void removeRow(Transaction entry) throws SQLException {

        // sql delete
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM TransactionTable WHERE transaction_id = '"+entry.getId()+"'" +
                        " AND sender = '"+entry.getSender()+"'" +
                        "AND receiver = '"+entry.getReceiver()+"'" +
                        "AND amount = '"+entry.getAmount()+"'");
        statement.executeUpdate();
    }

    /**
     * Sets the transaction to be paid
     * @param entry
     * @throws SQLException
     */
    public void setTransactionToPaid(Transaction entry) throws SQLException {

        removeRow(entry);
        entry.setPaid(true);
        insertRow(entry);
    }

    /**
     * Removes row from GroupsTable
     * @param entry group data
     * @throws SQLException exception handling
     */
    public void removeRow(Group entry) throws SQLException {
        // sql delete
        int id = entry.getID();
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM GroupsTable WHERE group_id = '"+id+"'" +
                        "AND group_name = '"+entry.getGroupName()+"'" +
                        "AND host = '"+entry.getUserAdmin()+"'"+
                        "AND members = '"+entry.getUsers().toString()+"'" +
                        "AND balance = '"+entry.getBalance()+"'");
        statement.executeUpdate();
        statement.close();
    }

    /**
     * deletes the group from the database
     * @param id
     * @throws SQLException
     */
    public void deleteGroup(int id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM GroupsTable WHERE group_id = '"+id+"'");
        statement.executeUpdate();
        statement.close();

        // remove groups from each user that is in it
        PreparedStatement statement2 = connection.prepareStatement(
                "SELECT * FROM UserTable"
        );
        ResultSet query = statement2.executeQuery();

        while (query.next())
        {
            boolean needsUpdating = false;
            ArrayList<String> group_array = new ArrayList<>(Arrays.asList(query.getString("groups").replace("[", "").replace("]", "").split(",")));
            List<Group> group_list = new ArrayList<>();
            for (String group_id : group_array)
            {
                Group currGroup = getGroupData(Integer.parseInt(group_id));
                if (Integer.parseInt(group_id) == id)
                {
                    group_array.remove(group_id);
                    needsUpdating = true;
                }
                else
                {
                    group_list.add(currGroup);
                }
            }

            ArrayList<String> friends = (ArrayList<String>) Arrays.asList(query.getString("friends").replace("[","").replace("]","").split(","));
            if (needsUpdating)
            {
                User updated_user = new User(query.getString("user_id"), query.getString("password"), query.getDouble("balance"),
                        group_list, friends);
                insertRow(updated_user);
            }
        }
    }

    /**
     * Returns a User object with data from database given a user id
     * @param user_id user id
     * @return reference to User
     * @throws SQLException exception handling
     */
    public User getUserData(String user_id) throws SQLException {

        // query db
        Statement statement = connection.createStatement();
        ResultSet query = statement.executeQuery("SELECT * FROM UserTable WHERE user_id = '"+user_id+"'");

        String id = "";
        double balance = 0;
        String groupsString = "";
        String friendsString = "";
        String password = "";
        // get data
        while (query.next()) {
            id = query.getString("user_id");
            password = query.getString("password");
            balance = query.getDouble("balance");
            groupsString = query.getString("groups");
            friendsString = query.getString("friends");
        }
        query.close();

        // convert string of group id to list of Group objects
        List<String> groupIdList = stringToList(groupsString);
        List<Group> groups = new ArrayList<>();
        for (String s : groupIdList)
        {
            if (!s.equals(""))
            {
                groups.add(getGroupData(Integer.parseInt(s)));
            }
        }

        // create new User
        User newUser = new User(id, password, balance, groups, stringToList(friendsString));
        return newUser;
    }

    /**
     * Returns a Transaction object with data from database given group id
     * @param transaction_id transaction id
     * @return reference to Transaction
     * @throws SQLException exception handling
     */
    public Transaction getTransactionData(int transaction_id) throws SQLException {

        // query db
        Statement statement = connection.createStatement();
        ResultSet query = statement.executeQuery("SELECT * FROM TransactionTable WHERE transaction_id = '"+transaction_id+"'");

        int id = -1;
        boolean priv = false;
        String sender = "";
        String receiver = "";
        double amount = 0;
        boolean paid = false;
        boolean request = false;
        String commentsString = "";

        // get data
        while (query.next()) {
            id = query.getInt("transaction_id");
            priv = query.getBoolean("private");
            sender = query.getString("sender");
            receiver = query.getString("receiver");
            amount = query.getDouble("amount");
            paid = query.getBoolean("paid");
            request = query.getBoolean("request");
            commentsString = query.getString("comments");
        }
        query.close();

        // create new Transaction
        Transaction newTransaction = new Transaction(id, priv, sender, receiver, amount, paid, request, stringToList(commentsString));
        return newTransaction;
    }

    /**
     * Returns a Group object with data from database given group id
     * @param group_id group id
     * @return reference to Group
     * @throws SQLException exception handling
     */
    public Group getGroupData(int group_id) throws SQLException {

        // query db
        Statement statement = connection.createStatement();
        ResultSet query = statement.executeQuery("SELECT * FROM GroupsTable WHERE group_id = '"+group_id+"'");
        int id = -1;
        String groupName = "";
        String host = "";
        String membersString = "";
        double balance = 0;

        // get data
        while (query.next()) {
            id = query.getInt("group_id");
            groupName = query.getString("group_name");
            host = query.getString("host");
            membersString = query.getString("members");
            balance = query.getDouble("balance");
        }
        query.close();

        // create new group
        Group newGroup = new Group(id, groupName, host, stringToList(membersString), balance);
        return newGroup;
    }

    /**
     * Returns true if user exists, false otherwise
     * @param user_id user id
     * @return true or false
     * @throws SQLException exception handling
     */
    public boolean checkIfUserExists(String user_id) throws SQLException
    {
        // query db
        ResultSet check = connection.createStatement().executeQuery("SELECT COUNT(*) FROM UserTable WHERE user_id = '" + user_id + "'");
        int count = 0;
        if (check.next())
        {
            count = check.getInt(1);
        }
        check.close();
        return (count > 0);
    }

    /**
     * Checks if group exists with id
     * @param group_id group id
     * @return true or false
     * @throws SQLException exception handling
     */
    public boolean checkIfGroupExists(int group_id) throws SQLException
    {
        // query db
        ResultSet check = connection.createStatement().executeQuery("SELECT COUNT(*) FROM GroupsTable WHERE group_id = '" + group_id + "'");
        int count = 0;
        if (check.next())
        {
            count = check.getInt(1);
        }
        check.close();
        return (count > 0);
    }

    /**
     * Helper function for converting a string to a list
     * @param input string
     * @return list
     */
    private List<String> stringToList(String input)
    {
        input = input.replace("[", "").replace("]", "");
        List<String> members = Arrays.asList(input.split(","));

        return members;
    }

    /**
     * Prints the column names of a table query
     * @param query query
     * @throws SQLException exception handling
     */
    private void printColumnNames(ResultSet query) throws SQLException {
        ResultSetMetaData metadata = query.getMetaData();
        int cols = metadata.getColumnCount();
        for (int i = 0; i < cols; i++)
        {
            System.out.print(metadata.getColumnLabel(i+1));
            if (i < cols-1)
            {
                System.out.print(", ");
            }
        }
        System.out.println("\n");
    }

    /**
     * Returns the number of rows with duplicate ids
     * @return count
     * @throws SQLException exception handling
     */
    public int getUserDuplicateCounts() throws SQLException {

        // query db
        ResultSet check = connection.createStatement().executeQuery(
                "SELECT user_id, COUNT(user_id) " +
                        "FROM UserTable " +
                        "GROUP BY user_id " +
                        "HAVING COUNT(user_id) > 1");
        int count = 0;
        if (check.next())
        {
            count = check.getInt(1);
        }
        check.close();
        return count;
    }

    /**
     * Returns the number of transaction duplicate id
     * @return count
     * @throws SQLException exception handling
     */
    public int getTransactionDuplicateCounts() throws SQLException {

        // query db
        ResultSet check = connection.createStatement().executeQuery(
                "SELECT transaction_id, COUNT(transaction_id) " +
                        "FROM TransactionTable " +
                        "GROUP BY transaction_id " +
                        "HAVING COUNT(transaction_id) > 1");
        int count = 0;
        if (check.next())
        {
            count = check.getInt(1);
        }
        check.close();
        return count;
    }

    /**
     * Returns the number of group duplicate id
     * @return count
     * @throws SQLException exception handling
     */
    public int getGroupDuplicateCounts() throws SQLException {

        // query db
        ResultSet check = connection.createStatement().executeQuery(
                "SELECT group_id, COUNT(group_id) " +
                        "FROM GroupsTable " +
                        "GROUP BY group_id " +
                        "HAVING COUNT(group_id) > 1");
        int count = 0;
        if (check.next())
        {
            count = check.getInt(1);
        }
        check.close();
        return count;
    }

    /**
     * Gets the number of transactions stored on the database
     * @return number of transactions
     * @throws SQLException exception handling
     */
    public int getNumberOfTransactions() throws SQLException {

        // query
        ResultSet query = connection.createStatement().executeQuery(
                "SELECT COUNT(*) FROM TransactionTable");

        int count = 0;
        if (query.next())
        {
            count = query.getInt(1);
        }
        query.close();
        return count;
    }

    /**
     * Returns true if the password is correct
     * @param username username
     * @param password password
     * @return true or false
     * @throws SQLException exception handling
     */
    public boolean checkPassword(String username, String password) throws SQLException {
        boolean matches = false;
        ResultSet query = connection.createStatement().executeQuery(
                "SELECT * FROM UserTable WHERE user_id = '"+username+"' AND password = '"+password+"'");
        if (query.next())
        {
            matches = true;
        }
        query.close();
        return matches;
    }

    /**
     * Returns a list of transactions on the database
     * @return list of Transaction objects
     * @throws SQLException exception handling
     */
    public List<Transaction> getTransactionList() throws SQLException
    {
        ResultSet query = connection.createStatement().executeQuery(
                "SELECT transaction_id FROM TransactionTable"
        );

        // iterate through transaction id
        List<Transaction> transaction_list = new ArrayList<>();
        while (query.next())
        {
            int transaction_id = query.getInt("transaction_id");
            Transaction transaction = getTransactionData(transaction_id);
            transaction_list.add(transaction);
        }
        query.close();
        return transaction_list;
    }

    /**
     * Checks is a user belongs to a group
     * @param id
     * @param username
     * @return
     * @throws SQLException
     */
    public boolean checkIfUserBelongsToGroup(int id, String username) throws SQLException {
        ResultSet check = connection.createStatement().executeQuery(
                "SELECT members FROM GroupsTable WHERE group_id = '"+id+"'");
        if (check.next())
        {
            String member_string  = (String) check.getObject("members");
            String[] members = member_string.replace("[", "").replace("]", "").split(",");
            for (String member : members) {
                if (member.equals(username)) {
                    return true;
                }
            }
        }
        check.close();
        return false;
    }

    /**
     * Gets the pending payment requests
     * @return Hashmap of users and requests
     * @throws SQLException
     */
    public HashMap<String ,List<Transaction>> getPendingRequests() throws SQLException
    {
        ResultSet query = connection.createStatement().executeQuery(
                "SELECT * FROM TransactionTable WHERE receiver = '"+App.getCurrentClient().getUser().user_id+"'" +
                        "AND paid = '"+false+"'"
        );

        HashMap<String, List<Transaction>> pendingRequests = new HashMap<>();
        while (query.next())
        {
            List<String> comments = Arrays.asList(query.getString("comments").replace("[", "").replace("]", "").split(","));
            Transaction new_transaction = new Transaction(query.getInt("transaction_id"), query.getBoolean("private"),
                    query.getString("sender"), query.getString("receiver"), query.getDouble("amount"),
                    query.getBoolean("paid"), query.getBoolean("request"),comments);

            if (pendingRequests.containsKey(query.getString("sender")))
            {
                List<Transaction> existing = pendingRequests.get(query.getString("sender"));
                existing.add(new_transaction);
                pendingRequests.put(query.getString("sender"),existing);
            }
            else
            {
                List<Transaction> new_transactions = new ArrayList<>();
                new_transactions.add(new_transaction);
                pendingRequests.put(query.getString("sender"), new_transactions);
            }
        }
        query.close();

        return pendingRequests;
    }

    /**
     * adds the user to the group they put the group id for
     * @param username
     * @param group_id
     * @throws SQLException
     */
    public void addUserToGroup(String username, int group_id) throws SQLException
    {
        User curr_user = getUserData(username);
        Group curr_group = getGroupData(group_id);

        curr_user.addGroup(curr_group);
        curr_group.addUser(curr_user.user_id);

        insertRow(curr_user);
        insertRow(curr_group);
    }

    /**
     * removes the user from the group that they set the group id to
     * @param username
     * @param group_id
     * @throws SQLException
     */
    public void removeUserToGroup(String username, int group_id) throws SQLException
    {
        User curr_user = getUserData(username);
        Group curr_group = getGroupData(group_id);

        for (Group g : curr_user.getGroups())
        {
            System.out.println(g.getGroupName());
        }

        System.out.println("==================");
        curr_user.removeGroup(curr_group);
        curr_group.removeUser(curr_user.user_id);

        ArrayList<Group> curr_groups = (ArrayList<Group>) curr_user.getGroups();
        curr_groups.remove(curr_group);
        curr_user.setGroups(curr_groups);

        for (Group g : curr_user.getGroups())
        {
            System.out.println(g.getGroupName());
        }
        insertRow(curr_user);
        insertRow(curr_group);
    }

    private Connection connection;
}

