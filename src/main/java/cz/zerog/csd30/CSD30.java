package cz.zerog.csd30;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import cz.zerog.csd30.i2cbus.I2CMode;

import java.io.IOException;


public class CSD30 {

    public static int MAX_ATTEMPTS = 3;

    //i2cbus or modbus
    private Mode mode;

    //pooling csd30
    private Thread thread;

    private float co2;
    private float humidity;
    private float temperature;

    public CSD30(Mode mode) {
        this.mode = mode;
    }

    public float getCO2() {
        return co2;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumanity() {
        return humidity;
    }

    public void start() {
        if (thread != null) {
            return;
        }



        thread = new Thread(() -> {
            try {

                int interval = mode.getInterval() + 300;


                while (!Thread.interrupted()) {

                    Thread.sleep(interval);

                    if (!mode.isDataReady()) {
                        continue;
                    }

                    measurement();

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void setPressure(int pressure) {

    }

    public void setMeasurementInterval(int interval)  {

        CsdException exception = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try {
                mode.setInterval(interval);

                if (mode.getInterval() == interval) {
                    return;
                }

            } catch (CsdException e) {
                exception = e;
            }
        }

        throw exception;
    }

    public int getMeasurementInterval() {

        CsdException exception = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try {

                int interval = mode.getInterval();

                if (interval > -1) {
                    return interval;
                }

            } catch (CsdException e) {
                exception = e;
            }
        }

        throw exception;
    }

    public String getFirmwareVersion() {
        return mode.getFirmwareVersion();
    }

    public void measurement() {
        float[] load = mode.getMeasurement();
        co2 = load[0];
        temperature = load[1];
        humidity = load[2];
    }

    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {

        CSD30 csd30 = new CSD30(new I2CMode(I2CBus.BUS_1));
        csd30.setMeasurementInterval(2);
        csd30.start();

        while(true) {
            try {
                Thread.sleep(3 * 1000);

                if (!csd30.mode.isDataReady()) {
                    System.out.println("Is not ready");
                    continue;
                }

                csd30.measurement();
                System.out.println("CO2: " + csd30.getCO2());
            } catch (CsdException e) {
                e.printStackTrace();
            }

        }

    }

}
