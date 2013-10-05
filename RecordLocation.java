
public class RecordLocation {
	public final int fileIndex;
	public final int blockIndex;
	
	public RecordLocation(int fileIndex, int blockIndex){
		this.fileIndex = fileIndex;
		this.blockIndex = blockIndex;
	}
	
	public byte[] getBytes(){
		byte[] bytes = new byte[8];
		Bytes.intToBytes(fileIndex, bytes, 0);
		Bytes.intToBytes(blockIndex, bytes, 4);
		return bytes;
	}
	
	@Override
	public int hashCode() {
		throw new UnsupportedOperationException();
	}

	public static RecordLocation fromBytes(byte[] bytes) {
		if(bytes.length != 8) throw new BTreeException("RecordLocation fromBytes() requires 8 bytes");
		return new RecordLocation(Bytes.bytesToInt(bytes, 0), Bytes.bytesToInt(bytes, 4));
	}
}
