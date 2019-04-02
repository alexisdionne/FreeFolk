import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Find implements Behavior{
	
	MovePilot mp;
	SampleProvider irDistanceSample;
	float[] irSample;
	
	boolean suppressed = true;
	
	public Find(MovePilot mp, EV3IRSensor ir) {
		this.mp = mp;
		irDistanceSample = ir.getSeekMode();
		irSample = new float[irDistanceSample.sampleSize()];
	}
	
	public boolean takeControl() {
		irDistanceSample.fetchSample(irSample, 0);
		return (Math.abs(irSample[0]) > 2);
	}
	
	public void action() {
		suppressed = false;
		irDistanceSample.fetchSample(irSample, 0);
		
		// finding
		// rotate towards the ir remote by rotating the inverse of the bearing value
		if(irSample[0] != 0) {
			mp.rotate(irSample[0]*-1);
		}
		
	}
	
	public void suppress() {
		suppressed = true;
	}

}
