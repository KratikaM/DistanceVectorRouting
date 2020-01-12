import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class RoutingTableAlgo implements Serializable{
	
	public static final double INFINITECOST = 16.0;
	private List<RoutingTable> routingTables = new ArrayList<>();
	private HashMap<String, Double> tableEntry = new HashMap<String, Double>();
	private List<String> neighbours = new ArrayList<>();
	private transient BufferedReader bufferedReader;
	private String fileName;
	private String routerName;
	
	public RoutingTableAlgo()
	{
		
	}
	public RoutingTableAlgo(RoutingTableAlgo rta)
	{
		this.routingTables = rta.routingTables;
	}
	
	public List<RoutingTable> getRoutingTables() {
		return routingTables;
	}
	public void setRoutingTables(List<RoutingTable> routingTables) {
		this.routingTables = routingTables;
	}
	public List<String> getNeighbours() {
		return neighbours;
	}
	public void setNeighbours(List<String> neighbours) {
		this.neighbours = neighbours;
	}
	public String getRouterName() {
		return routerName;
	}
	public void setRouterName(String routerName) {
		this.routerName = routerName;
	}
	
	// initialize routing table with entries from text file
	public void initializeRoutingTable(String fileName) throws IOException
	{
		this.fileName = fileName;
		String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
		this.routerName = name;
		System.out.println("Router Name: " + this.routerName);
		RoutingTable router = new RoutingTable(routerName, routerName, "-", 0.0);
		neighbours.add(routerName);
		routingTables.add(router);
		tableEntry.put(routerName, 0.0);
		
		// read file line by line and make entries in table
		try {
		bufferedReader = new BufferedReader(new FileReader(fileName));
		String line = bufferedReader.readLine();
		while(line != null)
		{
			String[] contentsOfFile = line.split(" ");
			if(contentsOfFile.length==2)
			{
				String destination = contentsOfFile[0];
				double cost = Double.parseDouble(contentsOfFile[1]);
				router = new RoutingTable(routerName, destination, destination, cost);
				neighbours.add(destination);
				routingTables.add(router);
				tableEntry.put(destination, cost);
				
			}
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		
	}catch(Exception exception)
		{
		
		}
	}
	
	// update table when new/updated entries received from neighbors
	public synchronized void updateRoutingTable(RoutingTableAlgo own, RoutingTableAlgo received)
	{
		List<String> receivedNeighbours = new ArrayList<>(received.getNeighbours());
		List<String> ownNeighbours = new ArrayList<>(own.getNeighbours());
		for(String newNeigbour : receivedNeighbours)
		{
			if(! ownNeighbours.contains(newNeigbour))
			{
				RoutingTable router = new RoutingTable(own.getRouterName(), newNeigbour, "-", INFINITECOST);
				own.routingTables.add(router);
				own.neighbours.add(newNeigbour);
			}
		}
		
		RoutingTableAlgo receivedTable = new RoutingTableAlgo(received);
		double costToAdd = 0.0;
		for(RoutingTable entry : received.getRoutingTables())
		{
			if(entry.getDestinationNode().equals(own.getRouterName()))
			{
				costToAdd = entry.getCost();
				break;
			}
		}
		
		for(RoutingTable entry : receivedTable.getRoutingTables())
		{
			entry.setNextHopNode(entry.getSourceNode());
			double originalCost = entry.getCost();
			entry.setCost(originalCost + costToAdd);
		}
		
		for(RoutingTable entry : receivedTable.getRoutingTables())
		{
			if(entry.getNextHopNode().equals(own.getRouterName()))
			{	
				entry.setCost(INFINITECOST);
			}
		}
		
		for(RoutingTable entryReceived : receivedTable.getRoutingTables())
		{
			String destination = entryReceived.getDestinationNode();
			String nextHop = entryReceived.getNextHopNode();
			double cost = entryReceived.getCost();
			
			for(RoutingTable entryOwn : own.getRoutingTables())
			{
				if(entryOwn.getDestinationNode().equals(entryOwn.getSourceNode()))
				{
					continue;
				}
				if(entryOwn.getDestinationNode().equals(destination))
				{
					if(! entryOwn.getNextHopNode().equals(nextHop))
					{
						if(entryOwn.getCost() > cost)
						{
							entryOwn.setCost(cost);
							entryOwn.setNextHopNode(nextHop);
						}
					}
					else
					{
						entryOwn.setCost(cost);
						entryOwn.setNextHopNode(nextHop);
					}
				}
			}
		}
	}

	// update table when cost changes
	public void updateOnCostChange()
	{
		RoutingTableAlgo newTable = new RoutingTableAlgo();
		RoutingTable changed;
		ArrayList<String> changedRouters = new ArrayList<>();
		String name = this.fileName.substring(0, this.fileName.lastIndexOf("."));
		this.routerName = name;
		changed = new RoutingTable(routerName, routerName, "-", 0.0);
		newTable.routingTables.add(changed);
		try {
			bufferedReader = new BufferedReader(new FileReader(this.fileName));
			String line = bufferedReader.readLine();
			while(line != null)
			{
				String[] contentsOfFile = line.split(" ");
				if(contentsOfFile.length==2)
				{
					String destination = contentsOfFile[0];
					double cost = Double.parseDouble(contentsOfFile[1]);
					changed = new RoutingTable(routerName, destination, destination, cost);
					newTable.routingTables.add(changed);
				}
				line = bufferedReader.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(Map.Entry<String, Double> entry : tableEntry.entrySet())
		{
			for(RoutingTable newEntry : newTable.getRoutingTables())
			{
				if(newEntry.getDestinationNode().equals(entry.getKey()))
				{
					if(Double.compare(newEntry.getCost(), entry.getValue()) != 0)
					{
						System.out.println("Link Cost changed");
						changedRouters.add(entry.getKey());
					}
				}
			}
		}
		
		for(RoutingTable entry : routingTables)
		{
			for(RoutingTable newEntry : newTable.getRoutingTables())
			{
				if(newEntry.getDestinationNode().equals(entry.getDestinationNode()))
				{
					if(changedRouters.contains(newEntry.getDestinationNode()))
					{
						entry.setCost(newEntry.getCost());
						tableEntry.put(newEntry.getDestinationNode(), newEntry.getCost());
						System.out.println("Routing table updated for link cost changes");
					}
				}
			}
		}
	}
	
	
	public synchronized void displayRoutingTable(List<RoutingTable> list)
	{
		for(RoutingTable entry : list)
		{
			System.out.println("Shortest Path " + entry.getSourceNode() + "-" + entry.getDestinationNode() + 
					" : the next hop is " + entry.getNextHopNode() + " and the cost is: " + entry.getCost());
		}
	}

	
}
