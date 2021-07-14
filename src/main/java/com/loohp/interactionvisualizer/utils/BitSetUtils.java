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
