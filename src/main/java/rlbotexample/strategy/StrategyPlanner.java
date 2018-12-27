package rlbotexample.strategy;

import rlbot.cppinterop.RLBotDll;
import rlbotexample.input.CarData;
import rlbotexample.input.DataPacket;
import rlbotexample.intercept.BallPath;
import rlbotexample.intercept.Intercept;
import rlbotexample.intercept.InterceptCalculator;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles choosing what strategy to use in certain situations and according to the
 * available information.
 */
public class StrategyPlanner {

    private final boolean aerial = false;
    public static final Vector3 ORANGE_GOAL = new Vector3(0,5125, 100);
    public static final Vector3 BLUE_GOAL = new Vector3(0,-5125, 100);
    public static final int FIELD_WIDTH = 3000;
    public static final int FIELD_DEPTH = 5125;
    public static final int FIELD_HEIGHT = 3000;
    private static final int AIM_DISTANCE = 125;

    public Strategy state;

    private int team;
    private double ballDistance;
    public static boolean isClosest;
    private boolean badPosition;
    private boolean kickOff;
    private Vector3 ballPosition;
    private Vector3 carPosition;
    private Vector3 carDirection;
    private double positionFromBall;
    public static Vector3 predictedBallPosition;
    public static Vector3 predictedBallAimPoint;
    public static Vector3 ballAimPoint;
    public static BallPath ballPath;

    private List<CarData> enemyCars;
    private List<CarData> friendlyCars;

    /**
     * This method initializes usefull data used in deciding the strategies.
     * @param startingState The state we start in.
     * @param input The values at the start of the game.
     */
    public StrategyPlanner(Strategy startingState, DataPacket input){
        enemyCars = new ArrayList<>();
        friendlyCars = new ArrayList<>();
        state = startingState;
        team = input.car.team;
        predictedBallPosition = input.ball.position;
        ballAimPoint = input.ball.position;
        calculateInfo(input);
    }

    /**
     * This method calculates the distance between a car and the ball.
     * @param ballPosition The current position of the ball.
     * @param carPosition The position of the car we want to check with
     * @param flat Do we check the z axis (for aerials).
     * @return the distance between car and ball.
     */
    public double ballDistance(Vector3 ballPosition, Vector3 carPosition, boolean flat){
        if(flat){
            return carPosition.flatten().distance(ballPosition.flatten());
        } else {
            return carPosition.distance(ballPosition);
        }
    }

    /**
     * This method calculates all the usefull info, that is going to be used to choose certain
     * states.
     * @param input the current state of the game.
     */
    private void calculateInfo(DataPacket input){
        isClosest = true;
        kickOff = input.kickOffPause;
        ballPosition = input.ball.position;
        carPosition = input.car.position;
        carDirection = input.car.orientation.noseVector;
        positionFromBall = ballPosition.y - carPosition.y;

        // logic for repositioning
        calculateBadPosition(team, positionFromBall, ballPosition.y);

        try {
            ballPath = new BallPath(RLBotDll.getBallPrediction());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intercept interception = InterceptCalculator.getIntercept(ballPath, input.car);
        if(interception != null){
            predictedBallPosition = interception.getPosition();
        }

        calculateAimPoint(interception);

        ballDistance = ballDistance(ballPosition,carPosition, aerial);

        //check all cars
        for(CarData car : input.allCars){
            if(car.team == team && car != input.car){
                friendlyCars.add(car);
            } else if(car != input.car) {
                enemyCars.add(car);
            }
            double otherDistance = ballDistance(ballPosition, car.position, aerial);
            if(isClosest && otherDistance < ballDistance){
                isClosest = false;
            }
        }
    }

    public void calculateBadPosition(int team, double positionFromBall, double ballYPosition){
        if(team == 0 && positionFromBall < 0){
            if(ballPosition.y > -Positioning.MAXDISTANCE) {
                badPosition = true;
            } else {
                badPosition = false;
            }
        } else if(team == 1 && positionFromBall > 0){
            if(ballPosition.y < Positioning.MAXDISTANCE) {
                badPosition = true;
            } else {
                badPosition = false;
            }
        } else {
            badPosition = false;
        }
    }

    /**
     * This method calculates the point where the agent needs to hit the ball in order to
     * @param interception
     */
    public void calculateAimPoint(Intercept interception){
        Vector3 directionVec;
        if(team == 0){
            directionVec = ballPosition.minus(ORANGE_GOAL).normalized();
        } else{
            directionVec = ballPosition.minus(BLUE_GOAL).normalized();
        }
        ballAimPoint = ballPosition.plus(directionVec.scaled(AIM_DISTANCE));

        // Simple precautions to avoid an exception
        if(interception != null){
            if(team == 0){
                directionVec = predictedBallPosition.minus(ORANGE_GOAL).normalized();
            } else{
                directionVec = predictedBallPosition.minus(BLUE_GOAL).normalized();
            }
            predictedBallAimPoint = predictedBallPosition.plus(directionVec.scaled(AIM_DISTANCE));
        }
    }

    /**
     * This method sets the state of the strategy planner, we only want to change the state if we
     * are not already in the state.
     * @param newState
     */
    public void setState(Strategy newState){
        if(!state.getStrategy().equals(newState.getStrategy()) && !state.isBusy()) {
            state = newState;
        }
    }

    public String getState(){
        return state.getStrategy();
    }

    public double getBallDistance(){
        return ballDistance;
    }

    public int getTeam(){
        return team;
    }

    public ControlsOutput doMove(DataPacket input){
        return state.doMove(input);
    }

    private void chooseState(DataPacket input){
        if(kickOff){
            setState(new KickOff());
        } else if(badPosition && !state.isBusy()){
            setState(new Positioning());
        } else if(ballDistance > 2250){
            setState(new MoveToBall());
        } else if(predictedBallPosition.z <= 125 && ballDistance < 365 || !isClosest){
            setState(new PowerShot());
        } else {
            setState(new Dribbling());
        }
    }

    public void refresh(DataPacket input){
        calculateInfo(input);
        chooseState(input);
    }
}
