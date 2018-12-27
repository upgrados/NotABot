package rlbotexample.strategy;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class Defending implements Strategy{

    private static final int AIM_DISTANCE = 135;

    private boolean isLeft(Vector3 carPosition, Vector3 ballPosition){
        if(carPosition.x < ballPosition.x){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;
        int team = input.team;
        boolean isLeft = isLeft(carPosition, ballPosition);
        // try to hit the ball to a safe location
        Vector3 targetPosition = new Vector3(0,0,0);
        Vector3 directionVec;



        // decide where to hit the ball if you are closer to the ball (safe location).
        if(team == 0){
            directionVec = new Vector3(0,0,0);
        }

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = targetPosition.minus(carPosition);
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        return new ControlsOutput();
    }

    @Override
    public String getStrategy() {
        return "Defending";
    }

    @Override
    public boolean isBoosting() {
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }
}
