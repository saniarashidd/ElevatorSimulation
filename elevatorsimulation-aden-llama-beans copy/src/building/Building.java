package building;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import myfileio.MyFileIO;
import genericqueue.GenericQueue;

/**
 * The Class Building.
 * 
 * Peer reviewed by Sania
 */
public class Building {
	
	/**  Constants for direction. */
	private final static int UP = 1;
	
	/** The Constant DOWN. */
	private final static int DOWN = -1;
	
	/** The Constant LOGGER. */
	private final static Logger LOGGER = Logger.getLogger(Building.class.getName());
	
	/**  The fh - used by LOGGER to write the log messages to a file. */
	private FileHandler fh;
	
	/**  The fio for writing necessary files for data analysis. */
	private MyFileIO fio;
	
	/**  File that will receive the information for data analysis. */
	private File passDataFile;

	/**  passSuccess holds all Passengers who arrived at their destination floor. */
	private ArrayList<Passengers> passSuccess;
	
	/**  gaveUp holds all Passengers who gave up and did not use the elevator. */
	private ArrayList<Passengers> gaveUp;
	
	/**  The number of floors - must be initialized in constructor. */
	private final int NUM_FLOORS;
	
	/**  The size of the up/down queues on each floor. */
	private final int FLOOR_QSIZE = 10;	
	
	/** passQ holds the time-ordered queue of Passengers, initialized at the start 
	 *  of the simulation. At the end of the simulation, the queue will be empty.
	 */
	private GenericQueue<Passengers> passQ;

	/**  The size of the queue to store Passengers at the start of the simulation. */
	private final int PASSENGERS_QSIZE = 1000;	

	/**  The number of elevators - must be initialized in constructor. */
	private final int NUM_ELEVATORS;
	
	/** The floors. */
	public Floor[] floors;
	
	/** The elevators. */
	private Elevator[] elevators;
	
	/**  The Call Manager - it tracks calls for the elevator, analyzes them to answer questions and prioritize calls. */
	private CallManager callMgr;
	
	/** The Calculated offLoad Delay **/ 
	private int offLoadDelay = 0;
	
	/** private variable used in board state to track number of passengers boarded  **/
	private int numBoarded = 0;
	
	/** A boolean which returns true or false, depending on if the elevator is at full capacity **/
	private boolean isCapacity = false;
		
	/** ArrayList used to track the passengers removed from floor (for GUI) **/
    private ArrayList<Passengers> removedFromFloor = new ArrayList<Passengers>();

	/**
	 * Instantiates a new building.
	 *
	 * @param numFloors the number of floors in building
	 * @param numElevators the number of elevators in building
	 * @param logfile the logFile used to log events
	 * Peer reviewed by Sania
	 */
	public Building(int numFloors, int numElevators, String logfile) {
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		
		createPassengerQueue();
		
		passSuccess = new ArrayList<Passengers>();
		gaveUp = new ArrayList<Passengers>();
		Passengers.resetStaticID();		
		initializeBuildingLogger(logfile);
		// passDataFile is where you will write all the results for those passengers who successfully
		// arrived at their destination and those who gave up...
		fio = new MyFileIO();
		passDataFile = fio.getFileHandle(logfile.replaceAll(".log","PassData.csv"));
		
		// create the floors, call manager and the elevator arrays
		// note that YOU will need to create and config each specific elevator...
		floors = new Floor[NUM_FLOORS];
		for (int i = 0; i < NUM_FLOORS; i++) {
			floors[i]= new Floor(FLOOR_QSIZE); 
		}
		callMgr = new CallManager(floors,NUM_FLOORS);
		elevators = new Elevator[NUM_ELEVATORS];
		
	}
	
	/**
	 * Gets the elevator's current floor.
	 *
	 * @return the current floor
	 */
	public int getCurrentFloor() {
		return elevators[0].getCurrFloor();
	}
	
	/**
	 * Gets the elevator's direction.
	 *
	 * @return the direction
	 */
	public int getDirection() {
		return elevators[0].getDirection();
	}
	
	/**
	 * Gets the number of passengers in the passenger group.
	 *
	 * @return the number of passengers in group
	 * Peer reviewed by Sania
	 */
	public int getNumPassengersInGroup() {
		if(passQ.peek() != null) {
			return passQ.peek().getNumPass();
		}
		return 0;
	}
	
	/**
	 * Gets the number of passengers in the elevator.
	 *
	 * @return the number of passengers in elevator
	 * Peer reviewed by Sania
	 */
	public int getNumPassInElevator() {
	    return elevators[0].getPassengers();
	}
	
	/**
	 * Gets the floor in which the front of passenger queue appeared.
	 *
	 * @return the floor started on
	 * Peer reviewed by Sania
	 */
	public int getFloorStartedOn() {
		if(passQ.peek() != null) {
			return passQ.peek().getOnFloor();
		}
		return -1;	
	}
	
	/**
	 * Creates the passenger queue.
	 * Peer reviewed by Sania
	 */
	public void createPassengerQueue() {
		passQ = new GenericQueue<Passengers>(PASSENGERS_QSIZE);
	}
	
	/**
	 * Adds the passengers to passenger queue
	 *
	 * @param time the current time
	 * @param numPass the number of passengers of the added passenger group
	 * @param on the floor that the passenger group at the front of passenger queue is on
	 * @param dest the added passenger group's destination
	 * @param polite the polite attribute of the added passenger group
	 * @param waitTime the time that the added passenger group is willing to wait until they give up
	 * @return true, if successful
	 * Peer reviewed by Sania
	 */
	public boolean addPassengersToQueue(int time, int numPass, int on, int dest, boolean polite, int waitTime) {
		return passQ.add(new Passengers(time, numPass, on, dest, polite, waitTime));
	}
	
	/**
	 * Check passenger queue (for controller). 
	 *
	 * @param time the time
	 * @return the list of new passengers
	 * Peer reviewed by Sania
	 */
	public List<Passengers> checkPassengerQueue(int time) {
	    List<Passengers> newPassengers = new ArrayList<Passengers>();
		Passengers p;
		while(!passQ.isEmpty() && passQ.peek().getTime()==time) {
			p = passQ.poll();
			logCalls(time, p.getNumPass(), p.getOnFloor(), p.getDirection(), p.getId());
			floors[p.getOnFloor()].addPassengerToFloorQueue(p);
			newPassengers.add(p);
		}
		updateCallStatus();
		return newPassengers;
	}
	
	/**
	 * Creates and configures elevators.
	 *
	 * @param capacity the maximum passengers to possibly fit in the elevator
	 * @param floorTicks the floor ticks
	 * @param passPerTick the pass per tick 
	 * Peer reviewed by Sania
	 */
	public void configElevators(int capacity, int floorTicks, int doorTicks, int passPerTick) {
		for(int i = 0; i < NUM_ELEVATORS; i++) {
			elevators[i] = new Elevator(NUM_FLOORS, capacity, floorTicks, doorTicks, passPerTick);
		}
	}
	
	/**
	 * Tells us if the elevator state has changed.
	 *
	 * @param el the Elevator
	 * @return true, if elevator state has changed
	 * Peer reviewed by Sania
	 */
	public boolean elevatorStateChanged(Elevator el) {
		if(el.getCurrState()!= el.getPrevState()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is elevator simulation over.
	 *
	 * @return true, if simulation is over
	 * Peer reviewed by Sania
	 */
	public boolean isSimOver() {	
		return (elevators[0].getCurrState()==Elevator.STOP && passQ.isEmpty());
	}
	
	/**
	 * Gets the current elevator state.
	 *
	 * @return the current state
	 */
	public int getCurrState() {
		return elevators[0].getCurrState();
	}
	
	/**
	 * Stop state method where when active, implies that there are no calls whatsoever.
	 *
	 * @param time the time
	 * @param el the Elevator
	 * @return the next state
	 * Peer reviewed by Sania
	 */
	private int currStateStop(int time, Elevator el) {
		if(!callMgr.callPending()) {
			return Elevator.STOP;
		}
		Passengers p = callMgr.prioritizePassengerCalls(el.getCurrFloor());
		
		if(p != null) {
			el.setMoveToFloor(p.getDestFloor());
			if(p.getOnFloor()==el.getCurrFloor()) {
				el.setDirection(p.getDirection());
				return Elevator.OPENDR; 
			}
			else {
        		el.setMoveToFloor(p.getOnFloor());
        		if(el.getMoveToFloor()-el.getCurrFloor()<0) {
        			el.setDirection(-1);
        			
        		} else {
        			el.setDirection(1);
        			
        		}
        		el.setPostMoveToFloorDir(p.getDirection());
        		return Elevator.MVTOFLR;
			}
			
		}
		return Elevator.STOP;
	}
	
	/**
	 * Move to floor state method that when active, moves elevator to a specified floor.
	 *
	 * @param time the time
	 * @param el the Elevator
	 * @return the next state
	 * Peer reviewed by Sania
	 */
	private int currStateMvToFlr(int time, Elevator el) {
		el.moveElevator();
		
        if(el.getCurrFloor() == el.getMoveToFloor()) {
        	el.setDirection(el.getPostMoveToFloorDir());
        	return Elevator.OPENDR;
        }
        return Elevator.MVTOFLR;
    }
	
	/**
	 * Open door state method that when active, opens elevator doors.
	 *
	 * @param time the time
	 * @param el the Elevator
	 * @return the next state
	 * Peer reviewed by Sania
	 */
	private int currStateOpenDr(int time, Elevator el) {
		el.updateCurrFloor();
		el.openDoor();
		
		if(el.isDoorOpen()) {
			
			if(el.passToLeave()) {
				return Elevator.OFFLD;
			}
			if(callMgr.passToBoard(el.getCurrFloor(), el.getDirection())) {
				return Elevator.BOARD;
			}
		}
		return Elevator.OPENDR; // else door isn't open yet so will continue to increment doorState until door becomes open
	}
	
    /**
     * offLoad state method that when active, offLoads elevator passengers onto the respective floor.
     *
     * @param time the time
     * @param el the Elevator
     * @return the next state
     * Peer reviewed by Sania
     */
    private int currStateOffLd(int time, Elevator el) {
    	ArrayList<Passengers> temp;
    	int dir = el.getDirection();
    	int currFloor = el.getCurrFloor();
    	if(el.getPrevState()!=Elevator.OFFLD) {
    		temp = el.offloadPassengers();
    		offLoadDelay = ((el.numOfOffloadedPassengers(temp)-1)/3)+1;
    		for(Passengers p : temp) {
    			passSuccess.add(p);
    			logArrival(time, p.getNumPass(), p.getDestFloor(), p.getId());
    		}
    	}
    	if(el.getTimeInState()==offLoadDelay) {
    		if(callMgr.passToBoard(currFloor, dir)) {
    			return Elevator.BOARD;
    		}
    		if(el.isEmpty() && (callMgr.callsFromFloorsInCurrDir(currFloor, dir)==0) && callMgr.passToBoard(currFloor, -dir)) {
    			el.changeDirection();
    			return Elevator.BOARD;
    		}
    		return Elevator.CLOSEDR;
    	}
    	return Elevator.OFFLD;

    }
	
	/**
	 * Board state method that when active, boards passengers waiting to get on the elevator.
	 *
	 * @param time the time
	 * @param el the Elevator
	 * @return the next state
	 * Peer reviewed by Sania
	 */
	private int currStateBoard(int time, Elevator el) {
		Passengers p;
		Floor f = floors[el.getCurrFloor()];
		if(el.elevatorStateChanged()) {
			numBoarded = 0;
			isCapacity = false;
		}
		while(!isCapacity && callMgr.passToBoard(el.getCurrFloor(), el.getDirection())) {
			p = f.peekFloorQueue(el.getDirection());
			if(p != null) {
				if(time > p.getTimeWillGiveUp()) { // if they gave up then move and remove floor contents to gaveUp content
				    Passengers passengers = f.pollFloorQueue(el.getDirection());
					gaveUp.add(passengers);
					removedFromFloor.add(passengers);
					logGiveUp(time, p.getNumPass(), el.getCurrFloor(), p.getDirection(), p.getId());
				} else if(el.noRoomToBoardNextPassenger(p)) {
					logSkip(time, p.getNumPass(), p.getOnFloor(), p.getDirection(), p.getId());
					p.setPolite(true);
					isCapacity = true;
					break;
				} else {
					numBoarded += p.getNumPass();
					p.setBoardTime(time);
					logBoard(time, p.getNumPass(), p.getOnFloor(), p.getDirection(), p.getId());
					el.setPassengers(el.getPassengers()+p.getNumPass());
					Passengers passengers = f.pollFloorQueue(el.getDirection());
                    el.addPassengers(passengers);
                    removedFromFloor.add(passengers);
				}
			}
		}
		if(el.getTimeInState()>=(((numBoarded-1)/3)+1)) return Elevator.CLOSEDR;
		return Elevator.BOARD;
	}

	/**
	 * Passenger list that provides passengers removed from the floor who are waiting to be boarded (for controller).
	 *
	 * @return passenger list of passengers removed from floor
	 * Peer reviewed by Sania
	 */
	public List<Passengers> getRemovedFromFloor() {
	    return removedFromFloor.subList(0, removedFromFloor.size());
	}

	/**
	 * clears the removed floor passengers (for controller). 
	 * Peer reviewed by Sania
	 */
	public void clearRemovedFromFloor() {
	    removedFromFloor.clear();
	}

	/**
	 * Close door state method that when active, closes elevator doors.
	 *
	 * @param time the time
	 * @param el the Elevator
	 * @return the next state
	 * Peer reviewed by Sania
	 */
	private int currStateCloseDr(int time, Elevator el) {
		int currFloor = el.getCurrFloor(); int dir = el.getDirection();
		Passengers p = floors[currFloor].peekFloorQueue(dir);
		
		el.closeDoor();
		
		if(p!=null && !p.isPolite()) {
			p.setPolite(true);
			return Elevator.OPENDR;
		}
		
		if(el.getDoorState() == 0) {
			if(el.isEmpty()) {
				if(!callMgr.callPending()) return Elevator.STOP;
				if(dir == 1 && callMgr.callsAboveCurrFloor(currFloor) > 0) {
						return Elevator.MV1FLR;
				}
				if(dir == -1 && callMgr.callsBelowCurrFloor(currFloor) > 0) {
						return Elevator.MV1FLR;
				}
				if(callMgr.passToBoard(currFloor, dir)) {
					return Elevator.OPENDR;
				}
				else {
					el.changeDirection();
					if(callMgr.passToBoard(currFloor, -dir)) return Elevator.OPENDR;
					else return Elevator.MV1FLR;
				}
			} else {
				return Elevator.MV1FLR;
			}
		} 
		return Elevator.CLOSEDR;
	}
	
	/**
	 * Move 1 floor state method that when active, moves elevator 1 floor in current direction.
	 *
	 * @param time the time
	 * @param el the Elevator
	 * @return the next state
	 * Peer reviewed by Sania
	 */
	private int currStateMv1Flr(int time, Elevator el) {

        int dir = el.getDirection();
         
        el.moveElevator();
        
        if (el.atNewFloor()) {
            if (el.passToLeave()) {
                return Elevator.OPENDR;
            }
            if (callMgr.passToBoard(el.getCurrFloor(), dir)) { // if there are passengers to board on this floor in the current direction, open the door
                return Elevator.OPENDR;
            }
            if (el.isEmpty() && (callMgr.callsFromFloorsInCurrDir(el.getCurrFloor(), dir)==0) && callMgr.passToBoard(el.getCurrFloor(), -dir)) { // elevator is empty, no calls above if dir = up, no calls below if dir = down
                el.setDirection(-dir);
                return Elevator.OPENDR;
            }

        }
        
        return Elevator.MV1FLR;
         
    }
    
	
	// DO NOT CHANGE ANYTHING BELOW THIS LINE:
	/**
	 * Initialize building logger. Sets formating, file to log to, and
	 * turns the logger OFF by default
	 *
	 * @param logfile the file to log information to
	 * Peer reviewed by Sania
	 */
	void initializeBuildingLogger(String logfile) {
		System.setProperty("java.util.logging.SimpleFormatter.format","%4$-7s %5$s%n");
		LOGGER.setLevel(Level.OFF);
		try {
			fh = new FileHandler(logfile);
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	/**
	 * Update elevator - this is called AFTER time has been incremented.
	 * -  Logs any state changes, if the have occurred,
	 * -  Calls appropriate method based upon currState to perform
	 *    any actions and calculate next state...
	 *
	 * @param time the time
	 * Peer reviewed by Sania
	 */
	// YOU WILL NEED TO CODE ANY MISSING METHODS IN THE APPROPRIATE CLASSES...
	public void updateElevator(int time) {
		for (Elevator lift: elevators) {
			if (elevatorStateChanged(lift)) {
				logElevatorStateChanged(time,lift.getPrevState(),lift.getCurrState(),lift.getPrevFloor(),lift.getCurrFloor());
			}

			switch (lift.getCurrState()) {
				case Elevator.STOP: lift.updateCurrState(currStateStop(time,lift)); break;
				case Elevator.MVTOFLR: lift.updateCurrState(currStateMvToFlr(time,lift)); break;
				case Elevator.OPENDR: lift.updateCurrState(currStateOpenDr(time,lift)); break;
				case Elevator.OFFLD: lift.updateCurrState(currStateOffLd(time,lift)); break;
				case Elevator.BOARD: lift.updateCurrState(currStateBoard(time,lift)); break;
				case Elevator.CLOSEDR: lift.updateCurrState(currStateCloseDr(time,lift)); break;
				case Elevator.MV1FLR: lift.updateCurrState(currStateMv1Flr(time,lift)); break;
			}
		}
	}





	/**
	 * Process passenger data. Do NOT change this - it simply dumps the 
	 * collected passenger data for successful arrivals and give ups. These are
	 * assumed to be ArrayLists...
	 */
	public void processPassengerData() {
		
		try {
			BufferedWriter out = fio.openBufferedWriter(passDataFile);
			out.write("ID,Number,From,To,WaitToBoard,TotalTime\n");
			for (Passengers p : passSuccess) {
				String str = p.getId()+","+p.getNumPass()+","+(p.getOnFloor()+1)+","+(p.getDestFloor()+1)+","+
				             (p.getBoardTime() - p.getTime())+","+(p.getTimeArrived() - p.getTime())+"\n";
				out.write(str);
			}
			for (Passengers p : gaveUp) {
				String str = p.getId()+","+p.getNumPass()+","+(p.getOnFloor()+1)+","+(p.getDestFloor()+1)+","+
				             p.getWaitTime()+",-1\n";
				out.write(str);
			}
			fio.closeFile(out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	
	/**
	 * Enable logging. Prints the initial configuration message.
	 * For testing, logging must be enabled BEFORE the run starts.
	 * Peer reviewed by Sania
	 */
	public void enableLogging() {
		LOGGER.setLevel(Level.INFO);
		for (Elevator el:elevators)
			logElevatorConfig(el.getCapacity(),el.getTicksPerFloor(), el.getTicksDoorOpenClose(), el.getPassPerTick(), el.getCurrState(), el.getCurrFloor());
	}
	
	/**
	 * Close logs, and pause the timeline in the GUI.
	 *
	 * @param time the time
	 * Peer reviewed by Sania
	 */
	public void closeLogs(int time) {
		if (LOGGER.getLevel() == Level.INFO) {
			logEndSimulation(time);
			fh.flush();
			fh.close();
		}
	}
	
	/**
	 * Prints the state.
	 *
	 * @param state the state
	 * @return the string
	 * Peer reviewed by Sania
	 */
	private String printState(int state) {
		String str = "";
		
		switch (state) {
			case Elevator.STOP: 		str =  "STOP   "; break;
			case Elevator.MVTOFLR: 		str =  "MVTOFLR"; break;
			case Elevator.OPENDR:   	str =  "OPENDR "; break;
			case Elevator.CLOSEDR:		str =  "CLOSEDR"; break;
			case Elevator.BOARD:		str =  "BOARD  "; break;
			case Elevator.OFFLD:		str =  "OFFLD  "; break;
			case Elevator.MV1FLR:		str =  "MV1FLR "; break;
			default:					str =  "UNDEF  "; break;
		}
		return(str);
	}
	
	/**
	 * Dump passQ contents. Debug hook to view the contents of the passenger queue...
	 * Peer reviewed by Sania
	 */
	public void dumpPassQ() {
		ListIterator<Passengers> passengers = passQ.getListIterator();
		if (passengers != null) {
			System.out.println("Passengers Queue:");
			while (passengers.hasNext()) {
				Passengers p = passengers.next();
				System.out.println(p);
			}
		}
	}

	/**
	 * Log elevator config.
	 *
	 * @param capacity the capacity
	 * @param ticksPerFloor the ticks per floor
	 * @param ticksDoorOpenClose the ticks door open close
	 * @param passPerTick the pass per tick
	 * @param state the state
	 * @param floor the floor
	 * Peer reviewed by Sania
	 */
	private void logElevatorConfig(int capacity, int ticksPerFloor, int ticksDoorOpenClose, int passPerTick, int state, int floor) {
		LOGGER.info("CONFIG:   Capacity="+capacity+"   Ticks-Floor="+ticksPerFloor+"   Ticks-Door="+ticksDoorOpenClose+
				    "   Ticks-Passengers="+passPerTick+"   CurrState=" + (printState(state))+"   CurrFloor="+(floor+1));
	}
		
	/**
	 * Log elevator state changed.
	 *
	 * @param time the time
	 * @param prevState the prev state
	 * @param currState the curr state
	 * @param prevFloor the prev floor
	 * @param currFloor the curr floor
	 * Peer reviewed by Sania
	 */
	private void logElevatorStateChanged(int time, int prevState, int currState, int prevFloor, int currFloor) {
		LOGGER.info("Time="+time+"   Prev State: " + printState(prevState) + "   Curr State: "+printState(currState)
		+"   PrevFloor: "+(prevFloor+1) + "   CurrFloor: " + (currFloor+1));
	}
	
	/**
	 * Log arrival.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param id the id
	 * Peer reviewed by Sania
	 */
	private void logArrival(int time, int numPass, int floor,int id) {
		LOGGER.info("Time="+time+"   Arrived="+numPass+" Floor="+ (floor+1)
		+" passID=" + id);						
	}
	
	/**
	 * Log calls.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 * Peer reviewed by Sania
	 */
	private void logCalls(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Called="+numPass+" Floor="+ (floor +1)
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);
	}
	
	/**
	 * Log give up.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 * Peer reviewed by Sania
	 */
	private void logGiveUp(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   GaveUp="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}

	/**
	 * Log skip.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 * Peer reviewed by Sania
	 */
	private void logSkip(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Skip="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	/**
	 * Log board.
	 *
	 * @param time the time
	 * @param numPass the num pass
	 * @param floor the floor
	 * @param dir the dir
	 * @param id the id
	 * Peer reviewed by Sania
	 */
	private void logBoard(int time, int numPass, int floor, int dir, int id) {
		LOGGER.info("Time="+time+"   Board="+numPass+" Floor="+ (floor+1) 
				+" Dir="+((dir>0)?"Up":"Down")+"   passID=" + id);				
	}
	
	/**
	 * Log end simulation.
	 *
	 * @param time the time
	 * Peer reviewed by Sania
	 */
	private void logEndSimulation(int time) {
		LOGGER.info("Time="+time+"   Detected End of Simulation");
	}

	/**
	 * Update call status.
	 * Peer reviewed by Sania
	 */
	public void updateCallStatus() {
		callMgr.updateCallStatus();
		
	}
}
