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
    private static ArrayList<InstructionInfo> parseInstructions() {
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

	private static void createServers(HashMap<String, HashMap<String, CityInfo>> dataset, int numServers, int port)
    {
        servers = new ServerInterface[numServers];
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.createRegistry(port);

            for (int i = 0; i < numServers; i++) {

                // Create and export a new server instance
                servers[i] = new Server(registry, i, port + i, dataset);

                // Print information about each server
                int norwayPop = servers[i].getPopulationofCountry("Sweden");
                int nocities = servers[i].getNumberofCities("Norway", 100000);
                int nocitieCountPop = servers[i].getNumberofCountries(2, 5000000);
                int nocitiesBetween = servers[i].getNumberofCountries(30, 100000, 800000);

                System.out.printf(
                        "Server %d:\n" +
                                "  getPopulationofCountry('Sweden') = %d\n" +
                                "  getNumberofCities('Norway', 100000) = %d\n" +
                                "  getNumberofCountries(2, 5000000) = %d\n" +
                                "  getNumberofCountries(30, 100000, 800000) = %d\n",
                        i, norwayPop, nocities, nocitieCountPop, nocitiesBetween);
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

        createServers(dataset, numServers, port);
        createProxy(numServers, port);
    }
}
