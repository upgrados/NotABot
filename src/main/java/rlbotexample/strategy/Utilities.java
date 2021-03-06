package rlbotexample.strategy;

import rlbotexample.output.ControlsOutput;

public class Utilities {

    public static ControlsOutput move(double steerCorrectionRadians, float throttle, boolean boost){
        if(steerCorrectionRadians > 1.5){
            return new ControlsOutput()
                    .withThrottle(throttle)
                    .withSteer(-1)
                    .withSlide(true);
        } else if(steerCorrectionRadians < -1.5){
            return new ControlsOutput()
                    .withThrottle(throttle)
                    .withSteer(1)
                    .withSlide(true);
        } else if(steerCorrectionRadians > 0.1) {
            return new ControlsOutput()
                    .withSteer(-1)
                    .withThrottle(throttle);
        } else if(steerCorrectionRadians < -0.1){
            return new ControlsOutput()
                    .withSteer(1)
                    .withThrottle(throttle);
        } else {
            return new ControlsOutput()
                    .withSteer(0)
                    .withThrottle(throttle)
                    .withBoost(boost);
        }
    }
}
