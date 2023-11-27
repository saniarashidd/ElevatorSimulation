package building;
//OWNER: Sania


// TODO: Auto-generated Javadoc
/**
 * The Class CallManager. This class models all of the calls on each floor,
 * and then provides methods that allow the building to determine what needs
 * to happen (ie, state transitions).
 * 
 * Peer reviewed by Connor
 */
public class CallManager {
	
	/** The floors. */
	private Floor[] floors;
	
	/** The num floors. */
	private final int NUM_FLOORS;
	
	/** The Constant UP. */
	private final static int UP = 1;
	
	/** The Constant DOWN. */
	private final static int DOWN = -1;
	
	/** The up calls array indicates whether or not there is a up call on each floor. */
	private boolean[] upCalls;
	
	/** The down calls array indicates whether or not there is a down call on each floor. */
	private boolean[] downCalls;
	
	/**  The up call pending - true if any up calls exist. */
	private boolean upCallPending;
	
	/**  The down call pending - true if any down calls exist. */
	private boolean downCallPending;
	
	//TODO: Add any additional fields here..
	
	/**
	 * Instantiates a new call manager.
	 * Peer reviewed by Connor
	 * @param floors the floors
	 * @param numFloors the num floors
	 */
	public CallManager(Floor[] floors, int numFloors) {
		this.floors = floors;
		NUM_FLOORS = numFloors;
		upCalls = new boolean[NUM_FLOORS];
		downCalls = new boolean[NUM_FLOORS];
		upCallPending = false;
		downCallPending = false;
		
		//TODO: Initialize any added fields here
	}
	
	
	
	
	/**
	 * Update call status. This is an optional method that could be used to compute
	 * the values of all up and down call fields statically once per tick (to be
	 * more efficient, could only update when there has been a change to the floor queues -
	 * either passengers being added or being removed. The alternative is to dynamically
	 * recalculate the values of specific fields when needed.
	 * Peer reviewed by Connor
	 */
	public void updateCallStatus() {
        upCallPending = false;
        downCallPending = false;
        for (int i = 0; i < NUM_FLOORS; ++i) {
            upCalls[i] = !floors[i].isEmptyFloorQueue(1);
            upCallPending = upCallPending || upCalls[i];
            downCalls[i] = !floors[i].isEmptyFloorQueue(-1);
            downCallPending = downCallPending || downCalls[i];
        }
    }
	


	//TODO: Write any additional methods here. Things that you might consider:
	//      1. pending calls - are there any? only up? only down?
	//      2. is there a call on the current floor in the current direction
	//      3. How many up calls are pending? how many down calls are pending? 
	//      4. How many calls are pending in the direction that the elevator is going
	//      5. Should the elevator change direction?
	//
	//      These are an example - you may find you don't need some of these, or you may need more...
	
	/**
	 * returns whether or not there are passengers 
	 * to board on the current floor in the given dir
	 * @param currFloor the curr floor
	 * @param dir the dir
	 * @return true, if successful
	 * Peer reviewed by Connor
	 */
	public boolean passToBoard(int currFloor, int dir) {
		return floors[currFloor].hasCalls(dir);
	}
	
	/**
	 * returns the actual number of passengers 
	 * to board on the current floor in the given dir
	 * @param currFloor the curr floor
	 * @param dir the dir
	 * @return the int
	 * Peer reviewed by Connor
	 */
	public int numPassToBoard(int currFloor, int dir) {
		return floors[currFloor].getNumOfCalls(dir);
	}
	
	/**
	 * Calls from floors in curr dir.
	 *
	 * @param floor the floor
	 * @param dir the dir
	 * @return the int
	 * Peer reviewed by Connor
	 */
	public int callsFromFloorsInCurrDir(int floor, int dir) {
		if(dir==1) {
			return callsAboveCurrFloor(floor);
		} else {
			return callsBelowCurrFloor(floor);
		}
	}
	
	
	/**
	 * Calls above curr floor.
	 *
	 * @param floor the floor
	 * @return the int
	 * Peer reviewed by Connor
	 */
	public int callsAboveCurrFloor(int floor) {
		int callsAbove = 0;
		for(int i = floor+1; i < floors.length; i++) {
			callsAbove += floors[i].getNumOfCalls(1);
			callsAbove += floors[i].getNumOfCalls(-1);
		}
		return callsAbove;
	}
	
	/**
	 * Calls below curr floor.
	 *
	 * @param floor the floor
	 * @return the int
	 * Peer reviewed by Connor
	 */
	public int callsBelowCurrFloor(int floor) {
		int callsBelow = 0;
		for(int i = floor-1; i >= 0; i--) {
			callsBelow += floors[i].getNumOfCalls(1);
			callsBelow += floors[i].getNumOfCalls(-1);
		}
		return callsBelow;
	}
	
	
	
	/**
	 * Call pending.
	 *
	 * @param floor the floor
	 * @return true, if successful
	 * Peer reviewed by Connor
	 */
	public boolean callPending() {
		
		return upCallPending || downCallPending;
		
	}
	
	/**
	 * checks whether or not there are calls in the given direction on any floor
	 *
	 * @param dir the dir
	 * @return true, if successful
	 * Peer reviewed by Connor
	 */
	public boolean callInCurrDir(int dir) {
		for(int i = 0; i < floors.length; i++) {
			if(floors[i].hasCalls(dir)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns the number of calls in the given direction on any floor
	 *
	 * @param dir the dir
	 * @return int, the num of calls in given dir
	 * Peer reviewed by Connor
	 */
	public int numCallsInCurrDir(int dir) {
		int calls = 0;
		for(int i = 0; i < floors.length; i++) {
			if(floors[i].hasCalls(dir)) {
				calls++;
			}
		}
		return calls;
	}

	/**
	 * Prioritize passenger calls from STOP STATE.
	 * 
	 * @param floor the floor
	 * @return the passengers
	 * Peer reviewed by Connor
	 */
	Passengers prioritizePassengerCalls(int floor) {
		int numCallsUp = numCallsInCurrDir(1);
		int numCallsDown = numCallsInCurrDir(-1);
		int LUFloor = 0;
		int HDFloor = 0;
		if(passToBoard(floor, 1) && !passToBoard(floor, -1)) {
			return floors[floor].peekFloorQueue(1);
		}
		if(passToBoard(floor, -1) && !passToBoard(floor, 1)) {
			return floors[floor].peekFloorQueue(-1);
		}
		if(passToBoard(floor, 1) && passToBoard(floor, -1)) {
			if(callsAboveCurrFloor(floor) >= callsBelowCurrFloor(floor)) {
				return floors[floor].peekFloorQueue(1);
			} 
			return floors[floor].peekFloorQueue(-1);
		}
		//call is not on current floor	
		for(int i = 0; i < floors.length; i++) {
			if(numPassToBoard(i, 1) > 0) {
				LUFloor = i; 
				break;
			}
		}
		for(int i = floors.length-1; i >= 0; i--) {
			if(numPassToBoard(i, -1) > 0) {
				HDFloor = i; 
				break;
			}
		}
		if(numCallsUp > numCallsDown) return floors[LUFloor].peekFloorQueue(1);
		else if(numCallsDown > numCallsUp) return floors[HDFloor].peekFloorQueue(-1);
		else { //same number of calls up and down
			if(Math.abs(LUFloor - floor) > Math.abs(HDFloor - floor)) { //highest down is closer
				return floors[HDFloor].peekFloorQueue(-1);
			}
			return floors[LUFloor].peekFloorQueue(1);
		}	
	}
	
	
	/**
	 * returns whether or not the building 
	 * should call the elevator to change direction
	 * @param floor the current floor
	 * @return true, if successful
	 * Peer reviewed by Connor
	 */
	public boolean changeDirection(int currDirection, int floor) {
		boolean change = false;
		int oppDirection = -currDirection;
		int numCallsUp = 0;
		int numCallsDown = 0;
		for(int i = floor+1; i < floors.length; i++) {
			if(floors[i].hasCalls(0)) {
				numCallsUp++;
			}
		}
		for(int i = floor-1; i >= 0; i--) {
			if(floors[i].hasCalls(0)) numCallsDown++;
		}
		if(floors[floor].peekFloorQueue(currDirection) != null) { //means there are  passengers going in the same direction
			return false;
		}
		
		if((currDirection == 1 && numCallsUp > 0) || (currDirection == -1 && numCallsDown >0)) { //means there are still calls in the same dir the elevator is moving
			return false;
		}
		
		//passengers on the curr floor moving in opp dir or calls on floors in opp dir
		if((floors[floor].hasCalls(0) && floors[floor].peekFloorQueue(oppDirection) != null) ||
				(currDirection == 1 && numCallsDown > 0) || (currDirection == -1 && numCallsUp >0)) {
			change = true;
		}
		return change;
	}
}
