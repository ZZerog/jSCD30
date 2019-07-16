package cz.zerog.scd30.examples;

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

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import cz.zerog.scd30.SCD30;
import cz.zerog.scd30.i2cbus.I2CMode;

import java.io.IOException;

/**
 * The simplest examples of using this lib.
 */
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
