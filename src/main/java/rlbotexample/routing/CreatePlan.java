package rlbotexample.routing;

import rlbotexample.input.CarData;
import rlbotexample.intercept.BallPath;
import rlbotexample.output.ControlsOutput;
import rlbotexample.strategy.Utilities;
import rlbotexample.vector.Vector3;

public class CreatePlan {

    public static ControlsOutput moveAndDribble(CarData carData, Vector3 predictedPosition,
                                           Vector3 ballPosition){
        Vector3 carPosition = carData.position;
        Vector3 carDirection = carData.orientation.noseVector;

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = predictedPosition.minus(carPosition);

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // flick attempt
//        if(Math.abs(ballPosition.z-carPosition.z) <= 150 && ballDistance < 25 && carData.hasWheelContact){
//            return new ControlsOutput()
//                    .withJump();
//        }
//        if(!carData.hasWheelContact){
//            return new ControlsOutput()
//                    .withJump(true);
//        }

        //dribbling
        if(predictedPosition.z > 75 && carData.velocity.magnitude() > 200 &&
                predictedPosition.flatten().distance(carPosition.flatten()) < 200){
            return new ControlsOutput()
                    .withThrottle(-1)
                    .withSteer(0);
        }

        // stop boosting in move
        if(steerCorrectionRadians > -0.1 && steerCorrectionRadians < 0.1 && ballDistance < 30){
            return new ControlsOutput()
                    .withSteer(0)
                    .withThrottle(1);
        }

        return Utilities.move(steerCorrectionRadians, 1, true);
    }

    public static ControlsOutput moveAndHit(CarData carData, Vector3 predictedPosition,
                                            Vector3 ballPosition){
        Vector3 carPosition = carData.position;
        Vector3 carDirection = carData.orientation.noseVector;

        // Subtract the two positions to get a vector pointing from the car to the ball.
        Vector3 carToBall = predictedPosition.minus(carPosition);
        double ballDistance = carPosition.flatten().distance(ballPosition.flatten());

        // How far does the car need to rotate before it's pointing exactly at the ball?
        double steerCorrectionRadians = carDirection.flatten().correctionAngle(carToBall.flatten());

        // if we predict to hit the ball in the air we wait...
        if(predictedPosition.z > 125 ){
            return Utilities.move(steerCorrectionRadians, 0.5f, false);
        }

        if(ballDistance < 350 && carPosition.z < 17.5 && Math.abs(steerCorrectionRadians) < 0.5){
            return new ControlsOutput()
                    .withJump();
        }
        if(!carData.hasWheelContact && ballDistance < 350 && Math.abs(steerCorrectionRadians) < 0.5){
            return new ControlsOutput()
                    .withPitch(-1)
                    .withJump(true);
        }

        return Utilities.move(steerCorrectionRadians, 1, true);
    }


}
