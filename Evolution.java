
public class Evolution {

    public static final int NUM_GENERATIONS = 1000;
    public static final int INITIAL_POP_SIZE = 1000;
    public static final double PROPORTION_MATING = .667;
    public static final int NUM_MUTATIONS = 10;
    
    public static void main(String[] args) {
	Initializer init = new Initializer(Initializer.TOWN_FILE, INITIALIZER.CAR_FILE);
	ArrayList<Car> allCars = init.getCars();
	Arraylist<Rider> allRiders = init.getRiders();

	ArrayList<Rider[]> population = generateInitialPopulation(allRiders);
	for (int i = 0; i < NUM_GENERATIONS; i++) {
	    Rider[] bestSolution = getBestSolution(population, allCars);
	    int cost = getCostOfSolution(bestSolution, allCars);
	    ArrayList<Rider[]> population = cullPopulation(population);
	    ArrayList<Rider[]> nextGen = produceOffspring(population);
	    population.addAll(nextGen);
	}
	bestSolution = getBestSolution(population);
	System.out.println("Your best solution is: " + bestSolution);
    }

    private static ArrayList<Rider[]> produceOffspring(ArrayList<Rider[]> population) {
	ArrayList<Rider[]> nextGen = new ArrayList<Rider[]>();
	shuffleList(population);
	int matingCutoff = population.size() * PROPORTION_MATING;
	for (int i = 0; i < matingCutoff; i = i+2) {
	    Rider[] offSpring = makeBaby(population.get(i), population.get(i+1));
	    nextGen.add(offSpring);
	}
	for (int i = matingCutoff; i < population.size(); i++) {
	    Rider[] mutated = mutateSolution(population.get(i));
	    nextGen.add(mutated);
	}
    }

    private static Rider[] mutateSolution(Rider[] mutatee) {
	Rider[] mutation = mutatee.clone();
	for (int i = 0; i < NUM_MUTATIONS; i++) {
	    int i1 = (int)(Math.random() * mutation.length);
	    int i2 = (int)(Math.random() * mutation.length);
	    mutation[i1] = mutatee[i2];
	    mutation[i2] = mutatee[i1];
	}
	return mutation;
    }

    private static Rider[] makeBaby(Rider[] mommy, Rider[] daddy) {
	//Preserve a section of the mommy
	Rider[] baby = new Rider[mommy.length];
	int momStart = (Math.random()*mommy.length)/4;
	int momEnd = (Math.random()*mommy.length)/4;
	if (momEnd < momStart) {
	    int temp = momEnd;
	    momEnd = momStart;
	    momStart = temp;
	}
	for (int i=4*momStart; i<4*momEnd; i++) {
	    baby[i] = mommy[i];
	}
	
	//Preserve as much as possible from the daddy, adding ghosts
	Rider ghostRider = new Rider("BOOOOO!");
	List<Integer> ghostLocs = new ArrayList<Integer>();
	for (int i=0; i<4*mommyStart; i++) {
	    if (isRiderBetween(daddy[i], baby, 4*momStart, 4*momEnd)){
		baby[i] = ghostRider;
		ghostLocs.add(i);
	    } else {
		baby[i] = daddy[i];
	    }
	}
	for (int i=4*mommyEnd; i<baby.length; i++) {
	    if (isRiderBetween(daddy[i], baby, 4*momStart, 4*momEnd)){
		baby[i] = ghostRider;
		ghostLocs.add(i);
	    } else {
		baby[i] = daddy[i];
	    }
	}
	//Find unassigned riders
	List<Rider> unassigned = new ArrayList<Rider>();
	for (int i=4*momStart; i<4*momEnd; i++){
	    if (!isRiderBetween(daddy[i], mommy, 4*momStart, 4*momEnd))
		unassigned.add(daddy[i]);
	}
	//Replace the ghosts
	shuffleList(ghostLocs);
	shuffleList(unassigned);
	for (int i=0; i<ghostLocs.size() && i<unassigned.size(); i++) {
	    baby[ghostLocs.get(i)] = unassigned.get(i);
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

    private static ArrayList<Rider[]> cullPopulation(ArrayList<Rider[]> population) {
	ArrayList<Rider[]> culledPop = new ArrayList<Rider[]>();
	double[] probs = getProportionalProbs(population);
	for (int i = 0; i < population.length; i++) {
	    if (Math.random() < probs[i]) culledPop.add(population.get(i));
	}
	return culledPop;
    }

    // TODO: IMPLEMENT THIS METHOD--FO REAL
    private static double[] getProportionalProbs(ArrayList<Rider[]> population) {
	double[] probs = new double[population.size()];
	for (int i = 0; i < probs.length; i++) {
	    probs[i] = .5;
	}
    }

    private static ArrayList<Rider[]> generateInitialPopulation(ArrayList<Rider> allRiders) {
	ArrayList<Rider[]> initialPop = new ArrayList<Rider[]>();
	for (int i=0; i<INITIAL_POP_SIZE; i++) {
	    List<Rider> solution = shuffleList(allRiders.clone());
	    Rider[] solutionArray = new Rider[solution.size()];
	    solution.toArray(solutionArray);
	    initialPop.add(solution);
	}
    }

    private static Rider[] getBestSolution(ArrayList<Rider[]> population, ArrayList<Car> allCars) {
	int bestCostSoFar = Integer.MAX_VALUE;
	Rider[] bestSolutionSoFar;
	for (int i = 0; i < population.size(); i++) {
	    Rider[] currSoln = population.get(i);
	    int cost = getCostOfSolution(currSoln, allCars);
	    if (cost < bestCostSoFar) {
		bestCostSoFar = cost;
		bestSolutionSoFar = currSoln;
	    }
	}
	return bestSolutionSoFar;
    }

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

}