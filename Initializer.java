import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

//Written by Mike
public class Initializer {

	public static final String LOCAL_DEBUG = "src/";
	public static final String TOWN_FILE = LOCAL_DEBUG + "student_towns.txt";
	public static final String CAR_FILE = LOCAL_DEBUG + "Student Cars by State.csv";
	public static final long CAR_SEED = 123456l;
	
	private ArrayList<Car> cars;
	private ArrayList<Rider> riders;
	
	// Reads in the students, cars, and assigns
	// the cars to specific students 
	// (we had which state the cars were registered,
	// so we randomly assigned the car to a student
	// from that state)
	public Initializer(String studentFile, String carFile) {
		
		List<String> students = parseStudents(studentFile);
		HashMap<String, Integer> carsByState = parseCars(carFile);
		
		cars = new ArrayList<Car>();
		riders = new ArrayList<Rider>();
		assignDrivers(CAR_SEED, students, carsByState, cars, riders);
		
		//groupSameTowns(cars, riders);
		
	}
	
	public ArrayList<Car> getCars(){
		return cars;
	}
	
	public ArrayList<Rider> getRiders() {
		return riders;
	}
	
	private void groupSameTowns(List<Car> cars, List<Rider> riders) {
		for (Rider rider : riders) {
			for (Car car : cars) {
				if (!car.isFull() && car.getDestination().equals(rider.getDestination()))
					car.addRider(rider);
			}
		}
	}
	
	//cars and riders are outputs (will be modified)
	private void assignDrivers(long randSeed, List<String> students,
								HashMap<String, Integer> carsByState,
								List<Car> cars, List<Rider> riders) {
		
		for (Entry<String, Integer> entry : carsByState.entrySet()) {
			String state = entry.getKey();
			List<String> studentsInState = getStudentsInState(students, state);
			
			shuffleList(randSeed, studentsInState);
			
			int i=0;
			int count = entry.getValue();
			for (String student : studentsInState) {
				if (i < count) {
					cars.add(new Car(student));
				} else {
					riders.add(new Rider(student));
				}
				i++;
			}
		}
		
		
	}
	
	private void shuffleList(long randSeed, List<String> list) {
		Random rand = new Random(randSeed);
		for (int i=list.size()-1; i>=0; i--) {
			int x = rand.nextInt(i+1);
			String temp = list.get(x);
			list.set(x, list.get(i));
			list.set(i, temp);
		}
	}
	
	private ArrayList<String> getStudentsInState(List<String> students, String state) {
		
		ArrayList<String> studentsInState = new ArrayList<String>();
		
		for (String s : students) {
			String[] splits = s.split(",");
			String studentState = splits[splits.length-1].trim().toUpperCase();
			if (studentState.equals(state)) {
				studentsInState.add(s);
			}
		}
		
		return studentsInState;
		
	}
	
	private HashMap<String, Integer> parseCars(String carFile) {
		HashMap<String, Integer> carsByState = new HashMap<String, Integer>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(carFile));
		 
			String line;
			while ((line=in.readLine()) != null) {
				String[] splits = line.split(",");
				carsByState.put(splits[0].trim().toUpperCase(), Integer.parseInt(splits[1]));
			}
		 
		} catch(Exception e) {
			System.out.println(e);
		}
		return carsByState;
	}
	
	private List<String> parseStudents(String studentFile) {
		List<String> students = new LinkedList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(studentFile));
		 
			String line;
			while ((line=in.readLine()) != null) {
				students.add(line.trim());
			}
		 
		} catch(Exception e) {
			System.out.println(e);
		}
		return students;
	}
	
	public static void main(String[] args) {
		
		Initializer init = new Initializer(TOWN_FILE, CAR_FILE);
		
	}

}
