package ass1.server;

import ass1.data.CityInfo;
import ass1.proxy.ProxyServer;

import java.io.*;
import java.util.*;


public class ServerSimulator {

    private static ServerInterface[] servers;

    private static ProxyServer proxy;
    private static final int BASE_PORT = 1099;
    private static final String filepath = "info/exercise_1_dataset.csv";


    /**
     * Goes through and parses data from a given file to a hashmap, later used by server
     *
     * @return map filled hashmap with data
     */
    private static HashMap<String, HashMap<String, CityInfo>> parseData()
    {
        HashMap<String, HashMap<String, CityInfo>> countryMap = new HashMap<>();

        InputStream inputStream = ServerSimulator.class.getClassLoader().getResourceAsStream(filepath);
        if (inputStream == null) {
            System.err.printf("File '%s' not found!\n", filepath);
            return countryMap;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
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
     * Creates the proxy to be used
     *
     * @param port base-port used
     */
    private static void createProxy(int port)
    {
        proxy = new ProxyServer(port - 1);
    }


    /**
     * Creates servers which is started within
     *
     * @param numServers how many servers to start
     * @param port base-port used
     * @param dataset data-set that servers use
     */
	private static void createServers(int numServers, int port, HashMap<String, HashMap<String, CityInfo>> dataset, boolean usingCache)
    {
        servers = new ServerInterface[numServers];

        // Create servers
        for (int i = 1; i <= numServers; i++) {
            Server server = new Server(i, port + i, dataset, usingCache);
            servers[i-1] = server;
            proxy.registerServer(i, server);

        }
    }


    public static void main(String[] args)
    {
        // Parse dataset.csv
        HashMap<String, HashMap<String, CityInfo>> dataset = parseData();

        // how many servers to run at once
        int numDevices = 5;

        // main port used
        int port = BASE_PORT;

        // using intenral cache or not
        boolean usingCache = Boolean.parseBoolean(args[0]);

        // start server and proxy
        createProxy(port);
        createServers(numDevices, port, dataset, usingCache);
    }
}
