package rlbotexample.Movement;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

import javax.naming.ldap.Control;

public class DodgeToPoint implements Move{

    private static final int MAXTIMEOUT = 750;
    private static final int NODODGEDISTANCE = 40;

    private boolean doingMove;
    private boolean dodged;
    private boolean jumped;
    private Vector3 jumpDirection;
    private double startTimeoutTime;

    public DodgeToPoint(){
        doingMove = true;
        dodged = false;
        jumped = false;
        jumpDirection = new Vector3(0,0,0);
        startTimeoutTime = System.currentTimeMillis();
    }


    public ControlsOutput jumpDirection(boolean onGround, float roll, float pitch, float yaw){
        if(onGround && !jumped){
            //we want to move forward if the pitch is negative and vice versa.
            jumped = true;
            return new ControlsOutput()
                    .withThrottle(-pitch)
                    .withJump();
        } else if(onGround && jumped) {
            return new ControlsOutput();
        } else {
            dodged = true;
            jumpDirection = new Vector3(roll, pitch, yaw);
            return new ControlsOutput()
                    .withRoll(roll)
                    .withPitch(pitch)
                    .withYaw(yaw)
                    .withJump(true);
        }
    }

    @Override
    public ControlsOutput move(DataPacket input, Vector3 targetLocation) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        boolean onGround = input.car.hasWheelContact;
        double distanceToPoint = carPosition.flatten().distance(targetLocation.flatten());

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = targetLocation.minus(carPosition);

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        //add Timeout if something goes wrong
        if(System.currentTimeMillis() - startTimeoutTime > MAXTIMEOUT){
            doingMove = false;
            return new ControlsOutput();
        }

        // Move completion criterion
        if(onGround && dodged){
            doingMove = false;
            return new ControlsOutput();
        }

        if(dodged){
            return new ControlsOutput()
                    .withRoll(jumpDirection.x)
                    .withPitch(jumpDirection.y)
                    .withYaw(jumpDirection.z);

        } else if(distanceToPoint < NODODGEDISTANCE) {
            doingMove = false;
            return new ControlsOutput();
        } else if(Math.abs(steerCorrectionRadians) > 2.25){
            return jumpDirection(onGround, 0, 1, 0);
        } else if(steerCorrectionRadians > 1.65){
            return jumpDirection(onGround, -1, 1, 0);
        } else if(steerCorrectionRadians < -1.65){
            return jumpDirection(onGround, 1, 1, 0);
        } else if(steerCorrectionRadians > 1.00){
            return jumpDirection(onGround, -1, 0, 0);
        } else if(steerCorrectionRadians < -1.00){
            return jumpDirection(onGround, 1, 0, 0);
        } else if(steerCorrectionRadians > 0.15){
            return jumpDirection(onGround, -1, -1, 0);
        } else if(steerCorrectionRadians < -0.15){
            return jumpDirection(onGround, 1, -1, 0);
        } else{
            return jumpDirection(onGround, 0, -1, 0);
        }
    }

    @Override
    public boolean isDone() {
        return doingMove;
    }

    @Override
    public void setIsDone(boolean done) {
        doingMove = done;
    }
}
