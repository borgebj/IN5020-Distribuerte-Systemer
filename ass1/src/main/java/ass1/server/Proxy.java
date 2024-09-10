package ass1.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proxy implements ProxyInterface {
	private int port;
	private Map<Integer, ServerInterface> servers;


	public Proxy(int port) {
		this.port = port;
		this.servers = new HashMap<>();

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

			System.out.printf("Proxy %s:%d has started\n", proxyName, port);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerServer(int zone, ServerInterface server) throws RemoteException {
		servers.put(zone, server);
	}

	@Override
	public int handleRequest(String function, List<String> args, int zone) throws RemoteException {
		ServerInterface server = servers.get(zone - 1);
		if (server == null) {
			throw new RemoteException("Server not found for zone " + zone);
		}

		// TODO: Add queues and process queue sizes

		// process requested function
		if (args.size() > 0) {
			String country;

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
			} catch (Exception ignore) {}
		} return 0;
	}
}
