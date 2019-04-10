import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;
import java.util.Arrays;
import java.util.Collections;

public class Explore implements Behavior {
		
	private boolean suppressed;

	EV3IRSensor ir;

	MovePilot pilot;

	SampleProvider currDist;
	float[] sampleDist;

	double maxspd;
	double defaultspd;

	Integer[] grid = new Integer[3];

	public Explore(EV3IRSensor irSensor, MovePilot p) {
		
		suppressed = false;

		// set sensors
		ir = irSensor;
		
		// set pilot
		pilot = p;

		// sample variable for data
		currDist = ir.getSeekMode();
		sampleDist = new float[currDist.sampleSize()];

		// get max speed and set default speed
		maxspd = pilot.getMaxLinearSpeed();
		defaultspd = maxspd * 0.30;
	}

	@Override
	public boolean takeControl() {
		// near beacon
		currDist.fetchSample(sampleDist, 0);
		return ((int) sampleDist[0] <= 30 && (int) sampleDist[0] > 0);
	}

	@Override
	public void action() {
		System.out.println("EXPLORE");
		suppressed = false;
		
		pilot.forward();
		float minimum=100000000;
		int shortestDistance = 0;
		// turn left to avoid obstacle
		while (!suppressed) 
		{
			//Thread.yield();
			
			//while(currDist.fetchSample(sample, offset);)
			
			pilot.rotate(-45);
			for(int i=0; i<=90; i+=10)
			{
				currDist.fetchSample(sampleDist, 0);
				if(sampleDist[0]<=minimum)
				{
					minimum = sampleDist[0];
					shortestDistance = i;
				}
				pilot.rotate(10);
			}
			pilot.rotate(-90);
			pilot.rotate(shortestDistance);
			currDist.fetchSample(sampleDist,0);
			System.out.println(sampleDist[0]);
			pilot.setLinearSpeed(maxspd*0.70);
			while((int)sampleDist[0] > 11)
			{
				currDist.fetchSample(sampleDist,0);
				if((int)sampleDist[0] < 0) 
				{
					break;
				}
				pilot.forward();
			}
			System.out.println("end of explore");
			suppressed = true;
			// when the beacon is not close suppress behavior
			//if ((int) sampleDist[1] > 30 || (int) sampleDist[1] < 0) {
				//suppressed = true;
				//break;
			//}
		}
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
