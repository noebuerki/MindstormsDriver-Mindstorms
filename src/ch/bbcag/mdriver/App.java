package ch.bbcag.mdriver;

public class App {

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.startServer();
        controller.waitForClient();
        controller.startListening();
    }
}
