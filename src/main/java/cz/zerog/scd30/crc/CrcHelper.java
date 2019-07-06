package cz.zerog.scd30.crc;

/**
 * Created by anthony on 13.05.2017.
 * More at: https://github.com/meetanthony/crcjava
 * The MIT License (MIT) Copyright (c) 2017 Anton Isakov http://crccalc.com
 */
public class CrcHelper {

    static long ReverseBits(long ul, int valueLength)
    {
        long newValue = 0;

        for (int i = valueLength - 1; i >= 0; i--)
        {
            newValue |= (ul & 1) << i;
            ul >>= 1;
        }

        return newValue;
    }
}
