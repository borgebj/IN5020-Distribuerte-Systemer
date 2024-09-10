package ass1.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import ass1.server.ProxyInterface;
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

    private static void invokeRequest(ProxyInterface proxy) {
        System.out.println("Printing all requests to be invoked: \n");

        long startTime = System.nanoTime();

        try {
            for (InstructionInfo instruc : instructions) {
                String function = instruc.function;
                List<String> args = instruc.args;
                int zone = instruc.zone;

                int result = proxy.handleRequest(function, args, zone);
                if (result > 0) {
                    System.out.printf("Result for function %s(%s) = %d\n", function, args, result);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Convert the duration into minutes
        double durationInMinutes = (double) duration / 1_000_000_000.0 /60.0;

        System.out.println("Finished invoking insturctions, Time = " + String.format("%.3f", durationInMinutes) + " minutes ");
    }

    public void startClient() {
        try {
            // Create a new registry on the unique port
            Registry registry = LocateRegistry.getRegistry("localhost", 1098);

            // Lookup the proxy
            ProxyInterface proxy = (ProxyInterface) registry.lookup("proxy");

            // Invoke requests from instruction-set
            invokeRequest(proxy);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
