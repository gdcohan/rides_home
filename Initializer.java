import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

public class Initializer {

	public static final String LOCAL_DEBUG = "";
	public static final String TOWN_FILE = LOCAL_DEBUG + "student_towns.txt";
	public static final String CAR_FILE = LOCAL_DEBUG + "Student Cars by State.csv";
	public static final long CAR_SEED = 123456l;
	
	
	public Initializer(String studentFile, String carFile) {
		
		List<String> students = parseStudents(studentFile);
		HashMap<String, Integer> carsByState = parseCars(carFile);
		
		List<Car> cars = new ArrayList<Car>();
		List<Rider> riders = new ArrayList<Rider>();
		assignDrivers(CAR_SEED, students, carsByState, cars, riders);
		
	}
	
	
	
	//cars and riders are outputs (will be modified)
	private void assignDrivers(long randSeed, List<String> students,
								HashMap<String, Integer> carsByState,
								List<Car> cars, List<Rider> riders) {
		
		for (Entry entry : carsByState.getEntrySet()) {
			String state = entry.getKey();
			List<String> studentsInState = getStudentsInState(students, state);
			
			int i=0;
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
	
	private void shuffleList(long randSeed, ArrayList<String> list) {
		//TODO implement
	}
	
	private ArrayList<String> getStudentsInState(List<String> students, String state) {
		
		ArrayList<String> studentsInState = new ArrayList<String>();
		
		for (Student s : students) {
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
