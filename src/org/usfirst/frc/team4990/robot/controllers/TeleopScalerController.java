package org.usfirst.frc.team4990.robot.controllers;

import org.usfirst.frc.team4990.robot.subsystems.Scaler;
import org.usfirst.frc.team4990.robot.subsystems.F310Gamepad;
/**
 * Class for Scaler
 * @author Old Coders
 *
 */
public class TeleopScalerController {
	private Scaler scaler;
	private F310Gamepad gpad;
	private double speed;
	/**
	 * Constructor for class
	 * @param scale Scaler to controller
	 * @param pad Gamepad to read input from
	 * @param spd Speed to scale at
	 * @author Old Coders
	 */
	public TeleopScalerController(Scaler scale, F310Gamepad pad, double spd) {
		scaler = scale;
		gpad = pad;
		speed = spd;
	}
	
	/**
	 * Reads input and updates scaler
	 * @author Old Coders
	 */
	public void update() {		
		if(gpad.getLeftBumperPressed() && !gpad.getRightBumperPressed()) {
			scaler.setSpeed(-speed);
			return;
		} if(gpad.getRightBumperPressed() && !gpad.getLeftBumperPressed()) {
			scaler.setSpeed(speed);
			return;
		} else {
			scaler.setSpeed(0);
		}
	}

}
