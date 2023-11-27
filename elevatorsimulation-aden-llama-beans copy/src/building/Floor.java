package building;
// ListIterater can be used to look at the contents of the floor queues for 
// debug/display purposes...
import java.util.ListIterator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import genericqueue.GenericQueue;

// TODO: Auto-generated Javadoc
/**
 * The Class Floor. This class provides the up/down queues to hold
 * Passengers as they wait for the Elevator.
 * 
 * Peer reviewed by Sania
 */
public class Floor {
	/**  Constant for representing direction. */
	private static final int UP = 1;
	
	/** The Constant DOWN. */
	private static final int DOWN = -1;

	/**  The queues to represent Passengers going up. */	
	private GenericQueue<Passengers> down;
	
	/** The queue to represent Passengers going down. */
	private GenericQueue<Passengers> up;

	/**
	 * Instantiates a new floor.
	 *
	 * @param qSize the q size
	 * Peer reviewed by Sania
	 */
	public Floor(int qSize) {
		down = new GenericQueue<Passengers>(qSize);
		up = new GenericQueue<Passengers>(qSize);
	}
	
	/**
	 * Checks if floor queue (up or down depending on direction dir) is empty or not.
	 *
	 * @param dir the direction
	 * @return true, w floor queue (up or down) is empty
	 * Peer reviewed by Sania
	 */
	protected boolean isEmptyFloorQueue(int dir) {
		return whichQueue(dir).isEmpty();
	}
	
	/**
	 * Checks for calls.
	 *
	 * @param dir the direction
	 * @return true, if successful
	 * Peer reviewed by Sania
	 */
	protected boolean hasCalls(int dir) {
		switch(dir) {
		case DOWN: 
			if(down.size()>0) 
				return true;
			break;
		case 0: 
			if(down.size()>0 || up.size()>0) 
				return true;
			break;
		case UP: 
			if(up.size()>0) 
				return true;
			break;
		}
		return false;
	}
	
	/**
	 * Gets the number of calls on floor.
	 *
	 * @param dir the direction
	 * @return the number of calls
	 * Peer reviewed by Sania
	 */
	protected int getNumOfCalls(int dir) {
		switch(dir) {
		case DOWN: return down.size();
		case 0: return down.size()+up.size();
		case UP: return up.size();
		}
		return -100;
	}
	
	/**
	 * Adds the passenger to floor queue given passenger direction.
	 * - uses whichQueue() helper method to determine which queue to add to based off of direction
	 *
	 * @param p the passenger
	 * Peer reviewed by Sania
	 */
	protected void addPassengerToFloorQueue(Passengers p) {
		whichQueue(p.getDirection()).add(p);
	}
	
	/**
	 * Peeks floor queue, (up, down, or all) which returns Passenger at the front of the queue
	 *
	 * @param dir the direction 
	 * @return the passengers
	 * Peer reviewed by Sania
	 */
	protected Passengers peekFloorQueue(int dir) {	
		return whichQueue(dir).peek();
	}
	
	/**
	 * Returns and Removes Passenger at the front of the queue
	 *
	 * @param dir the direction
	 * @return the passengers
	 * Peer reviewed by Sania
	 */
	protected Passengers pollFloorQueue(int dir) {	
		return whichQueue(dir).poll();
	}
	
	/**
	 * Given direction dir, identifies which queue to modify.
	 *
	 * @param dir the direction
	 * @return the generic queue to modify (up or down)
	 * Peer reviewed by Sania
	 */
	protected GenericQueue<Passengers> whichQueue(int dir) {
		if(dir==UP) {
			return up;
		} else {
			return down;
		}
	}
	
	/**
	 * Queue string. This method provides visibility into the queue
	 * contents as a string. What exactly you would want to visualize 
	 * is up to you
	 *
	 * @param dir determines which queue to look at
	 * @return the string of queue contents
	 * Peer reviewed by Sania
	 */
	String queueString(int dir) {
		String str = "";
		ListIterator<Passengers> list;
		list = (dir == UP) ?up.getListIterator() : down.getListIterator();
		if (list != null) {
			while (list.hasNext()) {
				// choose what you to add to the str here.
				// Example: str += list.next().getNumPass();
				if (list.hasNext()) str += ",";
			}
		}
		return str;	
	}
	
	
}
