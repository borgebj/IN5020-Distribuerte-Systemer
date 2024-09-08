package ass1.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServerSimulator {

    /**
     * Goes through and parses data from a given file to a hashmap, later used by server
     *
     * @return map : filled hashmap with data
     */
    private static HashMap parseData() {
        HashMap<String, HashMap<String, CityInfo>> countryMap = new HashMap<>();

        File file = new File("ass1/info/exercise_1_dataset.csv");
        if (!file.exists()) {
            System.err.println("File not found: " + "ass1/info/exercise_1_dataset.csv");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {

                CityInfo city = new CityInfo();
                String[] parts = line.split(";");

                city.geonameId = Integer.parseInt(parts[0]);
                city.name = parts[1];
                city.countryCode=  parts[2];
                city.countryName = parts[3];
                city.population = Integer.parseInt(parts[4]);
                city.timezone = parts[5];
                city.coordinates = parts[6];

                HashMap<String, CityInfo> cityMap = countryMap.computeIfAbsent(city.countryName, k -> new HashMap<>());
                cityMap.put(city.name, city);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // ...
        return countryMap;
    }

    /**
     * Goes through and parses instructions from a given file to a hashmap, later used by clients
     *
     * @return map : filled hashmap with instructions
     */
    private static HashMap parseInstructions() {
        HashMap map = new HashMap();
        File file = new File("info/exercise_1_input.xt");
        // ...
        return map;
    }

    public static void main(String[] args) {
        
        /*
         * Create 5 instances of server class in different address and ports
         */

        // parse dataset.csv
        HashMap<String, HashMap<String, CityInfo>> dataset = parseData();

        // parse instructions aka input.txt
//        HashMap instructions = parseInstructions();

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


         Server[] servers = new Server[5];
         
         for (int i = 0; i < 1; i++) {
            servers[i] = new Server(dataset);
            int norwayPop = servers[i].getPopulationofCountry("Sweden");
            int nocities = servers[i].getNumberofCities("Norway", 100000);
            int nocitieCountPop = servers[i].getNumberofCountries(2, 5000000);
            int nocitiesBetween = servers[i].getNumberofCountries(30, 100000, 800000);

             System.out.printf("%d\n%d\n%d\n%d\n", norwayPop, nocities, nocitieCountPop, nocitiesBetween);
         }
    }
}
