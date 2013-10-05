import java.io.IOException;
import java.util.HashMap;


public class IntStringTreeIO {

	private BlockFile file;
	private int maxNodeSize;

	public IntStringTreeIO(BlockFile file){
		this.file = file;
	}

	public int writeTree(BPlusTreeIntToString60 tree, HashMap<Pair, RecordLocation> pairBlocks){
		this.maxNodeSize = tree.maxSize;
		int headerLocation = 0;
		
		HashMap<IntToStringNode, RecordLocation> nodeToBlock = new HashMap<IntToStringNode, RecordLocation>();

		try{
			// Write an empty block to reserve it for the header
			headerLocation = file.write(new byte[file.blockSize]);

			writeNode(tree.getRoot(), nodeToBlock, pairBlocks, new BlockManager(file));

			file.write(tree.getHeader(file.blockSize, nodeToBlock).getBytes(), headerLocation);
		}
		catch(IOException e){e.printStackTrace();}
		
		return headerLocation;
	}

	public BPlusTreeIntToString60 readTree(){
		HashMap<Integer, IntToStringNode> blockToNode = new HashMap<Integer, IntToStringNode>();

		try{
			//one and two
			Block header = new Block(file.read(0));
			int maxSize = Bytes.byteToInt(header.getByte(1));
			int rootBlock = Bytes.bytesToInt(header.getBytes(2, 4));

			readNode(maxSize, rootBlock, blockToNode);

			file.close();
			return BPlusTreeIntToString60.fromBytes(header, blockToNode);
		}catch(IOException e){e.printStackTrace();}
		return null;
	}

	private void writeNode(IntToStringNode node, 
			HashMap<IntToStringNode, RecordLocation> nodeToBlock,
			HashMap<Pair, RecordLocation> pairBlocks,
			BlockManager block) throws IOException{

		// Write the children first
		if(!node.isLeaf)
			for(int i = maxNodeSize; i >= 0; i--)
				if(node.getChild(i) != null)
					writeNode(node.getChild(i), nodeToBlock, pairBlocks, block);

		// Write this block
		nodeToBlock.put(node, block.write(node.toBlock(file.blockSize, nodeToBlock, pairBlocks).getBytes()));
	}

	private void readNode(int maxSize, int blockNum, HashMap<Integer, IntToStringNode> blockToNode) throws IOException{
		Block block = new Block(file.read(blockNum));
		
		if(!isLeaf(block)){ //If this block represents non leaf, read children first
			int numPairs = numPairs(block);
			for(int i = numPairs; i >= 0; i--){
				int offset = 3 + (64 * numPairs) + (i*4);
				byte[] bytes = block.getBytes(offset,  4);
				readNode(maxSize, Bytes.bytesToInt(bytes), blockToNode);
			}
		}
		
		//blockToNode.put(blockNum, IntToStringNode.fromBlock(maxSize, block, blockToNode));
	}

	private int numPairs(Block block){
		return Bytes.byteToInt(block.getByte(2));
	}
	
	private boolean isLeaf(Block block){
		return Bytes.byteToInt(block.getByte(1)) == 1;
	}

}
