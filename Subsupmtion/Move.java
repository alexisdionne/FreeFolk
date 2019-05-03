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
<<<<<<< HEAD

		// set motors
		mA = motorA;
		mD = motorD;
		
		//set pilot
		pilot = p;

		// get max speed and set default speed
		maxspdA = mA.getMaxSpeed();
		defaultspdA = (float) (maxspdA * 0.80);
		maxspdD = mD.getMaxSpeed();
		defaultspdD = (float) (maxspdD * 0.20);
		
		// get max speed and set default speed
		maxspd = pilot.getMaxLinearSpeed();
		defaultspd = maxspd * 0.45;
=======
>>>>>>> 6e03a9e635bcbe32566fa77e824eda11d745bc39
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