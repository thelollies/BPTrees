import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
  Implements a B+ tree in which the keys  are Strings (with
  maximum length 60 characters) and the values are integers
*/

public class BPlusTreeString60toInt {

	public final int maxSize;
	private StringToIntNode rootNode;

	public BPlusTreeString60toInt(int maxSize){
		if(maxSize > 255) throw new BTreeException("Cannot have BTree with node size > 255");
		this.maxSize = maxSize;
	}

	public BPlusTreeString60toInt(){
		this.maxSize = 14;
	}

    /**
     * Returns the integer associated with the given key,
     * or null if the key is not in the B+ tree.
     */
    public Integer find(String key){
		if(rootNode == null) return null;
		Integer result = find(key, rootNode);
		result = result == -Integer.MAX_VALUE ? null : result;
		return result;
    }


    /**
     * Stores the value associated with the key in the B+ tree.
     * If the key is already present, replaces the associated value.
     * If the key is not present, adds the key with the associated value
     * @param value
     * @param key
     * @return whether pair was successfully added.
     */
    public boolean put(String key, int value){
    	if(rootNode == null){
			// Create new leaf, add key value
			StringToIntNode leaf = new StringToIntNode(maxSize, true);
			leaf.addKeyValue(key, value);

			// Add to root
			rootNode = leaf;

			return true;
		}
		else{

			// Try adding the key and if there's no space handle the split
			StringNode addResult = add(key, value, rootNode);
			if(addResult != null){

				// Make a new root node
				StringToIntNode newNode = new StringToIntNode(maxSize, false);
				newNode.setChild(0, rootNode);
				newNode.setKey(0, addResult.key);
				newNode.setChild(1, addResult.node);

				rootNode = newNode;
			}

			return true;
		}
    }

    private StringNode add(String key, int value, StringToIntNode node){
		if(node.isLeaf){
			// Handle leaf
			if(node.size() < maxSize){
				node.addKeyValue(key, value);
				return null;
			}
			else{
				return splitLeaf(key, value, node);
			}
		}
		else{
			// Handle Node
			for(int i = 0; i < node.size(); i++){
				if(key.compareTo(node.getKey(i)) < 0){
					StringNode result = add(key, value, node.getChild(i));
					if(result == null)
						return null;
					else
						return handlePromote(result.key, result.node, node);
				}
			}

			StringNode result = add(key, value, node.getChild(node.size()));
			if(result == null)
				return null;
			else
				return handlePromote(result.key, result.node, node);

		}

	}

	private StringNode splitLeaf(String key, int value, StringToIntNode leaf){

		leaf.addKeyValue(key, value);
		StringToIntNode sibling = new StringToIntNode(maxSize, true);

		// Distributes the keys amongst the nodes
		String promoteKey = leaf.splitToLeaf(sibling);

		if(leaf.getNext() != null) sibling.setNext(leaf.getNext());
		leaf.setNext(sibling);

		return new StringNode(promoteKey, sibling);
	}

	private StringNode handlePromote(String newKey, StringToIntNode rightChild, StringToIntNode node){

		if(rightChild == null) return null;

		if(newKey.compareTo(node.getKey(node.size() - 1)) > 0){

			node.setKey(node.size(), newKey);
			node.setChild(node.size(), rightChild);

		}
		else{
			for(int i = 0; i < node.size(); i++){
				if(newKey.compareTo(node.getKey(i)) < 0){
					node.insertKey(i, newKey);
					node.insertChild(i+1, rightChild);
					break;
				}
			}
		}

		// No further promotes needed
		if(node.size() <= maxSize) return null;

		StringToIntNode sibling = new StringToIntNode(maxSize, false);

		// Move the mid to end over to the sibling
		String promoteKey = node.splitToNode(sibling);

		return new StringNode(promoteKey, sibling);
	}

	public int find(String key, StringToIntNode node){

		if(node.isLeaf()){
			// Handle leaf
			return node.getValue(key);
		}
		else{
			// Handle non-leaf node
			for(int i = 0; i < node.size(); i++)
				if(key.compareTo(node.getKey(i)) < 0) return find(key, node.getChild(i));

			return find(key, node.getChild(node.size()));
		}
	}

	/**
	 * This header block contains the type of nodes (string-int = 0 or int-string = 1),
	 * the maximum size of the nodes, the index of the root node.
	 * and the type of nodes being stored.
	 * @param blockSize
	 * @return
	 */
	public Block getHeader(int blockSize, HashMap<StringToIntNode, RecordLocation> nodes){
		int blockIndex = 0;
		Block block = new Block(blockSize);
		block.setByte(Bytes.intToByte(0), blockIndex++); //Set type to String-Int (zero)
		block.setByte(Bytes.intToByte(maxSize), blockIndex++); // Set max pairs in node

		RecordLocation rootBlock = rootNode == null ? 
				new RecordLocation(-Integer.MAX_VALUE, -Integer.MAX_VALUE) : nodes.get(rootNode);
				
		block.setBytes(rootBlock.getBytes(), blockIndex); // Write root node block location


		return block;
	}

	public static BPlusTreeString60toInt fromBytes(Block block, HashMap<RecordLocation, StringToIntNode> nodes){
		int blockIndex = 0;
		if(Bytes.byteToInt(block.getByte(blockIndex++)) != 0)
			throw new BTreeException("Cannot read IntToString tree into StringToInt tree.");

		int maxSize = Bytes.byteToInt(block.getByte(blockIndex++));
		BPlusTreeString60toInt tree = new BPlusTreeString60toInt(maxSize);

		RecordLocation rootBlock = RecordLocation.fromBytes(block.getBytes(blockIndex, 8));
		if(nodes != null) // This will be null when creating
			tree.rootNode = rootBlock.fileIndex == -Integer.MAX_VALUE ? null : nodes.get(rootBlock);

		return tree;
	}

	public StringToIntNode getRoot(){
		return rootNode;
	}
	
	public List<String> printAll(){
		StringToIntNode leaf = rootNode;
		while(!leaf.isLeaf){
			leaf = leaf.getChild(0);
		}

		List<String> output = new ArrayList<String>();

		while(leaf != null){
			for(Pair pair : leaf.getKeyValuePairs())
				if(pair != null) output.add(pair.toString());
			leaf = leaf.getNext();
		}

		return output;
	}

	@Override
	public String toString() {
		return String.format("Size: %d, Root: %s", maxSize, rootNode);
	}

}
