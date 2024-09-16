package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface ProxyClientInterface extends Remote {

	String requestServer(int zone) throws RemoteException;
		
	Server handleQue(ConcurrentMap<Integer, Server> servers, int zone) throws RemoteException;
}
