package ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ProxyServer implements ProxyClientInterface {
	private int port;
	private HashMap<Integer, Server> servers;

	// Hashmap keeping track of requests in each zone
	private ConcurrentMap<Integer, Integer> zoneRequests;

	static int numberOfServers =5;
	static int requestId =0; 
	static int balanceThreshold = 18;
	public ProxyServer(int port) {
		this.port = port;
		this.servers = new HashMap<>();
		this.zoneRequests = new ConcurrentHashMap<>();

		startProxy();
	}

	// used to register servers to this proxy - when creating servers
	public void registerServer(int zone, Server server) {
		servers.put(zone, server);
		zoneRequests.put(zone, 0);
	}

	@Override
	public String requestServer(int zone) throws RemoteException
	{
		Server destination =  handleQueue(zone);

		// 1. Save request-counter - increment requests
		zoneRequests.merge(zone, 1, Integer::sum);

		// 2. check request counter
		int requests = zoneRequests.get(zone);
		if (requests % 18 == 0) {

			int workload = destination.fetchWorkload();
			zoneRequests.put(zone, workload);
		}

		// ==================================

		return (destination.getHost()) + ":" + (destination.getPort());
	}

	

	private Server handleQueue(int zone)
	{
		String YELLOW = "\u001B[33m";
		String CYAN = "\u001B[36m";
		String RESET = "\u001B[0m";

		Server server = servers.get(zone);

		System.out.printf("Requesting zone:%d\n", zone);

		int requestedWorkload = zoneRequests.get(zone);
		int numServers = zoneRequests.size();

		if (requestedWorkload > 18){

			// calculate adjacent zones
			int adjacent1 = (zone % numServers) + 1;
			int adjacent2 = (adjacent1 % numServers) + 1;

			// servers for adjacent zones
			Server adjacentServer1 = servers.get(adjacent1) ;
			Server adjacentServer2 = servers.get(adjacent2);

			// workload for adjacent zones
			int adjacentWorkload1 = zoneRequests.get(adjacent1);
			int adjacentWorkload2 = zoneRequests.get(adjacent2);

			

			// redirect to adjacent zone 1 if below threshold
			if (adjacentWorkload1 < 18) {
				System.out.printf(CYAN + "Redirecting to zone:%d (Workload: %d)\n\n" + RESET, adjacent1, adjacentWorkload1);
				adjacentServer1.sleep(90);
				return adjacentServer1;
			}

			// redirect to adjacent zone 2 if below threshold
			if (adjacentWorkload2 < 18) {
				System.out.printf(CYAN + "Redirecting to zone:%d (Workload: %d)\n\n" + RESET, adjacent2, adjacentWorkload2);
				adjacentServer2.sleep(90);
				return adjacentServer2;
			}
		}

		System.out.printf(YELLOW + "Using requested zone: %d (Workload: %d)\n\n" + RESET, zone, requestedWorkload);

		return server ;
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





}
