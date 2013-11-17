
public class Evolution {

    public static final int NUM_GENERATIONS = 1000;
    public static final int INITIAL_POP_SIZE = 1000;
    public static final double PROPORTION_MATING = .667;
    public static final int NUM_MUTATIONS = 10;
    public static final int NUM_PRESERVED_POP = 100;
    
    public static void main(String[] args) {
	Initializer init = new Initializer(Initializer.TOWN_FILE, INITIALIZER.CAR_FILE);
	ArrayList<Car> allCars = init.getCars();
	Arraylist<Rider> allRiders = init.getRiders();

	ArrayList<Solution> population = generateInitialPopulation(allRiders);
	for (int i = 0; i < NUM_GENERATIONS; i++) {
	    Solution bestSolution = getBestSolution(population, allCars);
	    ArrayList<Solution> population = cullPopulation(population);
	    ArrayList<Solution> nextGen = produceOffspring(population);
	    population.addAll(nextGen);
	}
	bestSolution = getBestSolution(population);
	System.out.println("Your best solution is: " + bestSolution);
    }

    private static ArrayList<Solution> produceOffspring(ArrayList<Solution> population) {
	ArrayList<Solution> nextGen = new ArrayList<Solution>();
	shuffleList(population);
	int matingCutoff = population.size() * PROPORTION_MATING;
	for (int i = 0; i < matingCutoff; i = i+2) {
	    Solution offSpring = makeBaby(population.get(i), population.get(i+1));
	    nextGen.add(offSpring);
	}
	for (int i = matingCutoff; i < population.size(); i++) {
	    Solution mutated = mutateSolution(population.get(i));
	    nextGen.add(mutated);
	}
    }

    private static Solution mutateSolution(Solution mutatee) {
	Solution mutation = mutatee.clone();
	Rider[] riders = mutation.getRiders();
	for (int i = 0; i < NUM_MUTATIONS; i++) {
	    int i1 = (int)(Math.random() * riders.length);
	    int i2 = (int)(Math.random() * riders.length);
	    mutation[i1] = mutatee[i2];
	    mutation[i2] = mutatee[i1];
	}
	return mutation;
    }

    private static Solution makeBaby(Solution mommy, Solution daddy) {
	//Preserve a section of the mommy
	Solution baby = new Solution(new Rider[mommy.getRiders().length]);
	Rider[] mommysRiders = mommy.getRiders();
	Rider[] babysRiders = baby.getRiders();
	Rider[] daddysRiders = daddy.getRiders();
	int momStart = (Math.random()*babysRiders.length)/4;
	int momEnd = (Math.random()*babysRiders.length)/4;
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
	for (int i=0; i<4*mommyStart; i++) {
	    if (isRiderBetween(daddysRiders[i], babysRiders, 4*momStart, 4*momEnd)){
		babysRiders[i] = ghostRider;
		ghostLocs.add(i);
	    } else {
		babysRiders[i] = daddysRiders[i];
	    }
	}
	for (int i=4*mommyEnd; i<babysRiders.length; i++) {
	    if (isRiderBetween(daddysRiders[i], babysRiders, 4*momStart, 4*momEnd)){
		babysRiders[i] = ghostRider;
		ghostLocs.add(i);
	    } else {
		babysRiders[i] = daddysRiders[i];
	    }
	}
	//Find unassigned riders
	List<Rider> unassigned = new ArrayList<Rider>();
	for (int i=4*momStart; i<4*momEnd; i++){
	    if (!isRiderBetween(daddysRiders[i], mommysRiders, 4*momStart, 4*momEnd))
		unassigned.add(daddysRiders[i]);
	}
	//Replace the ghosts
	shuffleList(ghostLocs);
	shuffleList(unassigned);
	for (int i=0; i<ghostLocs.size() && i<unassigned.size(); i++) {
	    babysRiders[ghostLocs.get(i)] = unassigned.get(i);
	}
	
	return baby;
    }

    //Note: Doesn't check array[end]
    private static boolean isRiderBetween(Rider toCheck, Rider[] array, int start, int end) {
	for (int i=start; i<end; i++) {
	    if (toCheck == array[i])
		return true;
	}
	return false;
    }

    private static ArrayList<Solution> cullPopulation(ArrayList<Solution> population) {
	ArrayList<Solution> culledPop = new ArrayList<Solution>();
	Collections.sort(population, new Comparator<Solution>() {
		@Override
		public int compare(Solution s1, Solution s2) {
		    return (s1.getCost() < s2.getCost() ? -1 : (s1.getCost() == s2.getCost ? 0 : 1));
		}
	    });
	Random rand = new Random();
	// Preserve fixed amount from parent generation
	for (int i = 0; i < NUM_PRESERVED_POP; i++) {
	    culledPop.add(population.get(i));
	}
	population.removeRange(0, NUM_PRESERVED_POP);
	population.removeRange(population.size() - NUM_PRESERVED_POP, population.size());
	ArrayList<Solution> favorites = new ArrayList<Solution>();
	ArrayList<Solution> challengers = new ArrayList<Solution>();
	for (int i = 0; i < population.size() / 2; i++) {
	    favorites.add(population.get(i));
	    challengers.add(population.get(population.size() - i - 1));
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

    private static ArrayList<Solution> generateInitialPopulation(ArrayList<Rider> allRiders) {
	ArrayList<Solution> initialPop = new ArrayList<Solution>();
	for (int i=0; i<INITIAL_POP_SIZE; i++) {
	    List<Rider> solution = shuffleList(allRiders.clone());
	    
	    Rider[] solutionArray = new Rider[solution.size()];
	    solution.toArray(solutionArray);
	    initialPop.add(new Solution(solutionArray));
	}
    }

    private static Solution getBestSolution(ArrayList<Solution> population, ArrayList<Car> allCars) {
	int bestCostSoFar = Integer.MAX_VALUE;
	Solution bestSolutionSoFar;
	for (int i = 0; i < population.size(); i++) {
	    Solution currSoln = population.get(i);
	    int cost = currSoln.getCost(allCars);
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

    private static List<?> shuffleList(List<?> list) {
	Random rand = new Random();
	for (int i=list.size()-1; i>=0; i--) {
	    int x = rand.nextInt(i+1);
	    String temp = list.get(x);
	    list.set(x, list.get(i));
	    list.set(i, temp);
	}
	return list;
    }

    private class Solution() {
	private Rider[] riders;
	private int cost;
	private Car[] cars;

	private Solution(Rider[] riders, int cost, Car[] cars) {
	    this.riders = riders;
	    this.cost = cost;
	    this.cars = cars;
	}

	private Solution(Rider[] riders) {
	    this.riders = riders;
	}

	private int getCost(ArrayList<Car> allCars) {
	    int costSoFar = 0
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

	private Solution clone() {
	    return new Solution(riders.clone(), this.getCost());
	}
    }

}