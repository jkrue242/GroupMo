import org.junit.jupiter.api.Test;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This tests that we are not getting duplicate rows
 */
public class DatabaseTests {

    /**
     * Check for duplicate user_id
     * @throws SQLException exception handling
     */
    @Test
    public void TestForUserDuplicateId() throws SQLException {
        DatabaseConnector db = new DatabaseConnector();
        assertEquals(0, db.getUserDuplicateCounts());
    }

    /**
     * Check for duplicate transaction_id
     * @throws SQLException exception handling
     */
    @Test
    public void TestForTransactionDuplicateId() throws SQLException {
        DatabaseConnector db = new DatabaseConnector();
        assertEquals(0, db.getTransactionDuplicateCounts());
    }

    /**
     * Check for duplicate group_id
     * @throws SQLException exception handling
     */
    @Test
    public void TestForGroupDuplicateId() throws SQLException {
        DatabaseConnector db = new DatabaseConnector();
        assertEquals(0, db.getGroupDuplicateCounts());
    }
}
