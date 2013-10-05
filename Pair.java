import java.util.HashMap;


public class Pair {
	public final String str;
	public final int intgr;
	//private static HashMap<Integer, Pair> existingPairs = new HashMap<Integer, Pair>();
	boolean hasValue;

	private Pair(String key, int value) {
		this.str = key;
		this.intgr = value;
		//existingPairs.put(value, this);
	}

	public static void clear(){
		//existingPairs = new HashMap<Integer, Pair>();
	}
	
	public static Pair create(String str, int intgr){
		return new Pair(str, intgr);
		/*Pair existing = existingPairs.get(intgr);
		
		return existing != null && str != null && intgr != -Integer.MAX_VALUE ? existing : new Pair(str, intgr);*/
	}

	public byte[] getBytes(){
		byte[] bytes = new byte[64];

		byte[] stringBytes = str.getBytes();
		for(int i = 0; i < Math.min(stringBytes.length, 60); i++)
			bytes[i] = stringBytes[i];


		byte[] intBytes = Bytes.intToBytes(intgr);
		for(int i = 60; i < 64; i++)
			bytes[i] = intBytes[i-60];

		return bytes;
	}

	public static Pair fromBytes(byte[] bytes){

		byte[] stringBytes = new byte[60];
		for(int i = 0; i < 60; i++)
			stringBytes[i] = bytes[i];

		return Pair.create(new String(stringBytes).trim(), Bytes.bytesToInt(bytes, 60));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(str);
		sb.append("\t");
		sb.append(DNSDB.IPToString(intgr));
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Pair){
			Pair pair = (Pair)obj;
			if(str == null || pair.str == null)
				return this.intgr == pair.intgr && str == null && pair.str == null;
			return this.intgr == pair.intgr && this.str.equals(pair.str);
		}
		else
			return false;
	}
	
	@Override
	public int hashCode() {       
	    int strHash = str == null ? 0 : str.hashCode();
	    return strHash + powerOf52(intgr, 1);
	}

	public static int powerOf52(int result, int power) {
	    for (int i = 0; i < power; i++) {
	        result *= 52;
	    }
	    return result;
	}
}
