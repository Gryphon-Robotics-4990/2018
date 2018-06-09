package org.usfirst.frc.team4990.robot;
//This entire robot code is dedicated to Kyler Rosen, a friend, visionary, and a hero to the empire that is the Freshmen Union
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team4990.robot.controllers.*;
import org.usfirst.frc.team4990.robot.controllers.SimpleAutoDriveTrainScripter.StartingPosition;
import org.usfirst.frc.team4990.robot.subsystems.*;
import org.usfirst.frc.team4990.robot.subsystems.Gearbox;

import com.kauailabs.navx.frc.AHRS;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory. 
 */
public class Robot extends IterativeRobot {

	public SendableChooser<StartingPosition> autoChooser;
	public StartingPosition startPos = StartingPosition.FORWARD;

	public Preferences prefs;
	
	private F310Gamepad driveGamepad;
	private F310Gamepad opGamepad;
	
	private DriveTrain driveTrain;
	private TeleopDriveTrainController teleopDriveTrainController;
	private Intake intake;
	private TeleopIntakeController teleopIntakeController;
	private Elevator elevator;
	private TeleopElevatorController teleopElevatorController;
	private Scaler scaler;
	private TeleopScalerController teleopScalerController;


	public ADXRS450_Gyro gyro;
	public Ultrasonic ultrasonic;
	public AHRS ahrs;

	private SimpleAutoDriveTrainScripter autoScripter;
	//private SimpleAutoDriveTrainScripter testScripter;

    public void robotInit() { //This function is run when the robot is first started up and should be used for any initialization code.

    	System.out.println("Version 1.29.2018.6.18");
    	this.prefs = Preferences.getInstance();

    	//~~~~ Driving/Operator Components ~~~~
    	driveGamepad = new F310Gamepad(0);
    	opGamepad = new F310Gamepad(1);

    	this.driveTrain = new DriveTrain( 
    			new Gearbox(
    					new TalonMotorController(0),
    					new TalonMotorController(1),
    					0, 1),
    			new Gearbox(
    				new TalonMotorController(2),
    				new TalonMotorController(3),
    				2, 3));
    	
    	ultrasonic = new Ultrasonic(4, 5, Ultrasonic.Unit.kInches); //ping DIO (OUTPUT), echo DIO, units

    	intake = new Intake(new TalonMotorController(7), 
    			new TalonMotorController(6), 
    			new AnalogInput(0)); //Left motor, right motor, distance sensor
    	
    	teleopIntakeController = new TeleopIntakeController(intake, opGamepad);
    	
    	elevator = new Elevator(
    			new TalonMotorController(5), //Motor elevatorMotorA
    			new TalonMotorController(4), //Motor elevatorMotorB
    			6, //int topSwitchChannel (DIO)
    			7, //int bottomSwitchChannel (DIO)
    			8, //int Encoder DIO port A
    			9); //int Encoder DIO port B
    	
    	teleopElevatorController = new TeleopElevatorController(elevator,
    			opGamepad, //gamepad to control elevator
    			1.0); // max speed (0.1 to 1.0) 
    	
    scaler = new Scaler(new TalonMotorController(8));
    		
    	teleopScalerController = new TeleopScalerController(scaler, opGamepad, 0.7); //Scaler scaler, F310Gamepad opGamepad, double speed
    			

    	//~~~~ Sensor Init & Details ~~~~

    	gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
    	//use gyro.getAngle() to return heading (returns number -n to n) and reset() to reset angle
    	//gyro details: http://first.wpi.edu/FRC/roborio/release/docs/java/edu/wpi/first/wpilibj/ADXRS450_Gyro.html
    	
    	ahrs = new AHRS(SPI.Port.kMXP); 
    	//navX-MXP RoboRIO extension and 9-axis gyro thingy
    	//for simple gyro angles: use ahrs.getAngle() to get heading (returns number -n to n) and reset() to reset angle (and drift)
    	
    	updateAutoDashboard();
    	
    	simpleDashboardPeriodic();

    	resetSensors();
    	
    	liveWindowInit();
    	
    }
    
    public void robotPeriodic() {
    	//Don't put anything here or else the robot might lag severely.
    }
    
    public void disabledInit() {
    		System.out.println("ROBOT DISABLED.");
    }

    public void disabledPeriodic() { //This function is run periodically when the robot is DISABLED. Be careful.
    		if (System.currentTimeMillis() % 200 > 0 && System.currentTimeMillis() % 500 < 50) { //runs around every 1 second
    			startPos = autoChooser.getSelected();
    			simpleDashboardPeriodic();
    			updateAutoDashboard();
    		}
    		
    		controllerCheck();
    }

    public void autonomousInit() { //This function is called at the start of autonomous
	    	startPos = autoChooser.getSelected();
	    	autoScripter = new SimpleAutoDriveTrainScripter(driveTrain, startPos, gyro, intake, elevator, ultrasonic);
	    	System.out.println("Auto Init complete");
    }

    public void autonomousPeriodic() { //This function is called periodically during autonomous
	    	autoScripter.update();
	    	driveTrain.update();
	    	elevator.update();
	    	simpleDashboardPeriodic();
    }

    public void teleopInit() { //This function is called at the start of teleop
    	this.teleopDriveTrainController = new TeleopDriveTrainController(
        		this.driveGamepad,
        		this.driveTrain,
        		this.prefs.getBoolean("reverseTurningFlipped", false),
        		this.prefs.getDouble("smoothDriveAccTime", Constants.defaultAccelerationTime),
        		this.prefs.getDouble("lowThrottleMultiplier", .25),
        		this.prefs.getDouble("maxThrottle", 1.0));
    }

    public void teleopPeriodic() { //This function is called periodically during teleop

	    this.teleopDriveTrainController.updateDriveTrainState();
	    //ever heard of the tale of last minute code
	    //I thought not, it is not a tale the chairman will tell to you

	    	this.driveTrain.update();
	    	teleopElevatorController.update();
	    	teleopIntakeController.update();
	    	intake.update();
	    	teleopScalerController.update();
	    	scaler.update();
	    	
	    	simpleDashboardPeriodic();
	    	controllerCheck();
    } 
    
    public void testInit() { //TODO add commands for testing
    		liveWindowInit();
    		//testScripter = new SimpleAutoDriveTrainScripter(driveTrain, startPos, gyro, intake, elevator);
    		//testScripter.init();
    		teleopInit();
    }
    
    public void testPeriodic() {
    		//testScripter.update();
    		teleopPeriodic();
    }
    
    /**
     * Adds SendableChooser to SmartDashboard for Auto route choosing.
     */

    public void updateAutoDashboard() {
	    	//Auto chooser
	    	autoChooser = new SendableChooser<StartingPosition>();
	    	autoChooser.addObject("Left", StartingPosition.LEFT);
	    	autoChooser.addObject("Middle", StartingPosition.MID);
	    	autoChooser.addObject("Right",  StartingPosition.RIGHT);
	    	autoChooser.addObject("Stay", StartingPosition.STAY);
	    	autoChooser.addDefault("Forward (cross line)", StartingPosition.FORWARD);
	    	SmartDashboard.putData(autoChooser);
	    	SmartDashboard.putString("Selected Starting Position", startPos.toString());
	    	SmartDashboard.updateValues(); //always run at END of updateAutoDashboard
    }
    
    	public void simpleDashboardPeriodic() {
	    	SmartDashboard.putBoolean("Box", intake.isBoxPosition(Intake.BoxPosition.OUT));
	    	SmartDashboard.putBoolean("Elevator Top Limit Switch", this.elevator.isTopSwitched());
	    	SmartDashboard.putBoolean("Elevator Bottom Limit Switch", this.elevator.isBottomSwitched());
	    	
	    	SmartDashboard.updateValues(); //always run at END of simpleDashboardPeriodic
    	}
    
    	public void complexDashboardPeriodic() {
	    	
	    	//Other sensor gauges and data
	    	SmartDashboard.putNumber("Gyro Heading", gyro.getAngle());
	    	SmartDashboard.putNumber("Analog Infrared Voltage", intake.getAnalogInput());
	    	SmartDashboard.putNumber("Left Encoder", this.driveTrain.left.getDistanceTraveled());
	    	SmartDashboard.putNumber("Right Encoder", this.driveTrain.right.getDistanceTraveled());
	    	
	    	SmartDashboard.putBoolean("Box In", intake.isBoxPosition(Intake.BoxPosition.IN));
	    	SmartDashboard.putBoolean("Box Out", intake.isBoxPosition(Intake.BoxPosition.OUT));
	    	SmartDashboard.putBoolean("Box In and Out At The Same Time", intake.isBoxPosition(Intake.BoxPosition.MOVING));
	    	
	    	SmartDashboard.putNumber("Throttle Input", driveGamepad.getLeftJoystickY());
	    	SmartDashboard.putNumber("Turn Steepness Input", driveGamepad.getRightJoystickX());

	    	SmartDashboard.putBoolean("Elevator Top Limit Switch", this.elevator.isTopSwitched());
	    	SmartDashboard.putBoolean("Elevator Bottom Limit Switch", this.elevator.isBottomSwitched());
	    	SmartDashboard.putData("Elevator Encoder",elevator.encoder);
	    	
	    	SmartDashboard.putData("Left Drive Encoder",this.driveTrain.left.encoder);
	    	SmartDashboard.putData("Right Drive Encoder",this.driveTrain.right.encoder);
	    	
	    	SmartDashboard.updateValues(); //always run at END of dashboardPeriodic
    }

	public void resetSensors() {
    		System.out.print("Starting gyro calibration. DON'T MOVE THE ROBOT...");
    		gyro.calibrate();
    		System.out.print("Gyro calibration done. Resetting encoders...");
    		this.driveTrain.resetDistanceTraveled();
    		System.out.print("Sensor reset complete.");
	}
	
	public void liveWindowInit() {
		//Elevator
		elevator.encoder.setName("Elevator","Encoder");
		elevator.elevatorMotorA.setName("Elevator","MotorA");
		elevator.elevatorMotorB.setName("Elevator","MotorB");
		
		//Intake
		intake.motorL.setName("Intake", "LeftMotor");
		intake.motorR.setName("Intake", "RightMotor");
		intake.infrared.setName("Intake", "Infrared");
		
		//DriveTrain
		driveTrain.left.motorGroup.setName("DriveTrain","LeftMotors");
		driveTrain.right.motorGroup.setName("DriveTrain","RightMotors");
		driveTrain.left.encoder.setName("DriveTrain","LeftEncoder");
		driveTrain.right.encoder.setName("DriveTrain","RightEncoder");
		
		//General
		gyro.setName("General", "Gyro");
	}
	
	public void controllerCheck() {
    	if (driveGamepad.getRawButton(8)) {
    		System.out.println("Button 7 Pressed on DRIVE GAMEPAD");
    	} else if (opGamepad.getRawButton(8)) {
    		System.out.println("Button 7 Pressed on OP GAMEPAD");
    	}
	}

}
