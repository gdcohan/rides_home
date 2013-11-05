public class Rider {

    public String destination;
    public boolean isAssigned;
    public int carId;
    public int Id;
	
    public Rider(String destination) {
	this.destination = destination;
	isAssigned = false;
    }
    
    public String getDestination() {
	return destination;
    }
    
    public boolean isAssigned(){
	return isAssigned;
    }
    
    public void setAssigned(boolean b) {
	isAssigned = b;
    }
    
}
