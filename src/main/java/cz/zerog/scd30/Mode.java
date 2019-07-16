package cz.zerog.scd30;

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

public interface Mode {

    void setInterval(int interval);

    int getInterval();

    boolean isDataReady();

    float[] getMeasurement();

    String getFirmwareVersion();

    void start(int pressureCompensation);

    void stop();

    void selfCalibration(boolean active);

    boolean isSelfCalibration();

    void setRecalibrationValue(int value);

    void setTemperatureOffset(int offset);

    int getTemperatureOffset();

    void setAltitudeCompensation(int altitudeLevel)  ;

    int getAltitudeCompensation();

    void softReset();

    static String getHumanMessage(byte[] bytes) {
        return getHumanMessage(bytes, 0, bytes.length);
    }

    static String getHumanMessage(byte[] bytes, int offset, int length) {

        if(bytes==null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = offset; i < Math.min(bytes.length, length); i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF).toUpperCase().trim();
            sb.append(new String(new char[2 - hex.length()]).replace('\0', '0'))
                    .append(hex)
                    .append(" ");
        }

        if (sb.length() > 0) {
            return (sb.subSequence(0, sb.length() - 1)).toString();
        }

        return sb.append(" (").append(bytes.length).append(")").toString();
    }
}
