package ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ning.http.client.providers.netty.chmv8.ConcurrentHashMapV8;

public class Proxy implements ProxyInterface {
	private int port;
	private ConcurrentMap<Integer, ServerInterface> servers;
	int numberOfServers =5;
	int requestId =0; 

	public Proxy(int port) {
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
			ProxyInterface proxyStub = (ProxyInterface) UnicastRemoteObject.exportObject(this, port);

			// define proxy name
			String proxyName = "proxy";

			registry.bind(proxyName, proxyStub);

			System.out.printf("%s:%d has started\n", proxyName, port);

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerServer(int zone, ServerInterface server) throws RemoteException {
		servers.put(zone, server);
	}




	/*
	 *	workload should be added and executed seprately 
	 *
	 * Event loop should handle requests while list is being added onto
	 * How to handle request based on waiting list
	 * Waiting list cannot contain direct clients
	 * Identity?
	 * 
	 * server.getQue
	 */
	public void handleEventLoop() throws RemoteException{
		

		while (true){


			while(servers.size()>0){
				for (ServerInterface server   : servers.values()) {
					
					for (ArrayList<String> queuedRequest : server.getQueue()) {
						//int res = handleFunction(server, queuedRequest);
						
					}
				}
			}

		}



	}

	

	@Override
	public int handleRequest(String function, List<String> args, int zone) throws RemoteException {
	
		ServerInterface server = handleQue(servers, zone);
		if (server == null) {
			throw new RemoteException("Server not found for zone " + zone);
		}
	
		//server.getQueue().add(args);
		// TODO: Add queues and process queue sizes

		// process requested function
		
		if (args.size() > 0) {
			String  country;

			// try-catch for handling wrong entries in instruction-file
			try {
				switch (function) {
					case "getPopulationofCountry":
						country = args.get(0);
						return server.getPopulationofCountry(country);

					case "getNumberofCities":
						country = args.get(0);
						int min = Integer.parseInt(args.get(1));
						return server.getNumberofCities(country, min);

					case "getNumberofCountries":

						// min boundary
						if (args.size() == 2) {
							int cityCount = Integer.parseInt(args.get(0));
							int minPopulation = Integer.parseInt(args.get(1));
							return server.getNumberofCountries(cityCount, minPopulation);
						}

						// min and max boundaries
						else if (args.size() == 3) {
							int cityCOunt = Integer.parseInt(args.get(0));
							int minPopulation = Integer.parseInt(args.get(1));
							int maxPopulation = Integer.parseInt(args.get(2));
							return server.getNumberofCountries(cityCOunt, minPopulation, maxPopulation);
						} else {
							throw new RemoteException("Invalid number of arguments for 'getNumberofCountries'");
						}
					default:
						throw new RemoteException("Unknown function: " + function);
				}
			} catch (Exception ignore) {

			
			}
		}
		return 0;

	
	}

	
	@Override
	public ServerInterface handleQue(ConcurrentMap<Integer, ServerInterface> servers, int zone) throws RemoteException {
		// TODO Auto-generated method stub
		
		ServerInterface server = servers.get(zone-1);
		
		
		if(server.getQueue().size() >18){
	
			ServerInterface adjacentServer1= servers.get(zone %numberOfServers);
			ServerInterface adjacentServer2= servers.get((zone+1) %numberOfServers);
	
			Queue<ArrayList<String>> a1Que = adjacentServer1.getQueue();
			Queue<ArrayList<String>> a2Que = adjacentServer2.getQueue();
			
			
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
