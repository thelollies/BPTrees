
public class StringIntPair {
	public final String key;
	public final int value;

	public StringIntPair(String key, int value) {
		this.key = key;
		this.value = value;
	}

	public byte[] getBytes(){
		byte[] bytes = new byte[64];

		byte[] stringBytes = key.getBytes();
		for(int i = 0; i < Math.min(stringBytes.length, 60); i++)
			bytes[i] = stringBytes[i];


		byte[] intBytes = Bytes.intToBytes(value);
		for(int i = 60; i < 64; i++)
			bytes[i] = intBytes[i-60];

		return bytes;
	}

	public static StringIntPair fromBytes(byte[] bytes){

		byte[] stringBytes = new byte[60];
		for(int i = 0; i < 60; i++)
			stringBytes[i] = bytes[i];

		return new StringIntPair(new String(stringBytes).trim(), Bytes.bytesToInt(bytes, 60));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(key);
		sb.append("\t");
		sb.append(DNSDB.IPToString(value));
		return sb.toString();
	}
}
