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
		return ((int) sampleDist[1] <= 30 && (int) sampleDist[1] > 0);
	}

	@Override
	public void action() {
		System.out.println("EXPLORE");
		suppressed = false;
		
		pilot.forward();

		// turn left to avoid obstacle
		while (!suppressed) {
			Thread.yield();

			currDist.fetchSample(sampleDist, 0);

			// stop closer to the beacon
			if ((int) sampleDist[1] <= 20) {
				pilot.stop();

				// check left for beacon distance
				pilot.rotate(45);
				pilot.travel(50);
				currDist.fetchSample(sampleDist, 0);
				grid[0] = (int) sampleDist[1];
				pilot.travel(-50);
				pilot.rotate(-45);

				// check center for beacon distance
				pilot.travel(50);
				currDist.fetchSample(sampleDist, 0);
				grid[1] = (int) sampleDist[1];
				pilot.travel(-50);

				// check right for beacon distance
				pilot.rotate(-45);
				pilot.travel(50);
				currDist.fetchSample(sampleDist, 0);
				grid[2] = (int) sampleDist[1];
				pilot.travel(-50);
				pilot.rotate(45);

				// find the shortest distance
				int min = Collections.min(Arrays.asList(grid));
				int ind = Arrays.asList(grid).indexOf(min);
				
				// go in the direction of the shortest distance
				switch (ind) {
					case 0:
						// move left
						pilot.rotate(45);
						pilot.travel(100);
						pilot.rotate(-45);
						break;
					case 1:
						// move center
						pilot.travel(100);
						break;
					case 2:
						// move right
						pilot.rotate(-45);
						pilot.travel(100);
						pilot.rotate(45);
						break;
				}

			// move forward when the beacon is not close enough
			} else {
				pilot.setLinearSpeed(defaultspd);
			}

			// when the beacon is not close suppress behavior
			if ((int) sampleDist[1] > 30 || (int) sampleDist[1] < 0) {
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
