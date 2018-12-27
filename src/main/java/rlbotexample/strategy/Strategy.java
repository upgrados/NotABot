package rlbotexample.strategy;

import rlbotexample.input.DataPacket;
import rlbotexample.output.ControlsOutput;

public interface Strategy {

    /**
     * This method should pick the output for the car for this current strategy
     * @param input the gameState
     * @return the move to be done at this stage+
     */
    ControlsOutput doMove(DataPacket input);

    /**
     * This method should return the name of the strategy.
     * @return name of strategy.
     */
    String getStrategy();

    /**
     * This method should return true if this strategy involves a lot of boosting.
     * @return if this strategy uses boost or not.
     */
    boolean isBoosting();

    /**
     * This method should return if the strategy is busy (which indicates the planner should not
     * switch states right now).
     * @return if the strategy is doing a move or not.
     */
    boolean isBusy();
}
