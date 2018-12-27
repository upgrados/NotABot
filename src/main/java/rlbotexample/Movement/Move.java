package rlbotexample.Movement;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;
import rlbotexample.vector.Vector3;

public interface Move {

    /**
     * This method should handle doing the manouvre.
     * @param input the GameState.
     * @return the output to do the move.
     */
    ControlsOutput move(DataPacket input, Vector3 targetLocation);

    /**
     * This method should return true if the movement completion condition has been met.
     * @return if the move is completed or not.
     */
    boolean isDone();

    public void setIsDone(boolean done);
}
