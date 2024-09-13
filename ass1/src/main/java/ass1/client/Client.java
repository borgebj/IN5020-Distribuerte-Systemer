package ass1.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ass1.server.ProxyInterface;
import ass1.server.InstructionInfo;

public class Client {

    // Hashmap with city data
    private static ArrayList<InstructionInfo> instructions;
    private int zone;
    private int port;

    // Cache to store results, max size of 45
    private static final int CACHE_SIZE = 45;
    private LinkedHashMap<String, Integer> cache;

    public Client(int zone, int port, ArrayList<InstructionInfo> instructions) {
        Client.instructions = instructions;
        this.zone = zone;
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

    private static String generateCacheKey(String function, List<String> args, int zone) {
        // Generates a unique key for each request based on the function, args, and zone
        return function + args.toString() + zone;
    }

    private void saveCacheToFile() {
        // Save cache to a file named "client_cache.txt"
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("client_cache.txt"))) {
            writer.write("Client-Side Cache:\n");
            for (Map.Entry<String, Integer> entry : cache.entrySet()) {
                writer.write(entry.getKey() + " -> " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void invokeRequests(ProxyInterface proxy) {
        System.out.println("Printing all requests to be invoked: \n");

        long startTime = System.nanoTime();

        for (InstructionInfo instruc : instructions) {
            String function = instruc.function;
            List<String> args = instruc.args;
            int zone = instruc.zone;
            String cacheKey = generateCacheKey(function, args, zone);

            // Check if the result is already in the cache
            if (cache.containsKey(cacheKey)) {
                System.out.printf("[C%d] Cache hit for function %s(%s): %d\n", zone, function, args, cache.get(cacheKey));
                continue;
            }

            try {
                // Not in cache, so request from server
                int result = proxy.handleRequest(function, args, zone);

                if (result > 0) {
                    // Store the result in the cache
                    cache.put(cacheKey, result);

                    // Print the result
                    System.out.printf("[C%d] Result for function %s(%s) = %d\n", zone, function, args, result);
                }
            } catch (RemoteException e) {
                System.err.printf("Failed to invoke function %s(%s) for zone %d: %s\n",
                        instruc.function, instruc.args, instruc.zone, e.getMessage());
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Convert the duration into minutes
        double durationInMinutes = (double) duration / 1_000_000_000.0 / 60.0;

        System.out.println("Finished invoking instructions, Time = " + String.format("%.3f", durationInMinutes) + " minutes ");

        // Save cache to file
        saveCacheToFile();
    }

    public void startClient() {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);  // <- 1098 is proxy-port

            // Lookup the proxy
            ProxyInterface proxy = (ProxyInterface) registry.lookup("proxy");

            System.out.printf("Client%d:%d has started\n", zone, port);

            // Invoke requests from instruction-set
            invokeRequests(proxy);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
