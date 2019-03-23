package utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Messages {
	public static final int bufferSize = 256;
	private static final byte sentinel = -1;
	private static final int additionalBytes = 4;


	//////////// RECEPTION ////////////
	/**
	 * Copies n bytes read from the buffer, starting from its current position,
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
	 * Returns the name and content of the message into a Map. <br>
	 * If the message is unsigned, name = null.
	 * @param message : to decipher
	 * @return (possible) name and content
	 */
	public static Map<String, String> decodeMessage(byte[] message) {
		Map<String, String> map = new HashMap<String, String>();
		ByteBuffer buffer = ByteBuffer.wrap(message);
		String name;
		String content;
		
		// TODO java.nio.BufferUnderflowException when long message
		if (isSigned(message)) {
			// sentinel
			buffer.get();
			// name length
			int n = buffer.get();
			// name
			byte[] byteName = new byte[n];
			buffer.get(byteName, 0, n);
			name = new String(byteName);
			// content length
			int m = buffer.getShort();
			// content
			byte[] byteContent = new byte[m];
			buffer.get(byteContent, 0, m);
			content = new String(byteContent);
		}
		else {
			// whole message = content
			name = null;
			content = new String(buffer.array());
		}
		
		map.put("name", name);
		map.put("content", content);
		return map;
	}
	
	/**
	 * Returns true if the message contains a signature <br>
	 * (if the first byte of the message == sentinel)
	 * @param message : to be checked
	 */
	public static boolean isSigned(byte[] message) {
		return message[0] == sentinel;
	}

	/**
	 * Converts the message into a human-readable one.
	 */
	public static String toString(byte[] message) {
		String string = "";
		String name = null;
		String content;
		
		Map<String, String> list = decodeMessage(message);
		name = list.get("name");
		content = list.get("content");
		
		// formatting output
		if (name != null) {
			string += name + " : ";
		}
		string += content;
		
		return string;
	}

	/**
	 * Returns the content of the message in byteArray
	 * @param byteArray
	 */
	public static String getContent(byte[] byteArray) {
		return decodeMessage(byteArray).get("content");
	}

	//////////// SENDING ////////////
	/**
	 * Splits the array in 256-bytes segments, to be sent via the channel
	 * @param array : a byte[]
	 * @return an list of byte arrays, each shorter or equal to 256 bytes.
	 * Their content is that of the parameter
	 */
	public static List<byte[]> split(byte[] array) {
		List<byte[]> result = new ArrayList<byte[]>();
		int current = 0;
		byte[] segment = null;

		while (current < array.length) {
			int index = current % bufferSize;
			// each time we reach 256, we create a new segment
			if (index == 0) {
				// we try to make the segment as small as possible
				int bytesLeft = Math.min(array.length - current, bufferSize); 
				segment = new byte[bytesLeft];
				result.add(segment);
			}
			// we copy byte by byte
			segment[index] = array[current];
			current++;
		}
		return result;
	}

	/**
	 * Returns a signed version of the given message, in a separate array.
	 * A signed message structure is as follows :
	 * <br>- <strong>sentinel</strong> (1 byte)
	 * <br>- <strong>name length</strong> = n (1 byte)
	 * <br>- <strong>name</strong> (n bytes)
	 * <br>- <strong>content length</strong> = m (2 bytes)
	 * <br>- <strong>content</strong> (m bytes)
	 * @param name of the sender
	 * @param message to be signed
	 * @return the signed message
	 */
	public static byte[] signMessage(String name, byte[] message) {
		int newLength = message.length + name.length() + additionalBytes;
		// ByteBuffer for easier writing
		byte[] signedMessage = new byte[newLength];
		ByteBuffer buffer = ByteBuffer.wrap(signedMessage);
		// add sentinel first
		buffer.put(sentinel);
		// add name length
		buffer.put((byte) name.length());
		// copy name
		buffer.put(name.getBytes());
		// add content length
		buffer.putShort((short)message.length);
		// copy message
		buffer.put(message);
		
		return signedMessage;
	}

	

}
