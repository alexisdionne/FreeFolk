import lejos.robotics.subsumption.Behavior;
import lejos.robotics.navigation.MovePilot;

public class Move implements Behavior 
{

	private boolean suppressed;
	MovePilot pilot2;
	public Move(MovePilot p) 
	{
		pilot2 = p;
		suppressed = false;
	}

	@Override
	public boolean takeControl() 
	{
		return true;
	}

	@Override
	public void action() 
	{
		suppressed = false;
		
		double radius = 200.00; 
		
		while (!suppressed) 
		{
			Thread.yield();

			pilot2.arcForward(radius);
		}
			
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}