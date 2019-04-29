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
		
	}

}
