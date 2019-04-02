import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Listen implements Behavior{

	
	boolean suppressed = false;
	
	MovePilot mp;
	SampleProvider irDistanceSample;
	float[] irSample;
	
	public Listen(MovePilot mp, EV3IRSensor ir /*maybe pass a certain sound file for comedy*/) {
		this.mp = mp;
		irDistanceSample = ir.getSeekMode();
		irSample = new float[irDistanceSample.sampleSize()];
	}
	
	public boolean takeControl() {
		irDistanceSample.fetchSample(irSample, 0);
		return (Math.abs(irSample[0]) == 0);
	}

	public void action() {
		mp.stop();
		Sound.twoBeeps();
	}

	public void suppress() {
		suppressed = true;
	}
	
	
}
