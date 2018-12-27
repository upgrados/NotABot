package rlbotexample.Movement;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public class LandOnGround implements Move {

    /**
     * This method should calculate the movements necesarry to land correctly on the ground
     * @param input the GameState.
     * @param targetLocation
     * @return
     */
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
