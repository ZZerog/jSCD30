package cz.zerog.scd30.i2cbus;

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

import com.pi4j.io.i2c.I2CDevice;
import cz.zerog.scd30.ScdException;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

public class I2CMessageTest {

    /**
     * From mock device is read [0x01,0x02]. CRC is correct.
     * @throws Exception
     */
    @Test
    public void readTest1() throws Exception {

        //mock i2c device
        I2CDevice mockDevice = new MockDevice() {
            @Override
            public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize) throws IOException {
                readBuffer[0] = 0x01;
                readBuffer[1] = 0x02;
                readBuffer[2] = 0x17;

                return 3;
            }
        };

        //message executing
        I2CMessage message = I2CMessage.Factory.readMessage((short) 0xCAFE);
        message.exec(mockDevice);
        assertEquals(0x0102, message.getNextShort());
    }

    /**
     * Test if a exception is throw when CRC is incorrect
     * @throws Exception
     */
    @Test(expected = ScdException.class)
    public void readTestCRC8() throws Exception {

        I2CDevice mockDevice = new MockDevice() {
            @Override
            public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize) throws IOException {
                readBuffer[0] = 0x01;
                readBuffer[1] = 0x02;
                readBuffer[2] = 0x00;

                return 3;
            }
        };

        I2CMessage message = I2CMessage.Factory.readMessage((short) 0xCAFE);
        message.exec(mockDevice);
        assertEquals(0x0102, message.getNextShort());
    }

    /**
     * From mock device is read 6x short and then return -1.
     * Crc8 is always correct
     * @throws Exception
     */
    @Test
    public void readMeasurementTest() throws Exception {

        I2CDevice mockDevice = new MockDevice() {
            @Override
            public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize) throws IOException {

                int i = 0;
                //short 1
                readBuffer[i++] = 0x01;
                readBuffer[i++] = 0x02;
                readBuffer[i++] = 0x17; //crc

                //short 2
                readBuffer[i++] = (byte) 0xCA;
                readBuffer[i++] = (byte) 0xFE;
                readBuffer[i++] = 0x58;  //crc

                //short 3
                readBuffer[i++] = (byte) 0xFF;
                readBuffer[i++] = (byte) 0xFF;
                readBuffer[i++] = (byte) 0xAC;  //crc

                //short 4
                readBuffer[i++] = 0x01;
                readBuffer[i++] = 0x02;
                readBuffer[i++] = 0x17;  //crc

                //short 5
                readBuffer[i++] = (byte) 0xCA;
                readBuffer[i++] = (byte) 0xFE;
                readBuffer[i++] = 0x58; //crc

                //short 6
                readBuffer[i++] = (byte) 0xFF;
                readBuffer[i++] = (byte) 0xFF;
                readBuffer[i++] = (byte) 0xAC;  //crc

                return 18;
            }
        };

        I2CMessage message = I2CMessage.Factory.readDataMessage((short) 0xCAFE);
        message.exec(mockDevice);

        assertEquals((short)0x0102, message.getNextShort());
        assertEquals((short)0xcafe, message.getNextShort());
        assertEquals((short)0xFFFF, message.getNextShort());
        assertEquals((short)0x0102, message.getNextShort());
        assertEquals((short)0xCAFE, message.getNextShort());
        assertEquals((short)0xFFFF, message.getNextShort());
        assertEquals(-1, message.getNextShort());
        assertEquals(-1, message.getNextShort());
        assertEquals(-1, message.getNextShort());
    }

    /**
     * Test write message address
     * @throws Exception
     */
    @Test
    public void writeTest() throws Exception {
        I2CDevice mockDevice = new MockDevice() {

            @Override
            public void write(byte[] buffer, int offset, int size) throws IOException {
                assertArrayEquals(new byte[]{(byte) 0xca, (byte) 0xfe}, Arrays.copyOfRange(buffer, offset, size));
            }
        };

        I2CMessage message = I2CMessage.Factory.writeMessage((short) 0xCAFE);
        message.exec(mockDevice);
    }

    /**
     * Test write payload
     * @throws Exception
     */
    @Test
    public void writeWithMessageTest() throws Exception {
        I2CDevice mockDevice = new MockDevice() {

            @Override
            public void write(byte[] buffer, int offset, int size) throws IOException {
                assertArrayEquals(new byte[]{(byte) 0xca, (byte) 0xfe, (byte) 0xca, (byte) 0xfe, (byte) 0x58},
                        Arrays.copyOfRange(buffer, offset, size));
            }
        };

        I2CMessage message = I2CMessage.Factory.writeMessage((short) 0xCAFE, 0xCAFE);
        message.exec(mockDevice);
    }

    abstract class MockDevice implements I2CDevice {

        @Override
        public int getAddress() {
            return 0;
        }

        @Override
        public void write(byte b) throws IOException {

        }

        @Override
        public void write(byte[] buffer, int offset, int size) throws IOException {

        }

        @Override
        public void write(byte[] buffer) throws IOException {

        }

        @Override
        public void write(int address, byte b) throws IOException {

        }

        @Override
        public void write(int address, byte[] buffer, int offset, int size) throws IOException {

        }

        @Override
        public void write(int address, byte[] buffer) throws IOException {

        }

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public int read(byte[] buffer, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public int read(int address) throws IOException {
            return 0;
        }

        @Override
        public int read(int address, byte[] buffer, int offset, int size) throws IOException {
            return 0;
        }

        @Override
        public void ioctl(long command, int value) throws IOException {

        }

        @Override
        public void ioctl(long command, ByteBuffer data, IntBuffer offsets) throws IOException {

        }

        @Override
        public int read(byte[] writeBuffer, int writeOffset, int writeSize, byte[] readBuffer, int readOffset, int readSize) throws IOException {
            return 0;
        }
    }
}