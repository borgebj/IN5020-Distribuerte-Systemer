package ass1.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;


import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;


public class Client {


    /**
     * Goes through and parses instructions from a given file to a hashmap, later used by clients
     *
     * @return map : filled hashmap with instructions
     */
    private static ArrayList<InstructionInfo> parseInstructions() {
        ArrayList<InstructionInfo> instructions = new ArrayList<>();

        File file = new File("ass1/info/exercise_1_input.txt");
        if (!file.exists()) {
            System.err.println("File not found: " + "ass1/info/exercise_1_input.txt");
            return instructions; // Return empty list if file is not found
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" "); // <- | function | arg1 | arg2 | arg3 | zone+

                // parse method name and zone
                String function = parts[0];
                String zonePart = parts[parts.length - 1];
                int zone = Integer.parseInt(zonePart.split(":")[1]); // Extract the number after "Zone:"

                // parse arguments
                List<String> args = new ArrayList<>();
                for (int i = 1; i < parts.length - 1; i++) {
                    args.add(parts[i]);
                }

                InstructionInfo info = new InstructionInfo(function, args, zone);
                instructions.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return instructions;
    }

    private static void invokeRequest( ArrayList<InstructionInfo> instructions , ServerInterface server)
    {
        System.out.println("Printing all requests to be invoked: \n");

        try {
           
            for (InstructionInfo instruc : instructions) {
                
                //System.out.println(instruc.function);
                
                switch (instruc.function) {
                    case "getPopulationofCountry":
                        //System.out.println(instruc.args.get(1));
                        System.out.println("Population Norway = " + server.getPopulationofCountry("France"));
                        String country = " ";
                        for (String name  : instruc.args) {
                            country +=  name+ " ";
                            
                        }
                        System.out.println("proper country name :" + country);
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
        
    public static void main(String[] args) throws RemoteException
    {
        // Parse instructions aka input.txt
        ArrayList<InstructionInfo> instructions = parseInstructions();

        /* TODO:
            1) Create 5 clients
            2) Assign instructions and zone-info
            3) RMI through proxy-server
            4) Write result in output-file - Includes:
            - <result> <input query> <(turnaround, execution, waiting - time processed by <server>)>
         */


        
        try {
            Registry registry = LocateRegistry.getRegistry();
            ServerInterface server = (ServerInterface) registry.lookup("server_0");
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
