import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Explore implements Behavior {
		
	private boolean suppressed;

	EV3IRSensor ir;
	MovePilot pilot;
	SampleProvider seeker;
	float[] sample;

	Integer[] grid = new Integer[3];

	public Explore(EV3IRSensor infrared, MovePilot pilotSet) 
	{	
		suppressed = false;
		ir = infrared;
		pilot = pilotSet;
		seeker = ir.getSeekMode();
		sample = new float[seeker.sampleSize()];

	}

	@Override
	public boolean takeControl() 
    {
		// near beacon
		seeker.fetchSample(sample, 0);
		if((int) sample[0] <= 30 && (int) sample[0] > 0)
        {
            return(true);
        }
        else
        {
            return(false);
        }
	}

	@Override
	public void action() {
		System.out.println("EXPLORE");
		suppressed = false;
		
		pilot.forward();
		float minimum=100000000;
		int shortestDistance = 0;
		while (!suppressed) 
		{
			
			pilot.rotate(-45);
			for(int i=0; i<=90; i+=10)
			{
				seeker.fetchSample(sample, 0);
				if(sample[0]<=minimum)
				{
					minimum = sample[0];
					shortestDistance = i;
				}
				pilot.rotate(10);
			}
			pilot.rotate(-90);
			pilot.rotate(shortestDistance);
			seeker.fetchSample(sample,0);
			System.out.println(sample[0]);
			pilot.setLinearSpeed(pilot.getMaxLinearSpeed()*0.70);
			while((int)sample[0] > 11)
			{
				seeker.fetchSample(sample,0);
				if((int)sample[0] < 0) 
				{
					break;
				}
				pilot.forward();
			}
			System.out.println("end of explore");
			suppressed = true;

		}
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
