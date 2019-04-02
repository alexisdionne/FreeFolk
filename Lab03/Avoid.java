import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Avoid implements Behavior{

	boolean suppressed = false;
	
	MovePilot mp;
	
	SampleProvider usDistanceSample;
	float[] usSample;
	SampleProvider irDistanceSample;
	float[] irSample;
	
	public Avoid(NXTUltrasonicSensor us, MovePilot mp, EV3IRSensor ir) {
		this.mp = mp;
		
		usDistanceSample = us.getDistanceMode();
		usSample = new float[usDistanceSample.sampleSize()];
		irDistanceSample = ir.getSeekMode();
		irSample = new float[irDistanceSample.sampleSize()];
	}
	
	public boolean takeControl() {
		// return true if the bot is about to hit something
		usDistanceSample.fetchSample(usSample, 0);
		return (Math.abs(usSample[0]) < 10);
	}

	public void action() {
		suppressed = false;
		
		usDistanceSample.fetchSample(usSample, 0);
		irDistanceSample.fetchSample(irSample, 0);
		
		// decide how to best avoid this obstacle
		while(usSample[0] < 10) {
			// fetch fresh data
			usDistanceSample.fetchSample(usSample, 0);
			irDistanceSample.fetchSample(irSample, 0);
			
			// rotate towards the ir reading if there is one
			if(irSample[0] >= -25 || irSample[0] <= 25) {
				mp.rotate(irSample[0]); 
			}
			else {
				mp.rotate(45.0); // a 45 degree turn ought to be enough right?
			}
		}
	}

	public void suppress() {
		suppressed = true;
	}
	
	
}
