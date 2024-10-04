package ass2;

import java.util.ArrayList;
import java.util.List;
import ass2.Transaction;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class Client implements ClientInterface {

	// Bank info
	private String accountName;
	private double balance;
	private int order_counter;
	private int oustanding_counter;
	private List<Transaction> executedList;
	private List<Transaction> outstandingCollection;

	// Spread info
	private SpreadConnection connection;
	private SpreadGroup group;

	// constructor
	public Client() {
		SpreadConnection connection = new SpreadConnection();
		this.balance = 0;
		this.order_counter = 0;
		this.oustanding_counter = 0;
		this.executedList = new ArrayList<>();
		this.outstandingCollection = new ArrayList<>();

		try {
			group = new SpreadGroup();
			group.join(connection, "group8");
		} catch (SpreadException e) {
			e.printStackTrace();
		}
	}

	@Override
	public double getQuickBalance() {
		return balance;
	}

	@Override
	public double getSyncedBalance() {
		//TODO:
		return 0;
	}

	@Override
	public int deposit(double amount) {
		Transaction tx = new Transaction("deposit " + amount, accountName);
		outstandingCollection.add(tx);

		// broadcast transaction to all other replicas
		SpreadMessage msg = new SpreadMessage();
		msg.addGroup(group);
		msg.setFifo();
		msg.setReliable();
		try {
			connection.multicast(msg);
			return 1;
		} catch (SpreadException e) {
			return -1;
		}
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
		for (Transaction tx : outstandingCollection) {
			System.out.println(tx.command);
		}
	}

	@Override
	public String checkTxStatus(int uniqueId) {
		// check all outstanding transactions
		for (Transaction tx : outstandingCollection) {
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

	}

	@Override
	public void exit() {

	}
}
