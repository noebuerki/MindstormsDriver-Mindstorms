package ch.bbcag.mdriver.engine;

import ch.bbcag.mdriver.CommandExecutor;
import ch.bbcag.mdriver.commons.*;
import ch.bbcag.mdriver.connection.Server;
import ch.bbcag.mdriver.view.View;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecordPlayer implements Runnable {

    private Record record;
    private CommandExecutor commandExecutor;
    private Thread thread = null;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final View view = new View();

    public void play(Record record) {
        view.drawMap(record);
        if (isPlaying()) {
            stop();
        }
        this.record = record;
        thread = new Thread(this);
        running.set(true);
        thread.start();

    }

    @Override
    public void run() {
        if (record != null && commandExecutor != null) {
            List<Message> messages = record.getMessages();
            for (int i = 0; i < messages.size(); i++) {
                commandExecutor.onCommand(messages.get(i));
                int deltaTime;
                if (!running.get()) {
                    return;
                }
                if (i + 1 >= messages.size()) {
                    deltaTime = (int) (record.getTime() - messages.get(i).getTime());
                } else {
                    deltaTime = (int) (messages.get(i + 1).getTime() - messages.get(i).getTime());
                }
                try {
                    Thread.sleep(deltaTime);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
        end();
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public boolean isPlaying() {
        return thread != null && thread.isAlive();
    }

    public void stop() {
        running.set(false);
        if (isPlaying()) {
            thread.interrupt();
        }
        while (thread.isAlive()) ;
        end();
    }

    private void end() {
        view.connected();
        running.set(false);
        commandExecutor.onCommand(new DriveMessage(0, 0));
        Server.getInstance().send(new StateMessage(StateCode.RECORDING_ENDED));
    }
}
