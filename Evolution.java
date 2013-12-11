import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Written by Greg and Mike
public class Evolution {

    public static final int NUM_GENERATIONS = 1000;
    public static final int INITIAL_POP_SIZE = 1000; //1000;
    public static final double PROPORTION_MATING = .667;
    public static final int NUM_MUTATIONS = 10;
    public static final int NUM_PRESERVED_POP = 200; //100
    
    
    /*
     * The main method: 
     * 1) Generates a greedy solution
     * 2) Populates an initial population with clones of the greedy solution
     * 3) Runs evolutionary algorithm on the initial population
     * 4) Runs hillclimbing on the initial greedy solution
     */
    public static void main(String[] args) {
		Initializer init = new Initializer(Initializer.TOWN_FILE, Initializer.CAR_FILE);
		Car.setDistanceMatrix(getFakeDistanceMatrix());
		HashMap<String, HashMap<String, Double>> distances = Car.getDistanceMatrix();
		ArrayList<Car> allCars = init.getCars();
		ArrayList<Rider> allRiders = init.getRiders();
		Solution greedySolution = getGreedySolution(allRiders.toArray(new Rider[allRiders.size()]), 0, allCars, distances);
		ArrayList<Solution> population = generateInitialGreedyPopulation(greedySolution);
		//ArrayList<Solution> population = generateInitialPopulation(allRiders, allCars);
		Solution initHillClimb = greedySolution; //population.get(0);
		Solution bestSolution;
		
		// Evolution
		for (int i = 0; i < NUM_GENERATIONS; i++) {
		    bestSolution = getBestSolution(population, allCars);
		    System.out.println(bestSolution.getCost());
		    population = cullPopulation(population, allCars);
		    ArrayList<Solution> nextGen = produceOffspring(population, allCars);
		    population.addAll(nextGen);
		}
		bestSolution = getBestSolution(population, allCars);
		System.out.println("Your best solution is: " + bestSolution.getCost());
		
		// Hill climbing
		hillClimb(initHillClimb, allRiders);
    }
    
    // Takes initial greedy population and clones it to generate initial population
    public static ArrayList<Solution> generateInitialGreedyPopulation(Solution greedySolution) {
		ArrayList<Solution> initialPop = new ArrayList<Solution>();
		for (int i=0; i<INITIAL_POP_SIZE; i++) {
			initialPop.add(greedySolution.clone());
		}
		return initialPop;
    	
    }
    
    // Generates a greedy solution by taking each rider in order and assigning
    // him to the car that results in the lowest increase in cost
    public static Solution getGreedySolution(Rider[] riders, int cost, ArrayList<Car> allCars, HashMap<String, HashMap<String, Double>> distances) {
    	for (Rider rider : riders) {
    		double lowestCostInsert = Double.MAX_VALUE;
    		int lowestCarIndex = -1;
    		for (int i = 0; i < riders.length / 4 + 1; i++) {
    			Car car = allCars.get(i);
    			if (!car.isFull()) {
    				double currCost = car.getCost();
    				car.addRider(rider);
    				double newCost =  car.getCost();
    				car.removeLastRider();
    				double costDelta = newCost - currCost;
    				if (costDelta < lowestCostInsert) {
    					lowestCostInsert = costDelta;
    					lowestCarIndex = i;
    				}	
    			}
    		}
    		Car car = allCars.get(lowestCarIndex);
    		car.addRider(rider);
    	}
    	Rider[] newRiders = new Rider[riders.length];
    	int riderIndex = 0;
    	for (int i = 0; i < riders.length /4; i++) {
    		Car car = allCars.get(i);
    		Rider[] carsRiders = car.getRiders();
    		for (Rider rider : carsRiders) {
    			newRiders[riderIndex++] = rider;
    		}
    	}
    	Solution toReturn = new Solution(newRiders, 0, allCars);
    	return toReturn;
    }
    
    /*
     * On each iteration, the hill climbing algorithm finds
     * the first swap of two riders that reduces the overall
     * cost of the solution. If it can't find a cost reduction, 
     * it returns early.
     */
    public static void hillClimb(Solution initialSoln, ArrayList<Rider> riders) {
    	System.out.println("Starting Hill Climb");
    	for (int i = 0; i < 10000; i++) {
    		Point bestSwap = findBetterSwap(initialSoln, riders);
    		if (bestSwap.getX() == -1 && bestSwap.getY() == -1) {
    			System.out.println("Couldn't improve on: " + initialSoln.getCost());
    			return;
    		}
    		System.out.println(initialSoln.getCost());
    	}
    }
    
    // Finds the first swap of riders that improves
    // the cost of the solution
    public static Point findBetterSwap(Solution soln, ArrayList<Rider> riders) {
    	int counter = 0;
    	int currCost = soln.getCost();
    	for (int i = 0; i < riders.size(); i++) {
    		for (int j = i + 1; j < riders.size(); j++) {
    			Rider temp = soln.riders[i];
    			soln.riders[i] = soln.riders[j];
    			soln.riders[j] = temp;
    			int tryCost = soln.getCost();
    			if (tryCost < currCost) {
    				//System.out.println("Checked this many swaps: " + counter);
    				return new Point(i, j);
    			} else {
    				temp = soln.riders[i];
    				soln.riders[i] = soln.riders[j];
    				soln.riders[j] = temp;
    			}
    			counter++;
    		}
    	}
    	return new Point(-1, -1);
    }
    
    // Generates a fake distance matrix by assigning a distance for every possible
    // pair of destinations
    // Uses an exponential distribution for the distances with mean of 4 hrs
    public static HashMap<String, HashMap<String, Double>> getFakeDistanceMatrix() {
    	HashMap<String, HashMap<String, Double>> toReturn = new HashMap<String, HashMap<String, Double>>();
    	
    	Set<String> towns = new HashSet<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Initializer.TOWN_FILE));
		 
			String line;
			while ((line=in.readLine()) != null) {
				towns.add(line.trim());
			}
		 
		} catch(Exception e) {
			System.out.println(e);
		}
		
		for (String source : towns) {
			HashMap<String, Double> distMap = new HashMap<String, Double>();
			for (String destination : towns) {
				double cost = (Math.log(1-Math.random())*(-4));
				distMap.put(destination, cost);
			}
			toReturn.put(source, distMap);
		}
    	return toReturn;
    }

    /*
     * Takes a fixed proportion of the population and mates it,
     * then takes the rest of the population and mutates it.
     * Together, these generate the offspring to add to the next
     * generation.
     */
    private static ArrayList<Solution> produceOffspring(ArrayList<Solution> population, ArrayList<Car> allCars) {
		ArrayList<Solution> nextGen = new ArrayList<Solution>();
		shuffleList(population);
		int matingCutoff = (int) (population.size() * PROPORTION_MATING);
		for (int i = 0; i < matingCutoff; i = i+2) {
		    Solution offSpring = makeBaby(population.get(i), population.get(i+1), allCars);
		    nextGen.add(offSpring);
		}
		for (int i = matingCutoff; i < population.size(); i++) {
		    Solution mutated = mutateSolution(population.get(i));
		    nextGen.add(mutated);
		}
		//System.out.println("Improved: " + improve + ", Not improved: " + noImprove);
		return nextGen;
    }

    // Mutates a solution by making a fixed number of random swaps
    private static Solution mutateSolution(Solution mutatee) {
		Solution mutation = mutatee.clone();
		Rider[] oldRiders = mutatee.getRiders();
		Rider[] riders = mutation.getRiders();
		for (int i = 0; i < NUM_MUTATIONS; i++) {
		    int i1 = (int)(Math.random() * riders.length);
		    int i2 = (int)(Math.random() * riders.length);
		    riders[i1] = oldRiders[i2];
		    riders[i2] = oldRiders[i1];
		}
		return mutation;
    }

    /*
     * Mates two solution to make an offspring:
     * 1) Preserves a random segment of the 'mother'
     * 2) Preserves as much as possible from the 'father', given step (1)
     * 3) Randomly assigns all the leftover riders
     */
    private static Solution makeBaby(Solution mommy, Solution daddy, ArrayList<Car> allCars) {
		//Preserve a section of the mommy
		Solution baby = new Solution(new Rider[mommy.getRiders().length], 0, allCars);
		Rider[] mommysRiders = mommy.getRiders();
		Rider[] babysRiders = baby.getRiders();
		Rider[] daddysRiders = daddy.getRiders();
		int momStart = (int) ((Math.random()*babysRiders.length)/4);
		int momEnd = (int) ((Math.random()*babysRiders.length)/4);
		if (momEnd < momStart) {
		    int temp = momEnd;
		    momEnd = momStart;
		    momStart = temp;
		}
		for (int i=4*momStart; i<4*momEnd; i++) {
		    babysRiders[i] = mommysRiders[i];
		}
		
		//Preserve as much as possible from the daddy, adding ghosts
		Rider ghostRider = new Rider("BOOOOO!");
		List<Integer> ghostLocs = new ArrayList<Integer>();
		List<Rider> unassigned = new ArrayList<Rider>();
		for (int i=0; i<4*momStart; i++) {
		    //if (isRiderBetween(daddysRiders[i], babysRiders, 4*momStart, 4*momEnd)){
			if (isRiderIn(daddysRiders[i], babysRiders)) {
		    	babysRiders[i] = ghostRider;
				ghostLocs.add(i);
				unassigned.add(daddysRiders[i]);
		    } else {
		    	babysRiders[i] = daddysRiders[i];
		    }
		}
		for (int i=4*momEnd; i<babysRiders.length; i++) {
		    //if (isRiderBetween(daddysRiders[i], babysRiders, 0, babysRiders.length)){ //Daddy's rider is already accounted for
			if (isRiderIn(daddysRiders[i], babysRiders)) {
		    	babysRiders[i] = ghostRider;
		    	ghostLocs.add(i);
		    	unassigned.add(daddysRiders[i]);
		    } else {
		    	babysRiders[i] = daddysRiders[i];
		    }
		}
		//Replace the ghosts
		shuffleList(ghostLocs);
		shuffleList(unassigned);
		
		if ( ghostLocs.size() != unassigned.size()) {
			System.out.println("Mismatch: " + ghostLocs.size() + " " + unassigned.size());
		}
		
		for (int i=0; i<ghostLocs.size() && i<unassigned.size(); i++) {
		    babysRiders[ghostLocs.get(i)] = unassigned.get(i);
		}
		
		
		return baby;
    }

    // helper method to check if a rider is in an array of riders
    private static boolean isRiderIn(Rider toCheck, Rider[] array) {
    	for (int i = 0; i < array.length; i++) {
    		if (toCheck == array[i]) return true;
    	}
    	return false;
    }
    
    /*
     * Culls the population in two steps:
     * 1) Preserves a fixed portion of the best solutions (i.e. the top 10, 50, etc.)
     * 2) Runs a tournament with the remainder of the solutions, matching up a 'favored'
     * solution against a 'challenger' solution. The probability of the favored solution
     * beating the challenger is proportional to how much lower cost the favored solution is.
     */
    private static ArrayList<Solution> cullPopulation(ArrayList<Solution> population, final ArrayList<Car> allCars) {
    	ArrayList<Solution> culledPop = new ArrayList<Solution>();
    	Collections.sort(population, new Comparator<Solution>() {
    		@Override
    		public int compare(Solution s1, Solution s2) {
    			if (s1.getCost() < s2.getCost()) return -1;
    			else if (s1.getCost() == s2.getCost()) return 0;
    			else return 1;
    		}
	    });
		Random rand = new Random();
		// Preserve fixed amount from parent generation
		for (int i = 0; i < NUM_PRESERVED_POP; i++) {
		    culledPop.add(population.get(i));
		}

		population = keepRange(population, NUM_PRESERVED_POP, population.size());
		
		ArrayList<Solution> favorites = new ArrayList<Solution>();
		ArrayList<Solution> challengers = new ArrayList<Solution>();
		for (int i = 0; i < population.size() / 2; i++) {
		    favorites.add(population.get(i));
		    challengers.add(population.get(population.size() - NUM_PRESERVED_POP - i - 1));
		}
		shuffleList(challengers);
		for (int i = 0; i < favorites.size(); i++) {
		    Solution favorite = favorites.get(i);
		    Solution challenger = challengers.get(i);
		    double favoriteOdds = favorite.getCost() / (favorite.getCost() + challenger.getCost());
		    if (Math.random() > favoriteOdds) 
		    	culledPop.add(favorite);
		    else 
		    	culledPop.add(challenger);
		}
		return culledPop;
    }

    // Helper method to keep a certain portion of a list
    private static ArrayList keepRange(ArrayList array, int startIndex, int endIndex) {
    	ArrayList newList = new ArrayList();
    	for (int i = startIndex; i < endIndex; i++) {
    		newList.add(array.get(i));
    	}
    	return newList;
    }
    
    // Generates an initial population of random solutions
    private static ArrayList<Solution> generateInitialPopulation(ArrayList<Rider> allRiders, ArrayList<Car> allCars) {
		ArrayList<Solution> initialPop = new ArrayList<Solution>();
		for (int i=0; i<INITIAL_POP_SIZE; i++) {
		    List<Rider> solution = shuffleList((List<Rider>)(allRiders.clone()));
		    
		    Rider[] solutionArray = new Rider[solution.size()];
		    solution.toArray(solutionArray);
		    initialPop.add(new Solution(solutionArray, 0, allCars));
		}
		return initialPop;
    }

    // Finds the best solution in a population
    private static Solution getBestSolution(ArrayList<Solution> population, ArrayList<Car> allCars) {
		int bestCostSoFar = Integer.MAX_VALUE;
		Solution bestSolutionSoFar = null;
		for (int i = 0; i < population.size(); i++) {
		    Solution currSoln = population.get(i);
		    int cost = currSoln.getCost();
		    if (cost < bestCostSoFar) {
				bestCostSoFar = cost;
				bestSolutionSoFar = currSoln;
		    }
		}
		return bestSolutionSoFar;
    }

    // Shuffles a list
    private static List shuffleList(List list) {
		Random rand = new Random();
		for (int i=list.size()-1; i>=0; i--) {
		    int x = rand.nextInt(i+1);
		    Object temp = list.get(x);
		    list.set(x, list.get(i));
		    list.set(i, temp);
		}
		return list;
    }

    // Private class to represent a solution
    private static class Solution {
		private Rider[] riders;
		private int cost;
		private ArrayList<Car> allCars;
	
		private Solution(Rider[] riders, int cost, ArrayList<Car> allCars) {
		    this.riders = riders;
		    this.cost = cost;
		    this.allCars = allCars;
		}
		
		// Computes the cost of a solution by computing the cost of each
		// car within the solution
		private int getCost() {
		    int costSoFar = 0;
		    for (int i = 0; i < riders.length; i++) {
			int carIndex = i/4;
			Car currCar = allCars.get(carIndex);
			Rider[] carsRiders = new Rider[4];
			for (int j = 0; j < 4; j++) {
			    carsRiders[j] = riders[i++];
			}
			currCar.setRiders(carsRiders);
			costSoFar += (int) currCar.getCost();
		    }
		    return costSoFar;
		}
	
		private Rider[] getRiders() {
		    return riders;
		}
	
		public Solution clone() {
		    return new Solution(riders.clone(), this.getCost(), allCars);
		}
    }
}