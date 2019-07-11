package cz.zerog.scd30.examples;

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
