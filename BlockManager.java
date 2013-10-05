import java.io.IOException;


public class BlockManager {
	private int index = 0;
	private Block block;
	private BlockFile file;
	
	public BlockManager(BlockFile file) {
		block = new Block(file.blockSize);
		this.file = file;
	}
	
	public RecordLocation write(byte[] bytes) throws IOException{
		if(index + bytes.length >= file.blockSize){
			// Create new block, write it and reset the index
			Block temp = block;
			block = new Block(file.blockSize);
			index = 0;
			return new RecordLocation(file.write(temp.getBytes()), index);
		}
		else{
			block.setBytes(bytes, index);
			index += block.length();
			return new RecordLocation(file.getSize(), index);
		}
	}
}
