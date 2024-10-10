package ass2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	private Listener listener;


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

// Scanner for user input
		try (Scanner scanner = new Scanner(System.in)) {
			String command;
			System.out.println("\n===== [ Awaiting user input ] ===== ");

			while (true) {
				System.out.print("\n> ");
				String[] args = scanner.nextLine().trim().split(" ");
				command = args[0].toLowerCase();

				if ("exit".equals(command)) {
					break;
				}

				// Execute the requested command
				executeCommand(command, args);
			}
		}
		this.exit();

	}

	public Client(String serverAdress, String accountName, int numOfReps, int clientnr, String filename) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.filename = filename;
		this.clientnr = clientnr;
		InitializeClient();

		// Iterate File
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

			String line;
			double T;
			while ((line = br.readLine()) != null) {

				System.out.println(line);

				String[] args = line.trim().split(" ");
				String command = args[0].toLowerCase();
				executeCommand(command, args);

				// sleep [0.5, 1.5] seconds
				T = 0.5 + new Random().nextDouble();
				sleep(T);
				System.out.println('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		this.exit();
	}
	
	private void InitializeClient() throws UnknownHostException, SpreadException{

		// Connects to spread server
		this.connection = new SpreadConnection();
		this.listener = new Listener(this, this.clientnr);
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
		System.out.println("getsyncedbalance         - Get the synchronized balance from all replicas");
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

				case "getsyncedbalance":
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

				case "memberinfo":
					memberInfo();
					break;

				case "sleep":
					int duration = Integer.parseInt(args[1]);
					sleep(duration);
					break;

				case "exit":
					exit();
					break;

				case "help":
					displayHelp();
					break;

				default:
					String closestCommand = getClosestCommand(command);
					if (closestCommand != null) {
						System.out.printf("Unknown command '%s'. Did you mean '%s'?\n", command, closestCommand);
					} else {
						System.out.println("Unknown command ... ");
					}
			}
		}
		catch (NumberFormatException e) {
			System.err.println("\nInvalid argument provided for ("+command+")");
		}
	}

	/**
	 * Gets closest possible command to given input using levensthein distance algorithm
	 *
	 * @param input requested command
	 * @return null or closest command
	 */
	private String getClosestCommand(String input) {
		String[] commands = {
				"getquickbalance", "getsyncebalance", "deposit", "addinterest", "gethistory",
				"checktxstatus", "cleanhistory", "sleep", "help", "exit"
		};

		String closestCommand = null;
		int minDistance = Integer.MAX_VALUE;
		int threshold = 3; // suggestion threshold

		for (String command : commands) {
			int distance = getLevenshteinDistance(input, command);
			if (distance < minDistance && distance <= threshold) {
				minDistance = distance;
				closestCommand = command;
			}
		}

		return closestCommand;
	}

	/**
	 * Compares strings (commands) by counting minimum edits needed to change one string to another
	 * e.g. "help" and "hegpl" levensthein distance is 2:
	 * 	- substitute g with l -> "helpl"
	 * 	- delete last l -> "help"
	 *
	 * @param a first string to compare (e.g. help)
	 * @param b second string to compare (e.g. hegpl)
	 * @return the levensthein distance
	 */
	private int getLevenshteinDistance(String a, String b) {
		int[][] dp = new int[a.length() + 1][b.length() + 1];

		for (int i = 0; i <= a.length(); i++) {
			for (int j = 0; j <= b.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = Math.min(dp[i - 1][j - 1]
									+ (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
							Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
				}
			}
		}

		return dp[a.length()][b.length()];
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
		System.out.println("\nExecuted Transactions:");
		for (int i = 0; i < executedList.size(); i++) {
			Transaction tx = executedList.get(i);
			System.out.println((i+1) + ".\t"+tx.command);
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
		//TODO:
		return null;
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

		// 1. shuts down scheduler
		scheduler.shutdownNow();
		try {
			// 2. attempt to disconnect
			if (connection != null) {
				group.leave();
				connection.remove(listener);      // <--- denne skaper output på exit btw
				connection.disconnect();
			}
		} catch (SpreadException e) {
			System.err.println("Error during disconnection: " + e.getMessage());
		} finally {
			System.out.println("\nExiting ...");
			System.exit(0);
		}
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
		outstandingCollection.removeIf( e ->
				e.uniqueId.equals(tx.uniqueId) &&
				e.command.equals(tx.command));
		this.executedList.add(tx);
		order_counter++;
	}
}
