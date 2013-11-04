public class Car {

	public static final int CAPACITY = 4;

	public String destination;
	private Rider[] riders;
	private int numRiders;
	public boolean isFull;
	
	public Car(String destination) {
		this.destination = destination;
		riders = new Rider[CAPACITY];
		numRiders = 0;
		isFull = false;
	}
	
	public void addRider(Rider r) {
		if (isFull)
			return;
		
		riders[numRiders++] = r;
		isFull = (numRiders == CAPACITY);
	}
	
	public boolean isFull() {
		return isFull;
	}
	
	public String getDestination() {
		return destination;
	}
}
