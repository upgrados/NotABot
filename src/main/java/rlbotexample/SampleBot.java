package rlbotexample;

import rlbot.Bot;
import rlbot.ControllerState;
import rlbot.flat.GameTickPacket;
import rlbot.manager.BotLoopRenderer;
import rlbot.render.Renderer;
import rlbotexample.boost.BoostManager;
import rlbotexample.dropshot.DropshotTileManager;
import rlbotexample.input.CarData;
import rlbotexample.input.DataPacket;
import rlbotexample.intercept.BallPath;
import rlbotexample.output.ControlsOutput;
import rlbotexample.strategy.KickOff;
import rlbotexample.strategy.PowerShot;
import rlbotexample.strategy.StrategyPlanner;
import rlbotexample.vector.Vector3;

import java.awt.*;

public class SampleBot implements Bot {

    private final int playerIndex;
    private StrategyPlanner planner;

    public SampleBot(int playerIndex) {
        this.playerIndex = playerIndex;
    }

    /**
     * This is where we keep the actual bot logic. This function shows how to chase the ball.
     * Modify it to make your bot smarter!
     */
    private ControlsOutput processInput(DataPacket input) {

        Vector3 ballPosition = input.ball.position;
        CarData myCar = input.car;
        Vector3 carPosition = myCar.position;


//        // This is also optional!
//        if (input.ball.position.z > 500) {
//            RLBotDll.sendQuickChat(playerIndex, false, QuickChatSelection.Apologies_Cursing);
//        }

        planner.refresh(input);

        // This is optional!
        drawDebugLines(input, myCar);
        if(StrategyPlanner.ballPath != null) {
            drawBallPath(StrategyPlanner.ballPath, 350, Color.MAGENTA);
        }
        return planner.doMove(input);
    }

    /**
     * This is a nice example of using the rendering feature.
     */
    private void drawDebugLines(DataPacket input, CarData myCar) {
        // Here's an example of rendering debug data on the screen.
        Renderer renderer = BotLoopRenderer.forBotLoop(this);

        // Draw a line from the car to a goal
//        if(planner.getTeam() == 0){
//            renderer.drawLine3d(Color.BLUE, input.ball.position, planner.ORANGE_GOAL);
//        } else {
//            renderer.drawLine3d(Color.ORANGE, input.ball.position, planner.BLUE_GOAL);
//        }

        renderer.drawCenteredRectangle3d(Color.CYAN, new Vector3(-900,5125, 625), 5, 5, true);

        renderer.drawCenteredRectangle3d(input.team == 0 ? Color.GREEN : Color.red,
                StrategyPlanner.ballAimPoint, 5,5, true);

        // Draw a line that points out from the nose of the car.
        renderer.drawLine3d(Color.BLUE,
                myCar.position.plus(myCar.orientation.noseVector.scaled(150)),
                myCar.position.plus(myCar.orientation.noseVector.scaled(300)));

        renderer.drawString3d(planner.getState(), Color.WHITE, myCar.position, 2, 2);
    }

    /**
     * This should draw the path the ball is going to make.
     *
     * @param ballPath
     */
    private void drawBallPath(BallPath ballPath, int time, Color color) {
        Renderer renderer = BotLoopRenderer.forBotLoop(this);
        Vector3 prevSlice;
        Vector3 currentSlice;
        int i = 1;
        int max = time < ballPath.size() ? time : ballPath.size();
        while (i < max) {
            prevSlice = new Vector3(ballPath.getSlice(i - 1).physics().location());
            currentSlice = new Vector3(ballPath.getSlice(i).physics().location());
            renderer.drawLine3d(color, prevSlice, currentSlice);
            i++;
        }
    }


    @Override
    public int getIndex() {
        return this.playerIndex;
    }

    /**
     * This is the most important function. It will automatically get called by the framework with fresh data
     * every frame. Respond with appropriate controls!
     */
    @Override
    public ControllerState processInput(GameTickPacket packet) {

        if (packet.playersLength() <= playerIndex || packet.ball() == null || !packet.gameInfo().isRoundActive()) {
            // Just return immediately if something looks wrong with the data. This helps us avoid stack traces.
            return new ControlsOutput();
        }

        // Update the boost manager and tile manager with the latest data
        BoostManager.loadGameTickPacket(packet);
        DropshotTileManager.loadGameTickPacket(packet);

        // Translate the raw packet data (which is in an unpleasant format) into our custom DataPacket class.
        // The DataPacket might not include everything from GameTickPacket, so improve it if you need to!
        DataPacket dataPacket = new DataPacket(packet, playerIndex);

        //init planner
        if (planner == null) {
            planner = new StrategyPlanner(new KickOff(), dataPacket);
        }

        // Do the actual logic using our dataPacket.
        ControlsOutput controlsOutput = processInput(dataPacket);

        return controlsOutput;
    }

    public void retire() {
        System.out.println("Retiring sample bot " + playerIndex);
    }
}
