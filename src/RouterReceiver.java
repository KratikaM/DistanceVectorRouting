import java.io.*;
import java.net.*;
import java.util.*;

public class RouterReceiver {
	
	private static final String IPADDRESS = "239.255.255.250";
	public static void main(String[] args) throws SocketException, ClassNotFoundException {

		if(!validateInputs(args)) {
			System.exit(0);
		}
		
		int port = Integer.parseInt(args[0]);
		String fileName = args[1];
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		MulticastSocket multiCastSocket;
		InetAddress groupAddress;
		ByteArrayInputStream byteArrayInputStream;
		ObjectInputStream objectInputStream;
		
		byte [] buffer = new byte[10240];
		// using datagram packet for connectionless delivery of packets between routers
		DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length);
		
		// group routers together and initialize their routing tables
		try {
			multiCastSocket = new MulticastSocket(port);
			groupAddress = InetAddress.getByName(IPADDRESS);
			multiCastSocket.joinGroup(groupAddress);
			
			RoutingTableAlgo routingTable = new RoutingTableAlgo();
			routingTable.initializeRoutingTable(fileName);
		
			// start sending routing table to neighbors
			RouterSender sender = new RouterSender(multiCastSocket, routingTable, port);
			sender.start();

			// continuously receive neighbor's entries and update own routing table
			while(true) {
				multiCastSocket.receive(datagramPacket);
				byte[] receivedData = datagramPacket.getData();
				byteArrayInputStream = new ByteArrayInputStream(receivedData);
				objectInputStream = new ObjectInputStream(byteArrayInputStream);
				RoutingTableAlgo readObject = (RoutingTableAlgo) objectInputStream.readObject();
				List<String> neighbors = new ArrayList<String>(routingTable.getNeighbours());
				neighbors.remove(routingTable.getRouterName());
				if(neighbors.contains(readObject.getRouterName())) {
					routingTable.updateRoutingTable(routingTable, readObject);
				}
			}
		}
		catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}
	}
	private static boolean validateInputs(String[] args) {
		// check the invocation by the user
		if(args.length<2) {
			System.out.println("Incorrect invocation");
			return false;
		}
		
		try {
			Integer.parseInt(args[0]);
		}
		catch (Exception ex) {
			System.out.println("Invalid port number");
			return false;
		}
		
		String fileName = args[1];
		
		if (fileName == null || fileName.length() > 0 || fileName.indexOf(".dat") > -1) {
			System.out.println("Incorrect file Name");
			return false;
		}
		
		return true;
	}

}
