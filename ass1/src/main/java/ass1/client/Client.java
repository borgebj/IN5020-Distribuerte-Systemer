package ass1.client;

import java.io.BufferedWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import ass1.server.ProxyClientInterface;
import ass1.server.Response;
import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;

public class Client {

    // Hashmap with city data
    private static ArrayList<InstructionInfo> instructions;
    private int port;

    // Cache to store results, max size of 45
    private static final int CACHE_SIZE = 45;
    private LinkedHashMap<String, Integer> cache;
    BufferedWriter writer; 



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

    // generates a unique key for each request
    private static String generateCacheKey(String function, List<String> args, int zone) {
        return function + args.toString() + zone;
    }

    public Response handleRequest(String function, List<String> args, ServerInterface server) throws RemoteException
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
        return null;
    }

    private void saveResult(InstructionInfo instruc, int result, int zone, long[] timing)
    {
        // TODO
        // overfør output til fil
        long turnaround = timing[0];
        long execution = timing[1];
        long waiting = timing[2];

        String formattedOutputString = String.format("%d %s " +
            "(turnaround time: %d ms, execution time: %d ms, waiting time: %d, " +
            "processed by server %d)\n",
            result, instruc, turnaround, execution, waiting, zone);


        

    
    }

    private void invokeRequests(ProxyClientInterface proxy) {
        for (InstructionInfo instruc : instructions) {
            String function = instruc.function;
            List<String> args = instruc.args;
            int zone = instruc.zone;

            String cacheKey = generateCacheKey(function, args, zone);
            long startTime = System.nanoTime();

            if (cache.containsKey(cacheKey)) {
                int result = cache.get(cacheKey);
                saveResult(instruc, result, zone, new long[]{0, 0, 0});
            } else {
                try {
                    String serverInfo = proxy.requestServer(zone);
                    String[] addressParts = serverInfo.split(":");
                    String host = addressParts[0];
                    int serverPort = Integer.parseInt(addressParts[1]);
                    int resultZone = Character.getNumericValue(host.charAt(host.length() - 1));

                    Registry serverRegistry = LocateRegistry.getRegistry("127.0.0.1", serverPort);
                    ServerInterface server = (ServerInterface) serverRegistry.lookup(host);

                    // extract response




                    Response response = handleRequest(function, args, server);

                    if (response != null) {
                        int result = response.result;
                        int execTime = response.executionTime;
                        int waitTime = response.waitingTime;

                        cache.put(cacheKey, result);

                        long turnaround = (System.nanoTime() - startTime) / 1000000;

                        saveResult(instruc, result, resultZone, new long[]{turnaround, execTime, waitTime});
                    }
                } catch (RemoteException | NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startClient() {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);  // <- 1098 is proxy-port

            // Lookup the proxy
            ProxyClientInterface proxy = (ProxyClientInterface) registry.lookup("proxy");

            System.out.printf("Client:%d has started\n", port);

            // Invoke requests from instruction-set
            invokeRequests(proxy);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
