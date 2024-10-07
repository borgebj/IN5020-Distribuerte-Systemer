package ass2;

import spread.*;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Main implements ClientInterface {


    // Bank info
    private String accountName;
    private double balance = 0.0;
    private int order_counter = 0;
    private int outsanding_counter = 0;
    private List<Transaction> executedList = new ArrayList<>();
    private List<Transaction> outstanding_collection = new ArrayList<>();

    // Spread info
    static SpreadConnection connection;
    static SpreadGroup group;


    public static void main(String[] args) throws InterruptedException, InterruptedIOException, SpreadException {

        // 1. creates a connection to the spread server
        SpreadConnection connection = new SpreadConnection();

        Listener listener = new Listener(0, connection);
        Random rand = new Random();
        int id = rand.nextInt();
        try {

            // adds listener to connection
            connection.add(listener);

            // if the ifi machine is used <use the ifi machine ip address>
            //connection.connect(InetAddress.getByName("129.240.65.59"), 4803, "test connection", false, true);

            // for the local machine (172.18.0.1 is the loopback address in this machine)
            connection.connect(InetAddress.getByName("127.0.0.1"), 4803, String.valueOf(id), false, true);

            // spread group
            group = new SpreadGroup();
            group.join(connection, "group8");

            // spread messaged
            SpreadMessage message = new SpreadMessage();
            message.addGroup("group8");
            message.setFifo();
            message.setReliable();
            message.setObject("client name : "+id);
            connection.multicast(message);

            System.out.println("msg sent");

        } catch (SpreadException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
       

        System.out.println("Hello world!");
        Thread.sleep(100000000);
    }

    @Override
    public double getQuickBalance() {
        return balance;
    }

    @Override
    public double getSyncedBalance() {
        return 0;
    }

    @Override
    public int deposit(double amount) {
        Transaction tx = new Transaction();
        tx.command = "deposit " + amount;
        tx.uniqueId = accountName + " " + outsanding_counter;

        outstanding_collection.add(tx);
        outsanding_counter++;

        return 0;
    }

    @Override
    public int addInterest(double percent) {
        return 0;
    }

    @Override
    public void getHistory() {
        System.out.println("Executed Transactions:");
        for (int i = 0; i < executedList.size(); i++) {
            Transaction tx = executedList.get(i);
            System.out.println((i+1) + "."+tx.command);
        }

        System.out.println("\nOutstanding Transactions:");
        for (Transaction tx : outstanding_collection) {
            System.out.println(tx.command);
        }
    }

    @Override
    public String checkTxStatus(int uniqueId) {

        // check all outstanding transactions
        for (Transaction tx : outstanding_collection) {
            if (tx.uniqueId.equals(String.valueOf(uniqueId))) {
                return "Pending";
            }
        }

        // check all executed transactions
        for (Transaction tx : executedList) {
            if (tx.uniqueId.equals(String.valueOf(uniqueId))) {
                return "Executed";
            }
        }
        return "Transaction not found";
    }

    @Override
    public void cleanHistory() {
        executedList.clear();
    }

    @Override
    public List<String> memberInfo() {
        return null;
    }

    @Override
    public void sleep(int duration) {
        try {
            Thread.sleep(duration * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void exit() {
        System.exit(0);
    }
}