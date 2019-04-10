import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.*;
import lejos.hardware.Keys;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.EV3IRSensor;

public class Subby{

	public static void main(String[] args) {

		Keys buttons = LocalEV3.get().getKeys();
		
		// initialize ports
		Port portS1 = LocalEV3.get().getPort("S1");
		Port portS3 = LocalEV3.get().getPort("S3");
		Port portS4 = LocalEV3.get().getPort("S4");
		Port portA = LocalEV3.get().getPort("A");
		Port portD = LocalEV3.get().getPort("D");

		// assign sensors to ports
		EV3IRSensor irSensor = new EV3IRSensor(portS3);
		NXTUltrasonicSensor ultraSensorSide = new NXTUltrasonicSensor(portS1);
		NXTUltrasonicSensor ultraSensorFront = new NXTUltrasonicSensor(portS4);

		// initialize motors and pilot
		EV3LargeRegulatedMotor motorA = new EV3LargeRegulatedMotor(portA);
		EV3LargeRegulatedMotor motorD = new EV3LargeRegulatedMotor(portD);
		Wheel wheelA = WheeledChassis.modelWheel(motorA, 43.2).offset(-72);
		Wheel wheelD = WheeledChassis.modelWheel(motorD, 43.2).offset(72);
		//Wheel wheelx = WheeledChassis.modelWheel(motor, diameter)
		Chassis chassis = new WheeledChassis(new Wheel[]{wheelA, wheelD}, WheeledChassis.TYPE_DIFFERENTIAL);
		MovePilot pilot = new MovePilot(chassis);

		// create behaviors
		Behavior move = new Move(motorA, motorD, pilot);
		//Behavior avoid = new Avoid(ultraSensorFront, pilot);
		Behavior explore = new Explore(irSensor, pilot);
		Behavior locate = new Locate(irSensor, pilot);

		// create array of behaviors
		 Behavior [] bArray = {move, explore, locate};

		// send array to arbitrator
		Arbitrator arby = new Arbitrator(bArray);
		
		arby.go();
		if(buttons.readButtons() == buttons.ID_ENTER)
		{
			arby.stop();
		}
		
	}

}
