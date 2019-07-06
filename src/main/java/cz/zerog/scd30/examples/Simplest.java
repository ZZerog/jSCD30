package cz.zerog.scd30.examples;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import cz.zerog.scd30.SCD30;
import cz.zerog.scd30.i2cbus.I2CMode;

import java.io.IOException;

public class Simplest {

    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException {

        //init with I2C_BUS. RPi3 B+ => BUS_1;
        SCD30 scd30 = new SCD30(new I2CMode(I2CBus.BUS_1));

        System.out.println("Firmware: "+scd30.getFirmwareVersion());

        scd30.start();
        for (int i = 0; i < 3; i++) {
            Thread.sleep(scd30.getMeasurementInterval());
            System.out.println("CO2: " + scd30.getCO2()+" ppm");
            System.out.println("Temperature: " + scd30.getTemperature()+" °C");
            System.out.println("Humidity: " + scd30.getHumidity()+" %");
            System.out.println("~~~");
        }
        scd30.stop();
    }
}
