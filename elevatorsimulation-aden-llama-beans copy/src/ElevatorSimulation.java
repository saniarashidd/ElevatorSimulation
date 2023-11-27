import java.util.ArrayList;
import java.util.List;

import building.Elevator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The Class ElevatorSimulation
 * 
 * Peer reviewed by Aaron
 */
public class ElevatorSimulation extends Application {
	/** Instantiate the GUI fields */
	private ElevatorSimController controller;
	private final int NUM_FLOORS;
	private final int NUM_ELEVATORS;
	private int currFloor;
	private int passengers;
	private int time;

	/** Local copies of the states for tracking purposes */
	public static final int UP = 0;
	public static final int DOWN = 1;
	public static final int BOARDING = 2;
	public static final int OFFLOADING = 3;
	public static final int DOOR_MOVING = 4;
	public static final int STOPPED = 5;
	
	private BorderPane borderPane = new BorderPane();
	private GridPane gridPane = new GridPane();
	private StackPane stackPane = new StackPane();
	
	private Polygon up = new Polygon();
	private Polygon down = new Polygon();
	private Polygon boarding = new Polygon();
	private Polygon offloading = new Polygon();
	private Polygon doorMoving = new Polygon();
	private Polygon stopped = new Polygon();
	
	private Polygon upKey = new Polygon();
	private Polygon downKey = new Polygon();
	private Polygon boardingKey = new Polygon();
	private Polygon offloadingKey = new Polygon();
	private Polygon doorMovingKey = new Polygon();
	private Polygon stoppedKey = new Polygon();
	
	private Label upLabel = new Label("Going Up");
	private Label downLabel = new Label("Going Down");
	private Label boardingLabel = new Label("Boarding");
	private Label offloadingLabel = new Label("Offloading");
	private Label doorMovingLabel = new Label("Doors Moving");
	private Label stoppedLabel = new Label("Elevator Stopped");
	
	private Button run = new Button("Run");
	private Button pause = new Button("Pause");
	private Button stop = new Button("Stop");
	private Label timeLabel = new Label("Time: 0");
	private Button step = new Button("Step");
	private TextField numSteps = new TextField();
	private Label cycles = new Label("Cycle(s)");
	private Button log = new Button("Log");
	
	private PassGroupsAtFloor passGroupsF1 = new PassGroupsAtFloor();
	private PassGroupsAtFloor passGroupsF2 = new PassGroupsAtFloor();
	private PassGroupsAtFloor passGroupsF3 = new PassGroupsAtFloor();
	private PassGroupsAtFloor passGroupsF4 = new PassGroupsAtFloor();
	private PassGroupsAtFloor passGroupsF5 = new PassGroupsAtFloor();
	private PassGroupsAtFloor passGroupsF6 = new PassGroupsAtFloor();
	
	private Label passNumLabel = new Label("0 Passenger(s)");
	private Timeline timeline;

	/**
	 * Instantiates a new elevator simulation.
	 * Peer reviewed by Aaron
	 */
	public ElevatorSimulation() { 
		controller = new ElevatorSimController(this);
		NUM_FLOORS = controller.getNumFloors();
		NUM_ELEVATORS = controller.getNumElevators();
		currFloor = controller.getCurrentFloor();
	}

	/**
	 * Starts the gui
	 *
	 * @param primaryStage the primary stage
	 * @throws Exception the exception
	 * Peer reviewed by Aaron
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setResizable(false);
		setGridPaneAlignment();
		gridPane.add(passNumLabel, 0, 5);
		
		makeShapes();
		setShapeVisibility(STOPPED);
		stackPane.getChildren().addAll(up, down, boarding, offloading, doorMoving, stopped);
		gridPane.add(stackPane, 1, 5);
		addPassLabelsToView();
		borderPane.setCenter(gridPane);
		
		HBox topBox = new HBox(15);
		makeTopRow(topBox);
		borderPane.setTop(topBox);
		runButtonClicks();
		
		borderPane.setRight(makePictureKey());
		
		Scene scene = new Scene(borderPane, 600, 400);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Elevator Simulation - " + controller.getTestName());
		primaryStage.show();
	}
	
	/**
	 * Sets up all the buttons, and tells
	 * the buttons what code to run if and when
	 * they are pressed
	 * Peer reviewed by Aaron
	 */
	private void runButtonClicks() {
		run.setOnAction(e -> initTimeline(-1));
		pause.setOnAction(e -> initTimeline(0));
		stop.setOnAction(e -> endSimulation());
		step.setOnAction(e -> {
			int numToStep;
			if(numSteps.getText().equals("")) {
				numToStep = 1;
			} else {
			    try {
			        numToStep = Integer.parseInt(numSteps.getText());
			    } catch (NumberFormatException ex) {
			        System.out.println("You must put a number in the step text field!");
			        numToStep = 0;
			    }
			}
			initTimeline(numToStep);
		});
		log.setOnAction(e -> controller.enableLogging());
	}
	
	/**
	 * Sets the constraints for the rows
	 * and columns on the gridPane
	 * Peer reviewed by Aaron
	 */
	private void setGridPaneAlignment() {
		for(int i = 0; i < NUM_FLOORS; i++) {
			gridPane.getRowConstraints().add(new RowConstraints(60));
		}
		for(int i = 0; i < 3; i++) {
			if(i == 0) {
				gridPane.getColumnConstraints().add(new ColumnConstraints(100));
			} else if(i == 2) {
				gridPane.getColumnConstraints().add(new ColumnConstraints(200));
			} else {
				gridPane.getColumnConstraints().add(new ColumnConstraints(60));
			}
		}
	}
	
	/**
	 * Removes passenger groups from
	 * the GUI, given the floor they were
	 * on, and the direction that they
	 * were trying to go
	 * 
	 * @param floorToRemoveFrom
	 * @param direction
	 * @param numGroupsToRemove
	 * @throws IllegalArgumentException
	 * Peer reviewed by Aaron
	 */
	public void removePassGroupsFromGUI(int floorToRemoveFrom, int direction, int numGroupsToRemove) throws IllegalArgumentException {
	    PassGroupsAtFloor floor = getFloor(floorToRemoveFrom);
        int removed = 0;
        for (int i = 0; i < floor.groups.size(); i++) {
            PassengerGroup group = floor.groups.get(i);
            if (group.direction == direction) {
                floor.groups.remove(i);
                i--;
                removed++;
            }
            if (removed >= numGroupsToRemove) {
                break;
            }
        }
        floor.updateLabel();
    }

	/**
	 * Figures out which floor group a
	 * passenger group should be added to
	 * 
	 * @param floorNumber
	 * @return group to be added to
	 * Peer reviewed by Aaron
	 */
    private PassGroupsAtFloor getFloor(int floorNumber) {
	    switch(floorNumber) {
	    case 0:
	        return passGroupsF1;
	    case 1:
	        return passGroupsF2;
	    case 2:
	        return passGroupsF3;
	    case 3:
	        return passGroupsF4;
	    case 4:
	        return passGroupsF5;
	    case 5:
	        return passGroupsF6;
        default:
            throw new IllegalArgumentException("invalid floor: " + floorNumber);
	    }
    }

    /**
     * Sets up the shapes for the
     * elevator shapes, making their
     * points, colors, and borders
     * Peer reviewed by Aaron
     */
	private void makeShapes() {
		up.getPoints().addAll(0.0, 50.0, 50.0, 50.0, 25.0, 0.0);
		up.setStroke(Color.BLACK);
		up.setStrokeWidth(2);
		up.setFill(Color.GREEN);
		
		down.getPoints().addAll(0.0, 0.0, 50.0, 0.0, 25.0, 50.0);
		down.setStroke(Color.BLACK);
		down.setStrokeWidth(2);
		down.setFill(Color.GREEN);
		
		boarding.getPoints().addAll(50.0, 0.0, 50.0, 50.0, 0.0, 25.0);
		boarding.setStroke(Color.BLACK);
		boarding.setStrokeWidth(2);
		boarding.setFill(Color.YELLOW);
		
		offloading.getPoints().addAll(0.0, 0.0, 0.0, 50.0, 50.0, 25.0);
		offloading.setStroke(Color.BLACK);
		offloading.setStrokeWidth(2);
		offloading.setFill(Color.YELLOW);
		
		doorMoving.getPoints().addAll(0.0, 0.0, 0.0, 50.0, 50.0, 50.0, 50.0, 0.0);
		doorMoving.setStroke(Color.BLACK);
		doorMoving.setStrokeWidth(2);
		doorMoving.setFill(Color.ORANGE);
		
		stopped.getPoints().addAll(0.0, 25.0, 15.0, 0.0, 35.0, 0.0, 50.0, 25.0, 35.0, 50.0, 15.0, 50.0);
		stopped.setStroke(Color.BLACK);
		stopped.setStrokeWidth(2);
		stopped.setFill(Color.RED);
	}
	
	/**
	 * Sets up the pictures to go in
	 * the elevator shape key
	 * Peer reviewed by Aaron
	 */
	private void makeKeys() {
		upKey.getPoints().addAll(0.0, 50.0, 50.0, 50.0, 25.0, 0.0);
		upKey.setStroke(Color.BLACK);
		upKey.setStrokeWidth(2);
		upKey.setFill(Color.GREEN);
		
		downKey.getPoints().addAll(0.0, 0.0, 50.0, 0.0, 25.0, 50.0);
		downKey.setStroke(Color.BLACK);
		downKey.setStrokeWidth(2);
		downKey.setFill(Color.GREEN);
		
		boardingKey.getPoints().addAll(50.0, 0.0, 50.0, 50.0, 0.0, 25.0);
		boardingKey.setStroke(Color.BLACK);
		boardingKey.setStrokeWidth(2);
		boardingKey.setFill(Color.YELLOW);
		
		offloadingKey.getPoints().addAll(0.0, 0.0, 0.0, 50.0, 50.0, 25.0);
		offloadingKey.setStroke(Color.BLACK);
		offloadingKey.setStrokeWidth(2);
		offloadingKey.setFill(Color.YELLOW);
		
		doorMovingKey.getPoints().addAll(0.0, 0.0, 0.0, 50.0, 50.0, 50.0, 50.0, 0.0);
		doorMovingKey.setStroke(Color.BLACK);
		doorMovingKey.setStrokeWidth(2);
		doorMovingKey.setFill(Color.ORANGE);
		
		stoppedKey.getPoints().addAll(0.0, 25.0, 15.0, 0.0, 35.0, 0.0, 50.0, 25.0, 35.0, 50.0, 15.0, 50.0);
		stoppedKey.setStroke(Color.BLACK);
		stoppedKey.setStrokeWidth(2);
		stoppedKey.setFill(Color.RED);
	}
	
	/**
	 * Adds all the labels, text boxes,
	 * and text fields to a box that is
	 * to be put at the top of the GUI
	 * 
	 * @param topBox
	 * Peer reviewed by Aaron
	 */
	private void makeTopRow(HBox topBox) {
		HBox stepChildren = new HBox(1);
		stepChildren.getChildren().addAll(step, numSteps, cycles);
		topBox.getChildren().addAll(run, pause, stop, timeLabel, stepChildren, log);
	}
	
	/**
	 * Adds all the constraints, as well
	 * as the pictures and labels for the
	 * key that is put on the right side
	 * of the GUI
	 * 
	 * @return gridPane
	 * Peer reviewed by Aaron
	 */
	private GridPane makePictureKey() {
		makeKeys();
		keyVisibility();
		
		GridPane gridPane = new GridPane();
		
		for(int i = 0; i < NUM_FLOORS - 1; i++) {
			gridPane.getRowConstraints().add(new RowConstraints(60));
		}
		for(int i = 0; i < 2; i++) {
			if(i == 0) {
				gridPane.getColumnConstraints().add(new ColumnConstraints(60));
			} else {
				gridPane.getColumnConstraints().add(new ColumnConstraints(100));
			}
		}
		
		gridPane.add(upKey, 0, 0);
		gridPane.add(downKey, 0, 1);
		gridPane.add(boardingKey, 0, 2);
		gridPane.add(offloadingKey, 0, 3);
		gridPane.add(doorMovingKey, 0, 4);
		gridPane.add(stoppedKey, 0, 5);
		
		gridPane.add(upLabel, 1, 0);
		gridPane.add(downLabel, 1, 1);
		gridPane.add(boardingLabel, 1, 2);
		gridPane.add(offloadingLabel, 1, 3);
		gridPane.add(doorMovingLabel, 1, 4);
		gridPane.add(stoppedLabel, 1, 5);
		
		return gridPane;
	}
	
	/**
	 * Moves the elevator and the 
	 * label with the number of passengers
	 * on the elevator to the correct floor,
	 * and updates the passenger label
	 * 
	 * @param floorToMoveTo
	 * @param numPassengersOnElevator
	 * Peer reviewed by Aaron
	 */
	public void moveElevator(int floorToMoveTo, int numPassengersOnElevator) {
	    if (floorToMoveTo < 0 || floorToMoveTo > 5) {
	        throw new IllegalArgumentException();
	    }
	    int floorIndex = 5 - floorToMoveTo;
	    if(floorToMoveTo != currFloor) {
	        gridPane.getChildren().remove(passNumLabel);
            gridPane.add(passNumLabel, 0, floorIndex);
	        gridPane.getChildren().remove(stackPane);
	        gridPane.add(stackPane, 1, floorIndex);
	        currFloor = floorToMoveTo;
	    }
	    updatePassengerNum(numPassengersOnElevator);
	}
	
	/**
	 * Sets the current elevator state
	 * visibility given which state
	 * the elevator should be in
	 * 
	 * @param shapeToSetVisible
	 * @throws IllegalArgumentException
	 * Peer reviewed by Aaron
	 */
	public void setShapeVisibility(int shapeToSetVisible) throws IllegalArgumentException {
		up.setVisible(false);
		down.setVisible(false);
		boarding.setVisible(false);
		offloading.setVisible(false);
		doorMoving.setVisible(false);
		stopped.setVisible(false);
		
		if(shapeToSetVisible == UP) {
			up.setVisible(true);
		} else if(shapeToSetVisible ==  DOWN) {
			down.setVisible(true);
		} else if(shapeToSetVisible == BOARDING) {
			boarding.setVisible(true);
		} else if(shapeToSetVisible == OFFLOADING) {
			offloading.setVisible(true);
		} else if(shapeToSetVisible == DOOR_MOVING) {
			doorMoving.setVisible(true);
		} else if(shapeToSetVisible == STOPPED) {
			stopped.setVisible(true);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Makes sure that all the shapes
	 * in the key are visible
	 * Peer reviewed by Aaron
	 */
	private void keyVisibility() {
		upKey.setVisible(true);
		downKey.setVisible(true);
		boardingKey.setVisible(true);
		offloadingKey.setVisible(true);
		doorMovingKey.setVisible(true);
		stoppedKey.setVisible(true);
	}
	
	/**
	 * Updates the number of passengers
	 * that are on the elevator
	 * 
	 * @param numPassengers
	 * Peer reviewed by Aaron
	 */
	public void updatePassengerNum(int numPassengers) {
		passNumLabel.setText(numPassengers + " Passenger(s)");
	}
	
	/**
	 * Updates the labels on each floor
	 * that say the passenger groups that
	 * are waiting for the elevator
	 * 
	 * @param numPassInGroup
	 * @param direction
	 * @param floorStartedOn
	 * Peer reviewed by Aaron
	 */
	public void updatePassengerView(int numPassInGroup, int direction, int floorStartedOn) {
        PassGroupsAtFloor floor = getFloor(floorStartedOn);
        PassengerGroup group = new PassengerGroup(numPassInGroup, direction);
        floor.groups.add(group);
        floor.updateLabel();
	}
	
	/**
	 * Adds all the lists of passenger
	 * groups to the GUI based on their floor
	 * Peer reviewed by Aaron
	 */
	private void addPassLabelsToView() {
		gridPane.add(passGroupsF1.label, 2, 5);
		gridPane.add(passGroupsF2.label, 2, 4);
		gridPane.add(passGroupsF3.label, 2, 3);
		gridPane.add(passGroupsF4.label, 2, 2);
		gridPane.add(passGroupsF5.label, 2, 1);
		gridPane.add(passGroupsF6.label, 2, 0);
	}
	
	/**
	 * Updates the time given the time
	 * 
	 * @param timeToChangeTo
	 * Peer reviewed by Aaron
	 */
	public void updateTimeLabel(int timeToChangeTo) {
		timeLabel.setText("Time: " + timeToChangeTo);
	}
	
	/**
	 * Runs the simulation a number of times;
	 * -1 for indefinite, 0 for pause, and a
	 * positive number for that many times
	 * 
	 * @param numOfCycles
	 * Peer reviewed by Aaron
	 */
	private void initTimeline(int numOfCycles) {
		if(timeline == null) {
		    timeline = new Timeline(new KeyFrame(Duration.millis(250), ae -> controller.stepSim()));
	    }
		
		timeline.stop();
		if(numOfCycles == -1) {
			timeline.setCycleCount(Animation.INDEFINITE);
			timeline.play();
		} else if(numOfCycles != 0) {
		    timeline.setCycleCount(numOfCycles);
	        timeline.play();
		}
	}
	
	/**
	 * Closes the program
	 * Peer reviewed by Aaron
	 */
	public void endSimulation() {
		System.exit(0);
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main (String[] args) {
		Application.launch(args);
	}
	
	/**
	 * Holds information needed for the GUI
	 * about a singal passenger group
	 * 
	 * @author connorvolkert
	 * Peer reviewed by Aaron
	 */
	private static class PassengerGroup {
	    int size;
	    int direction;
	    
	    /**
	     * Creates a new instance of
	     * a PassengerGroup
	     * 
	     * @param size
	     * @param direction
	     */
	    PassengerGroup(int size, int direction) {
	        this.size = size;
	        this.direction = direction;
	    }
	    
	    @Override
	    public String toString() {
	        String result = "";
	        
	        if(direction == 1) {
	            result = "U";
	        } else {
	            result = "D";
	        }
	        
	        result = size + result;
	        return result;
	    }
	}
	
	/**
	 * Holds information needed by the
	 * GUI about which passenger groups
	 * are waiting at a certain floor
	 * 
	 * @author connorvolkert
	 * Peer reviewed by Aaron
	 */
	private static class PassGroupsAtFloor {
        List<PassengerGroup> groups = new ArrayList<PassengerGroup>();
	    Label label = new Label("");
	    
	    void updateLabel() {
	        List<String> groupStrings = new ArrayList<String>();
	        for (PassengerGroup group : groups) {
	            groupStrings.add(group.toString());
	        }
	        label.setText(String.join(", ", groupStrings));
	    }
	}
}