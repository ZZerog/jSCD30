package cz.zerog.scd30.i2cbus;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import cz.zerog.scd30.ScdException;
import cz.zerog.scd30.Mode;
import cz.zerog.scd30.i2cbus.I2CMessage.Factory;

import java.io.IOException;

public class I2CMode implements Mode {

    private I2CDevice i2cDevice;
    private I2CBus i2cBus;

    /**
     * I2C bus address of CSD30
     */
    public final int DEVICE_ADDRESS = 0x61;

    /*
     * Messages address (header)
     */

    public final short TRIGGER_START = 0x0010;
    public final short TRIGGER_STOP = 0x0104;
    public final short INTERVAL = 0x4600;
    public final short DATA_READY = 0x0202;
    public final short MEASUREMENT = 0x0300;
    public final short FIRMWARE = (short) 0xD100;
    public final short SELF_CALIBRATION = (short) 0x5306;
    public final short RECALIBRATION = (short) 0x5204;
    public final short TEMP_OFFSET = (short) 0x5403;
    public final short ALTITUDE = 0x5102;
    public final short SOFT_RESET = (short) 0xD304;



    public I2CMode(int busIndex) throws IOException, I2CFactory.UnsupportedBusNumberException {
        i2cBus = I2CFactory.getInstance(busIndex);
        i2cDevice = i2cBus.getDevice(DEVICE_ADDRESS);
    }


    /**
     * Sets the interval used by the SCD30 sensor to measure in continuous measurement mode.
     *
     * @param interval
     * @throws ScdException
     */
    @Override
    public void setInterval(int interval) throws ScdException {
        try {
            I2CMessage message = Factory.writeMessage(INTERVAL, interval);
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Get measurement interval.
     * @return measurement interval or -1 when error occurs
     * @throws IOException
     */
    @Override
    public int getInterval() throws ScdException {

        I2CMessage message = Factory.readMessage(INTERVAL);

        try {
            message.exec(i2cDevice);
            return message.getNextShort();

        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Data ready command is used to determine
     * if a measurement can be read from the sensor’s buffer.
     *
     * @return true if so otherwise false
     * @throws ScdException
     */
    @Override
    public boolean isDataReady() throws ScdException {
        I2CMessage message = Factory.readMessage(DATA_READY);

        try {
            message.exec(i2cDevice);
            return message.getNextShort()==1;
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * When new measurement data is available it can be read out with the following command.
     * @return float array with CO2 (index 0), temperature (index 1) adm humidity (index 2).
     */
    @Override
    public float[] getMeasurement() throws ScdException {

        float[] meas = new float[3];
        I2CMessage message = Factory.readDataMessage(MEASUREMENT);

        try {
            message.exec(i2cDevice);

            meas[0] = getAsFloat(message); //CO2
            meas[1] = getAsFloat(message); //temperature
            meas[2] = getAsFloat(message); //humidity

            return meas;
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    @Override
    public String getFirmwareVersion() throws ScdException {
        I2CMessage message = Factory.readMessage(FIRMWARE);
        try {
            message.exec(i2cDevice);
            short v = message.getNextShort();
            return ((v >> 2) & 0xFF)+"."
                    + (v & 0x00FF);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Starts continuous measurement of the SCD30
     * to measure CO2 concentration, humidity and temperature.
     * @param pressureCompensation
     */
    @Override
    public void start(int pressureCompensation) throws ScdException {
        I2CMessage message = Factory.writeMessage(TRIGGER_START, pressureCompensation);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Stop continuous measurement.
     */
    @Override
    public void stop() throws ScdException {
        try {
            I2CMessage message = Factory.writeMessage(TRIGGER_STOP);
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Continuous automatic self-calibration can be
     * (de-)activated with the following command.
     */
    public void selfCalibration(boolean active) throws ScdException {
        I2CMessage message = Factory.writeMessage(SELF_CALIBRATION, active?1:0);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Get result if self-calibration is active or deactive.
     * @return true if active otherwise false
     */
    public boolean isSelfCalibration() throws ScdException {
        I2CMessage message = Factory.readMessage(SELF_CALIBRATION);
        try {
            message.exec(i2cDevice);
            return message.getNextShort()==1;
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }


    /**
     * Forced recalibration (FRC) is used to compensate for sensor drifts
     * when a reference value of the CO2 concentration in close
     * proximity to the SCD30 is available.
     * @param value
     */
    public void setRecalibrationValue(int value) throws ScdException {
        I2CMessage message = Factory.writeMessage(RECALIBRATION, value);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * The on-board RH/T sensor is influenced by thermal self-heating of SCD30
     * and other electrical components. Design-in alters the thermal properties
     * of SCD30 such that temperature and humidity offsets may occur when operating
     * the sensor in end-customer devices. Compensation of those effects is achievable
     * by writing the temperature offset found in continuous operation
     * of the device into the sensor.
     * @param offset
     */
    public void setTemperatureOffset(int offset) throws ScdException {
        I2CMessage message = Factory.writeMessage(TEMP_OFFSET, offset);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Get temperature offset.
     * @return temperature offset
     */
    public int getTemperatureOffset() throws ScdException {
        I2CMessage message = Factory.readMessage(TEMP_OFFSET);
        try {
            message.exec(i2cDevice);
            return message.getNextShort();
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Measurements of CO2 concentration based on the NDIR principle
     * are influenced by altitude. SCD30 offers to compensate
     * deviations due to altitude by using the following command.
     * @param altitudeLevel
     */
    public void setAltitudeCompensation(int altitudeLevel) throws ScdException {
        I2CMessage message = Factory.writeMessage(ALTITUDE, altitudeLevel);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * Get altitude level.
     * @return altitude level
     */
    public int getAltitudeCompensation() throws ScdException {
         I2CMessage message = Factory.readMessage(ALTITUDE);
        try {
            message.exec(i2cDevice);
            return message.getNextShort();
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * The SCD30 provides a soft reset mechanism that forces the sensor into
     * the same state as after powering up without the need
     * for removing the power-supply. It does so by restarting its system controller.
     * After soft reset the sensor will reload all calibrated data.
     * However, it is worth noting that the sensor reloads calibration data prior
     * to every measurement by default. This includes previously set reference
     * values from ASC or FRC as well as temperature offset values last setting.
     */
    public void softReset() throws ScdException {
        I2CMessage message = Factory.writeMessage(SOFT_RESET);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new ScdException(e);
        }
    }

    /**
     * From message reads two shorts, join it into integer and convert to a float.
     *
     * @param message source
     * @return float representation of int.
     */
    private float getAsFloat(I2CMessage message) {
        short a = message.getNextShort();
        short b = message.getNextShort();

        //System.out.println(Integer.toHexString(a));
        //System.out.println(Integer.toHexString(b));

        int i = a  << 16;
        i = i | b & 0xFFFF;
        //System.out.println(Integer.toHexString(i));
        return Float.intBitsToFloat(i);
    }

}
