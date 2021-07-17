package ch.bbcag.mdriver;

import ch.bbcag.mdriver.commons.*;
import ch.bbcag.mdriver.connection.Server;
import ch.bbcag.mdriver.engine.Engine;
import ch.bbcag.mdriver.engine.RecordPlayer;

public class CommandExecutor implements OnCommandListener {

    private final Engine engine;
    private final RecordPlayer player;

    public CommandExecutor(Engine engine, RecordPlayer player) {
        this.engine = engine;
        this.player = player;
    }

    private void handleStatusCode(StateCode code) {
        switch (code) {
            case ABORD_RECORDING:
                player.stop();
                break;
            case SERVER_CLOSING:
                Server.getInstance().close();
                System.exit(0);
                break;
        }
    }

    @Override
    public void onCommand(Message message) {
        switch (message.getType()) {
            case DRIVE:
                DriveMessage driveMessage = (DriveMessage) message;
                engine.move(driveMessage.getLeft(), driveMessage.getRight());
                break;
            case SHOOT:
                engine.rotate();
                break;
            case RECORD:
                player.play((Record) message);
                break;
            case STATE:
                handleStatusCode(((StateMessage) message).getStateCode());
                break;
        }
    }
}
