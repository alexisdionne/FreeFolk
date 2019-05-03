import lejos.robotics.chassis.*;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.*;
import lejos.hardware.Keys;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.*;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.hardware.sensor.EV3IRSensor;

public class Subby{

	public static void main(String[] args) {

		Keys buttons = LocalEV3.get().getKeys();
		
<<<<<<< HEAD
		// initialize ports
		Port portS1 = LocalEV3.get().getPort("S1");
		Port portS3 = LocalEV3.get().getPort("S3");
		Port portS4 = LocalEV3.get().getPort("S4");
		Port portA = LocalEV3.get().getPort("A");
		Port portD = LocalEV3.get().getPort("D");

		// assign sensors to ports
		EV3IRSensor irSensor = new EV3IRSensor(portS3);
		//NXTUltrasonicSensor ultraSensorSide = new NXTUltrasonicSensor(portS1);
		NXTUltrasonicSensor ultraSensorFront = new NXTUltrasonicSensor(portS1);

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
		Behavior avoid = new Avoid(ultraSensorFront, pilot);
		Behavior explore = new Explore(irSensor, pilot);
		Behavior locate = new Locate(irSensor, pilot);

		// create array of behaviors
		 Behavior [] bArray = {move, explore, locate, avoid};

		// send array to arbitrator
		Arbitrator arby = new Arbitrator(bArray);
		
		arby.go();

		if(buttons.readButtons() == buttons.ID_ENTER)
		{
			arby.stop();
		}
=======
		EV3IRSensor Infrared = new EV3IRSensor(LocalEV3.get().getPort("S3"));
		NXTUltrasonicSensor USS = new NXTUltrasonicSensor(LocalEV3.get().getPort("S1"));

		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		Wheel rightWheel = WheeledChassis.modelWheel(rightMotor, 15).offset(-75);
		Wheel leftWheel = WheeledChassis.modelWheel(leftMotor, 15).offset(75);
		Chassis chass = new WheeledChassis(new Wheel[]{rightWheel, leftWheel}, WheeledChassis.TYPE_DIFFERENTIAL);
		MovePilot pilot = new MovePilot(chass);

		Behavior move = new Move(pilot);
		Behavior avoid = new Avoid(USS, pilot);
		Behavior find = new Find(Infrared, pilot);
		Behavior locate = new Locate(Infrared, pilot);


		Behavior [] behaviorSet = {move, find, locate, avoid};
		Arbitrator arbitrator = new Arbitrator(behaviorSet);
		arbitrator.go();
>>>>>>> 6e03a9e635bcbe32566fa77e824eda11d745bc39
		
	}

}
