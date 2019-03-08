package utils;

import java.nio.ByteBuffer;

public class BufferMethods {
	
	/**
	 * Copies n bytes from the buffer, starting from its current position, to a new array that is returned
	 * @param buffer : source of the bytes
	 * @param n : number of bytes copied
	 * @return resulting array
	 */
	public static byte[] trimArray(ByteBuffer buffer, int n) {
		byte[] array = new byte[n];
		for (int i = 0; i < n ; i++) {
			array[i] = buffer.get();
		}
		return array;		
	}
}
