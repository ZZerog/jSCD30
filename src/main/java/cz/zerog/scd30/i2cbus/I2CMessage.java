package cz.zerog.scd30.i2cbus;

import com.pi4j.io.i2c.I2CDevice;
import cz.zerog.scd30.ScdException;
import cz.zerog.scd30.Mode;
import cz.zerog.scd30.crc.AlgoParams;
import cz.zerog.scd30.crc.CrcCalculator;

import java.io.IOException;

public class I2CMessage {

    private final CrcCalculator crc8 = new CrcCalculator(new AlgoParams("CRC-8", 8, 0x31, 0xFF, false, false, 0x00, 0xF4));

    private final byte[] write = new byte[5]; //buffer with maximal length
    private int writeLen = 2; //how many bytes will be write into i2c device

    private byte read[] = null; //read buffer

    private final int writeLoad; //parameter
    private final short message; //address

    //index of 'next short' result
    private int resultIndex = 0;


    /**
     * Private constructor. Use Factory inner class instead.
     *
     * @see Factory
     * @param message address
     * @param load parameter
     * @param responseLength
     */
    private I2CMessage(short message, int load, int responseLength) {
        this.message = message;
        this.writeLoad = load;

        if(responseLength > 0) {
            read = new byte[responseLength];
        }
    }

    /**
     * Private constructor. Use Factory inner class instead.
     *
     * @see Factory
     * @param message address
     */
    private I2CMessage(short message) {
        this(message, -1, -1);
    }

    /**
     * Execute i2c read/write operation.
     * If command has a response, data is saved into read buffer
     * and crc8 tested.
     *
     * @param i2cDevice
     * @throws IOException
     */
    void exec(I2CDevice i2cDevice) throws IOException {

        //build cmd
        write[0] = (byte) (message >> 8);
        write[1] = (byte) message;

        //write request as soft reset, set measurement interval and so on...
        if(read == null) {

            //if request has parameter
            if(writeLoad>=0) {
                writeLen = 5;
                //System.out.println("write load = "+writeLoad);
                write[2] = (byte) (writeLoad >> 8);
                write[3] = (byte) (writeLoad);
                write[4] = (byte) crc8.calc(write, 2, 2);
            }

            //System.out.println("Write 1: "+Mode.getHumanMessage(write));
            i2cDevice.write(write, 0, writeLen);
        }
        //reading something
        else {
            //System.out.println("Write 2: "+Mode.getHumanMessage(write));
            i2cDevice.read(write, 0, writeLen, read, 0, read.length);
            //System.out.println("READ: "+Mode.getHumanMessage(read));
            crcCheck(read);
        }
    }


    /**
     * Return next short from read buffer.
     * @return If no next short, -1 is returned
     */
    short getNextShort() {
        return getShortResult(read);
    }

    /**
     * Test byte buffer if every third byte is crc8 of previous two.
     *
     * @param buffer input buffer
     * @return true if a crc is correct through whole buffer otherwise false
     * @throws ScdException when crc8 is not correct!
     */
    private void crcCheck(byte[] buffer) {

        //System.out.println("crcCheck: "+buffer.length);

        for (int i = 0; i < buffer.length; i+=3) {
            if(!(crc8.calc(buffer,i,2) == (buffer[i+2] & 0xFF))) {

                String input = Mode.getHumanMessage(buffer, i, 2);
                String expected = Integer.toHexString((int) crc8.calc(buffer,i,2));
                String was = Integer.toHexString(buffer[i+2] & 0xFF);

                throw new ScdException("CRC is not correct! Data: ["+input+"]. Expected crc8: "+expected+", Was crc8:"+was);
            }
        }
    }

    /**
     * Join buffer into short.
     *
     * @param buffer
     * @return short value
     */
    private short getShortResult(byte[] buffer) {

        if (resultIndex < buffer.length) {
            int r = (buffer[resultIndex] & 0xFF) << 8;
            r = r | (buffer[resultIndex+1] & 0xFF);
            resultIndex+=3;

            return (short) r;
        }

        return -1;
    }


    /**
     * Factory message builder. Use this instead constructor.
     */
    static class Factory {

        static I2CMessage writeMessage(short message, int load) {
            return new I2CMessage(message, load, -1);
        }

        static I2CMessage writeMessage(short message) {
            return new I2CMessage(message);
        }

        static I2CMessage readMessage(short message) {
            return new I2CMessage(message, -1, 3);
        }

        static I2CMessage readDataMessage(short message) {
            return new I2CMessage(message, -1, 18);
        }
    }

}
