package rlbotexample.Movement;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class Flick implements Move {
    @Override
    public ControlsOutput move(DataPacket input, Vector3 targetLocation) {
        return null;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void setIsDone(boolean done) {

    }
}
