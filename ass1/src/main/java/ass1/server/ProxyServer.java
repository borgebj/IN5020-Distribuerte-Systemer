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
		Server server =  handleQue(servers, zone);
		server.getReqQueue().add(requestId);
		System.out.println( " zone oujt " + server.getHost() + " request id " + requestId);
		System.out.println("Found server " + server);



		// TODO
		// finn passende server

		return (server.getHost()) + ":" + (server.getPort());
	}

	
	@Override
	public Server handleQue(ConcurrentMap<Integer, Server> servers, int zone) throws RemoteException {
		// TODO Auto-generated method stub
		
		Server server = servers.get(zone);
		
		System.out.println("Zone in "+  zone );
		

		Server server1 = servers.get(0);
		if(server.getReqQueue().size() >18){

	

		
			System.out.println("Zone right now "+ ((zone +1 ) %numberOfServers));
	
		
			Server adjacentServer1= servers.get((zone +1 ) %numberOfServers);
			Server adjacentServer2= servers.get((zone+2) %numberOfServers);
		


			if( adjacentServer2.getReqQueue() ==null){
				System.out.println(" Que 2 is null");	
		}	
			if( adjacentServer1.getReqQueue() ==null){
				System.out.println(" Que 1 is");	
		}	
			Queue<Integer> a1Que = adjacentServer1.getReqQueue();
			Queue<Integer>  a2Que = adjacentServer2.getReqQueue();
			
			if(server.getReqQueue().size() % 18 ==0){
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
		
		requestId++;
		
		return server ;
	}
}
