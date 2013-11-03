
public class Car {

	public String destination;
	public Rider[] riders;
	public boolean isFull;
	
	public Car(String destination) {
		this.destination = destination;
		riders = new Rider[4];
		isFull = false;
	}
}
