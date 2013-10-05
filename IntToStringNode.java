import java.io.IOException;
import java.util.HashMap;


public class IntToStringNode{

	private Pair[] pairs;
	private IntToStringNode[] children;
	private final int maxSize;
	public final boolean isLeaf;
	private IntToStringNode next;

	// Used to track # of pairs & children
	private int numPairs;
	private int numChildren;

	public IntToStringNode(int size, boolean isLeaf){
		this.maxSize = size;
		this.isLeaf = isLeaf;

		// Initialise arrays
		pairs = new Pair[size + 1];
		children = new IntToStringNode[size + 2];
	}

	public void setNext(IntToStringNode next){
		if(!isLeaf) throw new BTreeException("Attempted to set next node on non-leaf.");
		if(!next.isLeaf) throw new BTreeException("Attempted to set next node to a non-leaf.");

		this.next = next;
	}

	public IntToStringNode getNext(){
		return next;
	}

	public boolean isLeaf(){
		return isLeaf;
	}

	public int numChildren(){
		return numChildren;
	}

	public int size(){
		return numPairs;
	}

	public IntToStringNode getRightChild(){
		for(int i = children.length - 1; i >= 0; i--)
			if(children[i] != null) return children[i];

		return null;
	}

	public int getKey(int keyIndex){
		return pairs[keyIndex].intgr;
	}

	public void insertKey(int position, int key){
		// Shift keys to make space for new one
		for(int i = pairs.length - 1; i > position; i--)
			pairs[i] =  pairs[i - 1];

		pairs[position] = Pair.create(null, key);
		numPairs++;
	}

	public void addKeyValue(int key, String value){
		if(!isLeaf) throw new BTreeException("Attempted to add key-value pair to non-leaf node");
		add(Pair.create(value, key));
	}

	private void add(Pair pair){
		// Insert key if it fits between existing ones
		if(pair == null) return;

		int index = size();
		for(int i = 0; i < size(); i++){
			if(pairs[i] != null && pair.intgr == pairs[i].intgr){
				pairs[i] = pair;
				return;
			}
			if(pairs[i] == null || pair.intgr < pairs[i].intgr){
				index = i;
				break;
			}
		}

		insert(index, pair, pairs);
		numPairs++;
	}

	public void insertChild(int position, IntToStringNode child){
		// Shift keys to make space for new one
		for(int i = children.length - 1; i > position; i--)
			children[i] = children[i - 1];

		children[position] =  child;
		numChildren++;
	}

	public boolean setChild(int position, IntToStringNode node){
		insert(position, node, children);
		numChildren++;

		return true;
	}

	public void setKey(int position, int key){
		insert(position, Pair.create(null, key), pairs);
		numPairs++;
	}

	public IntToStringNode getChild(int childIndex){
		return children[childIndex];
	}

	/**
	 * A helper method for inserting into an array of BNode
	 */
	private static void insert(int index, IntToStringNode node, IntToStringNode[] array){
		for(int i = array.length - 1; i > index; i--)
			array[i] = array[i - 1];

		array[index] = node;
	}

	/**
	 * A helper method for inserting into an array of BNode
	 */
	private static void insert(int index, Pair pair, Pair[] array){
		for(int i = array.length - 1; i > index; i--)
			array[i] = array[i - 1];

		array[index] = pair;
	}

	public String getValue(int key){
		if(!isLeaf) throw new BTreeException("Non-leaf trying to look up key values.");

		for(int i = 0; i < size(); i++){
			/*if(pairs[i] == null)
				System.out.printf("null pair on index %d at: %s", i, this);*/
			if(pairs[i].intgr == key) return pairs[i].str;
		}

		return null;
	}

	public int splitToLeaf(IntToStringNode sibling){
		if(!isLeaf) throw new BTreeException("Attempted to split leaf on non-leaf.");
		if(!sibling.isLeaf) throw new BTreeException("Attempted to split leaf to a non-leaf.");

		int mid = (maxSize + 1) / 2;

		int key = pairs[mid].intgr;

		for(int i = mid; i < pairs.length; i++){
			sibling.add(pairs[i]);
			pairs[i] = null;
		}

		numPairs = mid;

		return key;
	}

	public int splitToNode(IntToStringNode sibling) {
		if(isLeaf) throw new BTreeException("Attempted to split node on non-node.");
		if(sibling.isLeaf) throw new BTreeException("Attempted to split node to a non-node.");

		int mid = (maxSize + 1) / 2;

		int key = pairs[mid].intgr;

		for(int i = mid + 1; i < pairs.length; i++){
			sibling.add(pairs[i]);
			pairs[i] = null;
		}
		pairs[mid] = null;

		for(int i = mid + 1; i < children.length; i++){
			sibling.addChild(children[i]);
			children[i] = null;
		}

		numChildren = mid + 1;
		numPairs = mid;

		return key;
	}

	private void addChild(IntToStringNode node) {
		// Insert key if it fits between existing ones
		children[numChildren] = node;
		numChildren++;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		for(int i = 0; i < pairs.length - 1; i++){
			if(i != 0) sb.append(",");
			if(pairs[i] != null) sb.append(pairs[i].toString());
		}

		// Append the overhanging key if there is one
		if(pairs[pairs.length - 1] != null){
			sb.append("|");
			sb.append(pairs[pairs.length - 1]);
		}

		sb.append("}");

		return sb.toString();
	}

	/**
	 * Creates a block representation of this node with the format [header pairs children]
	 * Leaf Block: [0, isLeaf (1 true), #pairs, next, pairs ...]
	 * Node Block: [0, isLeaf (0 false), #pairs, pairs, children ...] (numChildren = numPairs + 1)
	 */
	public Block toBlock(int blockSize, HashMap<IntToStringNode, RecordLocation> nodes, 
			HashMap<Pair, RecordLocation> pairBlocks){
		int blockIndex = 0;
		Block bytes = new Block(blockSize);

		// Header Begins
		bytes.setByte(Bytes.intToByte(1), blockIndex++); 								// 1 is StringToIntNode
		bytes.setByte(Bytes.intToByte(isLeaf ? 1 : 0), blockIndex++); 					// isLeaf
		bytes.setByte(Bytes.intToByte(numPairs), blockIndex++);							// numPairs

		// Write the next node if this is a leaf
		if(isLeaf){																	// next
			RecordLocation nextLeaf = next == null ?
					new RecordLocation(-Integer.MAX_VALUE, -Integer.MAX_VALUE) : nodes.get(next);
					bytes.setBytes(nextLeaf.getBytes(), blockIndex);
					blockIndex += 8;
		}
		// Header Ends

		// Write the pair pointers
		for(int i = 0; i < numPairs; i++){
			if(isLeaf){
				bytes.setBytes(pairBlocks.get(pairs[i]).getBytes(), blockIndex);
				blockIndex += 8;
			}
			else{
				bytes.setBytes(Bytes.intToBytes(pairs[i].intgr), blockIndex);
				blockIndex += 4;
			}
		}

		// Write the children if this is not a leaf
		if(!isLeaf)
			for(int i = 0; i < numChildren; i++){
				bytes.setBytes(nodes.get(children[i]).getBytes(), blockIndex);
				blockIndex += 8;
			}

		return bytes;
	}

	public static IntToStringNode fromBlock(int maxSize, int blockIndex, Block block, 
			HashMap<RecordLocation, IntToStringNode> nodes, BlockFile file) throws IOException{

		// Begin header reading
		if(Bytes.byteToInt(block.getByte(blockIndex++)) != 1)
			throw new BTreeException("Attempted to read StringToIntNode into IntToStringNode");

		boolean isLeaf = Bytes.byteToInt(block.getByte(blockIndex++)) == 1;			// isLeaf
		IntToStringNode newNode = new IntToStringNode(maxSize, isLeaf);				// Create new empty node

		newNode.numPairs = Bytes.byteToInt(block.getByte(blockIndex++));			// numPairs
		newNode.numChildren = newNode.numPairs == 0 ? 0 : newNode.numPairs + 1;		// numChildren

		if(isLeaf){
			RecordLocation nextNode = RecordLocation.fromBytes(block.getBytes(blockIndex, 8));
			newNode.next = nextNode.fileIndex != -Integer.MAX_VALUE ? nodes.get(nextNode) : null;
			blockIndex += 8;
		}
		// End header reading

		// Read pairs
		for(int i = 0; i < newNode.numPairs; i++){
			if(isLeaf){
				RecordLocation pairLocation = RecordLocation.fromBytes(block.getBytes(blockIndex, 8));
				blockIndex += 8;
				newNode.pairs[i] = Pair.fromBytes(new Block(file.read(pairLocation.fileIndex)).getBytes(pairLocation.blockIndex, 64));
			}
			else{
				newNode.pairs[i] = Pair.create(null, block.getInt(blockIndex));
				blockIndex += 4;
			}
		}

		// Read children if this is not a leaf
		if(!newNode.isLeaf)
			for(int i = 0; i < newNode.numChildren; i++){
				RecordLocation childLocation = RecordLocation.fromBytes(block.getBytes(blockIndex, 8));
				newNode.children[i] = nodes.get(childLocation);
				blockIndex += 8;
			}

		return newNode;
	}

	public Pair[] getKeyValuePairs() {
		return pairs;
	}


}
