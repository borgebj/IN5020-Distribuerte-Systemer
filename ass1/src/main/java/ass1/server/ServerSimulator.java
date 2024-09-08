package ass1.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerSimulator {

    /**
     * Goes through and parses data from a given file to a hashmap, later used by server
     *
     * @return map : filled hashmap with data
     */
    private static HashMap<String, HashMap<String, CityInfo>> parseData()
    {
        HashMap<String, HashMap<String, CityInfo>> countryMap = new HashMap<>();

        File file = new File("ass1/info/exercise_1_dataset.csv");
        if (!file.exists()) {
            System.err.println("File not found: " + "ass1/info/exercise_1_dataset.csv");
        }

        // iterate over lines in file
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {

                String[] parts = line.split(";");

                // create city-info
                CityInfo city = new CityInfo(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        parts[2],
                        parts[3],
                        Integer.parseInt(parts[4]),
                        parts[5],
                        parts[6]
                );

                HashMap<String, CityInfo> cityMap = countryMap.computeIfAbsent(city.countryName, k -> new HashMap<>());
                cityMap.put(city.name, city);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return countryMap;
    }

    /**
     * Goes through and parses instructions from a given file to a hashmap, later used by clients
     *
     * @return map : filled hashmap with instructions
     */
    private static ArrayList<InstructionInfo> parseInstructions()
    {
        ArrayList<InstructionInfo> instructions = new ArrayList<>();

        File file = new File("ass1/info/exercise_1_input.txt");
        if (!file.exists()) {
            System.err.println("File not found: " + "ass1/info/exercise_1_input.csv");
        }

        // iterate over lines in file
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] parts = line.split(" "); // <- | function | arg1 | arg2 | arg3 | zone+

                System.out.printf("%d\n", parts.length);

                // create instruction-info
//                InstructionInfo info = new InstructionInfo(
//                        parts[0],                       // function name
//                        Integer.parseInt(parts[1]),     // arg1
//                        Integer.parseInt(parts[2]),     // arg2
//                        Integer.parseInt(parts[3]),     // arg3
//                        Integer.parseInt(parts[4])      // zone
//                );

//                instructions.add(info);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return instructions;
    }

    public static void main(String[] args)
    {
        /*
         * Create 5 instances of server class in different address and ports
         */

        // parse dataset.csv
        HashMap<String, HashMap<String, CityInfo>> dataset = parseData();

        // parse instructions aka input.txt
        ArrayList<InstructionInfo> instructions = parseInstructions();

//        for (Map.Entry<String, HashMap<String, CityInfo>> countryEntry : dataset.entrySet()) {
//            String country = countryEntry.getKey();
//            HashMap<String, CityInfo> cities = countryEntry.getValue();
//
//            System.out.println("========== [ Country: " + country + " ] ==========");
//            for (Map.Entry<String, CityInfo> cityEntry : cities.entrySet()) {
//                System.out.println("  City: " + cityEntry.getKey());
//                System.out.println("  Info: " + cityEntry.getValue());
//            }
//            System.out.println("============================================");
//        }


         ServerInterface[] servers = new Server[5];

         try {
             for (int i = 0; i < 5; i++) {
                 int port = 1099 + i;
                 Registry registry = LocateRegistry.getRegistry(port);
                 ServerInterface server = (ServerInterface) registry.lookup("server" + i);

                 servers[i] = server;
                 int norwayPop = servers[i].getPopulationofCountry("Sweden");
                 int nocities = servers[i].getNumberofCities("Norway", 100000);
                 int nocitieCountPop = servers[i].getNumberofCountries(2, 5000000);
                 int nocitiesBetween = servers[i].getNumberofCountries(30, 100000, 800000);

                 System.out.printf(
                         "getPopulationofCountry('Norway') = %d\n" +
                         "getNumberofCities('Norway', 100000) = %d\n" +
                         "getNumberofCountries(2, 5000000) = %d\n" +
                         "getNumberofCountries(30, 100000, 800000) = %d\n",
                         norwayPop, nocities, nocitieCountPop, nocitiesBetween);
             }
         }
         catch (RemoteException | NotBoundException e) {
             e.printStackTrace();
         }
    }
}
