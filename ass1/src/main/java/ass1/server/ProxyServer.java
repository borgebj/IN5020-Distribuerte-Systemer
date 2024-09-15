package ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProxyServer implements ProxyClientInterface {
	private int port;
	private ConcurrentMap<Integer, Server> servers;

	static int numberOfServers =5;
	static int requestId =0; 
	static int balanceThreshold = 18;
	public ProxyServer(int port) {
		this.port = port;
		this.servers = new ConcurrentHashMap<>();

		startProxy();
	}

	private void startProxy()
	{
		try {
			// Create registry unique for proxy
			Registry registry = LocateRegistry.createRegistry(port);

			// export proxy to registry
			ProxyClientInterface proxyStub = (ProxyClientInterface) UnicastRemoteObject.exportObject(this, port);

			// define proxy name
			String proxyName = "proxy";

			registry.bind(proxyName, proxyStub);

			System.out.printf("%s:%d has started\n", proxyName, port);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// used to register servers to this proxy - when creating servers
	public void registerServer(int zone, Server server) {
		servers.put(zone, server);
	}

	@Override
	public String requestServer(int zone) throws RemoteException {
		Server server = servers.get(zone);

		System.out.println("Found server " + server);

		// TODO
		// finn passende server

		return (server.getHost()) + ":" + (server.getPort());
	}

	
	@Override
	public ServerInterface handleQue(ConcurrentMap<Integer, ServerInterface> servers, int zone) throws RemoteException {
		// TODO Auto-generated method stub
		
		ServerInterface server = servers.get(zone-1);
		
		
		int balanceThreshold;
		if(server.getQueue().size() >18){


			balanceThreshold  = server.getQueue().size();

		
	
			ServerInterface adjacentServer1= servers.get(zone %numberOfServers);
			ServerInterface adjacentServer2= servers.get((zone+1) %numberOfServers);
	
			Queue<ArrayList<String>> a1Que = adjacentServer1.getQueue();
			Queue<ArrayList<String>> a2Que = adjacentServer2.getQueue();
			
			if(server.getQueue().size() % 18 ==0){
				System.out.println("print load ");
			}
			
			if(a1Que.size() <8 && a2Que.size() <8){

				if( a1Que.size()<a2Que.size())return adjacentServer1;
				else return adjacentServer1;	

			} else if(a1Que.size() <8){
				return adjacentServer1;

			 
			} else if(a2Que.size() <8){
				return adjacentServer2;
			}

		}	
		
		return server ;
	}
}
