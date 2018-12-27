package rlbotexample.strategy;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;

public class Aerial implements Strategy{
    @Override
    public ControlsOutput doMove(DataPacket input) {
        return null;
    }

    @Override
    public String getStrategy() {
        return "Aerial";
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
