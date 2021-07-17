package ch.bbcag.mdriver;

import ch.bbcag.mdriver.commons.StateCode;
import ch.bbcag.mdriver.commons.StateMessage;
import ch.bbcag.mdriver.connection.Server;
import ch.bbcag.mdriver.engine.Engine;
import ch.bbcag.mdriver.engine.RecordPlayer;
import ch.bbcag.mdriver.view.View;
import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;

public class Controller {

    private final CommandExecutor commandExecutor;

    private final View view = new View();
    private Server server;

    public Controller() {
        setExitOnEscape();
        view.setPattern(View.RED);
        view.showText("Please wait:" + System.lineSeparator() + "calibrating...");
        Engine engine = new Engine();
        RecordPlayer player = new RecordPlayer();
        commandExecutor = new CommandExecutor(engine, player);
        player.setCommandExecutor(commandExecutor);
    }

    public void startServer() {
        server = Server.getInstance();
    }

    public void waitForClient() {
        view.awaitConnection();
        server.acceptClient();

        server.setOnCommandListener(commandExecutor);
    }

    public void startListening() {
        server.startListening();
    }

    public void setExitOnEscape() {
        Button.ESCAPE.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(Key k) {
            }

            @Override
            public void keyPressed(Key k) {
                Server.getInstance().send(new StateMessage(StateCode.SERVER_CLOSING));
                System.exit(0);
            }
        });
    }

}
