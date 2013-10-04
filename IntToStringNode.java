
public class IntToStringNode{

	private IntStringPair[] pairs;
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
		pairs = new IntStringPair[size + 1];
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
		return pairs[keyIndex].key;
	}

	public void insertKey(int position, int key){
		// Shift keys to make space for new one
		for(int i = pairs.length - 1; i > position; i--)
			pairs[i] =  pairs[i - 1];

		pairs[position] = new IntStringPair(key, null);
		numPairs++;
	}

	public void addKeyValue(int key, String value){
		if(!isLeaf) throw new BTreeException("Attempted to add key-value pair to non-leaf node");
		add(new IntStringPair(key, value));
	}

	private void add(IntStringPair pair){
		// Insert key if it fits between existing ones
		if(pair == null) return;

		int index = size();
		for(int i = 0; i < size(); i++){
			if(pairs[i] != null && pair.key == pairs[i].key){
				pairs[i] = pair;
				return;
			}
			if(pairs[i] == null || pair.key < pairs[i].key){
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
		insert(position, new IntStringPair(key, null), pairs);
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
	private static void insert(int index, IntStringPair pair, IntStringPair[] array){
		for(int i = array.length - 1; i > index; i--)
			array[i] = array[i - 1];

		array[index] = pair;
	}

	public String getValue(int key){
		if(!isLeaf) throw new BTreeException("Non-leaf trying to look up key values.");

		for(int i = 0; i < size(); i++){
			/*if(pairs[i] == null)
				System.out.printf("null pair on index %d at: %s", i, this);*/
			if(pairs[i].key == key) return pairs[i].value;
		}

		return null;
	}

	public int splitToLeaf(IntToStringNode sibling){
		if(!isLeaf) throw new BTreeException("Attempted to split leaf on non-leaf.");
		if(!sibling.isLeaf) throw new BTreeException("Attempted to split leaf to a non-leaf.");

		int mid = (maxSize + 1) / 2;

		int key = pairs[mid].key;

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

		int key = pairs[mid].key;

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

	public IntStringPair[] getKeyValuePairs() {
		return pairs;
	}


}
