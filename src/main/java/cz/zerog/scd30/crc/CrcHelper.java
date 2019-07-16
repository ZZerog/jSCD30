package cz.zerog.scd30.crc;

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
