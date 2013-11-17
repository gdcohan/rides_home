public class Car {

    public static final int CAPACITY = 4;
    
    private static HashMap<String, HashMap<String, Double>> distances;
    
    public String destination;
    private Rider[] riders;
    private int numRiders;
    public boolean isFull;
    public int id;
    
    public Car(String destination) {
	this.destination = destination;
	riders = new Rider[CAPACITY];
	numRiders = 0;
	isFull = false;
    }
    
    public static void setDistanceMatrix(HashMap<String, HashMap<String, Double>> distanceMatrix) {
    	distances = distanceMatrix;
    }
    
    public void addRider(Rider r) {
	if (isFull || r.isAssigned())
	    return;
	
	riders[numRiders++] = r;
	r.setAssigned(true);
	isFull = (numRiders == CAPACITY);
    }
    
    public void setRiders(Rider[] toSet) {
    	riders = toSet.clone();
    	numRiders = toSet.length;
    }
    
    public double getCost() {
    	
    	//1: Initialize a matrix of distances
    	double[][] carCosts = new double[CAPACITY+2][CAPACITY+2];
    	//carCosts[i][j] is dist from i to j 
    	
    	//Amherst to others
    	for (int i=1; i<carCosts.length-1; i++) {
    		carCosts[0][i] = distances.get("Amherst, MA").get(riders[i-1].getDestination());
    	}
    	//carCosts[0][carCost[0].length-1] = distances.get("Amherst, MA").get(destination);
    	
    	//Intra-riders
    	for (int i=1; i<carCosts.length-1; i++) {
    		for (int j=1; j<carCosts[i].length-1; j++) {
    			carCosts[i][j] = distances.get(riders[i-1].getDestination()).get(riders[j-1].getDestination());
    		}
    	}
    	
    	//Riders to driver
    	for (int i=1; i<carCosts.length-1; i++) {
    		carCosts[carCosts.length-1][i] = distances.get(riders[i-1].getDestination()).get(destination);
    	}
    	
    	//2: Greedily construct route
    	LinkedList<Integer> order = new LinkedList<Integer>();
    	order.add(0);
    	for (int i=1; i<carCosts.length-1; i++) {
    		order.addLast(findNextStop(carCosts[order.getLast()], order));
    	}
    	order.add(carCosts[0].length-1);
    	
    	//3: Return the total cost
    	double totalCost = 0;
    	for (int i=1; i<order.size(); i++) {
    		totalCost += carCosts[order.get(i-1)][order.get(i)];
    	}
    	
    	return totalCost;
    		
    	
    }
    
    private int findNextStop(double[] dists, LinkedList<Integer> excluding) {
    	//Don't check first or last stop
    	double minCost = Double.MAX_VALUE;
    	int minIndex = -1;
    	for (int i=1; i<dists.length-1; i++) {
    		if(!excluding.contains(i)) {
    			double cost = dists[i];
    			if (cost < minCost){
    				minCost = cost;
    				minIndex = i;
    			}
    		}
    		
    	}
    }
    
    public void swapRiders(Rider departing, Rider arriving) {
	for (int i = 0; i < numRiders; i++) {
	    if (departing.Id == riders[i].Id) {
		riders[i] = arriving;
	    }
	}
    }

   public boolean isFull() {
	return isFull;
    }
    
    public String getDestination() {
	return destination;
    }
}
