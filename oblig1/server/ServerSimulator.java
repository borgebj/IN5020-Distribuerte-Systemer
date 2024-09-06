package com.ass1.server;

import java.io.File;
import java.util.HashMap;

public class ServerSimulator {

    /**
     * Goes through and parses data from a given file to a hashmap, later used by server
     *
     * @param filename : name of file used
     * @return map : filled hashmap with data
     */
    private static HashMap parseData(String filename) {
        HashMap map = new HashMap();
        File file = new File(filename);
        // ...
        return map;
    }

    /**
     * Goes through and parses instructions from a given file to a hashmap, later used by clients
     *
     * @param filename : name of file used
     * @return map : filled hashmap with instructions
     */
    private static HashMap parseInstructions(String filename) {
        HashMap map = new HashMap();
        File file = new File(filename);
        // ...
        return map;
    }

    public static void main(String[] args) {
        
        /*
         * Create 5 instances of server class in different address and ports
         */

        // parse dataset.csv
        HashMap dataset = parseData("excercise_1_dataset.csv");

        // parse instructions aka input.txt
        HashMap instructions = parseInstructions("excercise_1_input.xt");


         Server[] servers = new Server[5];
         
         for (int i = 0; i < 5; i++) {
            servers[i] = new Server();
         }
    }
}
