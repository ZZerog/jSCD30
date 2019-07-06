package cz.zerog.scd30;

import cz.zerog.scd30.Event.Type;

public class SCD30 {

    public static int MAX_ATTEMPTS = 3;

    private EventListener eventListener = null;

    private float co2Threshold;
    private float humidityThreshold;
    private float temperatureThreshold;

    // value 0 == pressure compensation is disable
    private int pressureCompensation = 0;

    //i2cbus or modbus
    private Mode mode;

    //pooling scd30
    private Thread thread;

    private volatile float co2;
    private volatile float humidity;
    private volatile float temperature;

    public SCD30(Mode mode) {
        this.mode = mode;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public float getCO2() {
        return co2;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void start() {
        if (thread != null) {
            return;
        }

        thread = new Thread(() -> {

            mode.start(pressureCompensation);

            int interval = getMeasurementInterval() + 100;

            while (!Thread.interrupted()) {

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    return;
                }

                try {
                    if (!mode.isDataReady()) {
                        continue;
                    }

                    measurement();

                } catch (CsdException e) {
                    //fixme print into log
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.setName("scd30-measurement-thread");
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }

        mode.stop();
    }

    /**
     * Set ambient pressure compensation.
     *
     * @param pressureCompensation 700 to 1400 mBar or 0 as disabled
     */
    public void setPressureCompensation(int pressureCompensation) {
        this.pressureCompensation = pressureCompensation;
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

        //event handle
        if(eventListener!=null) {
            if(Math.abs(load[0]-co2)>co2Threshold) {
                eventListener.event(new Event(Type.CO2, load[0]));
            }

            if(Math.abs(load[1]-humidity)>humidityThreshold) {
                eventListener.event(new Event(Type.HUMID, load[1]));
            }

            if(Math.abs(load[2]-temperature)>temperatureThreshold) {
                eventListener.event(new Event(Type.TEMP, load[2]));
            }
        }

        co2 = load[0];
        temperature = load[1];
        humidity = load[2];
    }

    public float getCo2Threshold() {
        return co2Threshold;
    }

    public void setCo2Threshold(float co2Threshold) {
        this.co2Threshold = co2Threshold;
    }

    public float getHumidityThreshold() {
        return humidityThreshold;
    }

    public void setHumidityThreshold(float humidityThreshold) {
        this.humidityThreshold = humidityThreshold;
    }

    public float getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public void setTemperatureThreshold(float temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }
}
