package building;
//OWNER: Sania


// TODO: Auto-generated Javadoc
/**
 * The Class Passengers. Represents a GROUP of passengers that are 
 * traveling together from one floor to another. Tracks information that 
 * can be used to analyze Elevator performance.
 * 
 * Peer reviewed by Connor
 */
public class Passengers {
	
	/**  Constant for representing direction. */
	private static final int UP = 1;
	private static final int DOWN = -1;
	
	/**  ID represents the NEXT available id for the passenger group. */
	private static int ID=0;

	/** id is the unique ID assigned to each Passenger during construction.
	 *  After assignment, static ID must be incremented.
	 */
	private int id;
	
	/** These fields will be passed into the constructor by the Building.
	 *  This data will come from the .csv file read by the SimController
	 */
	private int time;         // the time that the Passenger will call the elevator
	private int numPass;      // the number of passengers in this group
	private int onFloor;      // the floor that the Passenger will appear on
	private int destFloor;	  // the floor that the Passenger will get off on
	private boolean polite;   // will the Passenger let the doors close?
	private int waitTime;     // the amount of time that the Passenger will wait for the
	                          // Elevator
	
	/** These values will be calculated during construction.
	 */
	private int direction;    // The direction that the Passenger is going
	private int timeWillGiveUp; // The calculated time when the Passenger will give up
	
	/** These values will actually be set during execution. Initialized to -1 */
	private int boardTime=-1;
	private int timeArrived=-1;

	/**
	 * Instantiates a new passengers.
	 *
	 * @param time the time
	 * @param numPass the number of people in this Passenger
	 * @param on the floor that the Passenger calls the elevator from
	 * @param dest the floor that the Passenger is going to
	 * @param polite - are the passengers polite?
	 * @param waitTime the amount of time that the passenger will wait before giving up
	 * Peer reviewed by Connor
	 */
	public Passengers(int time, int numPass, int on, int dest, boolean polite, int waitTime) {
	// TODO: Write the constructor for this class
	//       Remember to appropriately adjust the onFloor and destFloor to account  
	//       to convert from American to European numbering...
		this.time = time;
		this.numPass = numPass;
		this.onFloor = on-1;
		this.destFloor = dest-1;
		this.polite = polite;
		this.waitTime = waitTime;
		this.id = ID; 
		ID++;
		if(dest>on) {
			direction = 1;
		} else {
			direction = -1;
		}
		timeWillGiveUp = time+waitTime;
	}
	
	
	// TODO: Write any required getters/setters for this class

	// 
	/**
	 * Reset static ID. 
	 * This method MUST be called during the building constructor BEFORE
	 * reading the configuration files. This is to provide consistency in the
	 * Passenger ID's during JUnit testing.
	 * Peer reviewed by Connor
	 */
	static void resetStaticID() {
		ID = 0;
	}
	
	

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}


	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}


	/**
	 * @param time the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}


	/**
	 * @return the numPass
	 */
	public int getNumPass() {
		return numPass;
	}


	/**
	 * @param numPass the numPass to set
	 */
	public void setNumPass(int numPass) {
		this.numPass = numPass;
	}


	/**
	 * @return the onFloor
	 */
	public int getOnFloor() {
		return onFloor;
	}


	/**
	 * @param onFloor the onFloor to set
	 */
	public void setOnFloor(int onFloor) {
		this.onFloor = onFloor;
	}


	/**
	 * @return the destFloor
	 */
	public int getDestFloor() {
		return destFloor;
	}


	/**
	 * @param destFloor the destFloor to set
	 */
	public void setDestFloor(int destFloor) {
		this.destFloor = destFloor;
	}


	/**
	 * @return  whether or not passenger is polite
	 */
	public boolean isPolite() {
		return polite;
	}


	/**
	 * @param polite the polite to set
	 */
	public void setPolite(boolean polite) {
		this.polite = polite;
	}


	/**
	 * @return the waitTime
	 */
	public int getWaitTime() {
		return waitTime;
	}


	/**
	 * @param waitTime the waitTime to set
	 */
	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}


	/**
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}


	/**
	 * @param direction the direction to set
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}


	/**
	 * @return the timeWillGiveUp
	 */
	public int getTimeWillGiveUp() {
		return timeWillGiveUp;
	}


	/**
	 * @param timeWillGiveUp the timeWillGiveUp to set
	 */
	public void setTimeWillGiveUp(int timeWillGiveUp) {
		this.timeWillGiveUp = timeWillGiveUp;
	}


	/**
	 * @return the boardTime
	 */
	public int getBoardTime() {
		return boardTime;
	}


	/**
	 * @param boardTime the boardTime to set
	 */
	public void setBoardTime(int boardTime) {
		this.boardTime = boardTime;
	}


	/**
	 * @return the timeArrived
	 */
	public int getTimeArrived() {
		return timeArrived;
	}


	/**
	 * @param timeArrived the timeArrived to set
	 */
	public void setTimeArrived(int timeArrived) {
		this.timeArrived = timeArrived;
	}


	/**
	 * toString - returns the formatted string for this class
	 *
	 * @return the 
	 */
	@Override
	public String toString() {
		return("ID="+id+"   Time="+time+"   NumPass="+numPass+"   From="+(onFloor+1)+"   To="+(destFloor+1)+"   Polite="+polite+"   Wait="+waitTime);
	}

}
