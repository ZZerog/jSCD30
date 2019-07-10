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

        System.out.println("SCD30 firmware version: "+scd30.getFirmwareVersion());

        scd30.start();
        scd30.setMeasurementInterval(2);

        int interval = scd30.getMeasurementInterval()*1000;
        System.out.println("interval: "+interval+"ms \n~~~");

        for (int i = 0; i < 5; i++) {
            Thread.sleep(interval+100);
            System.out.println("CO2: " + scd30.getCO2()+" ppm");
            System.out.println("Temperature: " + scd30.getTemperature()+" °C");
            System.out.println("Humidity: " + scd30.getHumidity()+" %\n~~~");
        }

        //see javadoc
        //A longer running sensor return batter result
        //scd30.stop();
    }
}
