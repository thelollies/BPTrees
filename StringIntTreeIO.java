import java.io.IOException;
import java.util.HashMap;


public class StringIntTreeIO {

	private BlockFile file;
	private int maxNodeSize;

	public StringIntTreeIO(BlockFile file){
		this.file = file;
	}

	public void writeTree(BPlusTreeString60toInt tree){
		this.maxNodeSize = tree.maxSize;

		//file.write(tree.getHeader(file.blockSize, ))
		HashMap<StringToIntNode, Integer> nodeToBlock = new HashMap<StringToIntNode, Integer>();

		try{
			// Write an empty block to reserve it for the header
			file.write(new byte[file.blockSize]);

			writeNode(tree.getRoot(), nodeToBlock);

			file.write(tree.getHeader(file.blockSize, nodeToBlock).getBytes(), 0);
		}
		catch(IOException e){e.printStackTrace();}
		
		nodeToBlock.values();

	}

	public BPlusTreeString60toInt readTree(){
		HashMap<Integer, StringToIntNode> blockToNode = new HashMap<Integer, StringToIntNode>();

		try{
			//one and two
			Block header = new Block(file.read(0));
			int maxSize = Bytes.byteToInt(header.getByte(1));
			int rootBlock = Bytes.bytesToInt(header.getBytes(2, 4));

			readNode(maxSize, rootBlock, blockToNode);

			return BPlusTreeString60toInt.fromBytes(header, blockToNode);
		}catch(IOException e){e.printStackTrace();}
		return null;
	}

	private void writeNode(StringToIntNode node, HashMap<StringToIntNode, Integer> nodeToBlock) throws IOException{

		// Write the children first
		if(!node.isLeaf)
			for(int i = maxNodeSize; i >= 0; i--){
				if(node.getChild(i) != null)
					writeNode(node.getChild(i), nodeToBlock);
			}

		// Write this block
		nodeToBlock.put(node, file.write(node.toBlock(file.blockSize, nodeToBlock).getBytes()));
	}

	private void readNode(int maxSize, int blockNum, HashMap<Integer, StringToIntNode> blockToNode) throws IOException{
		Block block = new Block(file.read(blockNum));
		
		if(!isLeaf(block)){ //If this block represents non leaf, read children first
			int numPairs = numPairs(block);
			for(int i = numPairs; i >= 0; i--){
				int offset = 3 + (64 * numPairs) + (i*4);
				readNode(maxSize, Bytes.byteToInt(block.getByte(offset)), blockToNode);
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

	public BPlusTreeString60toInt read(){
		try{
			System.out.println(BPlusTreeString60toInt.fromBytes(new Block(file.read(0)), null));
		}catch(IOException e){e.printStackTrace();}

		return null;
	}

}
