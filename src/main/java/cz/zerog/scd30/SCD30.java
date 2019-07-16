package cz.zerog.scd30;

/*
 * #%L
 * jSCD30
 * %%
 * Copyright (C) 2019 jSCD30
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import cz.zerog.scd30.Event.Type;
import cz.zerog.scd30.i2cbus.I2CMode;
import org.jetbrains.annotations.NotNull;

public class SCD30 {

    public static int MAX_ATTEMPTS = 3;

    private SCD30EventListener SCD30EventListener = null;

    private float co2Threshold = 0.1f;
    private float humidityThreshold = 0.1f;
    private float temperatureThreshold = 0.1f;

    // value 0 == pressure compensation is disable
    private int pressureCompensation = 0;

    //i2cbus or modbus
    private Mode mode;

    //pooling scd30
    private Thread thread;

    private volatile float co2;
    private volatile float humidity;
    private volatile float temperature;

    public SCD30(@NotNull final Mode mode) {
        this.mode = mode;
    }

    public void setEventListener(SCD30EventListener SCD30EventListener) {
        this.SCD30EventListener = SCD30EventListener;
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
            System.out.println("thread is not null");
            return;
        }

        thread = new Thread(() -> {

            mode.start(pressureCompensation);

            int interval = (getMeasurementInterval()*1000) + 100;


            main:while (!Thread.interrupted()) {

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    return;
                }

                for (int i = 0; i < MAX_ATTEMPTS; i++) {

                    try {

                        if (!mode.isDataReady()) {
                            continue main;
                        }

                        measurement();
                        continue main;

                    } catch (ScdException e) {
                        if(i == MAX_ATTEMPTS) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        thread.setDaemon(true);
        thread.setName("scd30-measurement-thread");
        thread.start();
    }

    /**
     * Stop measurement thread and send
     * stop command to SCD30 sensor.
     *
     * @see I2CMode#stop()
     */
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

        ScdException exception = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try {
                mode.setInterval(interval);

                int getInterval = mode.getInterval();
                if (getInterval == interval) {
                    return; //OK
                } else {
                    throw new ScdException("Set interval: "+interval+" but SCD30 return "+getInterval+" interval.");
                }

            } catch (ScdException e) {
                exception = e;
            }
        }

        throw exception;
    }

    public int getMeasurementInterval() {

        ScdException exception = null;

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try {

                int interval = mode.getInterval();
                return interval;

            } catch (ScdException e) {
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
        if(SCD30EventListener !=null) {
            if(Math.abs(load[0]-co2)>co2Threshold) {
                SCD30EventListener.event(new Event(Type.CO2, load[0]));
            }

            if(Math.abs(load[2]-humidity)>humidityThreshold) {
                SCD30EventListener.event(new Event(Type.HUMID, load[2]));
            }

            if(Math.abs(load[1]-temperature)>temperatureThreshold) {
                SCD30EventListener.event(new Event(Type.TEMP, load[1]));
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
