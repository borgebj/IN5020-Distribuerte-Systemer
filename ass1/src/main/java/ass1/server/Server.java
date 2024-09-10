package ass1.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import ass1.server.CityInfo;

public class Server implements ServerInterface {

    /** Global variables */

    // Hashmap with city data
    private HashMap<String, HashMap<String, CityInfo>> data;
    private int zone;
    private int port;

    public Server(int zone, int port, HashMap<String, HashMap<String, CityInfo>> data) {
        this.zone = zone;
        this.port = port;
        this.data = data;
        startServer();
    }

    /**
     * Method that starts this current server
     * Exporting server with stub, binding to registry
     */
    private void startServer()
    {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.createRegistry(port);

            // export server to registry
            ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(this, port);

            // define server name same with port
            String serverName = "server" + zone;

            // bind server to registry
            registry.bind(serverName, serverStub);

            System.out.printf("Server %s:%d has started\n", serverName, port);

        } catch (Exception e) {
            System.err.println();
        }
    }


    /**
     * Sleeps 'ms' milliseconds
     *
     * @param ms : integer, miliseconds to sleep
     */
    public void sleep(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** RMI methods */
    @Override
    public int Add(int num1, int num2) {
        return num1 + num2;
    }

    // given a country name as input, return population of country by summing population of cities in that country
    @Override
    public int getPopulationofCountry(String countryName)
    {
        // get appropriate country
        HashMap<String, CityInfo> country = data.get(countryName);

        int totalPopulation = 0;

        // go through country-map, sum city population
        for (Map.Entry<String, CityInfo> cityEntry : country.entrySet()) {
            int population = cityEntry.getValue().population;
            totalPopulation += population;
        }

        sleep(80); // network latency
        return totalPopulation;
    }

    // given a country name and min as input, return total number of cities in given country containing at least "min" population
    @Override
    public int getNumberofCities(String countryName, int min)
    {
        // get appropriate country
        HashMap<String, CityInfo> country = data.get(countryName);

        int citiesAboveMin = 0;

        // go through country-map, find cities with population >= min
        for (Map.Entry<String, CityInfo> cityEntry : country.entrySet()) {
            int population = cityEntry.getValue().population;
            if ( population >= min ) {
                citiesAboveMin++;
            }
        }

        sleep(80); // network latency
        return citiesAboveMin;
    }

    // returns number of countries with min "citycount" cities, and population at least "minpopulation"
    @Override
    public int getNumberofCountries(int citycount, int minpopulation)
    {
        int validCountries = 0;

        // iterate through all countries
        for (Map.Entry<String, HashMap<String, CityInfo>> countryEntry : data.entrySet()) {
            int validCities = 0;

            // get map of each city in current country
            HashMap<String, CityInfo> cities = countryEntry.getValue();

            // go through each city, check population
            for (CityInfo city : cities.values()) {
                if (city.population >= minpopulation) {
                    validCities++;
                }
            }

            // check if no. cities meet requirement
            if (validCities >= citycount) {
                validCountries++;
            }
        }

        sleep(80); // network latency+
        return validCountries;
    }

    // returns number of countries containing at least "citycount" number of cities
    // each included city has a population between min and max population
    @Override
    public int getNumberofCountries(int citycount, int minpopulation, int maxpopulation)
    {
        int validCountries = 0;

        // iterate through all countries
        for (Map.Entry<String, HashMap<String, CityInfo>> countryEntry : data.entrySet()) {
            int validCities = 0;

            // get map of each city in current country
            HashMap<String, CityInfo> cities = countryEntry.getValue();

            // go through each city, check population
            for (CityInfo city : cities.values()) {
                if (minpopulation <= city.population && city.population <= maxpopulation) {
                    validCities++;
                }
            }

            // check if no. cities meet requirement
            if (validCities >= citycount) {
                validCountries++;
            }
        }

        sleep(80); // network latency
        return validCountries;
    }

}
