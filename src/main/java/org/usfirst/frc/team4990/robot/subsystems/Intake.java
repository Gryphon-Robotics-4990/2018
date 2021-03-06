package org.usfirst.frc.team4990.robot.subsystems;

import org.usfirst.frc.team4990.robot.RobotMap;
import org.usfirst.frc.team4990.robot.SmartDashboardController;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * An Intake.
 * @author Class of '21 (created in 2018 season)
 * 
 */

public class Intake extends Subsystem {
	private double speed;
	
	public Intake() {
	}
	
	/**
	 * Enum describing ultrasonic visibility of box. MOVING indicates in between IN and OUT.
	 * @author Class of '21 (created in 2018 season)
	 */
	
	public enum BoxPosition {
		IN, MOVING, OUT
	}
	
	/**
	 * Sets speed of intake. Does not auto-stop based on sensor. Execute by calling update().
	 * @param speedInput
	 */
	
	public void setSpeed(double speedInput) {
		speed = speedInput;
	}
	
	/* (plz don't make this a javadoc because it will override the default javadoc for this method)
	 * Executes new speed of inkate.
	 */
	
	public void periodic() {
		RobotMap.intakeTalonA.set(speed);
		RobotMap.intakeTalonB.set(-speed);
	}
	
	/**
	 * returns distance in volts (0 to 5) read by distance sensor
	 * @return distance in volts (0 to 5) read by distance sensor
	 */
	
	public double getAnalogInput() {
		return RobotMap.intakeDistanceAnalogInput.getAverageVoltage();
		
	}
	
	/**
	 * returns enum for where distance sensor sees box
	 * @return enum for where distance sensor sees box
	 */
	
	public BoxPosition getBoxPosition() {
		if (getAnalogInput() >= SmartDashboardController.getConst("Intake/BoxPosInThreshold", 0.2)) {
			return Intake.BoxPosition.IN;
		} else if (getAnalogInput() >= SmartDashboardController.getConst("Intake/BoxPosInThreshold", 0.24)) {
			return Intake.BoxPosition.MOVING;
		} else {
			return Intake.BoxPosition.OUT;
		}
	}
	
	/**
	 * boolean check for box location
	 * @param pos position to check for
	 * @return boolean if distance sensor sees box in specified position
	 */
	
	public boolean isBoxPosition(BoxPosition pos) {
		return getBoxPosition() == pos;
	}

	@Override
	protected void initDefaultCommand() {

	}
	
}
