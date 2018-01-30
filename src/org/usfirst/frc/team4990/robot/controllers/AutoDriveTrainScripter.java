package org.usfirst.frc.team4990.robot.controllers;
//GODISTANCE

import org.usfirst.frc.team4990.robot.controllers.SimpleAutoDriveTrainScripter.StartingPosition;
import org.usfirst.frc.team4990.robot.subsystems.DriveTrain;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;

//import edu.wpi.first.wpilibj.Solenoid; //If we need to implement air compressors

import java.util.LinkedList;
import java.util.Queue;

//You shouldn't mess with this if you don't know what you're doing

public class AutoDriveTrainScripter {

	private interface CommandPackage {
		//called when the command first starts
		public void init();

		// called every time
		public void update();

		// returns true if command is finished
		public boolean done();
	}

	private Queue<CommandPackage> commands = new LinkedList<>();

	private DriveTrain dt;
	private StartingPosition startPos;
	private ADXRS450_Gyro gyro;
	//private Solenoid solenoid;

	public AutoDriveTrainScripter(DriveTrain dtrain, StartingPosition startP, ADXRS450_Gyro gy) {
		dt = dtrain;
		startPos = startP;
		gyro = gy;
		//Solenoid solenoid = solen;
	}

	public void init() {
		// needs to be called once when auto starts
		CommandPackage top = commands.peek();
		if(top == null) return;

		top.init();
	}

	public void update() {
		CommandPackage top = commands.peek();
		if(top == null) return;

		if(!top.done() ) {
			top.update();
		}
		else {
			commands.remove();

			// we can use recursion
			// but I don't want to take the risk
			top = commands.peek();
			if(top == null) return;
			top.init();
			top.update();
		}
	}
	
	public enum Direction {
		RIGHT,
		LEFT
	}

	public void goDistance(double distance, boolean backwards) {
		/*Test LOG (format date: specified distance | actual distance)
		 * 1-20-18: 3ft | 3ft+7in
		 *
		 *
		 */
		class F_Package implements CommandPackage {
			private double value;
			private DriveTrain dt;
			private boolean done;
			private boolean backwards;

			public F_Package(DriveTrain d, double v, boolean backwards) {
				this.dt = d;
				// Use abs to make sure a negative number doesn't break anything
				this.value = Math.abs(v);
				this.done = false;
				this.backwards = backwards;
			}

			public void init() {
				this.dt.resetDistanceTraveled();
			}

			public void update() {
				// only the right side works...
				// and it's backwards
				// this entire robot is backwards
				
				double speed = .3;
				if (this.backwards == true) {
					speed = -speed;
				}
				if(-this.dt.getRightDistanceTraveled() < this.value) {
					dt.setLeftSpeed(speed);
					dt.setRightSpeed(speed);
				}
				else {
					dt.setLeftSpeed(0.0);
					dt.setRightSpeed(0.0);
					this.done = true;
				}
			}

			public boolean done() {
				return this.done;
			}
		}

		commands.add(new F_Package(dt, distance, backwards));
	}
	
	public void wait(double time) { //time is in milliseconds
		class W_Package implements CommandPackage {
			private boolean done;
			private long duration;
			private long startMillis;

			public W_Package(double t) {
				this.duration = (long) t;
				this.done = false;
			}

			public void init() {
				this.startMillis = System.currentTimeMillis();
			}

			public void update() {
				if (startMillis + duration <= System.currentTimeMillis()) {
					//done waiting!
					this.done = true;
				}
			}

			public boolean done() {
				return this.done;
			}
		}

		commands.add(new W_Package(time));
	}


	public void turnForDegrees(double degrees, Direction lr) {
		//0.01709 feet per 1 degree
		class turnForDegrees_Package implements CommandPackage {
			// feetPer1Degree = ((24 * pi) inches / 360 degrees) / (12 inches / 1 foot)
			// times some other random constant lol
			private double feetPer1Degree = 0.01745329  * .99;
			private double classdegrees;
			private boolean done;
			private DriveTrain dt;
			private boolean left;
			private double encoderDistanceToStriveFor;
			private double currentEncoderDistance;

			public turnForDegrees_Package(DriveTrain d, double classdegrees, Direction classlr) {
				// please note that the right encoder is backwards
				this.dt = d;
				this.classdegrees = classdegrees;
				this.done = false;
				encoderDistanceToStriveFor = this.classdegrees * feetPer1Degree;

				currentEncoderDistance = 0;
				
				if (classlr == Direction.LEFT) {
					this.left = true;
				}
				else {
					this.left = false;
				}
			}

			public void init() {
				this.dt.resetDistanceTraveled();
			}

			public void update() {
				double speed = 0.20;
				if (this.left == true) {
					// if it's supposed to turn left (I know it's weird just go with it)
					speed = -speed;
				}

				if (currentEncoderDistance <= encoderDistanceToStriveFor) {
					//DONT TOUCH THIS NEXT LINE
					
					// Gets the average of each side's distance
					// Needs abs or else only works one direction
					currentEncoderDistance = (Math.abs(this.dt.getRightDistanceTraveled()) +
											  Math.abs(this.dt.getLeftDistanceTraveled())) / 2;

					//DEBUG ENCODER PRINTER
					System.out.println("LEFT: " + this.dt.getLeftDistanceTraveled() + "  RIGHT: " + this.dt.getRightDistanceTraveled() + "  "+ encoderDistanceToStriveFor);

					this.dt.setLeftSpeed(speed); // left needs to go forwards
					this.dt.setRightSpeed(-speed); // right needs to go backwards
				}
				else {
					this.dt.setSpeed(0.0, 0.0);
					this.done = true;
				}

			}
			
			public boolean done() {
				return this.done;
			}
		}
		commands.add(new turnForDegrees_Package(dt, degrees, lr));
	}
	
	public void gyroTurn(double degrees, Direction lr) {
		class gyroTurn_Package implements CommandPackage {
			private double degrees;
			private boolean done;
			private DriveTrain dt;
			private ADXRS450_Gyro gyro;
			private Direction dir;

			public gyroTurn_Package(DriveTrain d, ADXRS450_Gyro g, double classdegrees, Direction classlr) {
				// please note that the right encoder is backwards
				this.dt = d;
				this.degrees = classdegrees;
				this.done = false;
				gyro = g;
				dir = classlr;
			}

			public void init() {
				this.gyro.reset();
			}

			public void update() {
				double speed = 0.20;
				if (dir == Direction.LEFT) {
					// if it's supposed to turn left (I know it's weird just go with it)
					speed = -speed;
				}

				if (gyro.getAngle() <= this.degrees) {
					//DEBUG GYRO PRINTER
					System.out.println("Current: " + this.gyro.getAngle() + "  Stopping at: " + this.degrees);

					this.dt.setLeftSpeed(speed); // left needs to go forwards
					this.dt.setRightSpeed(-speed); // right needs to go backwards
				}
				else {
					this.dt.setSpeed(0.0, 0.0);
					this.done = true;
				}

			}
			
			public boolean done() {
				return this.done;
			}
		}
		commands.add(new gyroTurn_Package(dt, gyro, degrees, lr));
	}
}
