package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProxyServerInterface extends Remote{
	int fetchWorkload() throws RemoteException;
}

