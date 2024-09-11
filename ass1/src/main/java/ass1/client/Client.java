package ass1.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ass1.server.ProxyInterface;
import ass1.server.ServerInterface;
import ass1.server.InstructionInfo;

public class Client {

    // Hashmap with city data
    private static ArrayList<InstructionInfo> instructions;
    private int zone;
    private int port;

    public Client(int zone, int port, ArrayList<InstructionInfo> instructions) {
        Client.instructions = instructions;
        this.zone = zone;
        this.port = port;
        startClient();
    }

    private static void invokeRequests(ProxyInterface proxy) {
        System.out.println("Printing all requests to be invoked: \n");

        long startTime = System.nanoTime();

        for (InstructionInfo instruc : instructions) {
            try {
                String function = instruc.function;
                List<String> args = instruc.args;
                int zone = instruc.zone;

                int result = proxy.handleRequest(function, args, zone);
                if (result > 0) {
                    System.out.printf("[C%d] Result for function %s(%s) = %d\n", zone, function, args, result);
                }
            } catch (RemoteException e) {
                System.err.printf("Failed to invoke function %s(%s) for zone %d: %s\n",
                        instruc.function, instruc.args, instruc.zone, e.getMessage());
            }
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Convert the duration into minutes
        double durationInMinutes = (double) duration / 1_000_000_000.0 / 60.0;

        System.out.println("Finished invoking instructions, Time = " + String.format("%.3f", durationInMinutes) + " minutes ");
    }

    public void startClient() {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);  // <- 1098 is proxy-port

            // Lookup the proxy
            ProxyInterface proxy = (ProxyInterface) registry.lookup("proxy");

            System.out.printf("Client%d:%d has started\n", zone, port);

            // Invoke requests from instruction-set
            invokeRequests(proxy);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
