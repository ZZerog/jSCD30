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
 * Created by anthony on 11.05.2017.
 * More at: https://github.com/meetanthony/crcjava
 * The MIT License (MIT) Copyright (c) 2017 Anton Isakov http://crccalc.com
 */
public class CrcCalculator {

    public AlgoParams Parameters;
    public byte HashSize = 8;
    private long _mask = 0xFFFFFFFFFFFFFFFFL;
    private long[] _table = new long[256];

    public static final byte[] TestBytes = new byte[]{49,50,51,52,53,54,55,56,57};

    public CrcCalculator(AlgoParams params)
    {
        Parameters = params;

        HashSize = (byte) params.HashSize;
        if (HashSize < 64)
        {
            _mask = (1L << HashSize) - 1;
        }

        CreateTable();
    }

    public long calc(byte[] data, int offset, int length)
    {
        long init = Parameters.RefOut ? CrcHelper.ReverseBits(Parameters.Init, HashSize) : Parameters.Init;
        long hash = ComputeCrc(init, data, offset, length);
        return (hash ^ Parameters.XorOut) & _mask;
    }

    private long ComputeCrc(long init, byte[] data, int offset, int length)
    {
        long crc = init;

        if (Parameters.RefOut)
        {
            for (int i = offset; i < offset + length; i++)
            {
                crc = (_table[(int)((crc ^ data[i]) & 0xFF)] ^ (crc >>> 8));
                crc &= _mask;
            }
        }
        else
        {
            int toRight = (HashSize - 8);
            toRight = toRight < 0 ? 0 : toRight;
            for (int i = offset; i < offset + length; i++)
            {
                crc = (_table[(int)(((crc >> toRight) ^ data[i]) & 0xFF)] ^ (crc << 8));
                crc &= _mask;
            }
        }

        return crc;
    }

    private void CreateTable()
    {
        for (int i = 0; i < _table.length; i++)
            _table[i] = CreateTableEntry(i);
    }

    private long CreateTableEntry(int index)
    {
        long r = (long)index;

        if (Parameters.RefIn)
            r = CrcHelper.ReverseBits(r, HashSize);
        else if (HashSize > 8)
            r <<= (HashSize - 8);

        long lastBit = (1L << (HashSize - 1));

        for (int i = 0; i < 8; i++)
        {
            if ((r & lastBit) != 0)
                r = ((r << 1) ^ Parameters.Poly);
            else
                r <<= 1;
        }

        if (Parameters.RefOut)
            r = CrcHelper.ReverseBits(r, HashSize);

        return r & _mask;
    }
}
