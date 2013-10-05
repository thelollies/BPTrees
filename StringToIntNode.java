import java.util.HashMap;


public class StringToIntNode{

	private Pair[] pairs;
	private StringToIntNode[] children;
	private final int maxSize;
	public final boolean isLeaf;
	private StringToIntNode next;
	public final int id;
	private static int idCount = 0;

	// Used to track # of pairs & children
	private int numPairs;
	private int numChildren;

	public StringToIntNode(int size, boolean isLeaf){
		this.maxSize = size;
		this.isLeaf = isLeaf;
		this.id = StringToIntNode.idCount++;

		// Initialise arrays
		pairs = new Pair[size + 1];
		if(!isLeaf)
			children = new StringToIntNode[size + 2];
	}

	public void setNext(StringToIntNode next){
		if(!isLeaf) throw new BTreeException("Attempted to set next node on non-leaf.");
		if(!next.isLeaf) throw new BTreeException("Attempted to set next node to a non-leaf.");

		this.next = next;
	}

	public StringToIntNode getNext(){
		if(next != null && !next.isLeaf)
			throw new BTreeException("Cannot have non-leaf next");
		
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

	public StringToIntNode getRightChild(){
		for(int i = children.length - 1; i >= 0; i--)
			if(children[i] != null) return children[i];

		return null;
	}

	public String getKey(int keyIndex){
		return pairs[keyIndex].str;
	}

	public void insertKey(int position, String key){
		// Shift keys to make space for new one
		for(int i = pairs.length - 1; i > position; i--)
			pairs[i] =  pairs[i - 1];

		pairs[position] = Pair.create(key, -Integer.MAX_VALUE);
		numPairs++;
	}

	public void addKeyValue(String key, int value){
		if(!isLeaf) throw new BTreeException("Attempted to add key-value pair to non-leaf node");
		add(Pair.create(key, value));
	}

	private void add(Pair pair){
		// Insert key if it fits between existing ones
		if(pair == null) return;

		int index = size();
		for(int i = 0; i < size(); i++){
			if(pair.str == null)
				System.out.println("error");
			if(pairs[i] != null &&	pair.str.compareTo(pairs[i].str) == 0){
				pairs[i] = pair;
				return;
			}
			if(pairs[i] == null || pair.str.compareTo(pairs[i].str) < 0){
				index = i;
				break;
			}
		}

		insert(index, pair, pairs);
		numPairs++;
	}

	public void insertChild(int position, StringToIntNode child){
		// Shift keys to make space for new one
		for(int i = children.length - 1; i > position; i--)
			children[i] = children[i - 1];

		children[position] =  child;
		numChildren++;
	}

	public boolean setChild(int position, StringToIntNode node){
		insert(position, node, children);
		numChildren++;

		return true;
	}

	public void setKey(int position, String key){
		insert(position, Pair.create(key, -Integer.MAX_VALUE), pairs);
		numPairs++;
	}

	public StringToIntNode getChild(int childIndex){
		return children[childIndex];
	}

	/**
	 * A helper method for inserting into an array of BNode
	 */
	private static void insert(int index, StringToIntNode node, StringToIntNode[] array){
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

	public int getValue(String key){
		if(!isLeaf) throw new BTreeException("Non-leaf trying to look up key values.");

		for(int i = 0; i < size(); i++){
			/*if(pairs[i] == null)
				System.out.printf("null pair on index %d at: %s", i, this);*/
			if(pairs[i].str.equals(key)) return pairs[i].intgr;
		}

		return -Integer.MAX_VALUE;
	}

	public String splitToLeaf(StringToIntNode sibling){
		if(!isLeaf) throw new BTreeException("Attempted to split leaf on non-leaf.");
		if(!sibling.isLeaf) throw new BTreeException("Attempted to split leaf to a non-leaf.");

		int mid = (maxSize + 1) / 2;

		String key = pairs[mid].str;

		for(int i = mid; i < pairs.length; i++){
			sibling.add(pairs[i]);
			pairs[i] = null;
		}

		numPairs = mid;

		return key;
	}

	public String splitToNode(StringToIntNode sibling) {
		if(isLeaf) throw new BTreeException("Attempted to split node on non-node.");
		if(sibling.isLeaf) throw new BTreeException("Attempted to split node to a non-node.");

		int mid = (maxSize + 1) / 2;

		String key = pairs[mid].str;

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

	private void addChild(StringToIntNode node) {
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

	public Pair[] getKeyValuePairs() {
		return pairs;
	}

	/**
	 * Creates a block representation of this node with the format [header pairs children]
	 * Leaf Block: [0, isLeaf (1 true), #pairs, next, pairs ...]
	 * Node Block: [0, isLeaf (0 false), #pairs, pairs, children ...] (numChildren = numPairs + 1)
	 */
	public Block toBlock(int blockSize, HashMap<StringToIntNode, RecordLocation> nodes, 
			HashMap<Pair, RecordLocation> pairBlocks){
		int blockIndex = 0;
		Block bytes = new Block(blockSize);

		// Header Begins
		bytes.setByte(Bytes.intToByte(0), blockIndex++); 								// 0 is StringToIntNode
		bytes.setByte(Bytes.intToByte(isLeaf ? 1 : 0), blockIndex++); 					// isLeaf
		bytes.setByte(Bytes.intToByte(numPairs), blockIndex++);							// numPairs

		// Write the next node if this is a leaf
		if(isLeaf){																	// next
			RecordLocation nextLeaf = next == null ? new RecordLocation(-Integer.MAX_VALUE, -Integer.MAX_VALUE) : nodes.get(next);
			bytes.setBytes(nextLeaf.getBytes(), blockIndex);
			blockIndex += 8;
		}
		// Header Ends

		// Write the pairs
		for(int i = 0; i < numPairs; i++){
			if(isLeaf){
				bytes.setBytes(pairBlocks.get(pairs[i]).getBytes(), blockIndex);
				blockIndex += 8;
			}
			else{
				bytes.setBytes(pairs[i].str.getBytes(), blockIndex);
				blockIndex += 60;
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

	public static StringToIntNode fromBlock(int maxSize, Block block, HashMap<Integer, StringToIntNode> nodes){
		int blockIndex = 0;

		// Begin header reading
		if(Bytes.byteToInt(block.getByte(blockIndex++)) != 0)
			throw new BTreeException("Attempted to read IntToStringNode into StringToIntNode");

		boolean isLeaf = Bytes.byteToInt(block.getByte(blockIndex++)) == 1;
		StringToIntNode newNode = new StringToIntNode(maxSize, isLeaf);

		newNode.numPairs = Bytes.byteToInt(block.getByte(blockIndex++));
		newNode.numChildren = newNode.numPairs == 0 ? 0 : newNode.numPairs + 1;

		if(isLeaf){
			int nodeInt = Bytes.bytesToInt(block.getBytes(blockIndex, 4));
			newNode.next = nodeInt != -Integer.MAX_VALUE ? nodes.get(nodeInt) : null;
			blockIndex += 4;
		}
		// End header reading

		// Read pairs
		for(int i = 0; i < newNode.numPairs; i++){
			newNode.pairs[i] = Pair.fromBytes(block.getBytes(blockIndex, 64));
			blockIndex += 64;
		}

		// Read children if this is not a leaf
		if(!newNode.isLeaf)
			for(int i = 0; i < newNode.numChildren; i++){
				newNode.children[i] = nodes.get(Bytes.bytesToInt(block.getBytes(blockIndex, 4)));
				blockIndex += 4;
			}

		return newNode;
	}

	/*@Override
	public int hashCode() {
		return id;
	}*/

}
