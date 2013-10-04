
public class IntStringPair {
	public final int key;
	public final String value;

	public IntStringPair(int key, String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return "[" + DNSDB.IPToString(key) + " → " + value + "]";
	}
	
	public byte[] getBytes(){
		byte[] bytes = new byte[64];

		byte[] intBytes = Bytes.intToBytes(key);
		for(int i = 0; i < 4; i++)
			bytes[i] = intBytes[i];
		
		byte[] stringBytes = value.getBytes();
		for(int i = 4; i < 4 + Math.min(stringBytes.length, 60); i++)
			bytes[i] = stringBytes[i - 4];

		return bytes;
	}

	public static IntStringPair fromBytes(byte[] bytes){

		byte[] stringBytes = new byte[60];
		for(int i = 4; i < 64; i++)
			stringBytes[i - 4] = bytes[i];

		return new IntStringPair(Bytes.bytesToInt(bytes, 0), new String(stringBytes).trim());
	}
}
