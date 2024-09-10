package ass1.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
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


    private static void invokeRequest( ArrayList<InstructionInfo> instructions , ServerInterface server)
    {
        System.out.println("Printing all requests to be invoked: \n");

        try {
           
            for (InstructionInfo instruc : instructions) {
                
                //System.out.println(instruc.function);
                
                switch (instruc.function) {
                    case "getPopulationofCountry":
                        //System.out.println(instruc.args);                    
                        if(instruc.args.size() >0){
                            String country = instruc.args.get(0);
                            System.out.println("Population of " + country + " = " + server.getPopulationofCountry(country));
                        }
                        break;
                    case "getNumberofCities":
                    
                    break;
                    case "getNumberofCountries":
                    
                    break;
                    
                    
                    default:
                    break;
                }
                
                
            }
            
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run()
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
            ServerInterface server = (ServerInterface) registry.lookup("server0");
            invokeRequest(instructions, server);
            //System.out.println("Adding 10 + 20 = " + server.Add(10, 20));
            //System.out.println("Population Norway = " + server.getPopulationofCountry("Norway"));
        }
        catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < 5; i++) {

                //Create 5 clients to run at once

            }
        } catch (Exception  e) {
            // TODO: handle exception
        }
    }
}
