package ass1.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import ass1.server.CityInfo;

public class Server implements ServerInterface {

    /** Global variables */

    // Hashmap with city data
    private HashMap<String, HashMap<String, CityInfo>> data;
    private int zone;
    private int port;

    // Cache with capacity of 150 entries
    private LinkedHashMap<String, Integer> cache;
    private static final int CACHE_SIZE = 150;

    public Server(int zone, int port, HashMap<String, HashMap<String, CityInfo>> data) {
        this.zone = zone;
        this.port = port;
        this.data = data;

        // Initialize the cache with LRU eviction policy
        this.cache = new LinkedHashMap<String, Integer>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > CACHE_SIZE;
            }
        };

        startServer();
    }

    /**
     * Method that starts this current server Exporting server with stub, binding to
     * registry
     */
    private void startServer() {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.createRegistry(port);

            // export server to registry
            ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(this, port);

            // define server name same with port
            String serverName = "server" + zone;

            // bind server to registry
            registry.bind(serverName, serverStub);

            System.out.printf("%s:%d has started\n", serverName, port);

        } catch (Exception e) {
            System.err.println();
        }
    }

    /**
     * Sleeps 'ms' milliseconds
     *
     * @param ms : integer, miliseconds to sleep
     */
    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Utility method for cache lookup and storage
    private Integer getFromCacheOrCompute(String cacheKey, Supplier<Integer> computation) {
        // Check if the result is already cached
        if (cache.containsKey(cacheKey)) {
            System.out.println("Cache hit for: " + cacheKey);
            return cache.get(cacheKey);
        }

        // If not cached, compute the result
        Integer result = computation.get();

        // Store the result in the cache
        cache.put(cacheKey, result);

        return result;
    }

    /** RMI methods */
    @Override
    public int Add(int num1, int num2) {
        return num1 + num2;
    }

    // given a country name as input, return population of country by summing
    // population of cities in that country
    @Override
    public int getPopulationofCountry(String countryName) {
        return getFromCacheOrCompute("getPopulationofCountry:" + countryName, () -> {
            System.out.printf("Server%d:%d calling 'getPopulationofCountry'\n", zone, port);

            HashMap<String, CityInfo> country = data.get(countryName);
            if (country == null)
                return 0;

            int totalPopulation = country.values().stream().mapToInt(city -> city.population).sum();
            sleep(80); // network latency
            return totalPopulation;
        });
    }

    // given a country name and min as input, return total number of cities in
    // given country containing at least "min" population
    @Override
    public int getNumberofCities(String countryName, int min) {
        return getFromCacheOrCompute("getNumberofCities:" + countryName + ":" + min, () -> {
            System.out.printf("Server%d:%d calling 'getNumberofCities'\n", zone, port);

            HashMap<String, CityInfo> country = data.get(countryName);
            if (country == null)
                return 0;

            long count = country.values().stream().filter(city -> city.population >= min).count();
            sleep(80); // network latency
            return (int) count;
        });
    }

    // returns number of countries with min "citycount" cities, and population at
    // least "minpopulation"
    @Override
    public int getNumberofCountries(int citycount, int minpopulation) {
        return getFromCacheOrCompute("getNumberofCountries:" + citycount + ":" + minpopulation, () -> {
            System.out.printf("Server%d:%d calling 'getNumberofCountries'\n", zone, port);

            long count = data.values().stream()
                    .filter(cities -> cities.values().stream()
                            .filter(city -> city.population >= minpopulation).count() >= citycount)
                    .count();

            sleep(80); // network latency
            return (int) count;
        });
    }

    // returns number of countries containing at least "citycount" number of cities
    // each included city has a population between min and max population
    @Override
    public int getNumberofCountries(int citycount, int minpopulation, int maxpopulation) {
        return getFromCacheOrCompute(
                "getNumberofCountries:" + citycount + ":" + minpopulation + ":" + maxpopulation, () -> {
                    System.out.printf("Server%d:%d calling 'getNumberofCountries'\n", zone, port);

                    long count = data.values().stream()
                            .filter(cities -> cities.values().stream()
                                    .filter(city -> city.population >= minpopulation
                                            && city.population <= maxpopulation)
                                    .count() >= citycount)
                            .count();

                    sleep(80); // network latency
                    return (int) count;
                });
    }

    // Method to save cache to a file
    public void saveCacheToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("server_cache.txt"))) {
            for (Map.Entry<String, Integer> entry : cache.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
            System.out.println("Cache saved to server_cache.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
