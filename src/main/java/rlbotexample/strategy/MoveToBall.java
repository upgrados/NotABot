package rlbotexample.strategy;

import rlbotexample.Movement.DodgeToPoint;
import rlbotexample.Movement.Move;
import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class MoveToBall implements Strategy {

    private final boolean alwaysBoost = true;
    private final int dodgeDistance = 2500;
    private boolean isDoingMove;
    private Move currentMove;

    public MoveToBall() {
        isDoingMove = false;
        currentMove = null;
    }


    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = StrategyPlanner.predictedBallPosition.minus(carPosition);
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        if(isBusy()){
            return currentMove.move(input, StrategyPlanner.predictedBallPosition);
        } else if(steerCorrectionRadians > 1.5){
            return new ControlsOutput()
                    .withThrottle(1)
                    .withSteer(-1)
                    .withSlide(true);
        } else if(steerCorrectionRadians < -1.5){
            return new ControlsOutput()
                    .withThrottle(1)
                    .withSteer(1)
                    .withSlide(true);
        } else if(steerCorrectionRadians > 0.1) {
            return new ControlsOutput()
                    .withSteer(-1)
                    .withThrottle(1);
        } else if(steerCorrectionRadians < -0.1){
            return new ControlsOutput()
                    .withSteer(1)
                    .withThrottle(1);
        } else if(input.car.boost == 0 && ballDistance > dodgeDistance &&
                input.car.velocity.magnitude() > 1000){
            currentMove = new DodgeToPoint();
            return currentMove.move(input, StrategyPlanner.predictedBallPosition);
        } else {
            return new ControlsOutput()
                    .withSteer(0)
                    .withThrottle(1)
                    .withBoost(alwaysBoost);
        }
    }

    @Override
    public String getStrategy() {
        return "MoveToBall";
    }

    @Override
    public boolean isBoosting() {
        return true;
    }

    @Override
    public boolean isBusy() {
        if(currentMove == null) {
            return false;
        } else {
            return currentMove.isDone();
        }
    }
}
