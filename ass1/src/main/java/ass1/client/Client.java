package ass1.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;


import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;


public class Client {

    /** Global variables */

    // Hashmap with city data
    static ArrayList<InstructionInfo> instructions;
    int zone;
    int port;


    public Client(int zone, int port, ArrayList<InstructionInfo> instructions) {
        this.zone = zone;
        this.port = port;
        Client.instructions = instructions;
        startClient();
    }

    private void invokeRequest( ArrayList<InstructionInfo> instructions , ServerInterface server)
    {
        System.out.println("Printing all requests to be invoked: \n");

        try {
            for (InstructionInfo instruc : instructions) {

                long startTime = System.currentTimeMillis();
                
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

                long endTime = System.currentTimeMillis();
                long turnaroundTime = endTime - startTime;

                logResultToFile(instruc, turnaroundTime);
            }
            
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void logResultToFile(InstructionInfo instruc, long turnaroundTime) {
        // TODO:
    }

    public void startClient()
    {
        System.out.printf("Running Client %d\n", 1);
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
