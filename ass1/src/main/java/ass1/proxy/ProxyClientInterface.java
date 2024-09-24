package ass1.proxy;

import ass1.server.ServerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProxyClientInterface extends Remote {

	// client asks proxy for server in 'zone'
	// proxy find appropriate server based on workload in local area
	String requestServer(int zone) throws RemoteException;
}
