package ass1.client;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import ass1.proxy.ProxyClientInterface;
import ass1.server.ServerInterface;
import ass1.data.InstructionInfo;

public class Client {

    // Hashmap with city data
    private static ArrayList<InstructionInfo> instructions;
    private int port;


    // Cache to store results, max size of 45
    private static final int CACHE_SIZE = 45;
    private LinkedHashMap<String, Integer> cache;


    // keeping track of average times
    private Map<String, long[]> methodTimings = new HashMap<>();
    private Map<String, Integer> methodCounts = new HashMap<>();


    // filespaths
    private final String NAIVE_FILEPATH = "output/results/naive_server.txt";



    public Client(int port, ArrayList<InstructionInfo> instructions) {
        Client.instructions = instructions;
        this.port = port;

        // Initialize cache with LRU eviction policy
        this.cache = new LinkedHashMap<String, Integer>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > CACHE_SIZE;
            }
        };

        startClient();
    }

    /**
     * Sleeps 'ms' milliseconds
     *
     * @param ms integer, miliseconds to sleep
     */
    public void sleep(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Generates a unique cache key for a given request-function
     *
     * @param function function name
     * @param args its arguments
     * @param zone zone requested
     * @return unique key
     */
    private static String generateCacheKey(String function, List<String> args, int zone) {
        return function + args.toString() + zone;
    }

    /**
     * Invokes given function to a given server
     *
     * @param function function to invoke
     * @param args its arguments
     * @param server server to compute function
     * @return result or 0
     * @throws RemoteException if RMI fails
     */
    public int handleRequest(String function, List<String> args, ServerInterface server) throws RemoteException
    {
        // process requested function
        if (args.size() > 0) {
            String country;

            // try-catch for handling wrong entries in instruction-file
            try {
                switch (function) {
                    case "getPopulationofCountry":
                        if (args.size() == 1) {
                            country = args.get(0);
                            return server.getPopulationofCountry(country);
                        }

                    case "getNumberofCities":
                        if (args.size() == 2) {
                            country = args.get(0);
                            int min = Integer.parseInt(args.get(1));
                            return server.getNumberofCities(country, min);
                        }

                    case "getNumberofCountries":
                        if (args.size() == 2) {
                            int cityCount = Integer.parseInt(args.get(0));
                            int minPopulation = Integer.parseInt(args.get(1));
                            return server.getNumberofCountries(cityCount, minPopulation);
                        }

                        // min and max boundaries
                        else if (args.size() == 3) {
                            int cityCOunt = Integer.parseInt(args.get(0));
                            int minPopulation = Integer.parseInt(args.get(1));
                            int maxPopulation = Integer.parseInt(args.get(2));
                            return server.getNumberofCountries(cityCOunt, minPopulation, maxPopulation);
                        } else {
                            throw new RemoteException("Invalid number of arguments for 'getNumberofCountries'");
                        }
                    default:
                        throw new RemoteException("Unknown function: " + function);
                }
            } catch (Exception ignored) {
                // we ignore errors in requests due to a lot of errors in the input-text-file
            }
        }
        return 0;
    }

    private void appendAveragesToFile()
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NAIVE_FILEPATH, true));
             PrintWriter out = new PrintWriter(writer)) {

            out.println("\n[ === [ AVERAGES PER METHOD ] === \n");

            for (Map.Entry<String, long[]> entry : methodTimings.entrySet()) {
                String methodName = entry.getKey();
                long[] totals = entry.getValue();
                int count = methodCounts.get(methodName);

                // calculates averages
                long avgTurnaround = totals[0] / count;
                long avgExecution = totals[1] / count;
                long avgWaiting = totals[2] / count;
                long minTurnaround = totals[3];
                long maxTurnaround = totals[4];

                // writes average of function
                String averageLine = String.format("%s avg turn-around time: %d ms, avg execution time: %d ms, avg waiting time: %d ms, min turn-around time: %d ms, max turn-around time: %d ms\n",
                        methodName, avgTurnaround, avgExecution, avgWaiting, minTurnaround, maxTurnaround);

                out.print(averageLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMethodStats(String methodName, long[] timing)
    {
        long[] totals = methodTimings.getOrDefault(methodName, new long[]{0, 0, 0, Long.MAX_VALUE, Long.MIN_VALUE});
        int count = methodCounts.getOrDefault(methodName, 0);

        // Update totals and count
        totals[0] += timing[0]; // Total turnaround time
        totals[1] += timing[1]; // Total execution time
        totals[2] += timing[2]; // Total waiting time

        // Update min/max turnaround times
        totals[3] = Math.min(totals[3], timing[0]); // minTurnaround
        totals[4] = Math.max(totals[4], timing[0]); // maxTurnaround

        methodTimings.put(methodName, totals);
        methodCounts.put(methodName, count + 1);
    }

    /**
     * Writes a line to a file containing query information
     *
     * @param instruc instruction queried
     * @param result result acquired
     * @param zone zone processed from
     * @param timing [turnaround, execution, waiting]
     */
   private void saveResult(InstructionInfo instruc, int result, int zone, long[] timing)
    {
        long turnaround = timing[0];
        long execution = timing[1];
        long waiting = timing[2];
       
        // updates local timing-counter 
        String fullQuery =" ";
        
        if (instruc.function.equals("getNumberofCountries")){
            
            if (instruc.args.size()==2 ){   
                instruc.function = "getNumberofCountriesMin";
            } else if ( instruc.args.size() ==3){
                instruc.function = "getNumberofCountriesMinMax";
            }
              
            updateMethodStats(instruc.function, timing);  
            // query-string
            fullQuery = String.format("%d %s (turnaround time: %d ms, execution time: %d ms, waiting time: %d, processed by server %d)\n",
            result, instruc, turnaround, execution, waiting, zone);
            // print to terminal
            System.out.printf(fullQuery);
            
            
        } else {
            updateMethodStats(instruc.function, timing);
    
            // query-string
            fullQuery = String.format("%d %s (turnaround time: %d ms, execution time: %d ms, waiting time: %d, processed by server %d)\n",
                                result, instruc, turnaround, execution, waiting, zone);
    
            // print to terminal
            System.out.printf(fullQuery);
        }
        

        // ensures directory exists
        File resultsDir = new File("output/results");
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        // print to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(NAIVE_FILEPATH, true));
             PrintWriter out = new PrintWriter(writer)) {
            out.print(fullQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Goes through all previously parsed instructions and invokes request from a given server
     *
     * @param proxy proxy that will find server to be used
     */
    private void invokeRequests(ProxyClientInterface proxy, int lineReadingDelay) {
        for (InstructionInfo instruc : instructions) {

            // T = 50 or T = 20 input line reading delay
            sleep(lineReadingDelay);

            // extract request info
            String function = instruc.function;
            List<String> args = instruc.args;
            int zone = instruc.zone;

            // key for lookup in local cache
            String cacheKey = generateCacheKey(function, args, zone);

            long startTurnaround = System.currentTimeMillis();

            // if request is cached, retrieve it!
            if (cache.containsKey(cacheKey)) {
                int result = cache.get(cacheKey);
                saveResult(instruc, result, zone, new long[]{0, 0, 0});  // <-- result in cache? Time is "instant"
            }
            // if not, ask for server to compute it
            else {
                try {
                    // asks proxy for server, proxy gives appropriate server
                    String serverInfo = proxy.requestServer(zone);

                    // error handling
                    if (serverInfo == null) continue;

                    // extracted data from
                    String[] addressParts = serverInfo.split(":");
                    String host = addressParts[0];
                    int serverPort = Integer.parseInt(addressParts[1]);
                    int resultZone = Integer.parseInt(addressParts[2]);

                    // lookup server to use
                    Registry serverRegistry = LocateRegistry.getRegistry("127.0.0.1", serverPort);
                    ServerInterface server = (ServerInterface) serverRegistry.lookup(host);

                    // start execution timer,  extract response-result
                    long startExecutionTime = System.currentTimeMillis();
                    int result = handleRequest(function, args, server);

                    // end and save timers
                    long executionTime =  (System.currentTimeMillis() - startExecutionTime);
                    long turnaroundTime = (System.currentTimeMillis() - startTurnaround);
                    long waitingTime = (turnaroundTime - executionTime);

                    // cache request
                    cache.put(cacheKey, result);

                    // saves result and timing to file
                    saveResult(instruc, result, resultZone, new long[]{turnaroundTime, executionTime, waitingTime});

                } catch (RemoteException | NotBoundException | NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        appendAveragesToFile();
    }

    /**
     * Starts the client by locating the proxy and invoking requests
     */
    public void startClient() {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);  // <- 1098 is proxy-port

            // Lookup the proxy
            ProxyClientInterface proxy = (ProxyClientInterface) registry.lookup("proxy");

            System.out.printf("Client:%d has started\n", port);

            int LINE_DELAY = 20; // T = 20 / 50

            // Invoke requests from instruction-set
            invokeRequests(proxy, LINE_DELAY);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
