import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Locate implements Behavior {

	private boolean suppressed;

	EV3IRSensor ir;

	MovePilot pilot;

	SampleProvider currDist;
	float[] sampleDist;

	public Locate(EV3IRSensor irSensor, MovePilot p) {
		suppressed = false;

		// set sensors
		ir = irSensor;
		pilot = p;

		// sample variable for data
		currDist = ir.getSeekMode();
		sampleDist = new float[currDist.sampleSize()];
	}

	@Override
	public boolean takeControl() {
		// close to beacon
		currDist.fetchSample(sampleDist, 0);
		return ((int) sampleDist[1] <= 0);
	}

	@Override
	public void action() {
		System.out.println("LOCATE");
		suppressed = false;

		// turn left to avoid obstacle
		while (!suppressed) {
			Thread.yield();

			currDist.fetchSample(sampleDist, 0);

			// stop in front of beacon
			pilot.stop();

			// beep at beacon
			Sound.systemSound(true, 1);

			// when the beacon is not in front suppress behavior
			if ((int) sampleDist[1] > 0) {
				suppressed = true;
				break;
			}
		}
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}
