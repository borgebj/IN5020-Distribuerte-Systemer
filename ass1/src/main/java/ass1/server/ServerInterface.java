package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public interface ServerInterface extends Remote {

    // given a country name as input, return population of country by summing population of cities in that country
    int getPopulationofCountry(String countryName) throws RemoteException;
    // e.g. country name = "Norway" => "population = 3,162,856"

    // given a country name and min as input, return total number of cities in given country containing at least "min" population
    int getNumberofCities(String countryName, int min) throws RemoteException;

    // returns number of countries that contain at least "citycount" number of cities
    // each included city has a population of at least "minpopulation"
    int getNumberofCountries(int citycount, int minpopulation) throws RemoteException;

    // returns number of countries containing at least "citycount" number of cities
    // each included city has : population between min and max population
    int getNumberofCountries(int citycount, int minpopulation, int maxpopulation) throws RemoteException;


    Queue<ArrayList<String>> getQueue ()throws RemoteException;

}
