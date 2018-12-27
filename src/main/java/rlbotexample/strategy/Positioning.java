package rlbotexample.strategy;

import rlbotexample.Movement.DodgeToPoint;
import rlbotexample.Movement.Move;
import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class Positioning implements Strategy {

    private boolean isDoingMove;
    private final static int DISTANCE_AWAY_FROM_BALL = 1050;
    private final static int DODGEDISTANCE = 1000;
    private final static int NO_DODGE_AFTER_POINT = 3500;
    public final static int MAXDISTANCE = 3500;


    public Move currentMove;

    public Positioning() {
        isDoingMove = true;
        currentMove = null;
    }


    /**
     * This method calculates the position the car needs to move to, for a good shot at goal.
     * @param carPosition the current car position.
     * @param ballPosition the current ball position.
     * @param team the team we are on.
     * @return the location the car needs to move towards.
     */
    private Vector3 calculateTargetPosition(Vector3 carPosition, Vector3 ballPosition, int team){
        double distanceFromBall = ballPosition.y - carPosition.y;
        Vector3 targetPosition;
        // move to a spot away from ball
        if(team == 0){
            targetPosition = StrategyPlanner.predictedBallPosition.minus(new Vector3(0,
                    DISTANCE_AWAY_FROM_BALL, 0));
            if(distanceFromBall >= DISTANCE_AWAY_FROM_BALL+150 || ballPosition.y <= -MAXDISTANCE){
                isDoingMove = false;
            }
        } else {
            targetPosition = StrategyPlanner.predictedBallPosition.plus(new Vector3(0,
                    DISTANCE_AWAY_FROM_BALL, 0));
            if(distanceFromBall <= -DISTANCE_AWAY_FROM_BALL-150  || ballPosition.y >= MAXDISTANCE){
                isDoingMove = false;
            }
        }

        // if ball is a lot of to the left or right move to an aim position
        if(ballPosition.x > 2000) {
            targetPosition = new Vector3(StrategyPlanner.FIELD_WIDTH-200, targetPosition.y,
                    carPosition.z);
        } else if(ballPosition.x < -2000){
            targetPosition = new Vector3(-StrategyPlanner.FIELD_WIDTH+200, targetPosition.y,
                    carPosition.z);
        } else {
            targetPosition = new Vector3(carPosition.x, targetPosition.y, carPosition.z);
        }

        return targetPosition;
    }

    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;
        Vector3 targetPosition;
        int team = input.car.team;
        double distanceFromBall = ballPosition.y - carPosition.y;
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        if(ballDistance > StrategyPlanner.DISTANCE_FOR_MOVING){
            isDoingMove = false;
        }

        targetPosition = calculateTargetPosition(carPosition, ballPosition, team);

        Vector3 carToBall = targetPosition.minus(carPosition);
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        // if you are doing a move, complete this move first
        if(currentMove != null && currentMove.isDone()){
            return currentMove.move(input, targetPosition);
        }

        // Dodge toward the point if you have no more boost and certain conditions are true
        if ((team == 0 && carPosition.y <= -NO_DODGE_AFTER_POINT) || (team == 1 && carPosition.y >= NO_DODGE_AFTER_POINT)) {
            if (input.car.boost == 0 && ballDistance > DODGEDISTANCE &&
                    input.car.velocity.magnitude() > 1000 && Math.abs(steerCorrectionRadians) < 0.1) {
                currentMove = new DodgeToPoint();
                return currentMove.move(input, targetPosition);
            }
        }

        return Utilities.move(steerCorrectionRadians, 1, true);
    }

    @Override
    public String getStrategy() {
        return "Positioning";
    }

    @Override
    public boolean isBoosting() {
        return true;
    }

    @Override
    public boolean isBusy() {
        if(currentMove == null){
            return isDoingMove;
        } else {
            return currentMove.isDone() || isDoingMove;
        }
    }
}
