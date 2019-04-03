import lejos.hardware.ev3.EV3;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.HiTechnicEOPD;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.NXTUltrasonicSensor;

public class driver 
{

	public static void main(String[] args) throws InterruptedException 
	{
		EV3 ev3brick = (EV3) BrickFinder.getLocal();
		Keys buttons = ev3brick.getKeys();
		TextLCD LCD = ev3brick.getTextLCD();
		
		//Port port1 = LocalEV3.get().getPort("S1");
		NXTUltrasonicSensor USS = new NXTUltrasonicSensor(SensorPort.S1);
		HiTechnicEOPD eopd = new HiTechnicEOPD(SensorPort.S4); 
		
		SampleProvider eopdSample = eopd.getLongDistanceMode(); 
		SampleProvider distanceOf = USS.getDistanceMode();
		
		RegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
		RegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);
		
		float[] sample = new float[distanceOf.sampleSize()];
		float[] lightSample = new float[eopdSample.sampleSize()];
		double expected = 15.00;
		double actual = 0.00;
		int kp = 50;
		double error = 0;
		double differentialError = 0;
		
		while(true)
		{ 
			
			// getting sample data
			distanceOf.fetchSample(sample, 0);
			eopdSample.fetchSample(lightSample, 0);
			actual = sample[0]*kp;
			error = expected - actual;
			
			// avoid wall if light gets reading
			// got 4th decimal by multiplying by 1000 because those values changed the most
			if(lightSample[0] * 1000 > 9700)
			{
				// stop and rotate on light sample
				leftMotor.rotate(400);
				rightMotor.stop();
			}
			
			else
			{
					
				// This is the proportional part, speed is set based on the distance away or near the wall
				if(actual > 10)
				{
					rightMotor.setSpeed((int)(kp*actual));
					leftMotor.setSpeed((int)(kp*(0.25*actual)));
					rightMotor.forward();
					leftMotor.forward();
				}
				
				// sort of an integral type part here, as you are in the buffer, adjust the direction with the wheels accordingly
				// this happens for this whole if else statement
				if(error >2)
				{
					differentialError = expected - error;
					rightMotor.setSpeed((int)(kp*error));
					leftMotor.setSpeed((int)(kp*differentialError));
					rightMotor.forward();
					leftMotor.forward();
				}
				
				else if(error < -2)
				{
					
					differentialError = expected - Math.abs(error);
					rightMotor.setSpeed((int)(kp*differentialError));
					leftMotor.setSpeed((int)(kp*Math.abs(error)));
					rightMotor.forward();
					leftMotor.forward();
				}
				
				else if(error>=-2 && error<=2)
				{
					rightMotor.setSpeed((int)(kp*expected));
					leftMotor.setSpeed((int)(kp*expected));
					rightMotor.forward();
					leftMotor.forward();
				}
			}
			
			// Here we are printing out the value of the light sensor.
			// Commented out is the code for printing out the distance from the wall
			LCD.drawString(String.valueOf(lightSample[0]), 2, 4);
			
			if (buttons.readButtons() == buttons.ID_ENTER)
			{
				break;
			}
		}
		rightMotor.close();
		leftMotor.close();
		eopd.close();
		USS.close();
		
	}
}