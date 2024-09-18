package ass1.proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface ProxyClientInterface extends Remote {

	// client asks proxy for server in 'zone'
	// proxy find appropriate server based on workload in local area
	String requestServer(int zone) throws RemoteException;
		
	//Server handleQue( int zone) throws RemoteException;
}
