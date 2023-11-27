package building;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Connor Volkert
 * This class will represent an elevator, and will contain
 * configuration information (capacity, speed, etc) as well
 * as state information - such as stopped, direction, and count
 * of passengers targeting each floor...
 * 
 * Peer reviewed by Aaron
 */
public class Elevator {
	/** Elevator State Variables - These are visible publicly */
	public final static int STOP = 0;
	public final static int MVTOFLR = 1;
	public final static int OPENDR = 2;
	public final static int OFFLD = 3;
	public final static int BOARD = 4;
	public final static int CLOSEDR = 5;
	public final static int MV1FLR = 6;

	/** Default configuration parameters for the elevator. These should be
	 *  updated in the constructor.
	 *  
	 */
	private int numFloors = 6;
	private int capacity = 15;				// The number of PEOPLE the elevator can hold
	private int ticksPerFloor = 5;			// The time it takes the elevator to move between floors
	private int ticksDoorOpenClose = 2;  	// The time it takes for doors to go from OPEN <=> CLOSED
	private int passPerTick = 3;            // The number of PEOPLE that can enter/exit the elevator per tick
	
	/** Finite State Machine State Variables */
	private int currState;		// current state
	private int prevState;      // prior state
	private int currFloor;      // current floor
	private int prevFloor;      // prior floor
	private int direction;      // direction the Elevator is traveling in.

	private int timeInState;    // represents the time in a given state
	                            // reset on state entry, used to determine if
	                            // state has completed or if floor has changed
	                            // *not* used in all states 

	private int doorState;      // used to model the state of the doors - OPEN, CLOSED
	                            // or moving
	
	private int passengers;  	// the number of people in the elevator
	
	private ArrayList<Passengers>[] passByFloor;  // Passengers to exit on the corresponding floor

	private int moveToFloor;	// When exiting the STOP state, this is the floor to move to without
	                            // stopping.
	
	private int postMoveToFloorDir; // This is the direction that the elevator will travel AFTER reaching
	                                // the moveToFloor in MVTOFLR state.

	/**
	 * Instantiates a new elevator.
	 *
	 * @param numFloors the num floors
	 * @param capacity the capacity
	 * @param floorTicks the floor ticks
	 * @param doorTicks the door ticks
	 * @param passPerTick the pass per tick
	 * Peer reviewed by Aaron
	 */
	@SuppressWarnings("unchecked")
	
	public Elevator(int numFloors, int capacity, int floorTicks, int doorTicks, int passPerTick) {		
		this.prevState = STOP;
		this.currState = STOP;
		this.timeInState = 1;
		this.currFloor = 0;
		passByFloor = new ArrayList[numFloors];
		
		for(int i = 0; i < numFloors; i++) {
			passByFloor[i] = new ArrayList<Passengers>();
			
		}
		
		this.numFloors = numFloors;
		this.capacity = capacity;
		this.ticksPerFloor = floorTicks;
		this.ticksDoorOpenClose = doorTicks;
		this.passPerTick = passPerTick;
	}
	
	/**
	 * Takes an elevator state as an integer,
	 * and then changes the elevators state.
	 * Sets time in state to zero if the state
	 * to change to is not the previous state.
	 * 
	 * @param currState
	 * Peer reviewed by Aaron
	 */
	public void updateCurrState(int currState) {
		this.prevState = this.currState;
		this.currState = currState;
		if (this.prevState != this.currState) {
			timeInState = 1;
		} else if(this.prevState==this.currState) {
			timeInState++;
		}
	}
	
	/**
	 * Updates the time in the moving state
	 * by one, and then moves either up
	 * or down based on the elevators direction.
	 * Peer reviewed by Aaron
	 */
	public void moveElevator() {
		prevFloor = currFloor;
		if ((timeInState % ticksPerFloor) == 0) {
			currFloor = currFloor + direction;
		}
	}
	
	/**
	 * Returns the groups getting off of the elevator
	 * on the current floor, while simultaneously
	 * removing them from the elevator
	 * 
	 * @return ArrayList of the passenger groups
	 * Peer reviewed by Aaron
	 */
	public ArrayList<Passengers> offloadPassengers() {
		ArrayList<Passengers> returnList = new ArrayList<Passengers>(passByFloor[currFloor].size());
		for(Passengers p : passByFloor[currFloor]) {
			returnList.add(p);
		}
		passByFloor[currFloor].clear();
		passengers-=numOfOffloadedPassengers(returnList);
		return returnList;
	}
	
	/**
	 * Returns the number of passengers that are
	 * leaving the elevator on the current floor
	 * 
	 * @return Number of passengers on a floor
	 * Peer reviewed by Aaron
	 */
	public int numOfOffloadedPassengers(ArrayList<Passengers> p) {
		int totalPassengers = 0;
		
		for(int i = 0; i < p.size(); i++) {
			totalPassengers += p.get(i).getNumPass();
		}
		
		return totalPassengers;
		
		
	}
	
	/**
	 * Elevator state changed.
	 *
	 * @return true, if successful
	 * Peer reviewed by Aaron
	 */
	public boolean elevatorStateChanged() {
		return prevState!=currState;
	}
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 * Peer reviewed by Aaron
	 */
	public boolean isEmpty() {
		return (passengers==0);
	}

	/**
	 * @return the currState
	 */
	public int getCurrState() {
		return currState;
	}

	/**
	 * @param currState the currState to set
	 */
	public void setCurrState(int currState) {
		this.currState = currState;
	}

	/**
	 * @return the prevState
	 */
	public int getPrevState() {
		return prevState;
	}

	/**
	 * @param prevState the prevState to set
	 */
	public void setPrevState(int prevState) {
		this.prevState = prevState;
	}

	/**
	 * @return the currFloor
	 */
	public int getCurrFloor() {
		return currFloor;
	}

	/**
	 * @param currFloor the currFloor to set
	 */
	public void setCurrFloor(int currFloor) {
		this.currFloor = currFloor;
	}

	/**
	 * @return the prevFloor
	 */
	public int getPrevFloor() {
		return prevFloor;
	}

	/**
	 * @param prevFloor the prevFloor to set
	 */
	public void setPrevFloor(int prevFloor) {
		this.prevFloor = prevFloor;
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
	 * Change direction.
	 * Peer reviewed by Aaron
	 */
	public void changeDirection() {
		direction*=-1;
	}

	/**
	 * @return the doorState
	 */
	public int getDoorState() {
		return doorState;
	}

	/**
	 * @param doorState the doorState to set
	 */
	public void setDoorState(int doorState) {
		this.doorState = doorState;
	}

	/**
	 * @return the passengers
	 */
	public int getPassengers() {
		return passengers;
	}

	/**
	 * @param passengers the passengers to set
	 */
	public void setPassengers(int passengers) {
		this.passengers = passengers;
	}

	/**
	 * @return the numFloors
	 */
	public int getNumFloors() {
		return numFloors;
	}

	/**
	 * @param numFloors the numFloors to set
	 */
	public void setNumFloors(int numFloors) {
		this.numFloors = numFloors;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the ticksPerFloor
	 */
	public int getTicksPerFloor() {
		return ticksPerFloor;
	}

	/**
	 * @param ticksPerFloor the ticksPerFloor to set
	 */
	public void setTicksPerFloor(int ticksPerFloor) {
		this.ticksPerFloor = ticksPerFloor;
	}

	/**
	 * @return the ticksDoorOpenClose
	 */
	public int getTicksDoorOpenClose() {
		return ticksDoorOpenClose;
	}

	/**
	 * @param ticksDoorOpenClose the ticksDoorOpenClose to set
	 */
	public void setTicksDoorOpenClose(int ticksDoorOpenClose) {
		this.ticksDoorOpenClose = ticksDoorOpenClose;
	}

	/**
	 * @return the passPerTick
	 */
	public int getPassPerTick() {
		return passPerTick;
	}

	/**
	 * @param passPerTick the passPerTick to set
	 */
	public void setPassPerTick(int passPerTick) {
		this.passPerTick = passPerTick;
	}

	/**
	 * @return the passByFloor
	 */
	public ArrayList<Passengers>[] getPassByFloor() {
		return passByFloor;
	}

	/**
	 * @param passByFloor the passByFloor to set
	 */
	public void setPassByFloor(ArrayList<Passengers>[] passByFloor) {
		this.passByFloor = passByFloor;
	}

	/**
	 * @return the moveToFloor
	 */
	public int getMoveToFloor() {
		return moveToFloor;
	}

	/**
	 * @param moveToFloor the moveToFloor to set
	 */
	public void setMoveToFloor(int moveToFloor) {
		this.moveToFloor = moveToFloor;
	}

	/**
	 * @return the timeInState
	 */
	public int getTimeInState() {
		return timeInState;
	}

	/**
	 * @param timeInState the timeInState to set
	 */
	public void setTimeInState(int timeInState) {
		this.timeInState = timeInState;
	}

	/**
	 * @return the postMoveToFloorDir
	 */
	public int getPostMoveToFloorDir() {
		return postMoveToFloorDir;
	}

	/**
	 * @param postMoveToFloorDir the postMoveToFloorDir to set
	 */
	public void setPostMoveToFloorDir(int postMoveToFloorDir) {
		this.postMoveToFloorDir = postMoveToFloorDir;
	}

	/**
	 * Open door.
	 * Peer reviewed by Aaron
	 */
	public void openDoor() {
		prevFloor = currFloor;
		doorState++;
	}

	/**
	 * Checks if is door open.
	 *
	 * @return true, if is door open
	 * Peer reviewed by Aaron
	 */
	public boolean isDoorOpen() {
		return (doorState==ticksDoorOpenClose);
		
	}
	
	/**
	 * Close door.
	 * Peer reviewed by Aaron
	 */
	public void closeDoor() {
		doorState--;
	}
	
	/**
	 * At new floor.
	 *
	 * @return true, if successful
	 * Peer reviewed by Aaron
	 */
	public boolean atNewFloor() {
		return !(prevFloor == currFloor);
	}
	

	/**
	 * Pass to leave.
	 *
	 * @return true, if successful
	 * Peer reviewed by Aaron
	 */
	public boolean passToLeave() {
		return (getPassByFloor()[currFloor].size() > 0);
	}

	/**
	 * Checks if is full.
	 *
	 * @return true, if is full
	 * Peer reviewed by Aaron
	 */
	public boolean isFull() {
		return (passengers==capacity);
	}

	/**
	 * Adds the passengers.
	 *
	 * @param p the p
	 * Peer reviewed by Aaron
	 */
	public void addPassengers(Passengers p) {
		getPassByFloor()[p.getDestFloor()].add(p);
	}

	/**
	 * No room to board next passenger.
	 *
	 * @param p the p
	 * @return true, if successful
	 * Peer reviewed by Aaron
	 */
	public boolean noRoomToBoardNextPassenger(Passengers p) {
		return (passengers+p.getNumPass()>capacity);
	}

	/**
	 * Update curr floor.
	 * Peer reviewed by Aaron
	 */
	public void updateCurrFloor() {
		prevFloor=currFloor;
	}
	
	
	
	
	
}