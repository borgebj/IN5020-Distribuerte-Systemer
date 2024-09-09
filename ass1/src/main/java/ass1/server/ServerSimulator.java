package ass1.server;

import ass1.client.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;


public class ServerSimulator {

    private static ServerInterface[] servers;

    private static Proxy proxy;

    /**
     * Goes through and parses data from a given file to a hashmap, later used by server
     *
     * @return map : filled hashmap with data
     */
    private static HashMap<String, HashMap<String, CityInfo>> parseData()
    {
        HashMap<String, HashMap<String, CityInfo>> countryMap = new HashMap<>();

        File file = new File("ass1/info/exercise_1_dataset.csv");
        if (!file.exists()) {
            System.err.println("File not found: " + "ass1/info/exercise_1_dataset.csv");
            return countryMap; // Return empty map if file is not found
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");

                // create city-info
                CityInfo city = new CityInfo(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        parts[2],
                        parts[3],
                        Integer.parseInt(parts[4]),
                        parts[5],
                        parts[6]
                );

                HashMap<String, CityInfo> cityMap = countryMap.computeIfAbsent(city.countryName, k -> new HashMap<>());
                cityMap.put(city.name, city);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return countryMap;
    }

    /**
     * Goes through and parses instructions from a given file to a hashmap, later used by clients
     *
     * @return map : filled hashmap with instructions
     */
    private static ArrayList<InstructionInfo> parseInstructions()
    {
        ArrayList<InstructionInfo> instructions = new ArrayList<>();

        File file = new File("ass1/info/exercise_1_input.txt");
        if (!file.exists()) {
            System.err.println("File not found: " + "ass1/info/exercise_1_input.txt");
            return instructions; // Return empty list if file is not found
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" "); // <- | function | arg1 | arg2 | arg3 | zone+

                // parse method name and zone
                String function = parts[0];
                String zonePart = parts[parts.length - 1];
                int zone = Integer.parseInt(zonePart.split(":")[1]); // Extract the number after "Zone:"

                // parse arguments
                List<String> args = new ArrayList<>();
                for (int i = 1; i < parts.length - 1; i++) {
                    args.add(parts[i]);
                }

                InstructionInfo info = new InstructionInfo(function, args, zone);
                instructions.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instructions;
    }

	private static void createServers(int numServers, int port, HashMap<String, HashMap<String, CityInfo>> dataset)
    {
        servers = new ServerInterface[numServers];
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.createRegistry(port);

            // Create and export a new server instances
            for (int i = 0; i < numServers; i++) {
                servers[i] = new Server(registry, i, port + i, dataset);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void createProxy(int numServers, int port)
    {
        proxy = new Proxy(numServers, port + numServers + 1);
    }

    public static void main(String[] args)
    {
        // Parse dataset.csv
        HashMap<String, HashMap<String, CityInfo>> dataset = parseData();

        // Parse instructions
        ArrayList<InstructionInfo> instructions = parseInstructions();

        // how many servers to run at once
        int numServers = 5;

        // main port used
        int port  = 1099;

        // start server and proxy
        createServers(numServers, port, dataset);
        createProxy(numServers, port);
    }
}
