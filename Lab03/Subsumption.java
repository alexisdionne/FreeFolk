import lejos.hardware.ev3.EV3;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;


// Lab 03 - Subsumption Robot
// Nic, Jack, and Alexis


public class Subsumption {
	
	public static void main(String[] args) {
		// for use of the button
		EV3 ev3brick = (EV3) BrickFinder.getLocal();
		TextLCD LCD = ev3brick.getTextLCD(); 
		
		LCD.clear();
		LCD.drawString("Press to begin!", 0, 0);
		Button.waitForAnyPress();
		
		// motor stuff
		RegulatedMotor rightMotor = new EV3MediumRegulatedMotor(MotorPort.B);
		RegulatedMotor leftMotor = new EV3MediumRegulatedMotor(MotorPort.C);
		// move pilot
		// offset is distance between the robots yPose-axis and the center of the  wheel
		Wheel wheelL = WheeledChassis.modelWheel(leftMotor, /* Diameter in cm?*/15.0).offset(-9);
		Wheel wheelR = WheeledChassis.modelWheel(rightMotor, /* Diameter in cm?*/15.0).offset(9); 
		Chassis chassis = new WheeledChassis(new Wheel[] {wheelL, wheelR}, WheeledChassis.TYPE_DIFFERENTIAL);
		MovePilot mp = new MovePilot(chassis);
		mp.setLinearSpeed(4);
		mp.setAngularSpeed(45);
		
		// sensors 
		EV3IRSensor ir = new EV3IRSensor(SensorPort.S1); // infrared
		NXTUltrasonicSensor us = new NXTUltrasonicSensor(SensorPort.S4); // ultrasonic
		
		// create behaviors
		Behavior move = new Move(mp);
		Behavior avoid = new Avoid(us, mp, ir);
		Behavior find = new Find(mp, ir);
		Behavior listen = new Listen(mp, ir /*maybe pass a certain sound file for comedy*/);
		
		// array of behaviors
		Behavior [] bArray = {move, find, avoid, listen};
		
		Arbitrator arb = new Arbitrator(bArray);
		arb.go();
	}

}
