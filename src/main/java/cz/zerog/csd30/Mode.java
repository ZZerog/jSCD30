package cz.zerog.csd30;

import java.io.IOException;

public interface Mode {

    void setInterval(int interval) throws CsdException;

    int getInterval() throws CsdException;

    boolean isDataReady();

    float[] getMeasurement();

    String getFirmwareVersion();

    void start();

    void stop();

    static String getHumanMessage(byte[] bytes) {

        if(bytes==null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
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

    static String getHumanMessage(byte[] buffer, int offset, int length) {
        return null;
    }
}
