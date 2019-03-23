package utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ArrayMethods {
	
	/**
	 * Copies n bytes from the buffer, starting from its current position,
	 * to a new array that is returned
	 * @param buffer : source of the bytes
	 * @param n : number of bytes copied
	 * @return resulting array
	 */
	public static byte[] trimmedArray(ByteBuffer buffer, int n) {
		byte[] array = new byte[n];
		buffer.rewind();
		buffer.get(array);
		return array;		
	}

	/**
	 * Splits the array in 256-bytes segments
	 * @param array : a byte[]
	 * @return an list of byte arrays, each shorter or equal to 256 bytes.
	 * Their content is that of the parameter
	 */
	public static List<byte[]> split(byte[] array) {
		// TODO prettier split-up : it is currently very abrupt (in the middle of a word)
		List<byte[]> result = new ArrayList<byte[]>();
		int current = 0;
		byte[] segment = null;
		
		while (current < array.length) {
			int index = current % 256;
			// each time we reach 256, we create a new segment
			if (index == 0) {
				// we try to make the segment as small as possible
				int bytesLeft = Math.min(array.length - current, 256); 
				segment = new byte[bytesLeft];
				result.add(segment);
			}
			// we copy byte by byte
			segment[index] = array[current];
			current++;
		}
		return result;
	}
}
