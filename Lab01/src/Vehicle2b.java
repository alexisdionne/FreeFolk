package test;
import lejos.hardware.ev3.EV3;
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

import java.io.*;

import lejos.ev3.tools.LCDDisplay;
import lejos.hardware.*;


// Alexis, Jack, and Nic
// Lab1 - Vehicle2 Aggression


public class Vehicle2b {

	public static void main(String[] args) throws InterruptedException {
		// for use of the button
		EV3 ev3brick = (EV3) BrickFinder.getLocal();
		Keys buttons = ev3brick.getKeys();
		TextLCD LCD = ev3brick.getTextLCD();
		
		// Initialize light sensors (left and right)
		NXTLightSensor lsR = new NXTLightSensor(SensorPort.S4);
		NXTLightSensor lsL = new NXTLightSensor(SensorPort.S1);

		lsR.setCurrentMode("Ambient");
		lsL.setCurrentMode("Ambient");
		
		SampleProvider ambientR = lsR.getMode("Ambient");
		SampleProvider ambientL = lsL.getMode("Ambient");
		
		float[] sampleR = new float [ambientR.sampleSize()];
		float[] sampleL = new float [ambientL.sampleSize()];
		
		// initialize the light in the room
		float roomLight = avg(ambientR, sampleR);

		RegulatedMotor mR = new EV3MediumRegulatedMotor(MotorPort.B);
		RegulatedMotor mL = new EV3MediumRegulatedMotor(MotorPort.C);
		
		while(true) {
			ambientR.fetchSample(sampleR, 0);
			ambientL.fetchSample(sampleL, 0);
			
			LCD.drawString(String.valueOf(sampleR[0]), 2, 4);
			LCD.drawString(String.valueOf(sampleL[0]), 9, 4);
			// drawing text on the LCD screen based on coordinates

			// only move if the amount of light is larger than the room light
			if(sampleL[0] > roomLight+0.1)
				mR.rotate((int) (360*sampleL[0]));
			if(sampleR[0] > roomLight+0.1)
				mL.rotate((int) (360*sampleR[0]));
		    
		    // to escape the loop press middle button
		    if (buttons.readButtons() == buttons.ID_ENTER) {
		    	break;
		    }
		}
	}
	
	public static float avg(SampleProvider sample,float[] array)
	{
		float sum = 0;
		for(int i=0; i<1000; i++)
		{
			sample.fetchSample(array, 0);
			sum+= array[0];			
		}
		return(sum/(float) 1000.0);
	}

}
