package rlbotexample.strategy;

import rlbotexample.Movement.DodgeToPoint;
import rlbotexample.Movement.Move;
import rlbotexample.input.DataPacket;
import rlbotexample.intercept.Intercept;
import rlbotexample.intercept.InterceptCalculator;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class PowerShot implements Strategy {

    private Move currentMove;
    private static final double REGULAR_SPEED = 1400;
    private static final int DODGEDISTANCE = 350;
    private static final int HEIGHT_TO_HIT_BALL = 200;

    public PowerShot() {
        currentMove = null;
    }

    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;
        Vector3 targetPosition = StrategyPlanner.predictedBallAimPoint;

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = targetPosition.minus(carPosition);
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());


        if(isBusy()){
            if(ballDistance < 100){
                return currentMove.move(input, ballPosition);
            } else {
                return currentMove.move(input, StrategyPlanner.ballAimPoint);
            }
        }

        // if we predict to hit the ball in the air we wait...
        if(targetPosition.z > 150 ){
            return Utilities.move(steerCorrectionRadians, 0.5f, false);
        }

        //dodge towards the ball
        if(ballDistance < DODGEDISTANCE && ballPosition.z < HEIGHT_TO_HIT_BALL){
            currentMove = new DodgeToPoint();
            return currentMove.move(input, StrategyPlanner.ballAimPoint);
        }

        // if we go too fast we slow down
//        if(input.car.velocity.magnitude() > REGULAR_SPEED){
//            return Utilities.move(steerCorrectionRadians, -1, false);
//        }

        //move normally if all else fails
        return Utilities.move(steerCorrectionRadians, 1, false);
    }

    @Override
    public String getStrategy() {
        return "PowerShot";
    }

    @Override
    public boolean isBoosting() {
        return false;
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
