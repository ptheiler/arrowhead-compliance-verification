package eu.arrowhead.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.devices.GroveLed;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class OnboardingStatus
{
    private final Logger logger = LogManager.getLogger();
    private final ExecutorService executorService;
    private final GrovePi grovePi;
    private final GroveLed red;
    private final GroveLed blue;
    private final BlinkingLed blinkingRed;
    private final BlinkingLed blinkingBlue;

    public OnboardingStatus(final ExecutorService executorService, final GrovePi grovePi) throws IOException {
        this.grovePi = grovePi;
        this.executorService = executorService;
        red = new GroveLed(grovePi, 2); // D2
        blue = new GroveLed(grovePi, 4); // D4

        blinkingRed = new BlinkingLed(red);
        blinkingBlue = new BlinkingLed(blue);
    }

    public void disconnected()
    {
        try {
            blinkingRed.reset();
            blinkingBlue.stop();
            blue.set(false);
            executorService.execute(blinkingRed);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            error();
        }
    }

    public void connecting()
    {
        try {
            blinkingRed.stop();
            blinkingBlue.reset();
            red.set(false);
            executorService.execute(blinkingBlue);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            error();
        }
    }

    public void connected()
    {
        try {
            blinkingRed.stop();
            blinkingBlue.stop();
            red.set(false);
            blue.set(true);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            error();
        }
    }

    public void error()
    {
        try {
            blinkingRed.reset();
            blinkingBlue.stop();
            blue.set(false);
            executorService.execute(blinkingRed);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        red.set(false);
        blue.set(false);
        executorService.shutdown();
        blinkingRed.stop();
        blinkingBlue.stop();
    }

    private class BlinkingLed implements Runnable
    {
        private final AtomicBoolean running = new AtomicBoolean(false);
        private final GroveLed led;
        private boolean state = false;

        public BlinkingLed(final GroveLed led) {
            this.led = led;
        }
        public void reset()
        {
            running.set(true);
        }
        public void stop()
        {
            running.set(false);
        }

        @Override
        public void run() {
            while(running.get())
            {
                try {
                    state = !state;
                    led.set(state);
                    Thread.sleep(1000L);
                } catch (IOException | InterruptedException e) {
                    running.set(false);
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
