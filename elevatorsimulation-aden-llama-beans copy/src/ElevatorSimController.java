import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import building.Building;
import building.Elevator;
import building.Passengers;
import javafx.animation.Timeline;
import myfileio.MyFileIO;
//OWNER: Sania


// TODO: Auto-generated Javadoc
/**
 * The Class ElevatorSimController.
 * 
 * Peer reviewed by Connor
 */
// TODO: Auto-generated Javadoc
public class ElevatorSimController {
	
	/**  Constant to specify the configuration file for the simulation. */
	private static final String SIM_CONFIG = "ElevatorSimConfig.csv";
	
	/**  Constant to make the Passenger queue contents visible after initialization. */
	private boolean PASSQ_DEBUG=false;
	
	
	
	/** The gui. */
	private ElevatorSimulation gui;
	
	/** The building. */
	private Building building;
	
	/** The fio. */
	private MyFileIO fio;

	/** The num floors. */
	private final int NUM_FLOORS;
	
	/** The num elevators. */
	private final int NUM_ELEVATORS;
	
	/** The num floors. */
	private int numFloors;
	
	/** The num elevators. */
	private int numElevators;
	
	/** The capacity. */
	private int capacity;
	
	/** The floor ticks. */
	private int floorTicks;
	
	/** The door ticks. */
	private int doorTicks;
	
	/** The pass per tick. */
	private int passPerTick;
	
	/** The testfile. */
	private String testfile;
	
	/** The logfile. */
	private String logfile;
	
	/** The step cnt. */
	private int stepCnt = 0;
	
	/** The end sim. */
	private boolean endSim = false;
		
	/**
	 * Instantiates a new elevator sim controller. 
	 * Reads the configuration file to configure the building and
	 * the elevator characteristics and also select the test
	 * to run. Reads the passenger data for the test to run to
	 * initialize the passenger queue in building...
	 *
	 * @param gui the gui
	 * 
	 */
	public ElevatorSimController(ElevatorSimulation gui) {
		if(gui!=null) {
			this.gui = gui;
		}
		fio = new MyFileIO();
		// IMPORTANT: DO NOT CHANGE THE NEXT LINE!!! Update the config file itself
		// (ElevatorSimConfig.csv) to change the configuration or test being run.
		configSimulation(SIM_CONFIG);
		NUM_FLOORS = numFloors;
		NUM_ELEVATORS = numElevators;
		logfile = testfile.replaceAll(".csv", ".log");
		building = new Building(NUM_FLOORS,NUM_ELEVATORS,logfile);
		
		//TODO: YOU still need to configure the elevators in the building here....
		
		initializePassengerData(testfile);
		
		building.configElevators(capacity, floorTicks, doorTicks, passPerTick);
		
	}
	
	//TODO: Write methods to update the GUI display
	//      Needs to cover the Elevator state, Elevator passengers
	//      and queues for each floor, as well as the current time
	
	/**
	 * Config simulation. Reads the filename, and parses the
	 * parameters.
	 *
	 * @param filename the filename
	 */
	private void configSimulation(String filename) {
		File configFile = fio.getFileHandle(filename);
		try ( BufferedReader br = fio.openBufferedReader(configFile)) {
			String line;
			while ((line = br.readLine())!= null) {
				parseElevatorConfigData(line);
			}
			fio.closeFile(br);
		} catch (IOException e) { 
			System.err.println("Error in reading file: "+filename);
			e.printStackTrace();
		}
	}
	
	/**
	 * Parses the elevator simulation config file to configure the simulation:
	 * number of floors and elevators, the actual test file to run, and the
	 * elevator characteristics.
	 *
	 * @param line the line
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void parseElevatorConfigData(String line) throws IOException {
		String[] values = line.split(",");
		if (values[0].equals("numFloors")) {
			numFloors = Integer.parseInt(values[1]);
		} else if (values[0].equals("numElevators")) {
			numElevators = Integer.parseInt(values[1]);
		} else if (values[0].equals("passCSV")) {
			testfile = values[1];
		} else if (values[0].equals("capacity")) {
			capacity = Integer.parseInt(values[1]);
		} else if (values[0].equals("floorTicks")) {
			floorTicks = Integer.parseInt(values[1]);
		} else if (values[0].equals("doorTicks")) {
			doorTicks = Integer.parseInt(values[1]);
		} else if (values[0].equals("passPerTick")) {
			passPerTick = Integer.parseInt(values[1]);
		}
	}
	
	/**
	 * Initialize passenger data. Reads the supplied filename,
	 * and for each passenger group, identifies the pertinent information
	 * and adds it to the passengers queue in Building...
	 *
	 * @param filename the filename
	 */
	private void initializePassengerData(String filename) {
		boolean firstLine = true;
		File passInput = fio.getFileHandle(filename);
		try (BufferedReader br = fio.openBufferedReader(passInput)) {
			String line;
			while ((line = br.readLine())!= null) {
				if (firstLine) {
					firstLine = false;
					continue;
				}
				parsePassengerData(line);
			}
			fio.closeFile(br);
		} catch (IOException e) { 
			System.err.println("Error in reading file: "+filename);
			e.printStackTrace();
		}
		if (PASSQ_DEBUG) building.dumpPassQ();
	}	
	
	/**
	 * Parses the line of passenger data into tokens, and 
	 * passes those values to the building to be added to the
	 * passenger queue
	 *
	 * @param line the line of passenger input data
	 */
	private void parsePassengerData(String line) {
		int time=0, numPass=0,fromFloor=0, toFloor=0;
		boolean polite = true;
		int wait = 1000;
		String[] values = line.split(",");
		for (int i = 0; i < values.length; i++) {
			switch (i) {
				case 0 : time      = Integer.parseInt(values[i]); break;
				case 1 : numPass   = Integer.parseInt(values[i]); break;
				case 2 : fromFloor   = Integer.parseInt(values[i]); break;
				case 3 : toFloor  = Integer.parseInt(values[i]); break;
				case 5 : wait      = Integer.parseInt(values[i]); break;
				case 4 : polite = "TRUE".equalsIgnoreCase(values[i]); break;
			}
		}
		building.addPassengersToQueue(time,numPass,fromFloor,toFloor,polite,wait);	
	}
	
	/**
	 * Enable logging. A pass-through from the GUI to building
	 * Peer reviewed by Connor
	 */
	public void enableLogging() {
		building.enableLogging();
	}
	
	// TODO: Write any other helper methods that you may need to access data from the building...
	
	/**
	 * Disable logging.
	 * Peer reviewed by Connor
	 */
	public void closeLogs() {
		building.closeLogs(stepCnt);
	}
	
	/**
	 * Sim over.
	 *
	 * @return true, if successful
	 * Peer reviewed by Connor
	 */
	public boolean simOver() {
		return building.isSimOver();
	}
	
	//stop state, no more passengers in elevator, no more passengers in passengerque, no more calls
 	
	/**
	 * Step sim. See the comments below for the functionality you
	 * must implement......
	 * Peer reviewed by Connor
	 */
	public void stepSim() {
 		// DO NOT MOVE THIS - YOU MUST INCREMENT TIME FIRST!
		stepCnt++;
		
		// TODO: Write the rest of this method
		// If simulation is not completed (not all passengers have been processed
		// or elevator(s) are not all in STOP state), then
		// 		1) check for arrival of any new passengers
		// 		2) update the elevator
		// 		3) update the GUI 
		//  else 
		//    	1) update the GUI
		//		2) close the logs
		//		3) process the passenger results
		//		4) send endSimulation to the GUI to stop ticks.
		
		if(simOver()) {
			building.updateElevator(stepCnt);
			if(gui != null) {
				gui.updateTimeLabel(stepCnt);
				gui.setShapeVisibility(convertBuildingStateToGuiShape(convertBuildingStateToGuiShape(building.getCurrState()))); //updates what we see the elevator as
			}
			closeLogs();
			building.processPassengerData();
			if(gui != null) gui.endSimulation();
		}
		else {
			List<Passengers> newPassengers = building.checkPassengerQueue(stepCnt);
			building.updateCallStatus();
			building.updateElevator(stepCnt);
			if(gui != null) {
				gui.updateTimeLabel(stepCnt);
				for (Passengers passengers : newPassengers) {
	                gui.updatePassengerView(passengers.getNumPass(), passengers.getDirection(), passengers.getOnFloor()); //tells building to call gui to update passenger labels
				}
				gui.setShapeVisibility(convertBuildingStateToGuiShape(building.getCurrState())); //updates what we see the elevator as
				gui.moveElevator(building.getCurrentFloor(), building.getNumPassInElevator());
				for (Passengers passengers : building.getRemovedFromFloor()) {
	                gui.removePassGroupsFromGUI(passengers.getOnFloor(), passengers.getDirection(), 1);
				}
				building.clearRemovedFromFloor();
			}
		}
		
	}
	
	/**
	 * Convert building state to the corresponding gui state 
	 * so the gui can use it properly
	 * @param state the state
	 * @return the int
	 * @throws IllegalArgumentException the illegal argument exception
	 * Peer reviewed by Connor
	 */
	private int convertBuildingStateToGuiShape(int state) throws IllegalArgumentException {
	    switch (state) {
	    case Elevator.BOARD:
	        return ElevatorSimulation.BOARDING;
	    case Elevator.CLOSEDR:
            return ElevatorSimulation.DOOR_MOVING;
	    case Elevator.MV1FLR:
	        if(building.getDirection() == -1) {
	            return ElevatorSimulation.DOWN;
	        }
	        return ElevatorSimulation.UP;
	    case Elevator.MVTOFLR:
	        if(building.getDirection() == -1) {
                return ElevatorSimulation.DOWN;
            }
            return ElevatorSimulation.UP;
	    case Elevator.OFFLD:
            return ElevatorSimulation.OFFLOADING;
	    case Elevator.OPENDR:
            return ElevatorSimulation.DOOR_MOVING;
	    case Elevator.STOP:
            return ElevatorSimulation.STOPPED;
        default:
            throw new IllegalArgumentException();
	    }
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
	 * @return the numElevators
	 */
	public int getNumElevators() {
		return numElevators;
	}

	/**
	 * @param numElevators the numElevators to set
	 */
	public void setNumElevators(int numElevators) {
		this.numElevators = numElevators;
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
	 * @return the floorTicks
	 */
	public int getFloorTicks() {
		return floorTicks;
	}

	/**
	 * @param floorTicks the floorTicks to set
	 */
	public void setFloorTicks(int floorTicks) {
		this.floorTicks = floorTicks;
	}

	/**
	 * @return the doorTicks
	 */
	public int getDoorTicks() {
		return doorTicks;
	}

	/**
	 * @param doorTicks the doorTicks to set
	 */
	public void setDoorTicks(int doorTicks) {
		this.doorTicks = doorTicks;
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
	 * @return the endSim
	 */
	public boolean isEndSim() {
		return endSim;
	}

	/**
	 * @param endSim the endSim to set
	 */
	public void setEndSim(boolean endSim) {
		this.endSim = endSim;
	}

	/**
	 * @param building the building to set
	 */
	public void setBuilding(Building building) {
		this.building = building;
	}
	
	/**
	 * Gets the test name.
	 *
	 * @return the test name
	 */
	public String getTestName() {
		testfile.replace("\\c\\s\\v", "");
		return testfile;
	}
	
	/**
	 * Gets the current floor.
	 *
	 * @return the current floor
	 */
	public int getCurrentFloor() {
		return building.getCurrentFloor();
	}
	
	

	/**
	 * Gets the building. ONLY USED FOR JUNIT TESTING - YOUR GUI SHOULD NOT ACCESS THIS!.
	 *
	 * @return the building
	 */
	Building getBuilding() {
		return building;
	}
}
