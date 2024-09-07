package ass1.Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ass1.server.ServerInterface;

public class Client {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            ServerInterface server = (ServerInterface) registry.lookup("server");
            System.out.println(server.Add(10, 20));
        }
        catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
