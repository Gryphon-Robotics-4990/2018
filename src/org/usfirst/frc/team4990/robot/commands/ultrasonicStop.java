package org.usfirst.frc.team4990.robot.commands;

import org.usfirst.frc.team4990.robot.RobotMap;
import edu.wpi.first.wpilibj.command.Command;

public class ultrasonicStop extends Command {
	
	static double stopDistance = 20;
	Command command;

	/**
	 * Command for stopping when robot becomes in range of object.
	 * @param distance in inches
	 */
	
	public ultrasonicStop(double distance, Command command) {
		requires(RobotMap.driveTrain);
		stopDistance = distance;
		this.command = command;
	}
	
	/**
	 * Command for stopping when robot becomes within <b>20</b> inches of object.
	 */
	public ultrasonicStop() {
		requires(RobotMap.driveTrain);
	}
	
	/*public void execute() {
		System.out.println("current ultrasonic distance: " + RobotMap.ultrasonic.getRangeInches());
	}*/
	
	public boolean isFinished() {
		return RobotMap.ultrasonic.getRangeInches() < 20;
	}
	
	public void end() {
		command.cancel();
	}
	
	public void interrupted() {
		command.cancel();
	}
}
