import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Locate implements Behavior {

	private boolean suppressed;
	EV3IRSensor infrared;
	MovePilot pilot;
	SampleProvider irReading;
	float[] sample;

	public Locate(EV3IRSensor irSensor, MovePilot p) {
		suppressed = false;
		infrared = irSensor;
		pilot = p;
		irReading = infrared.getSeekMode();
		sample = new float[irReading.sampleSize()];
	}

	@Override
	public boolean takeControl() 
	{
		irReading.fetchSample(sample, 0);
		if(((int) sample[0] <= 0))
			return true;
		else 
			return false;
	}

	@Override
	public void action() {
		suppressed = false;

		while (!suppressed) {
			Thread.yield();

			irReading.fetchSample(sample, 0);
			pilot.stop();

			//SOUND BYTE
			Sound.systemSound(true, 1);

			if ((int) sample[1] > 0) {
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