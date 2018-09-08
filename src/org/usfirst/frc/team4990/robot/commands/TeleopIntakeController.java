package org.usfirst.frc.team4990.robot.commands;

import org.usfirst.frc.team4990.robot.RobotMap;

import edu.wpi.first.wpilibj.command.Command;

/**
 * Class for intake in teleop period
 * @author Class of '21 (created in 2018 season)
 *
 */
public class TeleopIntakeController extends Command{
	
	public enum direction {IN, OUT};
	
	private direction dir;

	private double maxSpeed = 0.65;
	/**
	 * Constructor for class
	 * @author Class of '21 (created in 2018 season)
	 */
	public TeleopIntakeController(direction dir) {
		this.dir = dir;
	}
	
	public TeleopIntakeController() {
	}
	/**
	 * Read input and updates motor speeds
	 * @author Class of '21 (created in 2018 season)
	 */
	public void execute() {
		double tempInAxis = RobotMap.opGamepad.getLeftTrigger();
		double tempOutAxis = RobotMap.opGamepad.getRightTrigger();
		boolean override = RobotMap.opGamepad.getXButtonPressed();

		if (dir == direction.IN && ((RobotMap.intake.getAnalogInput() < 1.9 || override) || override)) { //left bumper = elevator UP
			if (tempInAxis > maxSpeed) {
				RobotMap.intake.setSpeed(maxSpeed);
			} else { 
				RobotMap.intake.setSpeed(tempInAxis);
			}
			return;
		} else if (dir == direction.OUT) { 
			if (tempOutAxis > maxSpeed) {
				RobotMap.intake.setSpeed(-maxSpeed);
			} else { 
				RobotMap.intake.setSpeed(-tempOutAxis); 
			}
			return;
		} else {
			RobotMap.intake.setSpeed(0.0);
			return;
		}
	}
	
	@Override
	protected void end() {
		RobotMap.intake.setSpeed(0.0);
	}
	
	@Override
	public void cancel() {
		RobotMap.intake.setSpeed(0.0);
	}
	
	@Override
	protected boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

}