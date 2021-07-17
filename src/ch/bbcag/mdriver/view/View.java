package ch.bbcag.mdriver.view;

import ch.bbcag.mdriver.commons.Record;
import lejos.hardware.LED;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class View {

    public static final int OFF = 0;
    public static final int RED = 2;
    public static final int ORANGE = 3;
    public static final int GREEN = 1;
    public static final int STATIC = 1;
    public static final int BLINK_SLOWLY = 2;
    public static final int BLINK_QUICKLY = 3;

    private final LED led;
    private GraphicsLCD lcd;
    private final MapDrawer drawer = new MapDrawer();

    public View() {
        led = LocalEV3.get().getLED();
        lcd = LocalEV3.get().getGraphicsLCD();
    }

    public void setPattern(int pattern) {
        led.setPattern(pattern);
    }

    public void showText(String text) {
        clear();

        getLcd().setFont(Font.getFont(0, 0, 0));

        Font font = getLcd().getFont();
        String[] lines = text.split(System.lineSeparator());

        int y = (getLcd().getHeight() - (font.height * lines.length)) / 2;

        for (int i = 0; i < lines.length; i++) {
            int lineY = y + i * font.height;
            getLcd().drawString(lines[i], getX(getLcd(), font, lines[i]), lineY, 0);
        }
    }

    public GraphicsLCD getLcd() {
        return lcd;
    }

    public void setLcd(GraphicsLCD lcd) {
        this.lcd = lcd;
    }

    public static int getPattern(int color, int mode) {
        return color * mode;
    }

    public void clear() {
        lcd.clear();
    }

    public void awaitConnection() {
        showText(getAwaitConnectionText());
        setPattern(ORANGE);
    }

    public String getAwaitConnectionText() {
        return getIP() + System.lineSeparator() + getBluetoothName();
    }

    public String getBluetoothName() {
        return LocalEV3.get().getBluetoothDevice().getFriendlyName();
    }

    public void drawMap(Record record) {
        drawer.draw(record);
    }

    public String getIP() {

        try {
            Enumeration<NetworkInterface> netEnumeration = NetworkInterface.getNetworkInterfaces();
            while (netEnumeration.hasMoreElements()) {
                NetworkInterface netInf = netEnumeration.nextElement();
                for (InetAddress inet : Collections.list(netInf.getInetAddresses())) {
                    if (!inet.isLoopbackAddress()) {
                        return inet.getHostAddress();
                    }
                }
            }
        } catch (SocketException se) {
            se.printStackTrace();
        }
        return "No IP found";
    }

    public void connected() {
        setPattern(View.GREEN);
        clear();
        showText("Connected");
    }

    private int getX(GraphicsLCD lcd, Font font, String text) {
        return (lcd.getWidth() - font.stringWidth(text)) / 2;
    }
}
