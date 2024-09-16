package ass1.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ProxyServer implements ProxyClientInterface {
	private int port;
	private HashMap<Integer, Server> servers;

	// Hashmap keeping track of requests in each zone
	private ConcurrentMap<Integer, Integer> zoneRequests;


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
		int requestedZone = zone;

		zone = (zone % 5) + 1; // increment zone : TODO: REMOVE

		Server server = servers.get(zone);

		System.out.println("Requested server" + requestedZone + ", Found " + server);

		// TODO
		// finn passende server

		// TODO: annen thred ==============

		// 1. Save request-counter - increment requests
//		zoneRequests.merge(zone, 1, Integer::sum);
//
//		// 2. check request counter
//		// 2.1 Fetch every 18
//		int requests = zoneRequests.get(zone);
//		if (requests >= 18) {
//
//			int workload = server.fetchWorkload();
//			// zoneRequests.put(zone, workload);
//		}

		// ==================================

		return (server.getHost()) + ":" + (server.getPort());
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
