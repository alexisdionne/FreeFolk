import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class Avoid implements Behavior {

	private boolean suppressed;

	private boolean leftTurn = false;
	private boolean rightTurn = false;

	NXTUltrasonicSensor usFront;
	NXTUltrasonicSensor usSide;

	MovePilot pilot;

	SampleProvider currDistFront;
	float[] sampleDistFront;
	SampleProvider currDistSide;
	float[] sampleDistSide;

	double maxspd;
	double defaultspd;

	public Avoid(NXTUltrasonicSensor ultraSensorFront, MovePilot p) {
		suppressed = false;

		// set sensors
		usFront = ultraSensorFront;
		//usSide = ultraSensorSide;
		pilot = p;

		// sample variable for data front
		currDistFront = usFront.getDistanceMode();
		sampleDistFront = new float[currDistFront.sampleSize()];

		// sample variable for data side
		//currDistSide = usSide.getDistanceMode();
		//sampleDistSide = new float[currDistSide.sampleSize()];

		// get max speed and set default speed
		maxspd = pilot.getMaxLinearSpeed();
		defaultspd = maxspd * 0.20;
	}

	@Override
	public boolean takeControl() {
		// too close to obstacle
		currDistFront.fetchSample(sampleDistFront, 0);
		return (sampleDistFront[0] < 0.22);
	}

	@Override
	public void action() {
		System.out.println(sampleDistFront[0]);
		suppressed = false;
		
		while(!suppressed)
		{
			//Thread.yield();
			currDistFront.fetchSample(sampleDistFront, 0);

			System.out.println(sampleDistFront[0]);
			if(sampleDistFront[0] < 0.22)
			{
				pilot.rotate(67);
				pilot.forward();
			}
			
			currDistFront.fetchSample(sampleDistFront, 0);
			if(sampleDistFront[0] > 0.22)
			{
				suppressed = true;
				break;
			}
		}
		/*// turn left and go around the obstacle
		while (!suppressed) {
			Thread.yield();
			currDistFront.fetchSample(sampleDistFront, 0);
			//currDistSide.fetchSample(sampleDistSide, 0);
			// turn left when obstacle is in front
			if (sampleDistFront[0] < 0.22) {
				pilot.rotate(90);
				pilot.forward();
				leftTurn = true;
			// after left turn
			} else {
				if (leftTurn) {
					// obstacle to the right move forward
					if (sampleDistSide[0] < 0.30) {
						pilot.setLinearSpeed(defaultspd);
						// no obstacle to the right travel and turn right
					} else if (sampleDistSide[0] > 0.30) {
						pilot.travel(250);
						pilot.rotate(-90);
						rightTurn = true;
					}
				}
			}
			// after a right turn and nothing is to the right suppress behavior
			if (sampleDistSide[0] > 0.30 && rightTurn) {
				suppressed = true;
				break;
			}
		}*/

	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}