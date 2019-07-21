# jSCD30

<img align="right" width="300" height="200" src="https://cz.mouser.com/images/marketingid/2018/df/187534792_Sensirion_SCD30SensorModule.jpg">Simple java driver for the **SCD30** CO2 (temparature and humidity) sensor. The SCD30 is tested on **RaspberryPi 3B+** and uses the I2C BUS.

More at https://zzerog.github.io/jSCD30/.

### TODO LIST
- [ ] Code completion
- [ ] Add logging fasade (as SLF4J)
- [ ] Maven page (mvn page)
- [x] Unit testing
- [x] Create examples


### Usage
```java
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

```
more at example package

### Dependencies
* www.pi4j.com

### Resources
* More about sensor: https://cz.mouser.com/new/sensirion/sensirion-scd30/ 

### License
This project is licensed under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details
