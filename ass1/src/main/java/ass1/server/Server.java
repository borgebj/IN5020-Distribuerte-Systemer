package ass1.server;

import ass1.proxy.ProxyServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

public class Server implements ServerInterface, ProxyServerInterface {

    /** Global variables */

    // Hashmap with city data
    private HashMap<String, HashMap<String, CityInfo>> data;
    private String serverName;
    private int zone;
    private int port;

    // Cache with capacity of 150 entries
    private LinkedHashMap<String, Integer> cache;
    private static final int CACHE_SIZE = 150;

    // request queue with FIFO policy
    private final Queue<String> requestQueue;


    public Server(int zone, int port, HashMap<String, HashMap<String, CityInfo>> data)
    {
        this.zone = zone;
        this.port = port;
        this.data = data;
        this.serverName = "server"+zone;

        // Initialize the cache with LRU eviction policy
        this.cache = new LinkedHashMap<String, Integer>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > CACHE_SIZE;
            }
        };

        this.requestQueue = new LinkedBlockingQueue<>();

        startServer();
    }

    /**
     * @return hostname of the server
     */
    public String getHost()
    {
        return serverName;
    }

    /**
     * @return port of the server
     */
    public int getPort()
    {
        return port;
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
     * Checks local cache if request has been done before, either compute or get
     *
     * @param cacheKey key identifying request
     * @param computation supplier with computed value
     * @return result in cache or computation
     */
    private int getFromCacheOrCompute(String cacheKey, Supplier<Integer> computation)
    {
        requestQueue.add(cacheKey);

        // Check if the result is already cached
        if (cache.containsKey(cacheKey)) {
            System.out.println("Cache hit for: " + cacheKey);
            return cache.get(cacheKey);
        }

        // If not cached, compute the result, store it
        Integer result = computation.get();
        cache.put(cacheKey, result);

        return result;
    }

    /** RMI methods **/

    /**
     * Returns population of country given
     *
     * @param countryName : country to look at
     * @return population of country and timing
     */
    @Override
    public int getPopulationofCountry(String countryName)
    {
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

    /** Returns total cities in a given country with minimum population given
     *
     * @param countryName country to look at
     * @param min minimum population boundary
     * @return total number of cities within boundary and timing
     */
    @Override
    public int getNumberofCities(String countryName, int min)
    {
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

    /**
     * Returns number of countries with minimum citycount and minimum population
     *
     * @param citycount minimum city boundary
     * @param minpopulation minimum population boundary
     * @return number of countries and timing
     */
    @Override
    public int getNumberofCountries(int citycount, int minpopulation)
    {
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

    /**
     * Returns  number of countries containing at least citycount cities where each city has a population between min and max
     *
     * @param citycount minimum city boundary
     * @param minpopulation minimum population boundary
     * @param maxpopulation maximum populalation boundary
     * @return number of countries and timing
     */
    @Override
    public int getNumberofCountries(int citycount, int minpopulation, int maxpopulation)
    {
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

    @Override
    public int fetchWorkload() throws RemoteException {
        System.out.printf("Proxy requested workload (%d)\n", requestQueue.size());
        return requestQueue.size();
    }

    @Override
    public String toString()
    {
        return (getHost() + ":" + getPort());
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

            // bind server to registry
            registry.bind(serverName, serverStub);

            System.out.printf("%s:%d has started\n", serverName, port);

        } catch (Exception e) {
            System.err.println();
        }
    }
}
