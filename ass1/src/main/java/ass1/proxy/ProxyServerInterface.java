package ass1.proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProxyServerInterface extends Remote{
	// used by proxy to occationally fetch workload-info from server
	int fetchWorkload() throws RemoteException;
}

