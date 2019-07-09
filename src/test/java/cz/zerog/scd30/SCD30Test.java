package cz.zerog.scd30;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SCD30Test {

    @Test
    public void eventTest() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        doNothing().when(mockMode).start(anyInt());
        when(mockMode.getInterval()).thenReturn(1000);
        doNothing().when(mockMode).setInterval(anyInt());
        when(mockMode.isDataReady()).thenReturn(true);
        when(mockMode.getMeasurement()).thenReturn(new float[]{(float) 3.21, (float) 12.3, (float) 123.0});


        //testing code
        SCD30 scd30 = new SCD30(mockMode);

        scd30.setEventListener(event -> {
            if(event.getType()==Event.Type.CO2) {
                assertEquals(event.getValue(), 3.21, 0.001);
            }
        });

        scd30.setMeasurementInterval(1000);
        scd30.start();
        Thread.sleep(1200);
        scd30.stop();

    }


    @Test
    public void getCO2TempHumid() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        when(mockMode.getMeasurement()).thenReturn(new float[]{(float) 1.23, (float) 12.3, (float) 123.0});

        //go
        SCD30 scd30 = new SCD30(mockMode);
        scd30.measurement();

        //conclusion
        assertEquals((float)1.23, scd30.getCO2(), 0);
        assertEquals((float)12.3, scd30.getTemperature(), 0);
        assertEquals((float)123.0, scd30.getHumidity(), 0);

    }

    @Test
    public void start() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        doNothing().when(mockMode).start(anyInt());
        when(mockMode.getInterval()).thenReturn(1000);
        doNothing().when(mockMode).setInterval(anyInt());
        when(mockMode.isDataReady()).thenReturn(true);
        when(mockMode.getMeasurement()).thenReturn(new float[]{(float) 1.23, (float) 12.3, (float) 123.0});


        //testing code
        SCD30 scd30 = new SCD30(mockMode);
        scd30.start();
        Thread.sleep(2300);
        scd30.stop();

        //test
        verify(mockMode, times(2)).getMeasurement();
    }

    @Test
    public void setPressureCompensation() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        doNothing().when(mockMode).start(anyInt());
        doNothing().when(mockMode).stop();

        //testing code
        SCD30 scd30 = new SCD30(mockMode);
        scd30.setPressureCompensation(1000);
        scd30.start();
        Thread.sleep(200);
        scd30.stop();

        verify(mockMode).start(1000);
    }


    /**
     * Set specifics interval but scd30 get internally other value
     * @throws Exception
     */
    @Test(expected = ScdException.class)
    public void setMeasurementIntervalButSensorGetOther() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        doNothing().when(mockMode).setInterval(anyInt());
        when(mockMode.getInterval()).thenReturn(50);

        new SCD30(mockMode).setMeasurementInterval(6);
    }

    @Test
    public void setMeasurementInterval() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        doNothing().when(mockMode).setInterval(anyInt());
        when(mockMode.getInterval()).thenReturn(6);

        new SCD30(mockMode).setMeasurementInterval(6);
        verify(mockMode, times(1)).setInterval(6);
    }

    /**
     * Test if getMeasInterval alive single IO Error
     * @throws Exception
     */
    @Test
    public void getMeasurementIntervalWithIOError() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        when(mockMode.getInterval()).thenAnswer(new Answer() {
            private int callCount = 0;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                callCount++;

                if(callCount==1) {
                    throw new ScdException("IO Error");
                }

                return 13;
            }
        });

        //conclusion
        assertEquals(13, new SCD30(mockMode).getMeasurementInterval());

    }

    @Test
    public void getMeasurementInterval() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        when(mockMode.getInterval()).thenReturn(13);

        //conclusion
        assertEquals(13, new SCD30(mockMode).getMeasurementInterval());

    }

    @Test
    public void getFirmwareVersion() throws Exception {

        //prepare mock Mode
        Mode mockMode = mock(Mode.class);
        when(mockMode.getFirmwareVersion()).thenReturn("caca.fefe");

        //conclusion
        assertEquals("caca.fefe", new SCD30(mockMode).getFirmwareVersion());
    }
}