package ass1.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import ass1.server.CityInfo;
import ass1.server.Server;
import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;


public class Client {

    // Hashmap with city data
    static ArrayList<InstructionInfo> instructions;

    public Client(ArrayList<InstructionInfo> instructions) {
        Client.instructions = instructions;
    }

    public static void main(String[] args)
    {
        /* TODO:
            1) Create 5 clients
            2) zone-info
            3) RMI through proxy-server
            4) Write result in output-file - Includes:
            - <result> <input query> <(turnaround, execution, waiting - time processed by <server>)>
         */

        try {
            Registry registry = LocateRegistry.getRegistry();
            ServerInterface server = (ServerInterface) registry.lookup("server");
            System.out.println("Adding 10 + 20 = " + server.Add(10, 20));
            System.out.println("Population Norway = " + server.getPopulationofCountry("Norway"));
        }
        catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
