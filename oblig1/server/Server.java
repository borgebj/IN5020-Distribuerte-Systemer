package com.ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AlreadyBoundException;

public class Server implements ServerInterface {

    // FIFO processing
    int[] clientRequests = new int[45];

    public int Add(int num1, int num2) {
        return num1 + num2;
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            Server server = new Server();
            ServerInterface serverStub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
            registry.bind("server", serverStub);
        }
        catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPopulationofCountry(String countryName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPopulationofCountry'");
    }

    @Override
    public int getNumberofCities(String countryName, int min) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberofCities'");
    }

    @Override
    public int getNumberofCountries(String citycount, int minpopulation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberofCountries'");
    }

    @Override
    public int getNumberofCountries(int citycount, int minpopulation, int maxpopulation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumberofCountries'");
    }
}
