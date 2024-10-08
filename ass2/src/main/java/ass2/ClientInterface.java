package ass2;

import java.util.List;

public interface ClientInterface {
    
    /** 
     * Returns the current balance without synchronizing with any previously issued transactions.
     * @return the current balance.
     */
    double getQuickBalance();

    /** 
     * Returns the synchronized balance after applying outstanding transactions.
     * @return the synchronized balance.
     */
    double getSyncedBalance();

    /** 
     * Deposits the specified amount into the account.
     * @param amount the amount to be deposited.
     * @return 1 for success, -1 for failure.
     */
    int deposit(double amount);

    /** 
     * Adds interest to the current balance based on the specified percentage.
     * @param percent the percentage of interest to be added.
     * @return 1 for success, -1 for failure.
     */
    int addInterest(double percent);

    /** 
     * Prints the transaction history including executed and outstanding transactions.
     */
    void getHistory();

    /** 
     * Checks the status of a transaction based on given unique ID.
     * @param uniqueId the unique identifier of the transaction.
     * @return a string representing the status (e.g., "completed", "pending").
     */
    String checkTxStatus(int uniqueId);

    /** 
     * Clears the transaction history.
     */
    void cleanHistory();


    /** 
     * Causes the client to sleep for the specified duration in seconds.
     * @param duration the duration to sleep.
     */
    void sleep(double duration);

    /** 
     * Exits the client.
     */
    void exit();
}
