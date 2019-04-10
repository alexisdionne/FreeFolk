import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.subsumption.Behavior;
import lejos.robotics.navigation.MovePilot;
import java.util.Timer;
import java.util.TimerTask;


public class Move implements Behavior {

	private boolean suppressed;

	EV3LargeRegulatedMotor mA;
	EV3LargeRegulatedMotor mD;
	
	MovePilot pilot;

	SampleProvider currDist;
	float[] sampleDist;
	
	double maxspd;
	double defaultspd;

	float maxspdA;
	float defaultspdA;
	float maxspdD;
	float defaultspdD;
	float scaleA = (float) 0.05;
	int count = 0;

	public Move(EV3LargeRegulatedMotor motorA, EV3LargeRegulatedMotor motorD, MovePilot p) {
		suppressed = false;

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
	}

	@Override
	public boolean takeControl() {
		// base behavior
		return true;
	}

	@Override
	public void action() {
		System.out.println("MOVE");
		suppressed = false;
		
		double radius_circle = 100.0; // starting radius of wander circle
		int max_radius_circle = 122; // max cap on radius of wander circle
		double radius_scaling_factor = 1.3; // amount to scale radius_circle
		int count_mod = 250000;
		int count_scale = 200;
		
		pilot.setLinearSpeed(defaultspd);

		// move forward if there is nothing in front
		while (!suppressed) {
			Thread.yield();
						
			if(count % count_mod == 0){
				//count_mod =+ count_scale;
				radius_circle = radius_circle * radius_scaling_factor;
				pilot.arcForward(radius_circle);
			}
			count++;
		}
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

}