package rlbotexample.strategy;

import rlbotexample.Movement.DodgeToPoint;
import rlbotexample.Movement.Move;
import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class KickOff implements Strategy {

    private Move currentMove;

    public KickOff() {
        currentMove = null;
    }

    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;
        Vector3 targetPosition = StrategyPlanner.ballAimPoint;
        boolean hasBoost = input.car.boost != 0;

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = targetPosition.minus(carPosition);
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        if(isBusy()){
            return currentMove.move(input, targetPosition);
        }

        if(ballDistance < 650) {
            currentMove = new DodgeToPoint();
            return currentMove.move(input, targetPosition);
        } else if(ballDistance > 500 && hasBoost){
            return Utilities.move(steerCorrectionRadians, 1, true);
        } else if(ballDistance > 3250 && !hasBoost){
            currentMove = new DodgeToPoint();
            return currentMove.move(input, targetPosition);
        } else {
            return Utilities.move(steerCorrectionRadians, 1, true);
        }
    }

    @Override
    public String getStrategy() {
        return "KickOff";
    }

    @Override
    public boolean isBoosting() {
        return true;
    }

    @Override
    public boolean isBusy() {
        if(currentMove == null){
            return false;
        } else {
            return currentMove.isDone();
        }
    }
}
