package ass1.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import ass1.server.ProxyClientInterface;
import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;

public class Client {

    // Hashmap with city data
    private static ArrayList<InstructionInfo> instructions;
    private int port;

    // Cache to store results, max size of 45
    private static final int CACHE_SIZE = 45;
    private LinkedHashMap<String, Integer> cache;

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

    public int handleRequest(String function, List<String> args, ServerInterface server) throws RemoteException {

        // process requested function
        if (args.size() > 0) {
            String country;

            // try-catch for handling wrong entries in instruction-file
            try {
                switch (function) {
                    case "getPopulationofCountry":
                        country = args.get(0);
                        return server.getPopulationofCountry(country);

                    case "getNumberofCities":
                        country = args.get(0);
                        int min = Integer.parseInt(args.get(1));
                        return server.getNumberofCities(country, min);

                    case "getNumberofCountries":

                        // min boundary
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
            } catch (Exception ignore) {}
        } return 0;
    }

    private void invokeRequests(ProxyClientInterface proxy) {
        System.out.println("Printing all requests to be invoked: \n");

        long startTime = System.nanoTime();

        // go through requests one at a time
        for (InstructionInfo instruc : instructions) {

            // extract function-info
            String function = instruc.function;
            List<String> args = instruc.args;
            int zone = instruc.zone;

            String cacheKey = generateCacheKey(function, args, zone);

            // check if request has been done before
            if (cache.containsKey(cacheKey)) {
                System.out.printf("[C%d] Cache hit for function %s(%s): %d\n", zone, function, args, cache.get(cacheKey));
                continue;
            }

            // 1. ask for server
            try {
                String serverInfo = proxy.requestServer(zone);

                
                
                // 2. connect to the server - info on the form "server#:#port#:#"
                String[] addressParts = serverInfo.split(":");
                String host = addressParts[0];
                System.out.println("Requesting zone: " + zone);
                System.out.println("Request handled by "+ host);

                int serverPort = Integer.parseInt(addressParts[1]);

                //NOTE: realistically we use 'host' and 'port' to find the server on the network
                // in our case, we test locally and therefore use "localhost"

                // lookup given address and port
                Registry serverRegistry = LocateRegistry.getRegistry("localhost", serverPort);
                ServerInterface server = (ServerInterface) serverRegistry.lookup("server" + zone);

                // Process request and cache result
                int result = handleRequest(function, args, server);
                cache.put(cacheKey, result);
                System.out.printf("[C%d] Result for function %s(%s): %d\n", zone, function, args, result);

            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Convert the duration into minutes
        double durationInMinutes = (double) duration / 1_000_000_000.0 / 60.0;

        System.out.println("Finished invoking instructions, Time = " + String.format("%.3f", durationInMinutes) + " minutes ");
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
