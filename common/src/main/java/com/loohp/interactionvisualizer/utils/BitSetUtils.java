/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.utils;

import java.math.BigInteger;
import java.util.BitSet;

public class BitSetUtils {

    public static String toNumberString(BitSet bitSet) {
        if (bitSet.isEmpty()) {
            return "0";
        } else {
            return new BigInteger(1, ArrayUtils.reverse(bitSet.toByteArray())).toString();
        }
    }

    public static BitSet fromNumberString(String binaryString) {
        if (binaryString.isEmpty()) {
            return new BitSet();
        } else {
            BigInteger bi = new BigInteger(binaryString);
            if (bi.signum() < 0) {
                return new BitSet();
            } else {
                return BitSet.valueOf(ArrayUtils.reverse(bi.toByteArray()));
            }
        }
    }

}
