package ass1.server;

import ass1.client.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class ServerSimulator {

    /**
     * Goes through and parses data from a given file to a hashmap, later used by server
     *
     * @return map : filled hashmap with data
     */
    private static HashMap<String, HashMap<String, CityInfo>> parseData() {
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

    public static void main(String[] args) {
        // Parse dataset.csv
        HashMap<String, HashMap<String, CityInfo>> dataset = parseData();

        // Create an array to hold server instances
        ServerInterface[] servers = new Server[5];

        // test
        Server s = new Server(dataset);
        Client c = new Client();

        try {
            for (int i = 0; i < 1; i++) {
                int port = 1099 + i;

                // Create a new registry on the unique port
                Registry registry = LocateRegistry.createRegistry(port);

                // Create and export a new server instance
                Server server = new Server(dataset);
                ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(server, port);

                // Bind the serverStub with a unique name in the registry
                String serverName = "server_" + i;

            
                registry.bind(serverName, serverStub);

                // Store reference to the server instance
                servers[i] = server;

                System.out.println("Staring server: " + serverName + "\n");

                // Print information about each server (for demonstration purposes)

                /*
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
                    */
                }
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
