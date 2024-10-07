package ass2;

import java.util.*;

import ass2.Transaction;

import java.net.InetAddress;
import java.net.UnknownHostException;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class Client implements ClientInterface {


	// Client info
	private String serverAdress;
	private String accountName;
	private int numOfReps;
	private String filename;

	// Bank info
	private double balance;
	private int order_counter;
	private int oustanding_counter;
	private List<Transaction> executedList;
	private List<Transaction> outstandingCollection;

	// Provides each client with unique id
	static Random rand= new Random();
	int id = rand.nextInt();
	
	// Spread info
	private SpreadConnection connection;
	private SpreadGroup group;

	// constructor
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		InitializeClient();
		
		// While true --> user input
		Scanner scanner = new Scanner(System.in);

		System.out.printf("client %d: \n> ", clientnr);
		String inp = scanner.nextLine().toLowerCase();

		while (!Objects.equals(inp, "exit")) {
			System.out.printf("client %d: ", clientnr);
			inp = scanner.nextLine().toLowerCase();

			switch (inp) {
				// instruksjoner
			}
		}
		System.out.println("Exiting ...");
	}
	public Client(String serverAdress, String accountName, int numOfReps, String filename) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.filename = filename;
		InitializeClient();

		// Iterate File 
	}
	
	private void InitializeClient() throws UnknownHostException, SpreadException{

		// Connects to spread server
		SpreadConnection connection = new SpreadConnection();
		Listener listener = new Listener();

		connection.add(listener);
		connection.connect(InetAddress.getByName(serverAdress), 4803, String.valueOf(id), false, true);
        
		
		this.balance = 0.0;
		this.order_counter = 0;
		this.oustanding_counter = 0;
		this.executedList = new ArrayList<>();
		this.outstandingCollection = new ArrayList<>();
		
		// Client joins group 8
		group = new SpreadGroup();
		group.join(connection, "group8");

		System.out.println("Client " + id + " joined group: " + group);

		// Send a test message
		SpreadMessage msg = new SpreadMessage();
		msg.addGroup(group);
		msg.setObject("Hello from client " + id);
		connection.multicast(msg);
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

		Transaction tx = new Transaction();
		tx.command = "deposit " +amount;
		tx.uniqueId = accountName;
		outstandingCollection.add(tx);
		
		// broadcast transaction to all other replicas
		SpreadMessage msg = new SpreadMessage();
		msg.addGroup(group);
		msg.setFifo();
		msg.setReliable();
		try {
			msg.setObject(tx);
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
