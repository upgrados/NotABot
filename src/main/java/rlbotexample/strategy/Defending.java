package rlbotexample.strategy;

import rlbotexample.Movement.DodgeToPoint;
import rlbotexample.Movement.Move;
import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class Defending implements Strategy{

    private static final int AIM_DISTANCE = 100;
    private static final int DISTANCE_INFRONT_OF_BALL = 50;
    private static final int JUMP_HEIGHT = 200;
    private Move currentMove;

    public Defending(){
        currentMove = null;
    }

    /**
     * This method checks if the car is to the left of the ball or not.
     * @param carPosition current car position.
     * @param ballPosition current ball position.
     * @return boolean value for left to ball.
     */
    private boolean isLeft(Vector3 carPosition, Vector3 ballPosition){
        if(carPosition.x < ballPosition.x){
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method returns if you are behind the ball, according to this method if you are not
     * far enough in front of the ball you are still seen as behind the ball.
     * @param team the team you are on.
     * @param carPosition the current car position.
     * @param ballPosition the current ball position
     * @return a boolean indicating whether you are "behind" the ball.
     */
    private boolean behindBall(int team, Vector3 carPosition, Vector3 ballPosition){
        if(team == 0){
            if(carPosition.y + DISTANCE_INFRONT_OF_BALL > ballPosition.y){
                return true;
            } else{
                return false;
            }
        } else {
            if(carPosition.y - DISTANCE_INFRONT_OF_BALL < ballPosition.y){
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean inGoal(int team, Vector3 carPosition){
        if((team == 0 && carPosition.y <= StrategyPlanner.BLUE_GOAL.y)
                || (team == 1 && carPosition.y >= StrategyPlanner.ORANGE_GOAL.y)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Decide where to hit the ball if you are closer to the ball (safe location).
     * If the car is to the left, shoot to the right.
     * @param carPosition the current car position
     * @param ballPosition the current ball position
     * @param team the team we are on.
     * @return The calculated direction vector
     */
    private Vector3 calculateDirectionVector(Vector3 carPosition, Vector3 ballPosition, int team,
                                             boolean corner){
        Vector3 directionVec;
        boolean isLeft = isLeft(carPosition, ballPosition);
        double width = StrategyPlanner.FIELD_WIDTH;
        double depth = StrategyPlanner.FIELD_DEPTH;
        Vector3 position = StrategyPlanner.predictedBallPosition;
        if(corner){
            if (team == 0) {
                if (ballPosition.x < 0) {
                    directionVec = position.minus(new Vector3(width, -depth, 50)).normalized();
                } else {
                    directionVec = position.minus(new Vector3(-width, -depth, 50)).normalized();
                }
            } else {
                if (ballPosition.x < 0) {
                    directionVec = position.minus(new Vector3(width, depth, 50)).normalized();
                } else {
                    directionVec = position.minus(new Vector3(-width, depth, 50)).normalized();
                }
            }
        } else {
            if (team == 0) {
                if (isLeft) {
                    directionVec = position.minus(new Vector3(width, -1500, 50)).normalized();
                } else {
                    directionVec = position.minus(new Vector3(-width, -1500, 50)).normalized();
                }
            } else {
                if (isLeft) {
                    directionVec = position.minus(new Vector3(width, 1500, 50)).normalized();
                } else {
                    directionVec = position.minus(new Vector3(-width, 1500, 50)).normalized();
                }
            }
        }

        return directionVec;
    }

    @Override
    public ControlsOutput doMove(DataPacket input) {
        Vector3 carPosition = input.car.position;
        Vector3 carDirection = input.car.orientation.noseVector;
        Vector3 ballPosition = input.ball.position;
        int team = input.team;
        boolean hasBoost = input.car.boost != 0;
        boolean behindBall = behindBall(team, carPosition, ballPosition);
        boolean inGoal = inGoal(team, carPosition);

        double carSpeed = input.car.velocity.magnitude();
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        Vector3 directionVec = calculateDirectionVector(carPosition, ballPosition, team, false);
        Vector3 targetPosition = StrategyPlanner.predictedBallPosition.plus(directionVec.scaled(AIM_DISTANCE));

        //just hit the ball if it is too close to the wall
//        if((team == 0 && ballPosition.y < -5100) || (team == 1 && ballPosition.y > 5100)){
//            targetPosition = ballPosition;
//        }

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = targetPosition.minus(carPosition);

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        if(!StrategyPlanner.isClosest && behindBall && Math.abs(ballPosition.x) < 1250){
            directionVec = calculateDirectionVector(carPosition, ballPosition, team, true);
            targetPosition = StrategyPlanner.predictedBallPosition.plus(directionVec.scaled(AIM_DISTANCE));
            carToBall = targetPosition.minus(carPosition);
            steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());
        } else if(!StrategyPlanner.isClosest && behindBall) {
            if(team == 0){
                targetPosition = StrategyPlanner.BLUE_GOAL;
            } else {
                targetPosition = StrategyPlanner.ORANGE_GOAL;
            }
        } else if(!StrategyPlanner.isClosest){
            targetPosition = StrategyPlanner.predictedBallAimPoint;
        }

        if(isBusy()){
            if(StrategyPlanner.isClosest) {
                if (ballDistance > 2000) {
                    return currentMove.move(input, targetPosition);
                } else {
                    return currentMove.move(input, ballPosition);
                }
            } else {
                return currentMove.move(input, targetPosition);
            }
        }

        if(targetPosition.z > JUMP_HEIGHT && carSpeed > 50 &&
                targetPosition.flatten().distance(carPosition.flatten()) < 250){
            return Utilities.move(steerCorrectionRadians, -1, false);
        }

        // Move towards the ball as fast as possible and shoot it to one of the sides if you are
        // closest.
        if(StrategyPlanner.isClosest){
            if(ballDistance > 1500 && hasBoost){
                return Utilities.move(steerCorrectionRadians, 1, true);
            } else if(ballDistance > 2500 && carSpeed > 1000 && Math.abs(steerCorrectionRadians) < 0.1){
                currentMove = new DodgeToPoint();
                return currentMove.move(input, targetPosition);
            } else if(carSpeed > 1400){
                return Utilities.move(steerCorrectionRadians, 1, false);
            } else if (ballDistance > 350 || ballPosition.z > JUMP_HEIGHT) {
                return Utilities.move(steerCorrectionRadians, 1, false);
            } else {
                currentMove = new DodgeToPoint();
                return currentMove.move(input, ballPosition);
            }
        } else {
            if (behindBall) {
                if (ballDistance < 1000) {
                    return Utilities.move(steerCorrectionRadians, 1, false);
                } else if (ballDistance > 3000 && carSpeed > 1000 && Math.abs(steerCorrectionRadians) < 0.1) {
                    currentMove = new DodgeToPoint();
                    return currentMove.move(input, targetPosition);
                } else {
                    return Utilities.move(steerCorrectionRadians, 1, true);
                }
            } else {
                if (ballDistance < 1500) {
                    return Utilities.move(steerCorrectionRadians, 1, false);
                } else if (ballDistance > 3000 && carSpeed > 1000 && Math.abs(steerCorrectionRadians) < 0.1
                        || ballDistance < 300 && ballPosition.z < 200) {
                    currentMove = new DodgeToPoint();
                    return currentMove.move(input, targetPosition);
                } else {
                    return Utilities.move(steerCorrectionRadians, 1, false);
                }
            }
        }
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
        if(currentMove == null){
            return false;
        } else {
            return currentMove.isDone();
        }
    }
}
