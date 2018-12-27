package rlbotexample.strategy;

import rlbotexample.input.DataPacket;
import rlbotexample.intercept.InterceptCalculator;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class Dribbling implements Strategy {

    private static final double REGULAR_SPEED = 1400;

    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;
        Vector3 targetPosition;
        if(StrategyPlanner.predictedBallAimPoint != null) {
            targetPosition = StrategyPlanner.predictedBallAimPoint;
        } else {
            targetPosition = StrategyPlanner.ballAimPoint;
        }
        float throttle = 1;


        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = targetPosition.minus(carPosition);

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // flick attempt
//        if(Math.abs(ballPosition.z-carPosition.z) <= 150 && ballDistance < 25 && input.car.hasWheelContact){
//            return new ControlsOutput()
//                    .withJump();
//        }
//        if(!input.car.hasWheelContact){
//            return new ControlsOutput()
//                    .withJump(true);
//        }

        //dribbling
        if(targetPosition.z > 100 && input.car.velocity.magnitude() > 50 &&
                targetPosition.flatten().distance(carPosition.flatten()) < 250){
            return new ControlsOutput()
                    .withThrottle(-1)
                    .withSteer(0);
        }

        // This makes sure your speed is 1400 when necesarry
//        if(input.car.velocity.magnitude() > REGULAR_SPEED){
//            throttle = -1;
//        }


        // stop boosting in move
        if(steerCorrectionRadians > -0.1 && steerCorrectionRadians < 0.1 && ballDistance < 30){
            return new ControlsOutput()
                    .withSteer(0)
                    .withThrottle(throttle);
        }

        return Utilities.move(steerCorrectionRadians, throttle, false);
    }

    @Override
    public String getStrategy() {
        return "Dribbling";
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
