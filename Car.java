public class Car {

    public static final int CAPACITY = 4;
    
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
    
    public void addRider(Rider r) {
	if (isFull || r.isAssigned())
	    return;
	
	riders[numRiders++] = r;
	r.setAssigned(true);
	isFull = (numRiders == CAPACITY);
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
