/**
  Implements a B+ tree in which the keys are integers and the
  values are Strings (with maximum length 60 characters)
 */

public class BPlusTreeIntToString60 {

	private final int maxSize = 3;
	private IntToStringNode rootNode;

	/**
	 * Returns the String associated with the given key,
	 * or null if the key is not in the B+ tree.
	 */
	public String find(int key){

		if(rootNode == null) return null;
		return find(key, rootNode);
	}

	/**
	 * Stores the value associated with the key in the B+ tree.
	 * If the key is already present, replaces the associated value.
	 * If the key is not present, adds the key with the associated value
	 * @param key
	 * @param value
	 * @return whether pair was successfully added.
	 */
	public boolean put(int key, String value){

		if(rootNode == null){
			// Create new leaf, add key value
			IntToStringNode leaf = new IntToStringNode(maxSize, true);
			leaf.addKeyValue(key, value);

			// Add to root
			rootNode = leaf;

			return true;
		}
		else{

			// Try adding the key and if there's no space handle the split
			IntNode addResult = add(key, value, rootNode);
			if(addResult != null){

				// Make a new root node
				IntToStringNode newNode = new IntToStringNode(maxSize, false);
				newNode.setChild(0, rootNode);
				newNode.setKey(0, addResult.key);
				newNode.setChild(1, addResult.node);

				rootNode = newNode;
			}

			return true;
		}
	}

	private IntNode add(int key, String value, IntToStringNode node){
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
				if(key < node.getKey(i)){
					IntNode result = add(key, value, node.getChild(i));
					if(result == null)
						return null;
					else
						return handlePromote(result.key, result.node, node);
				}
			}

			IntNode result = add(key, value, node.getChild(node.size()));
			if(result == null)
				return null;
			else
				return handlePromote(result.key, result.node, node);

		}

	}

	private IntNode splitLeaf(int key, String value, IntToStringNode leaf){

		leaf.addKeyValue(key, value);
		IntToStringNode sibling = new IntToStringNode(maxSize, true);

		// Distributes the keys amongst the nodes
		int promoteKey = leaf.splitToLeaf(sibling);

		if(leaf.getNext() != null) sibling.setNext(leaf.getNext());
		leaf.setNext(sibling);

		return new IntNode(promoteKey, sibling);
	}

	private IntNode handlePromote(int newKey, IntToStringNode rightChild, IntToStringNode node){

		if(rightChild == null) return null;

		if(newKey > node.getKey(node.size() - 1)){

			node.setKey(node.size(), newKey);
			node.setChild(node.size(), rightChild);

		}
		else{
			for(int i = 0; i < node.size(); i++){
				if(newKey < node.getKey(i)){
					node.insertKey(i, newKey);
					node.insertChild(i+1, rightChild);
					break;
				}
			}
		}

		// No further promotes needed
		if(node.size() <= maxSize) return null;

		IntToStringNode sibling = new IntToStringNode(maxSize, false);

		int promoteKey = node.splitToNode(sibling);

		return new IntNode(promoteKey, sibling);
	}

	public String find(int key, IntToStringNode node){

		if(node.isLeaf()){
			// Handle leaf
			return node.getValue(key);
		}
		else{
			// Handle non-leaf node
			for(int i = 0; i < node.size(); i++)
				if(key < node.getKey(i)) return find(key, node.getChild(i));

			return find(key, node.getChild(node.size()));
		}
	}

	public String printAll(){
		IntToStringNode leaf = rootNode;
		while(!leaf.isLeaf){
			leaf = leaf.getChild(0);
		}

		StringBuilder sb = new StringBuilder();

		while(leaf != null){
			for(IntStringPair pair : leaf.getKeyValuePairs())
				if(pair != null) sb.append(pair + "\n");
			leaf = leaf.getNext();
		}

		return sb.toString();
	}

}
