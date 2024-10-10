package ass2;

import java.io.Serializable;
import java.util.*;

import ass2.Transaction;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class Client implements ClientInterface {


	// Client info
	private String serverAdress;
	private String accountName;
	private int clientnr;
	private int numOfReps;
	private String filename;


	// Bank info
	private double balance;
	public int order_counter;
	public int outstanding_counter;
	public List<Transaction> executedList;
	public Collection<Transaction> outstandingCollection;

	
	// Spread info
	private SpreadConnection connection;
	private SpreadGroup group;


	// Scheduler for broadcasting every 10 seconds
	private ScheduledExecutorService scheduler;


	// constructor
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.clientnr = clientnr;
		InitializeClient();
		
		// Scanner for user-input
		Scanner scanner = new Scanner(System.in);
		String[] args = null;
		String command = "";

		System.out.println("\n===== [ Awaiting user-input ] ===== ");

		while (!Objects.equals(command, "exit")) {

			System.out.print("\n> ");
			args = scanner.nextLine().split(" ");
			command = args[0].toLowerCase();

			// execute requested command
			executeCommand( command, args );
		}
		System.out.println("Exiting ...");
	}
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr, String filename) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.filename = filename;
		this.clientnr = clientnr;
		InitializeClient();

		// Iterate File 
	}
	
	private void InitializeClient() throws UnknownHostException, SpreadException{

		// Connects to spread server
		this.connection = new SpreadConnection();
		Listener listener = new Listener(this, this.clientnr);
		this.connection.add(listener);
		this.connection.connect(InetAddress.getByName(serverAdress), 4803, String.valueOf(this.clientnr), false, true);

		// set account info + scheduler
		this.balance = 0.0;
		this.order_counter = 0;
		this.outstanding_counter = 0;
		this.executedList = new ArrayList<>();
		this.outstandingCollection = new ArrayList<>();
		this.scheduler = Executors.newScheduledThreadPool(1);
		
		// Client joins group 8
		group = new SpreadGroup();
		group.join(connection, "group8");

		// wait for "numOfReps" has joined group8
		while (listener.getMembers()  < numOfReps) {
			this.sleep(0.5);
			System.out.printf("Client%d waiting (%d / %d)\n", clientnr, listener.getMembers(), numOfReps);
		}
		System.out.println("\n\nAll replicas has joined group8\n");
		sleep(2);
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

		// start broadcast
		scheduler.scheduleAtFixedRate(this::broadcastOutstandingTransactions, 2, 10, TimeUnit.SECONDS);
	}

	/**
	 * Method used by scheduler which broadcasts every 10 seconds
	 */
	private void broadcastOutstandingTransactions() {
		SpreadMessage msg = new SpreadMessage();
		msg.addGroup(group);
		msg.setFifo();
		msg.setReliable();
		try {
			msg.setObject((Serializable) this.outstandingCollection);
			connection.multicast(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void displayHelp() {
		System.out.println("\n==== [ Command Help ] ====");
		System.out.println("getquickbalance          - Get the current balance (quick, may be outdated)");
		System.out.println("getsyncebalance          - Get the synchronized balance from all replicas");
		System.out.println("deposit <amount>         - Deposit the specified amount into the account");
		System.out.println("addinterest <percent>    - Add interest to the account based on the given percentage");
		System.out.println("gethistory               - Show transaction history");
		System.out.println("checktxstatus <uniqueId> - Check the status of a transaction by its unique ID");
		System.out.println("cleanhistory             - Clear all executed transaction history");
		System.out.println("sleep <duration>         - Pause the client for the specified duration in seconds");
		System.out.println("help                     - Display this help menu");
		System.out.println("exit                     - Exit the application");
		System.out.println("=============================");
	}


	private void executeCommand(String command, String[] args) {
		try {
			switch (command) {
				case "getquickbalance":
					System.out.printf("[Balance] >> %f\n", getQuickBalance());
					break;

				case "getsyncebalance":
					System.out.printf("[Balance] >> %f\n", getSyncedBalance());
					break;

				case "deposit":
					double amount = Double.parseDouble(args[1]);
					deposit(amount);
					break;

				case "addinterest":
					double interest = Double.parseDouble(args[1]);
					addInterest(interest);
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

				case "help":
					displayHelp();
					break;

				default:
					System.out.println("\nUnknown command ... ");
					System.out.println("For help, type 'help'\n");

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addCommandToCollection(String command, double amount) {

		// Transaction-object associated with command
		Transaction tx = new Transaction();
		tx.command = (command + " " + amount);
		tx.uniqueId = (accountName + " " + outstanding_counter);

		outstandingCollection.add(tx);
		outstanding_counter++;
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
	public void deposit(double amount) {
		addCommandToCollection("deposit", amount);
	}

	@Override
	public void addInterest(double percent) {
		addCommandToCollection("addinterest", percent);
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

	/**
	 * Disconnects connection and exits program
	 */
	@Override
	public void exit() {
		try {
			connection.disconnect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void addToAccount(Transaction tx, boolean interest) {
		double amount = Double.parseDouble(tx.command.split(" ")[1]);

		if (interest) {
			this.balance = this.balance * (1 + amount/100);
		}
		else {
			this.balance += amount;
		}

		// remove from outstanding, add to executed
		outstandingCollection.removeIf(e -> e.uniqueId.equals(tx.uniqueId) && e.command.equals(tx.command));
		this.executedList.add(tx);
		order_counter++;
	}

	public boolean hasExecuted(String uniqueId) {
		for (Transaction tx : executedList) {
			if (Objects.equals(tx.uniqueId, uniqueId)) return true;
		}
		return false;
	}
}
