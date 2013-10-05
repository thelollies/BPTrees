import java.io.IOException;
import java.util.HashMap;


public class StringIntTreeIO {

	private BlockFile file;
	private int maxNodeSize;

	public StringIntTreeIO(BlockFile file){
		this.file = file;
	}

	public int writeTree(BPlusTreeString60toInt tree, HashMap<Pair, RecordLocation> pairBlocks){
		this.maxNodeSize = tree.maxSize;
		int writeLocation = 0;

		//file.write(tree.getHeader(file.blockSize, ))
		HashMap<StringToIntNode, RecordLocation> nodeToBlock = new HashMap<StringToIntNode, RecordLocation>();

		try{
			// Write an empty block to reserve it for the header
			writeLocation = file.write(new byte[file.blockSize]);

			if(pairBlocks.size() == 0){System.out.println("empty pairBlocks");}
			
			writeNode(tree.getRoot(), nodeToBlock, pairBlocks, new BlockManager(file));

			file.write(tree.getHeader(file.blockSize, nodeToBlock).getBytes(), writeLocation);
		}
		catch(IOException e){e.printStackTrace();}

		return writeLocation;
	}

	public BPlusTreeString60toInt readTree(){
		HashMap<Integer, StringToIntNode> blockToNode = new HashMap<Integer, StringToIntNode>();

		try{
			//one and two
			Block header = new Block(file.read(0));
			int maxSize = Bytes.byteToInt(header.getByte(1));
			int rootBlock = Bytes.bytesToInt(header.getBytes(2, 4));

			readNode(maxSize, rootBlock, blockToNode);

			return null;//BPlusTreeString60toInt.fromBytes(header, blockToNode);
		}catch(IOException e){e.printStackTrace();}
		return null;
	}

	private void writeNode(StringToIntNode node, 
			HashMap<StringToIntNode, RecordLocation> nodeToBlock, 
			HashMap<Pair, RecordLocation> pairBlocks,
			BlockManager block) throws IOException{

		// Write the children first
		if(!node.isLeaf)
			for(int i = maxNodeSize; i >= 0; i--){
				if(node.getChild(i) != null)
					writeNode(node.getChild(i), nodeToBlock, pairBlocks, block);
			}

		// Write this block
		nodeToBlock.put(node, block.write(node.toBlock(file.blockSize, nodeToBlock, pairBlocks).getBytes()));
	}

	private void readNode(int maxSize, int blockNum, HashMap<Integer, StringToIntNode> blockToNode) throws IOException{
		Block block = new Block(file.read(blockNum));

		if(!isLeaf(block)){ //If this block represents non leaf, read children first
			int numPairs = numPairs(block);
			for(int i = numPairs; i >= 0; i--){
				int offset = 3 + (64 * numPairs) + (i*4);
				byte[] bytes = block.getBytes(offset,  4);
				readNode(maxSize, Bytes.bytesToInt(bytes), blockToNode);
			}
		}

		blockToNode.put(blockNum, StringToIntNode.fromBlock(maxSize, block, blockToNode));
	}

	private int numPairs(Block block){
		return Bytes.byteToInt(block.getByte(2));
	}

	private boolean isLeaf(Block block){
		return Bytes.byteToInt(block.getByte(1)) == 1;
	}
}
