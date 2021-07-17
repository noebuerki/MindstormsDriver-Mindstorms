package ch.bbcag.mdriver.engine;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.RegulatedMotor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.bbcag.mdriver.view.View;

public class Engine {

	private static final int SPEED_MULTIYPLIER = 10;
	private static final int MAX_SPEED = 1000;
	
	private static final int FULL_ARM_ROTATION = 180;
	
    private float lastLeft;
    private float lastRight;

    private final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.B);
    private final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.C);

    private final EV3MediumRegulatedMotor armMotor = new EV3MediumRegulatedMotor(MotorPort.A);

    private boolean isArmUp;
    private final ExecutorService executor;
    private final Runnable rotateRunnable;

    public Engine() {
        leftMotor.synchronizeWith(new RegulatedMotor[]{rightMotor});

        executor = Executors.newSingleThreadExecutor();

        rotateRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (armMotor) {
                    if (isArmUp) {
                        armMotor.rotate(-FULL_ARM_ROTATION);
                    } else {
                        armMotor.rotate(FULL_ARM_ROTATION);
                    }
                    isArmUp = !isArmUp;
                    armMotor.stop();
                    armMotor.flt();
                }
            }
        };

        executor.execute(new Runnable() {
            @Override
            public void run() {
                orientateArm();
                new View().showText("Please wait:" + System.lineSeparator() + "server starting...");
            }
        });
    }

    public ExecutorService getExecutorService() {
        return executor;
    }

    public synchronized void move(final float left, final float right) {

        float lastLeft = this.lastLeft;
        float lastRight = this.lastRight;
        this.lastLeft = left;
        this.lastRight = right;

        leftMotor.startSynchronization();

        if (left != 0) {
            if (left < 0 && lastLeft >= 0) {
                leftMotor.backward();
            } else if (left > 0 && lastLeft <= 0) {
                leftMotor.forward();
            }
            leftMotor.setSpeed((int) Math.abs(left) * SPEED_MULTIYPLIER);
        } else {
            leftMotor.flt();
        }

        if (right != 0) {
            if (right < 0 && lastRight >= 0) {
                rightMotor.backward();
            } else if (right > 0 && lastRight <= 0) {
                rightMotor.forward();
            }
            rightMotor.setSpeed((int) Math.abs(right) * SPEED_MULTIYPLIER);
        } else {
            rightMotor.flt();
        }

        leftMotor.endSynchronization();

    }

    public void rotate() {
        executor.execute(rotateRunnable);
    }

    private void orientateArm() {
        armMotor.setSpeed(MAX_SPEED);
        armMotor.setStallThreshold(10, 1); // working stallthreashold
        while (!armMotor.isStalled()) {
            armMotor.rotate(FULL_ARM_ROTATION / 10);
        }
        armMotor.setStallThreshold(100, 1); // working stallthreashold
        armMotor.stop();
        armMotor.flt();
        isArmUp = true;
    }
}
