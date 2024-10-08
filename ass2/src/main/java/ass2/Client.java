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

	
	// Spread info
	private SpreadConnection connection;
	private SpreadGroup group;

	// constructor
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		InitializeClient(clientnr);
		
		// While true --> user input
		Scanner scanner = new Scanner(System.in);

		String[] args = null;
		String command = "";

		while (!Objects.equals(command, "exit")) {
			System.out.printf("\nclient %d: \n> ", clientnr);
			args = scanner.nextLine().split(" ");
			command = args[0].toLowerCase();

			System.out.println(Arrays.toString(args));

			try {
				double res = -1;
				switch (command) {
					case "getquickbalance":
						res = getQuickBalance();
						break;
					case "getsyncebalance":
						res = getSyncedBalance();
						break;
					case "deposit":
						double amount = Integer.parseInt(args[1]);
						res = deposit(amount);
						break;
					case "addinterest":
						double interest = Integer.parseInt(args[1]);
						res = addInterest(interest);
						break;
					case "gethistory":
						getHistory();
						break;
					case "checktxstatus":
						int uniqueId = Integer.parseInt(args[1]);
						String status = checkTxStatus(uniqueId);
						break;
					case "cleanhistory":
						cleanHistory();
						break;
					case "sleep":
						int duration = Integer.parseInt(args[1]);
						sleep(duration);
						break;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			sleep(0.5);
		}
		System.out.println("Exiting ...");
	}
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr, String filename) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.filename = filename;
		InitializeClient(clientnr);

		// Iterate File 
	}
	
	private void InitializeClient(int id) throws UnknownHostException, SpreadException{

		// Connects to spread server
		this.connection = new SpreadConnection();
		Listener listener = new Listener(id);

		this.connection.add(listener);
		this.connection.connect(InetAddress.getByName(serverAdress), 4803, String.valueOf(id), false, true);

		this.balance = 0.0;
		this.order_counter = 0;
		this.oustanding_counter = 0;
		this.executedList = new ArrayList<>();
		this.outstandingCollection = new ArrayList<>();
		
		// Client joins group 8
		group = new SpreadGroup();
		group.join(connection, "group8");
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
			msg.setObject("deposit " + amount);
			connection.multicast(msg);
			return 1;
		} catch (SpreadException e) {
			e.printStackTrace();
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
	public void sleep(double duration) {
		try {
			Thread.sleep((long) (duration * 1000L));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exit() {

	}
}
