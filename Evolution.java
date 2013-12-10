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


public class Evolution {

    public static final int NUM_GENERATIONS = 200;
    public static final int INITIAL_POP_SIZE = 100; //1000;
    public static final double PROPORTION_MATING = .667;
    public static final int NUM_MUTATIONS = 50;
    public static final int NUM_PRESERVED_POP = 10; //100
    
    public static void main(String[] args) {
		Initializer init = new Initializer(Initializer.TOWN_FILE, Initializer.CAR_FILE);
		Car.setDistanceMatrix(getFakeDistanceMatrix());
		ArrayList<Car> allCars = init.getCars();
		ArrayList<Rider> allRiders = init.getRiders();
	
		ArrayList<Solution> population = generateInitialPopulation(allRiders, allCars);
		Solution initHillClimb = population.get(0);
		Solution bestSolution;
		HashMap<String, HashMap<String, Double>> distances = Car.getDistanceMatrix();
		for (int i = 0; i < NUM_GENERATIONS; i++) {
		    bestSolution = getBestSolution(population, allCars);
		    population = cullPopulation(population, allCars);
		    ArrayList<Solution> nextGen = produceOffspring(population, allCars);
		    population.addAll(nextGen);
		    System.out.println(bestSolution.getCost());
		}
		bestSolution = getBestSolution(population, allCars);
		System.out.println("Your best solution is: " + bestSolution.getCost());
		hillClimb(initHillClimb, allRiders);
    }
    
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
    
    public static Point findBetterSwap(Solution soln, ArrayList<Rider> riders) {
    	int currCost = soln.getCost();
    	for (int i = 0; i < riders.size(); i++) {
    		for (int j = i + 1; j < riders.size(); j++) {
    			Rider temp = soln.riders[i];
    			soln.riders[i] = soln.riders[j];
    			soln.riders[j] = temp;
    			int tryCost = soln.getCost();
    			if (tryCost < currCost) {
    				return new Point(i, j);
    			} else {
    				temp = soln.riders[i];
    				soln.riders[i] = soln.riders[j];
    				soln.riders[j] = temp;
    			}
    		}
    	}
    	return new Point(-1, -1);
    }
    
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
    	/*Iterator<String> it = toReturn.keySet().iterator();
    	while (it.hasNext()) {
    		String name = it.next();
    		HashMap<String, Double> map = toReturn.get(name);
    		System.out.println(name + " has the following map:");
    		System.out.println(map);
    	}*/
    	return toReturn;
    }

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
		return nextGen;
    }

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
		//Find unassigned riders
		
		/*for (int i=0; i<daddysRiders.length; i++){//4xstart - 4xend
			if (!isRiderIn(daddysRiders[i], babysRiders))
		    	unassigned.add(daddysRiders[i]);
		}*/
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

    private static boolean isRiderIn(Rider toCheck, Rider[] array) {
    	for (int i = 0; i < array.length; i++) {
    		if (toCheck == array[i]) return true;
    	}
    	return false;
    }
    
    //Note: Doesn't check array[end]
    private static boolean isRiderBetween(Rider toCheck, Rider[] array, int start, int end) {
	for (int i=start; i<end; i++) {
	    if (toCheck == array[i])
		return true;
	}
	return false;
    }

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
		
		/*for (int i = population.size() - 1; i > population.size() - NUM_PRESERVED_POP - 1; i--) {
			culledPop.add(population.get(i));
		}*/
		System.out.println("Before KR: " + population.size());
		population = keepRange(population, NUM_PRESERVED_POP, population.size());
		System.out.println("After KR: " + population.size());
		
		
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
		    if (Math.random() < favoriteOdds) 
		    	culledPop.add(favorite);
		    else 
		    	culledPop.add(challenger);
		}
		return culledPop;
    }

    private static ArrayList keepRange(ArrayList array, int startIndex, int endIndex) {
    	ArrayList newList = new ArrayList();
    	for (int i = startIndex; i < endIndex; i++) {
    		newList.add(array.get(i));
    	}
    	return newList;
    }
    
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

    /*
    private static int getCostOfSolution(Rider[] solution, ArrayList<Car> allCars) {
	ArrayList<Car> currCars = allCars.clone();
	for (int i =0 ; i < solution.length; i++) {
	    currCars.get(i / 4).addRider(solution[i]);
	}
	int costSoFar = 0;
	for (int i = 0; i < currCars.size(); i++) {
	    costSoFar += currCars.get(i).getCost();
	}
	return costSoFar;
    }
    */

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

    private static class Solution {
		private Rider[] riders;
		private int cost;
		private ArrayList<Car> allCars;
	
		private Solution(Rider[] riders, int cost, ArrayList<Car> allCars) {
		    this.riders = riders;
		    this.cost = cost;
		    this.allCars = allCars;
		}
	
		/*
		private Solution(Rider[] riders) {
		    this.riders = riders;
		}
		*/
	
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