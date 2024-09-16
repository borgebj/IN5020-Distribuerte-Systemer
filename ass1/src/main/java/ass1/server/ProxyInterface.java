package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ning.http.client.providers.netty.chmv8.ConcurrentHashMapV8;

public interface ProxyInterface extends Remote {

	// Method for registring a server to the proxy
	void registerServer(int zone, ServerInterface server) throws RemoteException;


	// Method for handling requested function
	int handleRequest(String function, List<String> args, int zone) throws RemoteException;
	
	//ServerInterface handleQue(ConcurrentMap<Integer, ServerInterface> servers, int zone) throws RemoteException;

}
