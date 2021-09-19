package ch.noebuerki.mindstormsdriver.view;

import ch.noebuerki.mindstormsdriver.commons.DriveMessage;
import ch.noebuerki.mindstormsdriver.commons.Message;
import ch.noebuerki.mindstormsdriver.commons.Record;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.LCD;

import java.util.List;

public class MapDrawer {
    public final static float WHEEL_DIAMETER = 110;
    public final static float WIDTH = 44;

    public final static int START_POINT_OFFSET = 2;
    public final static int IMAGE_MARGIN = (int) (WHEEL_DIAMETER * 2);

    public final static int FULL_CIRCLE = 360;
    public double factor;
    private int x;
    private int y;
    private int angle;
    private int newX;
    private int newY;
    private int newAngle;
    private GraphicsLCD g;

    public MapDrawer() {
        loadGraphics();
    }

    public void draw(Record record) {
        List<Message> records = record.getMessages();

        if (records.size() < 1) {
            return;
        }

        x = 0;
        y = 0;
        factor = 1;

        reset();

        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;

        for (int i = 0; i < records.size(); i++) {
            executeMessageWhenAvailable(record, records, i, false);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        drawImage(record, minX, minY, maxX, maxY);
    }

    public void drawLine(float speed, double deltaTime, boolean shouldDraw) { // draw a straight line

        float way = (float) (deltaTime * getMmPerSec(speed));

        newX = x + (int) Math.round(Math.sin(Math.toRadians(angle)) * way);
        newY = y + (int) Math.round(Math.cos(Math.toRadians(angle)) * way);

        if (shouldDraw) {        	
        	g.drawLine(x, y, newX, newY);
        }
    }

    /**
     * @param deltaTime  is how long it was driven at the speed left and right
     */
    public void drawArc(float left, float right, double deltaTime, boolean shouldDraw) { // draw a arc
        float radius = (float) Math.round(getInnerRadius(left, right) + getWidth() / 2);
        float midSpeed = getMidSpeed(left, right);

        int startAngle = angle;
        int sign = 1; // says whether the circle should be drawn left or right from the start
        int angleSign = -1; // tells in which direction the circle should be drawn

        if (left < right) {
            sign = -sign;
            startAngle -= FULL_CIRCLE / 2;
            angleSign = -angleSign;
        }

        if (left < 0) {
            startAngle -= FULL_CIRCLE / 2;
            sign = -sign;
        }

        if (left * right < 0) {
            angleSign -= angleSign;
        }

        float circleMidX = (float) (x - Math.cos(Math.toRadians(angle)) * radius * sign);
        float circleMidY = (float) (y + Math.sin(Math.toRadians(angle)) * radius * sign);

        int circleX = Math.round(circleMidX - radius);
        int circleY = Math.round(circleMidY - radius);

        // calculates how many degrees the robot has rotated
        double circleCircumference = radius * 2 * Math.PI;
        double way = midSpeed * deltaTime;
        double angleRotation = way / circleCircumference * 360;

        int size = Math.round(radius * 2);

        if (shouldDraw) {
            try {
                g.drawArc(circleX, circleY, size, size, startAngle, (int) Math.round(Math.min(FULL_CIRCLE, angleRotation) * angleSign));
            } catch (ArithmeticException e) {
            	new View().setPattern(View.RED);
            }
        }

        newAngle = (int) Math.round((angle + angleRotation * angleSign) % FULL_CIRCLE); // the new angle of the roboter


        /*
        help solution on [this](docs/assets/ArcAid.svg) image
         */
        int deltaAngle = newAngle - angle;

        double hypotenuse = 2 * radius * Math.sin(Math.toRadians(deltaAngle / 2d));

        int a = (FULL_CIRCLE / 4 - Math.abs((angle + FULL_CIRCLE / 4) % FULL_CIRCLE));
        int b = FULL_CIRCLE / 4 - deltaAngle / 2;
        int tendonAngle = a + b;

        int dX = (int) Math.round(Math.cos(Math.toRadians(tendonAngle)) * hypotenuse);
        int dY = (int) Math.round(Math.sin(Math.toRadians(tendonAngle)) * hypotenuse);

        dX *= angleSign;
        dY *= angleSign;

        newX += dX;
        newY += dY;
    }

    public double getMmPerSec(float speed) {
        return getWheelDiameter() * Math.PI * (speed / FULL_CIRCLE);
    }

    public float getMidSpeed(float leftSpeed, float rightSpeed) {
        return (float) ((getMmPerSec(leftSpeed) + getMmPerSec(rightSpeed)) / 2);
    }

    /**
     * @return the inner radius of the Mindstorms when he has a left speed of
     * leftSpeed and a right speed of right speed
     */
    public float getInnerRadius(float leftSpeed, float rightSpeed) {
        double left = getMmPerSec(leftSpeed);
        double right = getMmPerSec(rightSpeed);

        double min = Math.min(left, right);
        double max = Math.max(left, right);

        try {
            return (float) ((getWidth() * min) / (max - min));
        } catch (ArithmeticException e) {
            return 0;
        }
    }

    private double getDeltaTime(Record record, int i) {
        DriveMessage driveMessage = (DriveMessage) record.getMessages().get(i);
        if (i >= record.getMessages().size() - 1) {
            return (record.getTime() - driveMessage.getTime()) * 0.001;
        }
        return (record.getMessages().get(i + 1).getTime() - driveMessage.getTime()) * 0.001;
    }

    private void interpretMessage(DriveMessage driveMessage, double deltaTime, boolean shouldDraw) {
        if (driveMessage.getLeft() == driveMessage.getRight()) {
            if (driveMessage.getLeft() == 0) {
                return;
            }
            drawLine(driveMessage.getLeft(), deltaTime, shouldDraw);
        } else {
            drawArc(driveMessage.getLeft(), driveMessage.getRight(), deltaTime, shouldDraw);
        }
    }

    private void drawImage(Record record, int minX, int minY, int maxX, int maxY) {
        double width = maxX - minX;
        double height = maxY - minY;

        double widthFactor = LCD.SCREEN_WIDTH / width;
        double heightFactor = LCD.SCREEN_HEIGHT / height;
        
        double finalWidthFactor = LCD.SCREEN_WIDTH / (width + IMAGE_MARGIN * widthFactor);
        double finalHeightFactor = LCD.SCREEN_HEIGHT / (height + IMAGE_MARGIN * heightFactor);
        
        factor = Math.min(finalWidthFactor, finalHeightFactor);
        g.clear();
        
        int startX = (int) Math.round((Math.abs(minX)) * factor + getImageMargin() / 2);
        int startY = (int) Math.round((Math.abs(minY)) * factor + getImageMargin() / 2);
        

        x = startX;
        y =  startY;

        reset();
        
        List<Message> records = record.getMessages();

        for (int i = 0; i < records.size(); i++) {
            executeMessageWhenAvailable(record, records, i, true);
        }
        g.fillArc(startX - START_POINT_OFFSET, startY - START_POINT_OFFSET, START_POINT_OFFSET * 2, START_POINT_OFFSET * 2, 0, FULL_CIRCLE);
    }

    private void executeMessageWhenAvailable(Record record, List<Message> records, int i, boolean shouldDraw) {
        Message message = records.get(i);
        
        if (!(message instanceof DriveMessage)) return;
        
        DriveMessage driveMessage = (DriveMessage) message;

        double deltaTime = getDeltaTime(record, i);

        interpretMessage(driveMessage, deltaTime, shouldDraw);

        x = newX;
        y = newY;
        angle = newAngle;
    }
    
    private void reset() {
        newX = x;
        newY = y;
        angle = FULL_CIRCLE / 2;
        newAngle = angle;
    }

    private void loadGraphics() {
        g = LocalEV3.get().getGraphicsLCD();

    }

    private double getWidth() {
        return WIDTH * factor;
    }

    private double getWheelDiameter() {
        return WHEEL_DIAMETER * factor;
    }
    
    private double getImageMargin() {
    	return getWheelDiameter() * 2;
    }

}