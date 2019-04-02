import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Move implements Behavior{
	
	boolean suppressed = false;
	MovePilot mp;
	
	public Move(MovePilot mp) {
		this.mp = mp;
	}
	
	public boolean takeControl() {
		return true;
	}
	
	public void action() {
		suppressed = false;
		//System.out.println("Moving");
		
		// motor forward
		mp.forward();
	}

	
	public void suppress() {
		suppressed = true;
	}
}
