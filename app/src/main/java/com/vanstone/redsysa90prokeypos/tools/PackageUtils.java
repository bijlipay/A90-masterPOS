package com.vanstone.redsysa90prokeypos.tools;

public class PackageUtils {

	public static int addStart(byte[] data) {
		data[0] = 0x02;
		return 1;
	}

	public static int addEnd(byte[] data, int index) {
		data[index] = 0x03;
		return 1;
	}

	public static int addLen(byte[] data, int startIndex, int length) {
		data[startIndex] = (byte) (length / 256);
		data[startIndex + 1] = (byte) (length % 256);
		return 2;
	}
	
}
