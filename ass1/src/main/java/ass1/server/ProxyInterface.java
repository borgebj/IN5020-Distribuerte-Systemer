package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProxyInterface extends Remote {

	// Method for registring a server to the proxy
	void registerServer(int zone, ServerInterface server) throws RemoteException;


	// Method for handling requested function
	int handleRequest(String function, List<String> args, int zone) throws RemoteException;

}
