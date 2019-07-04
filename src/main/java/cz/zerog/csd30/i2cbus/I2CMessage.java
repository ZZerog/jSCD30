package cz.zerog.csd30.i2cbus;

import com.pi4j.io.i2c.I2CDevice;
import cz.zerog.csd30.CsdException;
import cz.zerog.csd30.Mode;
import cz.zerog.csd30.crc.AlgoParams;
import cz.zerog.csd30.crc.CrcCalculator;

import java.io.IOException;

public class I2CMessage {

    private CrcCalculator crc8 = new CrcCalculator(new AlgoParams("CRC-8", 8, 0x31, 0xFF, false, false, 0x00, 0xF4));

    private byte[] write = new byte[5];
    private int writeLen = 2;

    private byte read[] = null;

    private int writeLoad;
    private short message;

    private int resultIndex = 0;


    private I2CMessage(short message, int load, int responseLength) {
        this.message = message;
        this.writeLoad = load;

        if(responseLength > 0) {
            read = new byte[responseLength];
        }
    }

    private I2CMessage(short message) {
        this(message, -1, -1);
    }

    void exec(I2CDevice i2cDevice) throws IOException {

        //build cmd
        write[0] = (byte) (message >> 8);
        write[1] = (byte) message;

        //write request as soft reset, set measurement interval and so on...
        if(read == null) {

            //if request has parameter
            if(writeLoad>=0) {
                writeLen = 5;
                System.out.println("writeload = "+writeLoad);
                write[2] = (byte) (writeLoad >> 8);
                write[3] = (byte) (writeLoad);
                write[4] = (byte) crc8.calc(write, 2, 2);
            }

            System.out.println("Write 1: "+Mode.getHumanMessage(write));
            i2cDevice.write(write, 0, writeLen);
        }
        //reading something
        else {
            System.out.println("Write 2: "+Mode.getHumanMessage(write));
            i2cDevice.read(write, 0, writeLen, read, 0, read.length);
            System.out.println("READ: "+Mode.getHumanMessage(read));
            crcCheck(read);
        }
    }

    private boolean crcCheck(byte[] buffer) {

        System.out.println("crcCheck: "+buffer.length);

        for (int i = 0; i < buffer.length; i+=3) {
            if(!(crc8.calc(buffer,i,2) == (buffer[i+2] & 0xFF))) {

                String input = Mode.getHumanMessage(buffer, i, 2);
                String expected = Integer.toHexString((int) crc8.calc(buffer,i,2));
                String was = Integer.toHexString(buffer[i+2] & 0xFF);

                throw new CsdException("CRC is not correct! Data: "+input+". Expected: "+expected+", Was:"+was);
            }
        }
        return true;
    }

    private short getShortResult(byte[] buffer) {

        if (resultIndex < buffer.length) {
            int r = (buffer[resultIndex] & 0xFF) << 8;
            r = r | (buffer[resultIndex+1] & 0xFF);
            resultIndex+=3;

            return (short) r;
        }

        return -1;
    }


    short getShort() {
        return getShortResult(read);
    }

    boolean isCorrect() {
        return true;
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

        public static I2CMessage readMessage(short message) {
            return new I2CMessage(message, -1, 3);
        }

        public static I2CMessage readDataMessage(short message) {
            return new I2CMessage(message, -1, 18);
        }
    }

    public static void main(String[] args) {
        short message = (short) 0x4600;
        int writeLoad =  0x0003;

        CrcCalculator crc8 = new CrcCalculator(new AlgoParams("CRC-8", 8, 0x31, 0xFF, false, false, 0x00, 0xF4));

        byte[] write = new byte[5];

        //build cmd
        write[0] = (byte) (message >> 8);
        write[1] = (byte) message;

        write[2] = (byte) (writeLoad >> 8);
        write[3] = (byte) (writeLoad);
        write[4] = (byte) crc8.calc(write, 2, 2);

        System.out.println(Integer.toHexString(write[0] & 0xFF));
        System.out.println(Integer.toHexString(write[1] & 0xFF));
        System.out.println(Integer.toHexString(write[2] & 0xFF));
        System.out.println(Integer.toHexString(write[3] & 0xFF));
        System.out.println(Integer.toHexString(write[4] & 0xFF));
    }

}
