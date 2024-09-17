package ass1.proxy;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProxyClientInterface extends Remote {

	String requestServer(int zone) throws RemoteException;
}
