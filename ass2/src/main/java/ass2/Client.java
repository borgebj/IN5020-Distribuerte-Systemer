package ass2;

// input and file-reading
import java.io.InputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// utility
import java.util.*;
import java.net.InetAddress;   							 // for internet connection through spread
import java.util.concurrent.TimeUnit;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

// spread imports
import spread.SpreadGroup;
import spread.SpreadMessage;
import spread.SpreadException;
import spread.SpreadConnection;


public class Client implements ClientInterface {

	// formatting / design / run-info
	private Date startTime;
	private final String RESET = "\u001B[0m";
	private final String HEADER_COLOR = "\u001B[34m";


	// Client info
	private String serverAdress;
	private String accountName;
	private int clientnr;
	private String clientName;
	private int numOfReps;
	private String filename;
	private Listener listener;
	private boolean usingFile;
	private boolean syncing;

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


	/**
	 * Initiates client for user-input
	 *
	 * @param serverAdress address to spread-server
	 * @param accountName name of account-in-use
	 * @param numOfReps how many total replicas should be active
	 * @param clientnr ID for _this_ client
	 * @throws UnknownHostException for spread-issues
	 * @throws SpreadException for spread-issues
	 */
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.clientnr = clientnr;
		this.usingFile = false;
		InitializeClient();

		// Scanner for user input
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			String command;
			System.out.println("\n===== [ Awaiting user input ] ===== ");

			while (true) {
				System.out.print("\n> ");
				String[] args = br.readLine().trim().split(" ");
				command = args[0].toLowerCase();

				if ("exit".equals(command)) {
					break;
				}

				// Execute the requested command
				executeCommand(command, args);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.exit();

	}

	/**
	 * Initiates client for file-reading
	 *
	 * @param serverAdress address to spread-server
	 * @param accountName name of account-in-use
	 * @param numOfReps how many total replicas should be active
	 * @param clientnr ID for _this_ client
	 * @throws UnknownHostException for spread-issues
	 * @throws SpreadException for spread-issues
	 */
	public Client(String serverAdress, String accountName, int numOfReps, int clientnr, String filename) throws UnknownHostException, SpreadException {
		this.serverAdress = serverAdress;
		this.accountName = accountName;
		this.numOfReps = numOfReps;
		this.filename = filename;
		this.clientnr = clientnr;
		this.usingFile = true;
		InitializeClient();

		// Iterate File
		InputStream inputStream = Client.class.getClassLoader().getResourceAsStream(filename);
		if (inputStream == null) {
			System.err.printf("File '%s' not found!\n", filename);
			exit();
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

			String line;
			double T;
			while ((line = br.readLine()) != null) {

				// parse command and argument
				String[] args = line.trim().split(" ");
				String command = args[0].toLowerCase();

				// run, print timestamp
				System.out.printf("%.1f: %s%n", getTimestamp(), line);
				executeCommand(command, args);

				// sleep between [0.5, 1.5] seconds
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

	/**
	 * Initialization of the client - various info is set such as Spread-info, group-info and the scheduler
	 *
	 * @throws UnknownHostException for spread-issues
	 * @throws SpreadException for spread-issues
	 */
	private void InitializeClient() throws UnknownHostException, SpreadException{

		// Connects to spread server
		this.connection = new SpreadConnection();
		this.listener = new Listener(this, numOfReps, this.clientnr, this.accountName);
		this.connection.add(listener);
		this.connection.connect(InetAddress.getByName(serverAdress), 4801, String.valueOf(this.clientnr), false, true);

		// set account info + scheduler
		this.balance = 0.0;
		this.order_counter = 0;
		this.outstanding_counter = 0;
		this.syncing = false;
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

		// set timestamp timer and set name of client
		this.startTime = new Date();
		this.clientName = String.format("Rep%d", clientnr);

		// start broadcast
		scheduler.scheduleAtFixedRate(() -> {
			try {
				if (!syncing) broadcastOutstandingTransactions(); 
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 2, 10, TimeUnit.SECONDS);

	}

	/**
	 * Method used by scheduler which broadcasts every 10 seconds
	 */

	private void broadcastSyncOutstanding(Collection<Transaction> syncTransactions){
		SpreadMessage msg = new SpreadMessage();
		msg.addGroup(group);
		msg.setFifo();
		msg.setReliable();

		try {
			msg.setObject((Serializable) syncTransactions);
	   		connection.multicast(msg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void broadcastOutstandingTransactions() {
		SpreadMessage msg = new SpreadMessage();
		msg.addGroup(group);
		msg.setFifo();
		msg.setReliable();
		try {
//			System.out.printf("%nTimestamp %.1f%n", getTimestamp());
//			System.out.printf("\t"+HEADER_COLOR+"%s broadcasts:%n"+RESET, clientName, clientnr);
//			outstandingCollection.forEach(item -> System.out.print("\t\t" + item));

			// prints as one string
			String output =
					"\n================================================" +
					String.format("%nTimestamp %.1f%n", getTimestamp()) +
					String.format("\t%s%s broadcasts:%s%n", HEADER_COLOR, clientName, RESET) +
					outstandingCollection.stream()
							.map(item -> "\t\t" + item)
							.collect(Collectors.joining("")) +
							"================================================\n\n";
			System.out.print("\n"+output+"\n");



			msg.setObject((Serializable) this.outstandingCollection);
			connection.multicast(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A simple help-interface to show the user in the terminal
	 */
	private void displayHelp() {
		System.out.println("\n==== [ Command Help ] ====");
		System.out.println("getquickbalance          - Get the current balance (quick, may be outdated)");
		System.out.println("getsyncedbalance         - Get the synchronized balance from all replicas");
		System.out.println("deposit <amount>         - Deposit the specified amount into the account");
		System.out.println("addinterest <percent>    - Add interest to the account based on the given percentage");
		System.out.println("gethistory               - Show transaction history");
		System.out.println("checktxstatus <uniqueId> - Check the status of a transaction by its unique ID");
		System.out.println("cleanhistory             - Clear all executed transaction history");
		System.out.println("memberinfo               - Displays members of current connection-session");
		System.out.println("sleep <duration>         - Pause the client for the specified duration in seconds");
		System.out.println("help                     - Display this help menu");
		System.out.println("exit                     - Exit the application");
		System.out.println("clear                    - Clears the screen");
		System.out.println("=============================");
	}


	/**
	 * Given a command and its arguments, execute the correct one through switch-case
	 *
	 * @param command command to be executed
	 * @param args args to provide with command
	 */
	private void executeCommand(String command, String[] args) {
		try {
			switch (command) {
				case "getquickbalance":
					getQuickBalance(false, false);
					break;

				case "getsyncedbalance":
					getSyncedBalance();
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
//					String uniqueId = (args[1] + " " + args[2]); // <-- uten 'handleFileTest'
					String uniqueId = handleFileTest(args);
					String status = checkTxStatus(uniqueId);
					System.out.printf("[Status for %s] >> %s\n", uniqueId, status);
					break;

				case "cleanhistory":
					cleanHistory();
					break;

				case "memberinfo":
					List<String> members = memberInfo();
					printMemberInfo(members);
					break;

				case "sleep":
					int duration = Integer.parseInt(args[1]);
					sleep(duration);
					break;

				case "exit":
					exit();
					break;

				case "clear":
					System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
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
	 * @return current timestamp in seconds
	 */
	private double getTimestamp() {
		if (startTime == null) startTime = new Date();
		return (new Date().getTime() - startTime.getTime()) / 1000.0;
	}
	
	/**
	 * Supports non-edited 'examples.txt' containing "... add transaction ID of ..."
	 * (makes it possible to run raw unedited input-files)
	 *
	 * @param args argument of user-input
	 * @return the uniqueId requested
	 */
	private String handleFileTest(String[] args) {

		// case 1: user-specified transaction ID
		if (args.length == 3) {
			return (args[1] + " " + args[2]);
		}

		// case 2: requested from example-file

		// assuming always end in command>
		String arg = args[args.length - 2]+".0";
		String command = args[args.length - 3].toLowerCase();
		String query = command + " " + arg;

		// check both queued and executed lists for requested command
		String id = null;
		for (Transaction tx : outstandingCollection) {
			if (tx.command.equals(query)) {
				id = tx.uniqueId;
				break;
			}
		}
		for (Transaction tx : executedList) {
			if (tx.command.equals(query)) {
				id = tx.uniqueId;
				break;
			}
		}

		return id;
	}

	/**
	 * Gets closest possible command to given input using levensthein distance algorithm
	 *
	 * @param input requested command
	 * @return null or closest command
	 */
	private String getClosestCommand(String input) {
		String[] commands = {
				"getquickbalance", "getsyncedbalance", "deposit", "addinterest", "gethistory",
				"checktxstatus", "memberinfo", "cleanhistory", "sleep", "help", "clear", "exit"
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

	/**
	 * Given a command and its amount, add it to future broadcasting list
	 *
	 * @param command command to be broadcast
	 * @param amount amount to be provided with command
	 * @param exclude exclude incrementations
	 */
	private void addCommandToCollection(String command, double amount, boolean exclude) {

		// Transaction-object associated with command
		Transaction tx = new Transaction();
		tx.timestamp = getTimestamp();;
		tx.command = (command + " " + amount);
		tx.uniqueId = (clientName + " " + outstanding_counter);

		outstandingCollection.add(tx);
		if (!exclude) outstanding_counter++;
	}

	@Override
	public void getQuickBalance(boolean format, boolean synced) {
		if (format && !usingFile) System.out.println("\n");
		System.out.printf("[%sBalance] >> %.2f\n", synced ? "(synced) " : "", balance);
		if (format && !usingFile) System.out.print("\n> ");
	}

	/**
	 * Naive implementation - waits for outstanding collection to be empty
	 * @return balance
	 */
	 /*
	 @Override
	 public void getSyncedBalance(){
		while (!outstandingCollection.isEmpty()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
				System.err.println("Sync balance interrupted");
				getQuickBalance(false, true );
			}
		}
		getQuickBalance(false, true);
	}
	*/
	
	/**
	 * Prints out synchronized balance (later) by adding the command to broadcasting list
	 */
	@Override
	public void getSyncedBalance() {
		System.out.println("(balance will be presented shortly)");
		addCommandToCollection("getsyncedbalance", 0.0, false);
	}

	/**
	 * Deposit given amount to all replicas
	 *
	 * @param amount the amount to be deposited.
	 */
	@Override
	public void deposit(double amount) {
		addCommandToCollection("deposit", amount, false);
	}

	/**
	 * Adds given percent interest to all replicas
	 *
	 * @param percent the percentage of interest to be added.
	 */
	@Override
	public void addInterest(double percent) {
		addCommandToCollection("addinterest", percent, false);
	}

	/**
	 * Prints the 'history' of this replica, meaning its executing and outstanding ist
	 */
	@Override
	public void getHistory() {
		int commandWidth = 20;

		System.out.println("======================================");
		System.out.println(HEADER_COLOR + "\n[ Executed Transactions ]" + RESET);

		// goes through executed queries
		for (int i = 0; i < executedList.size(); i++) {
			Transaction tx = executedList.get(i);
			System.out.printf("%d.\t%-" + commandWidth + "s\t%s\n", (i + 1), tx.command, tx.uniqueId);
		}

		// goes through queries in queue
		System.out.println(HEADER_COLOR + "\n[ Outstanding Transactions ]" + RESET);
		int i = 0;
		for (Transaction tx : outstandingCollection) {
			System.out.printf("%d.\t%-" + commandWidth + "s\t%s\n", (++i), tx.command, tx.uniqueId);
		}
		System.out.println("\n======================================");
	}

	/**
	 * Given an ID, check its current status
	 *
	 * @param uniqueId the unique identifier of the transaction.
	 * @return status in string-form
	 */
	@Override
	public String checkTxStatus(String uniqueId) {

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

	/**
	 * Cleans history by emptying executed list
	 */
	@Override
	public void cleanHistory() {
		executedList.clear();
	}

	/**
	 * Prints all members of current connection
	 *
	 * @return List of members
	 */
	@Override
	public List<String> memberInfo() {

		//Takes list of members from listener and builds a pretty print
		int x =0;
		List<String> members = new ArrayList<>();
		for (SpreadGroup member : this.listener.groupMembers) {
			x++;
			String[] seperateId = member.toString().split("#");
			String memberPrint= String.format("Rep%d: ID = %s",x, seperateId[1]);
			members.add(memberPrint);

		}
		return members;
	}

	/**
	 * takes list of members and prints them
	 *
	 * @param memberInfo list of members
	 */
	public void printMemberInfo(List<String> memberInfo){
		System.out.printf("===========%s[ Spreadgroup %s Members ]%s===========\n\n", HEADER_COLOR, accountName, RESET);
		for (String memberString : memberInfo) {
			System.out.println(memberString);
		}
		System.out.println("\n====================================================");

	}

	/**
	 * Sleeps for given duration
	 *
	 * @param duration the duration to sleep.
	 */
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
			sleep(1);
			System.exit(0);
		}
	}

	/**
	 * Removes transaction from outstandingCollection
	 * (Inspired by the wisdom of Selleban™)
	 *
	 * @param tx the transaction object
	 */
	public void removeFromOutstanding(Transaction tx) {
		outstandingCollection.removeIf( e ->
				e.uniqueId.equals(tx.uniqueId) &&
						e.command.equals(tx.command));
	}

	/**
	 * Adds balance from given transaction to this account
	 *
	 * @param tx Transaction with command and amount
	 * @param interest if interest or not
	 */
	public void addToAccount(Transaction tx, boolean interest) {
		double amount = Double.parseDouble(tx.command.split(" ")[1]);

		if (interest) {
			this.balance *= (1 + amount/100);
		}
		else {
			this.balance += amount;
		}

		// removes from outstanding, adds to executed
		removeFromOutstanding(tx);
		this.executedList.add(tx);
		order_counter++;
	}

	/**
	 * Creates and sends a multicast request for the latest balance
	 */
	public void requestLatest() {

		// creates Transaction to send request
		Collection<Transaction> askReq = new ArrayList<>();
		Transaction tx = new Transaction();
		tx.timestamp = getTimestamp();;
		tx.command = ("askLatest" + " " + 0.0);
		tx.uniqueId = (clientName + " " + "-1");

		askReq.add(tx);

		broadcastSyncOutstanding(askReq);
	}

	/**
	 * Creates and sends the latest balance
	 */
	public void sendLatest() {

		// creates transaction to send latest
		Collection<Transaction> sendLatest= new ArrayList<>();
		Transaction tx = new Transaction();
		tx.timestamp = getTimestamp();
		tx.command = ("sendLatest" + " " + this.balance);
		tx.uniqueId = (clientName + " " + "-1");

		sendLatest.add(tx);

		broadcastSyncOutstanding(sendLatest);
		setSyncModeFalse();
	}

	/**
	 * Sets synchronization mode of this Client to true
	 */
	synchronized void  setSyncModeTrue(){
		this.syncing = true;

		System.out.printf("\nRep %d entering syncing mode\n" , clientnr);
	}

	/**
	 * Sets synchronization mode of this Client to false
	 */
	synchronized void  setSyncModeFalse(){
		this.syncing = false;
		System.out.printf("\nRep %d exiting syncing mode\n" , clientnr);
	}

	/**
	 * Given a balance, set current balance to the latest given
	 *
	 * @param tx Transaction-object containing latest balance
	 */
	public void setBalance(Transaction tx) {
		this.balance = Double.parseDouble(tx.command.split(" ")[1]);
		System.out.printf("%n== Rep%d is synchronized ==%n%n", clientnr);
	}
}
