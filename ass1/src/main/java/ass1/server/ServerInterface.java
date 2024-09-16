package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {

    // given a country name as input, return population of country by summing population of cities in that country
    Response getPopulationofCountry(String countryName) throws RemoteException;
    // e.g. country name = "Norway" => "population = 3,162,856"

    // given a country name and min as input, return total number of cities in given country containing at least "min" population
    Response getNumberofCities(String countryName, int min) throws RemoteException;

    // returns number of countries that contain at least "citycount" number of cities
    // each included city has a population of at least "minpopulation"
    Response getNumberofCountries(int citycount, int minpopulation) throws RemoteException;

    // returns number of countries containing at least "citycount" number of cities
    // each included city has : population between min and max population
    Response getNumberofCountries(int citycount, int minpopulation, int maxpopulation) throws RemoteException;
}
