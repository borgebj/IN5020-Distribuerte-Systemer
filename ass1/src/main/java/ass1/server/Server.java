package ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AlreadyBoundException;
import java.util.HashMap;
import java.util.Map;

import ass1.server.CityInfo;

public class Server implements ServerInterface {

    /** Global variables */
    // FIFO processing
    static int[] clientRequests;

    // Hashmap with city data
    static HashMap<String, HashMap<String, CityInfo>> data;

    public Server(HashMap<String, HashMap<String, CityInfo>> data) {
        Server.data = data;
        clientRequests = new int[45];
    }


    public int Add(int num1, int num2) {
        return num1 + num2;
    }

    public void sleep(int ms)
    {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    //TODO: denne er ikke nødvendig, opprettelse skjer i simulator
    public static void main(String[] args)
    {
        try {
            Registry registry = LocateRegistry.getRegistry();
            Server server = new Server(data);
            ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
            registry.bind("server", serverStub);
        }
        catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
