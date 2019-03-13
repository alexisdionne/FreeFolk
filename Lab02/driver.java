import lejos.hardware.ev3.EV3;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.port.AnalogPort;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.NXTLightSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
//import lejos

public class driver 
{

	public static void main(String[] args) throws InterruptedException 
	{
		//ALG:
		/*
		 * Fetch Sample
		 * Determine Error
		 * Determine Time past?
		 * 
		 */
		EV3 ev3brick = (EV3) BrickFinder.getLocal();
		Keys buttons = ev3brick.getKeys();
		TextLCD LCD = ev3brick.getTextLCD();
		
		//Port port1 = LocalEV3.get().getPort("S1");
		NXTUltrasonicSensor USS = new NXTUltrasonicSensor(SensorPort.S1);
		//USS.getMode("Distance");
		SampleProvider distanceOf = USS.getDistanceMode();
		
		RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
		RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		
		float[] sample = new float[distanceOf.sampleSize()];
		double expected = 15.00;
		double actual = 0.00;
		int kp = 50;
		double error = 0;
		double differentialError = 0;
		while(true)
		{ 
			
			distanceOf.fetchSample(sample, 0);
			actual = sample[0]*kp;
			error = expected - actual;
			
			//error>0
			if(error<2)
			{
				differentialError = expected - error;
				rightMotor.setSpeed((int)(kp*error));
				leftMotor.setSpeed((int)(kp*differentialError));
				rightMotor.forward();
				leftMotor.forward();
			}
			//error<0
			else if(error>-2)
			{
				differentialError = expected - Math.abs(error);
				rightMotor.setSpeed((int)(kp*differentialError));
				leftMotor.setSpeed((int)(kp*Math.abs(error)));
				rightMotor.forward();
				leftMotor.forward();
			}
			if(error>=-2 && error<=2)
			{
				rightMotor.setSpeed((int)(kp*expected));
				leftMotor.setSpeed((int)(kp*expected));
				rightMotor.forward();
				leftMotor.forward();
			}
			
			/*//kPactual = kP*sample[0];
			//kPError = kPexpected - kPactual;
			//if(kPError>0.5)
			{
				
			}
			else //(kPError<0.5)
			{
				rightMotor.setSpeed(100);
				leftMotor.setSpeed(200);
				rightMotor.forward();
				leftMotor.forward();
			}*/
			
			/*if(sample[0]*100>10.00)
				rightMotor.setSpeed((int)(360*sample[0]));//setSpeed(]);
				leftMotor.set
			*/
			//LCD.drawString(String.valueOf(sample[0]), 2, 4);
			LCD.drawString(Float.toString(sample[0]), 2, 4);
			//Thread.sleep(2000);
			//LCD.drawString(String.valueOf(USS.getD), , y);
			if (buttons.readButtons() == buttons.ID_ENTER)
			{
				break;
			}
		}
	}

}

