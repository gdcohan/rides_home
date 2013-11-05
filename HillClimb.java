import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class HillClimb {
    
    public static final int NUM_SWAPS = 50;
    
    public static void main(String[] args) {
	ArrayList<Rider> riders = new ArrayList<Rider>();
	ArrayList<Car> cars = new ArrayList<Car>();
	ArrayList<ArrayList<Integer>> bestSwaps = new ArrayList<ArrayList<Integer>>();
	for (int i = 0; i < riders.size(); i++) {
	    Rider currRider = riders.get(i);
	    Set<Integer> swaps = new HashSet<Integer>();
	    int bestCostDelta = 0;
	    int swap = -1;
	    for (int j = 0; j < NUM_SWAPS; j++) {
		int index;
		while (true) {
		    Random rand = new Random(); 
		    index = rand.nextInt(riders.size()); 
		    if (!swaps.contains(index)) {
			swaps.add(index);
			break;
		    }
		}
		swapRiders(currRider, riders.get(index), cars);
		int costDelta = getCostChange();
		if (costDelta > bestCostDelta) {
		    bestCostDelta = costDelta;
		    swap = index;
		}
		swapRiders(riders.get(index), currRider, cars);
	    }
	    ArrayList<Integer> toAdd = new ArrayList<Integer>();
	    toAdd.add(i);
	    toAdd.add(swap);
	    toAdd.add(bestCostDelta);
	    bestSwaps.add(toAdd);
	}
	pickBestNonInterferingSwaps(bestSwaps, riders, cars);
    }
    
    public static void pickBestNonInterferingSwaps(ArrayList<ArrayList<Integer>> swaps, ArrayList<Rider> riders, ArrayList<Car> cars) {
	Collections.sort(swaps, new SwapComparator());
	Set<Integer> carsChanged = new HashSet<Integer>();
	for (int i = 0; i < swaps.size(); i++) {
	    ArrayList<Integer> thisSwap = swaps.get(i);
	    Rider firstRider = riders.get(thisSwap.get(0));
	    Rider secondRider = riders.get(thisSwap.get(1));
	    int firstRidersCar = firstRider.carId;
	    int secondRidersCar = secondRider.carId;
	    if (!carsChanged.contains(firstRidersCar) && !carsChanged.contains(secondRidersCar)) {
		carsChanged.add(firstRidersCar);
		carsChanged.add(secondRidersCar);
		swapRiders(firstRider, secondRider, cars);
	    }
	}
    }
    
    private static void swapRiders(Rider currRider, Rider swappingRider, ArrayList<Car> cars) {
	Car car1 = (currRider.carId != -1) ? cars.get(currRider.carId) : null;
	Car car2 = (swappingRider.carId != -1) ? cars.get(swappingRider.carId) : null;
	String tempDest = currRider.destination;
	boolean tempAssigned = currRider.isAssigned;
	int tempCarId = currRider.carId;
	currRider.destination = swappingRider.destination;
	currRider.isAssigned = swappingRider.isAssigned;
	currRider.carId = swappingRider.carId;
	swappingRider.destination = tempDest;
	swappingRider.isAssigned = tempAssigned;
	swappingRider.carId = tempCarId;
	if (car1 != null) car1.swapRiders(currRider, swappingRider);
	if (car2 != null) car2.swapRiders(swappingRider, currRider);
    }
    
    private static int getCostChange() {
	// put stuff here
	// 1. get old cost of first rider's car (should be passed as param)
	// 2. get old cost second rider's car
	// 3. get new costs
	// 4. subtract
	return 0;
    }
}