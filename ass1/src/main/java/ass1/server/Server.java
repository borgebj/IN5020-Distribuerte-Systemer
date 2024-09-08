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

    public static void main(String[] args) {
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

    // given a country name as input, return population of country by summing population of cities in that country
    @Override
    public int getPopulationofCountry(String countryName)
    {
        HashMap<String, CityInfo> country = data.get(countryName);
        int sum = 0;

        for (Map.Entry<String, CityInfo> cityEntry : country.entrySet()) {
            sum += cityEntry.getValue().population;
        }

        return sum;
    }

    // given a country name and min as input, return total number of cities in given country containing at least "min" population
    @Override
    public int getNumberofCities(String countryName, int min) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberofCities'");
    }

    // returns number of countries that contain at least "citycount" number of cities
    // each included city has a population of at least "minpopulation"
    @Override
    public int getNumberofCountries(String citycount, int minpopulation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberofCountries'");
    }

    // returns number of countries containing at least "citycount" number of cities
    // each included city has : population between min and max population
    @Override
    public int getNumberofCountries(int citycount, int minpopulation, int maxpopulation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberofCountries'");
    }
}
