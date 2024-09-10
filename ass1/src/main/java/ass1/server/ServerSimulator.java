package ass1.server;

import ass1.client.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;


public class ServerSimulator {

    private static ServerInterface[] servers;
    private static Client[] clients;

    private static Proxy proxy;

    private static final int BASE_PORT = 1099;

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

                List<String> args = new ArrayList<>();

                // combine all elements between
                StringBuilder currentArgs = new StringBuilder();
                for (int i = 1; i < parts.length - 1; i++) {
                    String part = parts[i];

                    // if arg is a number
                    if (part.matches("\\d+")) {
                        if (currentArgs.length() > 0) {
                            args.add(currentArgs.toString());
                            currentArgs.setLength(0);
                        }
                        args.add(part);
                    }
                    // if arg is a string
                    else {
                        if (currentArgs.length() > 0) {
                            currentArgs.append(" ");
                        }
                        currentArgs.append(part);
                    }
                }

                if (currentArgs.length() > 0) {
                    args.add(currentArgs.toString());
                }

                InstructionInfo info = new InstructionInfo(function, args, zone);
                instructions.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instructions;
    }


    /**
     * Creates the proxy to be used
     *
     * @param port : base-port used
     */
    private static void createProxy(int port)
    {
        proxy = new Proxy(port - 1);
    }


    /**
     * Creates servers which is started within
     *
     * @param numServers : how many servers to start
     * @param port : base-port used
     * @param dataset : data-set that servers use
     */
	private static void createServers(int numServers, int port, HashMap<String, HashMap<String, CityInfo>> dataset)
    {
        servers = new ServerInterface[numServers];

        // Create servers
        for (int i = 0; i < numServers; i++) {
            try {
                Server server = new Server(i, port + i, dataset);
                servers[i] = server;
                proxy.registerServer(i, server);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
       }
    }


    /**
     * Creates clients which is started within
     *
     * @param numClients : how many clients to create
     * @param port : base-port used
     * @param instructions : instruction-set that clients use
     */
    private static void createClients(int numClients, int port, ArrayList<InstructionInfo> instructions)
    {
        clients = new Client[numClients];

        // Create clients
        for (int i = 0; i < numClients; i++) {
            clients[i] = new Client(i, port + (numClients + i), instructions);
        }
    }

    public static void main(String[] args)
    {
        // Parse dataset.csv
        HashMap<String, HashMap<String, CityInfo>> dataset = parseData();

        // Parse instructions
        ArrayList<InstructionInfo> instructions = parseInstructions();

        // how many servers to run at once
        int numDevices = 5;

        // main port used
        int port  = BASE_PORT;

        // start server and proxy
        createProxy(port);
        createServers(numDevices, port, dataset);
        createClients(numDevices, port, instructions);
    }
}
