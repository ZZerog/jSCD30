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
import cz.zerog.scd30.Event;
import cz.zerog.scd30.SCD30;
import cz.zerog.scd30.SCD30EventListener;
import cz.zerog.scd30.i2cbus.I2CMode;

import java.io.IOException;
import java.util.Scanner;

public class ByEvent {


    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException {

        //init with I2C_BUS. RPi3 B+ => BUS_1;
        SCD30 scd30 = new SCD30(new I2CMode(I2CBus.BUS_1));

        scd30.setEventListener(event -> {
            switch (event.getType()) {

                case CO2:
                    System.out.println("CO2: " + event.getValue() + " ppm");
                    break;

                case TEMP:
                    System.out.println("Temperature: " + event.getValue() + " °C");
                    break;

                case HUMID:
                    System.out.println("Humidity: " + event.getValue() + " %");
                    break;
            }
        });

        System.out.println("SCD30 firmware version: "+scd30.getFirmwareVersion());

        scd30.start();

        scd30.setMeasurementInterval(2);
        scd30.setCo2Threshold(10.0f);

        System.out.println("Enter for exit");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
