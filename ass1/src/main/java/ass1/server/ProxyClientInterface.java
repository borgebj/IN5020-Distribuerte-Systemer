package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface ProxyClientInterface extends Remote {

	String requestServer(int zone) throws RemoteException;
		
	ServerInterface handleQue(ConcurrentMap<Integer, ServerInterface> servers, int zone) throws RemoteException;
}
