import java.io.IOException;
import java.util.HashMap;


public class TreeIO {

	private final BlockFile file;

	public TreeIO(BlockFile file){
		this.file = file;
	}

	public void readIntoTree(BPlusTreeIntToString60 intStrTree, BPlusTreeString60toInt strIntTree){
		// read the header to determine tree header locations (strint at 0, intstr at 4)
		try{
			HashMap<RecordLocation, StringToIntNode> blockToStrNode = new HashMap<RecordLocation, StringToIntNode>();
			HashMap<RecordLocation, IntToStringNode> blockToIntNode = new HashMap<RecordLocation, IntToStringNode>();


			byte[] metadata = file.read(0);
			Block strIntHeader = new Block(file.read(Bytes.bytesToInt(metadata, 0)));
			Block intStrHeader = new Block(file.read(Bytes.bytesToInt(metadata, 4)));

			int strMaxSize = Bytes.byteToInt(strIntHeader.getByte(1));
			int intMaxSize = Bytes.byteToInt(intStrHeader.getByte(1));

			RecordLocation strRootBlock = RecordLocation.fromBytes(strIntHeader.getBytes(2, 8));
			RecordLocation intRootBlock = RecordLocation.fromBytes(intStrHeader.getBytes(2, 8));

			// TODO readStrNode(strMaxSize, strRootBlock, blockToStrNode);
			readIntNode(intMaxSize, intRootBlock, blockToIntNode);

			strIntTree =  BPlusTreeString60toInt.fromBytes(strIntHeader, blockToStrNode);
			//intStrTree =  BPlusTreeIntToString60.fromBytes(intStrHeader, blockToIntNode);

		}catch(Exception e){e.printStackTrace();}
	}

	private void readIntNode(int maxSize, RecordLocation record,
			HashMap<RecordLocation, IntToStringNode> blockToNode) throws IOException{
		
		Block block = new Block(file.read(record.fileIndex));
		
		if(!isLeaf(block)){ //If this block represents non leaf, read children first
			int numPairs = numPairs(block);
			for(int i = numPairs; i >= 0; i--){
				int offset = 3 + (64 * numPairs) + (i*8);
				byte[] bytes = block.getBytes(offset + record.blockIndex,  8);
				readIntNode(maxSize, RecordLocation.fromBytes(bytes), blockToNode);
			}
		}

		blockToNode.put(record, IntToStringNode.fromBlock(maxSize, record.blockIndex, block, blockToNode, file));
	}

	private void readStrNode(int MaxSize, RecordLocation record,
			HashMap<RecordLocation, StringToIntNode> blockToNode) {
		// TODO Auto-generated method stub

	}

	public void writeTree(BPlusTreeIntToString60 intStrTree, BPlusTreeString60toInt strIntTree){
		//1 reserve header index
		//2 write all pairs first, in order (multiple to a block) and store hashmap of their block locations
		//3 write the 2 trees using hashmap to specify data blocks
		//4 write tree header locations to metadata header

		try{
			HashMap<Pair, RecordLocation> pairBlocks = new HashMap<Pair, RecordLocation>();
			int strIntHeader = 0;
			int intStrHeader = 0;

			// Write an empty block to reserve it for the header
			int metaLocation = file.write(new byte[file.blockSize]);

			// Write pairs and record their locations in pairBlocks HashMap

			writePairs(intStrTree, pairBlocks);

			// Write the trees and store their header locations
			strIntHeader = new StringIntTreeIO(file).writeTree(strIntTree, pairBlocks);
			intStrHeader = new IntStringTreeIO(file).writeTree(intStrTree, pairBlocks);

			// Write the tree header locations to metadata header
			byte[] metadata = new byte[file.blockSize];
			Bytes.intToBytes(strIntHeader, metadata, 0);
			Bytes.intToBytes(intStrHeader, metadata, 4);
			file.write(metadata, metaLocation);

			file.close();
		}
		catch(IOException e){e.printStackTrace();}

	}

	private void writePairs(BPlusTreeIntToString60 tree, HashMap<Pair, RecordLocation> pairBlocks) throws IOException{

		Block block = new Block(file.blockSize);
		int fileIndex = file.getSize();
		int blockIndex = 0;

		for(IntToStringNode leaf : tree){
			Pair[] pairs = leaf.getKeyValuePairs();

			for(int i = 0; i < leaf.size(); i++){
				if((blockIndex + 1) * 64 >= file.blockSize){ // Start new block if full
					file.write(block.getBytes());
					fileIndex++;
					block = new Block(file.blockSize);
					blockIndex = 0;
				}

				block.setBytes(pairs[i].getBytes(), blockIndex);
				pairBlocks.put(pairs[i], new RecordLocation(fileIndex, blockIndex));
				blockIndex += 64;
			}
		}

	}
	
	private int numPairs(Block block){
		return Bytes.byteToInt(block.getByte(2));
	}
	
	private boolean isLeaf(Block block){
		return Bytes.byteToInt(block.getByte(1)) == 1;
	}
}
