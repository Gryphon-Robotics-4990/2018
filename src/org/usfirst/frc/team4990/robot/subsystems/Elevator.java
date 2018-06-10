package org.usfirst.frc.team4990.robot.subsystems;

import org.usfirst.frc.team4990.robot.Constants;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;

public class Elevator {
	public TalonMotorController elevatorMotorA;
	public TalonMotorController elevatorMotorB;
	
	public LimitSwitch topSwitch;
	
	public LimitSwitch bottomSwitch;
	
	public Encoder encoder;
	
	private double stopFallingSpeed = -0.1;
	
	private boolean stopped = false;
	
	//for Elevator goToPostion
	public double doneTolerance = 3; //percent
	public PIDController elevatorPID;
	public boolean goToPostionActive = false;
	
	/**
	 * Initializes elevator.
	 * @param elevatorMotor Motor used to drive elevator (vex pro 775)
	 * @param topSwitchChannel DIO channel for top limit switch
	 * @param topSwitchCounterSensitivity sensitivity for top limit switch, default 4
	 * @param bottomSwitchChannel DIO channel for bottom limit switch
	 * @param bottomSwitchCounterSensitivity sensitivity for bottom limit switch, default 4
	 * @param encoderChannelA Encoder for elevator gearbox (Signal, Ground and 5v)
	 * @param encoderChannelB Encoder for elevator gearbox (just Signal)
	 */
	
	public Elevator(
			TalonMotorController elevatorMotorA, 
			TalonMotorController elevatorMotorB,
			int topSwitchChannel, 
			int bottomSwitchChannel, 
			int encoderChannelA, 
			int encoderChannelB) {
		this.elevatorMotorA = elevatorMotorA;
		this.elevatorMotorB = elevatorMotorB;
		
		this.topSwitch = new LimitSwitch(topSwitchChannel);
		this.bottomSwitch = new LimitSwitch(bottomSwitchChannel);
		
		encoder = new Encoder(encoderChannelA, encoderChannelB);
		this.encoder.setDistancePerPulse(1.16 * Math.PI / Constants.pulsesPerRevolution); //diameter of elevator chain gear * PI
		this.encoder.setMinRate(Constants.gearboxEncoderMinRate);
		this.encoder.setSamplesToAverage(Constants.gearboxEncoderSamplesToAvg);
		
		//for elevator PID goToPosition
		elevatorPID = new PIDController(1, 0, 0, encoder, new ElevatorPID(this));
		elevatorPID.setInputRange(0, 4.8); //minimumInput, maximumInput
		elevatorPID.setOutputRange(-1, 1); //minimumOutput, maximumoutput (motor constraints)
		elevatorPID.setAbsoluteTolerance(doneTolerance);
	}
	
	/**
	 * Sets elevator power and checks limit switches. If moving would hurt elevator, does not move.
	 * @param power positive value (0 to 1) makes it go up, negative values (-1 to 0) makes it go down
	 */
	
	public void setElevatorPower(double power) {
		if ((this.topSwitch.getValue() && power > 0) || (this.bottomSwitch.getValue() && power < 0)) {
			this.elevatorMotorA.setPower(0.0);
			this.elevatorMotorB.setPower(0.0);
			resetEncoderDistance();
			stopped = true;
			System.out.println("Elevator Safety Triggered in setElevatorPower");
		} else {
			System.out.println(this.elevatorMotorB.getPower());
			this.elevatorMotorA.setPower(-power);
			this.elevatorMotorB.setPower((power * 0.9) + stopFallingSpeed);
			if (power == 0 && ! stopped) {
				resetEncoderDistance();
				stopped = true;
			} else if (! stopped) {
				stopped = false;
			}
		}
	}
	
	/**
	 * Checks safety and (hopefully) stops it from falling if stopped
	 */
	
	public void update() {
		
		//updates elevator PID for goToPostion
		if (goToPostionActive) {
			if (elevatorPID.onTarget()){ //done
				elevatorPID.disable();
			} else {
				setElevatorPower(elevatorPID.get());
			} 
		}
		
		//if stopped, use encoders to run motors to stop intake from falling
		/*if (this.elevatorMotorA.getPower() == 0 || this.elevatorMotorA.getPower() == stopFallingSpeed) {
				setElevatorPower(stopFallingSpeed);
			} else {
				setElevatorPower(0.0);
			}*/
		
		//check limit switches, stop motors if going toward danger
		if ((this.topSwitch.getValue() && this.elevatorMotorA.getPower() > 0) || (this.bottomSwitch.getValue() && this.elevatorMotorA.getPower() < 0)) {
			this.elevatorMotorA.setPower(0.0);
			this.elevatorMotorB.setPower(0.0);
			resetEncoderDistance();
			stopped = true;
			System.out.println("Elevator Safety Triggered in update");
		}
		
	}
	
	public void goToPosition(double postionInput) {
		resetEncoderDistance();
		elevatorPID.setSetpoint(postionInput);
		elevatorPID.enable();
		setElevatorPower(elevatorPID.get());
		goToPostionActive = true;
	}
	
	/**
	 * returns boolean whether top is switched.
	 * @return boolean whether top is switched.
	 */
	
	public boolean isTopSwitched() {
		return this.topSwitch.getValue();
	}
	
	/**
	 * returns boolean whether bottom is switched.
	 * @return boolean whether bottom is switched.
	 */
	
	public boolean isBottomSwitched() {
		return this.bottomSwitch.getValue();
	}
	
	/**
	 * returns elevator gearbox's distance in unknown units
	 * @return elevator gearbox's distance in unknown units
	 */
	
	public double getEncoderDistance() {
		return encoder.getDistance();
	}
	
	/**
	 * resets encoder's distance.
	 */
	
	public void resetEncoderDistance() {
		this.encoder.reset();
	}
}
