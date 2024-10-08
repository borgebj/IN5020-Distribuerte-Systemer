package ass1.proxy;

import ass1.server.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ProxyServer implements ProxyClientInterface {

	// network info
	private int port;
	private HashMap<Integer, ServerInterface> servers;
	private HashMap<ServerInterface, String> serversInfo;

	// Hashmap keeping track of requests in each zone
	private int WORKLOAD_THRESHOLD = 18;
	private ConcurrentMap<Integer, Integer> zoneRequests;


	public ProxyServer(int port) {
		this.port = port;
		this.servers = new HashMap<>();
		this.serversInfo = new HashMap<>();
		this.zoneRequests = new ConcurrentHashMap<>();

		startProxy();
	}


	/**
	 * used to register a given server in a given zone to the proxy
	 *
	 * @param zone zone of server
	 * @param server server provided
	 */
	public void registerServer(int zone, int port, ServerInterface server) {
		servers.put(zone, server);
		zoneRequests.put(zone, 0);
		serversInfo.put(server, "server"+zone+":"+port+":"+zone);
	}

	/**
	 * client asks proxy for server in 'zone'
	 * proxy find appropriate server based on workload in local area
	 *
	 * @param zone requested zone client wants
	 * @return string containing address to server client will use
	 * @throws RemoteException if any RMI error occurs
	 */
	@Override
	public String requestServer(int zone) throws RemoteException
	{
		// error handling
		if (!zoneRequests.containsKey(zone)) return null;

		ServerInterface destination = handleQueue(zone);

		// 1. Save request-counter - increment requests
		zoneRequests.merge(zone, 1, Integer::sum);

		// 2. check request counter
		int requests = zoneRequests.get(zone);
		if (requests % WORKLOAD_THRESHOLD == 0)
		{
			// updating proxy with servers workload happens on a separate thread
			new Thread(()-> {
				try {
					// attempt to fetch server load
					int workload = destination.fetchWorkload();
					zoneRequests.put(zone, workload);
				}
				catch (RemoteException e) {
					e.printStackTrace();
				}
			}).start();
		}

		// e.g. "server2:1099:2"
		return serversInfo.get(destination);
	}

	/**
	 *
	 * @param zone requested zone client wants
	 * @return proper server based on zone param
	 */
	private ServerInterface handleQueue(int zone)
	{
		// color-codes for output
		String YELLOW = "\u001B[33m";
		String CYAN = "\u001B[36m";
		String RESET = "\u001B[0m";

		ServerInterface server = servers.get(zone);

		System.out.printf("\nRequesting zone %d (Workload: %d)\n", zone, zoneRequests.get(zone));

		// load of requested zone + how many servers in total
		int requestedWorkload = zoneRequests.get(zone);
		int numServers = zoneRequests.size();

		if (requestedWorkload > WORKLOAD_THRESHOLD){

			// calculate adjacent zones
			int adjacent1 = (zone % numServers) + 1;
			int adjacent2 = (adjacent1 % numServers) + 1;

			// servers for adjacent zones
			ServerInterface adjacentServer1 = servers.get(adjacent1) ;
			ServerInterface adjacentServer2 = servers.get(adjacent2);

			// workload for adjacent zones
			int adjacentWorkload1 = zoneRequests.get(adjacent1);
			int adjacentWorkload2 = zoneRequests.get(adjacent2);

			// redirect to adjacent zone 1 if below threshold
			if (adjacentWorkload1 < WORKLOAD_THRESHOLD) {
				System.out.printf(CYAN + "Redirecting to zone:%d (Workload: %d)\n" + RESET, adjacent1, adjacentWorkload1);
				return adjacentServer1;
			}

			// redirect to adjacent zone 2 if below threshold
			if (adjacentWorkload2 < WORKLOAD_THRESHOLD) {
				System.out.printf(CYAN + "Redirecting to zone:%d (Workload: %d)\n" + RESET, adjacent2, adjacentWorkload2);
				return adjacentServer2;
			}
		}

		System.out.printf(YELLOW + "Using requested zone: %d (Workload: %d)\n" + RESET, zone, requestedWorkload);

		return server ;
	}

	/**
	 * starts proxy by using registry and port
	 */

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
