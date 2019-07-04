package cz.zerog.csd30.i2cbus;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import cz.zerog.csd30.CsdException;
import cz.zerog.csd30.Mode;
import cz.zerog.csd30.i2cbus.I2CMessage.Factory;

import java.io.IOException;

public class I2CMode implements Mode {

    private I2CBus i2cBus;
    private I2CDevice i2cDevice;

    /**
     * I2C bus address of CSD30
     */
    public final int DEVICE_ADDRESS = 0x61;

    public final short TRIGGER_START = 0x0010;
    public final short TRIGGER_STOP = 0x0104;
    public final short INTERVAL = 0x4600;
    public final short DATA_READY = 0x0202;
    public final short MEASUREMENT = 0x0300;

    public I2CMode(int busIndex) throws IOException, I2CFactory.UnsupportedBusNumberException {
        i2cBus = I2CFactory.getInstance(busIndex);
        i2cDevice = i2cBus.getDevice(DEVICE_ADDRESS);

        System.out.println("init OK");
    }


    @Override
    public void setInterval(int interval) throws CsdException {
        try {
            I2CMessage message = Factory.writeMessage(INTERVAL, interval);
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new CsdException(e);
        }
    }

    /**
     * Get measurement interval.
     * @return measurement interval or -1 when error occurs
     * @throws IOException
     */
    @Override
    public int getInterval() throws CsdException {

        I2CMessage message = Factory.readMessage(INTERVAL);

        try {
            message.exec(i2cDevice);
            return message.getShort();

        } catch (IOException e) {
            throw new CsdException(e);
        }
    }

    @Override
    public boolean isDataReady() throws CsdException {
        I2CMessage message = Factory.readMessage(DATA_READY);

        try {
            message.exec(i2cDevice);
            return message.getShort()==1;
        } catch (IOException e) {
            throw new CsdException(e);
        }
    }

    @Override
    public float[] getMeasurement() {

        float[] meas = new float[3];
        I2CMessage message = Factory.readDataMessage(MEASUREMENT);

        try {
            message.exec(i2cDevice);
            short a = message.getShort();
            short b = message.getShort();


            System.out.println(Integer.toHexString(a));
            System.out.println(Integer.toHexString(b));


            int i = a  << 16;
            i = i | b & 0xFFFF ;
            System.out.println(Integer.toHexString(i));
            meas[0] = Float.intBitsToFloat(i);

            //meas[1] = message.getShort();
            //meas[2] = message.getShort();

            return meas;
        } catch (IOException e) {
            throw new CsdException(e);
        }
    }

    @Override
    public String getFirmwareVersion() {
    return null;
    }

    @Override
    public void start() {
        I2CMessage message = Factory.writeMessage(TRIGGER_START, 0);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new CsdException(e);
        }
    }

    @Override
    public void stop() {
        I2CMessage message = Factory.writeMessage(TRIGGER_STOP);
        try {
            message.exec(i2cDevice);
        } catch (IOException e) {
            throw new CsdException(e);
        }
    }

}
