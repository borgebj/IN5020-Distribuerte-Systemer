package ass1.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;

public class Client {

    // Hashmap with city data
    static ArrayList<InstructionInfo> instructions;
    int zone;
    int port;

    public Client(int zone, int port, ArrayList<InstructionInfo> instructions) {
        Client.instructions = instructions;
        this.zone = zone;
        this.port = port;
        startClient();
    }

    private static void invokeRequest(ArrayList<InstructionInfo> instructions, ServerInterface server) {
        System.out.println("Printing all requests to be invoked: \n");

        long startTime = System.nanoTime();

        try {

            for (InstructionInfo instruc : instructions) {

                // System.out.println(instruc.function);

                switch (instruc.function) {
                    case "getPopulationofCountry":
                        // System.out.println(instruc.args);
                        if (instruc.args.size() > 0) {
                            String country = instruc.args.get(0);

                            int result = server.getPopulationofCountry(country);
                            System.out.printf("Population of %s = %d\n", country, result);
                        }
                        break;
                    case "getNumberofCities":
                        if (instruc.args.size() > 1) {
                            String country = instruc.args.get(0);
                            String minStr = instruc.args.get(1);
                            int min = Integer.parseInt(minStr);

                            int result = server.getNumberofCities(country, min);
                            System.out.printf("Country name = %s, Cities with minimum population of %d, Result ==> %d\n", country, min, result);
                        }

                        break;
                    case "getNumberofCountries":
                        String cityCountStr = instruc.args.get(0);
                        String minStr = instruc.args.get(1);
                        String maxStr;

                        int cityCount = Integer.parseInt(cityCountStr);
                        int min = Integer.parseInt(minStr);
                        int max;

                        if (instruc.args.size() == 2) {
                            int result = server.getNumberofCountries(cityCount, min);

                            System.out.printf("Number of countries with %d Cities and minimum population of %d, Result ==> %d\n", cityCount, min, result);

                        }
                        else if (instruc.args.size() == 3) {
                            maxStr = instruc.args.get(2);
                            max = Integer.parseInt(maxStr);

                            int result = server.getNumberofCountries(cityCount, min, max);

                            System.out.printf("Number of countries with %d Cities and a population between %d and %d, Result ==> %d\n", cityCount, min, max, result);
                        }

                        break;

                    default:
                        break;
                }

            }

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        // Calculate the duration in nanoseconds
        long duration = endTime - startTime;

        // Convert the duration into minutes
        double durationInMinutes = (double) duration / 1_000_000_000.0 /60.0;

        System.out.println("Finished invoking insturctions, Time = " + String.format("%.3f", durationInMinutes) + " minutes ");
    }

    public void startClient() {
        /*
         * TODO:
         * 1) Create 5 clients
         * 2) zone-info
         * 3) RMI through proxy-server
         * 4) Write result in output-file - Includes:
         * - <result> <input query> <(turnaround, execution, waiting - time processed by
         * <server>)>
         */

        try {
            Registry registry = LocateRegistry.getRegistry();
            ServerInterface server = (ServerInterface) registry.lookup("server0");
            invokeRequest(instructions, server);
            // System.out.println("Adding 10 + 20 = " + server.Add(10, 20));
            // System.out.println("Population Norway = " +
            // server.getPopulationofCountry("Norway"));
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < 5; i++) {

                // Create 5 clients to run at once

            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
