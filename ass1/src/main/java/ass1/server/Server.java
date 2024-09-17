package ass1.server;

import ass1.data.CityInfo;
import ass1.data.Request;
import ass1.proxy.ProxyServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.CountDownLatch;
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
    private final Queue<Request> requestQueue;
    private Thread executioner;

    // latch awaiting first request
    private final CountDownLatch start = new CountDownLatch(1);


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
        startExecutionMode();
    }

    private void startExecutionMode() {
        executioner = new Thread(() -> {
            try {
                // Wait for the first request to be added
                start.await();
                while (true) {
                    try {
                        // pulls out request at start
                        Request request = requestQueue.poll();
                        if (request != null) {
                            System.out.printf("Executor [%d] processing : %s\n", zone, request);
                            processRequest(request);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
        executioner.setDaemon(true); // release the daemon
        executioner.start();
    }

    private void processRequest(Request request)
    {
        String cacheKey = request.getCacheKey();
        int result = getFromCacheOrCompute(cacheKey, request.getComputation());
        request.complete(result);
        System.out.printf("Finished %s = %d\n", request.getCacheKey(), result);
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

    private static String generateCacheKey(String function, Object... args) {
        return function + Arrays.toString(args);
    }


    private void enqueueRequest(Request request)
    {
        // puts in request at end
        if (requestQueue.isEmpty()) {
            start.countDown();
        }
        requestQueue.offer(request);
    }


    /** RMI methods - computed **/

    private int _COMPUTE_getPopulationofCountry(String countryName)
    {
        HashMap<String, CityInfo> country = data.get(countryName);
        if (country == null) return 0;
        return country.values().stream().mapToInt(city -> city.population).sum();
    }
    private int _COMPUTE_getNumberofCities(String countryName, int min)
    {
        HashMap<String, CityInfo> country = data.get(countryName);
        if (country == null) return 0;
        long count = country.values().stream().filter(city -> city.population >= min).count();
        return (int) count;
    }
    private int _COMPUTE_getNumberofCountries(int citycount, int minpopulation)
    {
        long count = data.values().stream()
                .filter(cities -> cities.values().stream()
                        .filter(city -> city.population >= minpopulation).count() >= citycount)
                .count();
        return (int) count;
    }
    private int _COMPUTE_getNumberofCountries(int citycount, int minpopulation, int maxpopulation)
    {
        long count = data.values().stream()
                .filter(cities -> cities.values().stream()
                        .filter(city -> city.population >= minpopulation && city.population <= maxpopulation)
                        .count() >= citycount)
                .count();
        return (int) count;
    }


    /** RMI methods - invoked **/

    /**
     * Returns population of country given
     *
     * @param countryName : country to look at
     * @return population of country and timing
     */
    @Override
    public int getPopulationofCountry(String countryName) throws RemoteException
    {
        // simulate network latency
        sleep(80);

        // creates the request
        Request request = new Request(
                generateCacheKey("getPopulationofCountry", countryName),
                () -> _COMPUTE_getPopulationofCountry(countryName)
        );
        // put it on queue
        enqueueRequest(request);

        // wait for result
        try {
            return request.getResult();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting for result", e);
        }
    }

    /** Returns total cities in a given country with minimum population given
     *
     * @param countryName country to look at
     * @param min minimum population boundary
     * @return total number of cities within boundary and timing
     */
    @Override
    public int getNumberofCities(String countryName, int min) throws RemoteException
    {
        // simulate network latency
        sleep(80);

        // creates the request
        Request request = new Request(
                generateCacheKey("getNumberofCities", countryName, min),
                () -> _COMPUTE_getNumberofCities(countryName, min)
        );
        // put it on queue
        enqueueRequest(request);

        // wait for result
        try {
            return request.getResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting for result", e);
        }
    }

    /**
     * Returns number of countries with minimum citycount and minimum population
     *
     * @param citycount minimum city boundary
     * @param minpopulation minimum population boundary
     * @return number of countries and timing
     */
    @Override
    public int getNumberofCountries(int citycount, int minpopulation) throws RemoteException
    {
        // simulate network latency
        sleep(80);

        // creates the request
        Request request = new Request(
                generateCacheKey("getNumberofCountries", citycount, minpopulation),
                () -> _COMPUTE_getNumberofCountries(citycount, minpopulation)
        );
        // put it on queue
        enqueueRequest(request);

        // wait for result
        try {
            return request.getResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting for result", e);
        }
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
    public int getNumberofCountries(int citycount, int minpopulation, int maxpopulation) throws RemoteException
    {
        // simulate network latency
        sleep(80);

        // creates the request
        Request request = new Request(
                generateCacheKey("getNumberofCountries", citycount, minpopulation, maxpopulation),
                () -> _COMPUTE_getNumberofCountries(citycount, minpopulation, maxpopulation)
        );
        // put it on queue
        enqueueRequest(request);

        // wait for result
        try {
            return request.getResult();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("Interrupted while waiting for result", e);
        }
    }

    /**
     * Occationally called by proxy for info on this server's workload
     * @return queue size
     * @throws RemoteException for RMI errors
     */
    @Override
    public int fetchWorkload() throws RemoteException
    {
        String PURPLE = "\u001B[35m";
        String RESET = "\u001B[0m";
        System.out.printf(PURPLE + "Proxy requested workload zone %d (%d)" + RESET + "\n", zone, requestQueue.size());
        return requestQueue.size();
    }

    /**
     * A simple toString
     * @return Server in string format
     */
    @Override
    public String toString()
    {
        return (getHost() + ":" + getPort());
    }


    // =========== Information getters ===========
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
     * @return zone of server
     */
    public int getZone()
    {
        return zone;
    }
    // ============= ============= =============

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
