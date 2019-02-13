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

import java.io.*;

import lejos.ev3.tools.LCDDisplay;
import lejos.hardware.*;

public class Vehicle2a {

	public static void main(String[] args) throws InterruptedException {
		// for use of the button
		EV3 ev3brick = (EV3) BrickFinder.getLocal();
		Keys buttons = ev3brick.getKeys();
				
		TextLCD LCD = ev3brick.getTextLCD();
		
		// Initialize light sensors (left and right)
		NXTLightSensor lsR = new NXTLightSensor(SensorPort.S1);
		NXTLightSensor lsL = new NXTLightSensor(SensorPort.S4);

		lsR.setCurrentMode("Ambient");
		lsL.setCurrentMode("Ambient");
		
		SampleProvider ambientR = lsR.getMode("Ambient");
		SampleProvider ambientL = lsL.getMode("Ambient");
		
		float[] sampleR = new float [ambientR.sampleSize()];
		float[] sampleL = new float [ambientL.sampleSize()];

		RegulatedMotor mR = new EV3LargeRegulatedMotor(MotorPort.C);
		RegulatedMotor mL = new EV3LargeRegulatedMotor(MotorPort.B);
		//LCD.drawString(buttons.waitForAnyEvent());
		while(buttons.waitForAnyPress(10000) > 0) {
			ambientR.fetchSample(sampleR, 0);
			ambientL.fetchSample(sampleL, 0);
			
			SampleProvider averageR = new MeanFilter(ambientR, 5);
			SampleProvider averageL = new MeanFilter(ambientL, 5);
			
			LCD.drawString("Percent: ", 2, 4);
			LCD.drawString(String.valueOf(sampleR[0]), 2, 9);
			LCD.drawString(String.valueOf(sampleL[0]), 9, 9);
			// drawing text on the LCD screen based on coordinates
		    Thread.sleep(1000);
		}
	}
}
