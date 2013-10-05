import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;

public class TestClass {
	public TestClass(){

	}

	public void testTree(){
		BPlusTreeIntToString60 tree = new BPlusTreeIntToString60();
		tree.put(1, "A");
		tree.put(5, "E");
		tree.put(6, "F");
		tree.put(8, "H");
		tree.put(2, "B");
		tree.put(4, "D");
		tree.put(9, "I");
		tree.put(3, "C");
		tree.put(7, "G");
		tree.put(10, "J");
		tree.put(11, "K");
		tree.put(12, "L");
		tree.put(0, "Z");
		tree.put(25, "Y");
		tree.put(13, "M");
		tree.put(14, "N");
		tree.put(15, "O");


		/*tree.put("D", 4);
		tree.put("B", 2);
		tree.put("A", 1);
		tree.put("F", 6);
		tree.put("C", 3);
		tree.put("E", 5);*/

		/*HashMap<StringToIntNode, Integer> nodeToBlock = new HashMap<StringToIntNode, Integer>();
		HashMap<Integer, StringToIntNode> blockToNode = new HashMap<Integer, StringToIntNode>();

		nodeToBlock.put(tree.getRoot(), 1);
		nodeToBlock.put(tree.getRoot().getChild(0), 2);
		nodeToBlock.put(tree.getRoot().getChild(1), 3);
		nodeToBlock.put(tree.getRoot().getChild(2), 4);

		blockToNode.put(1,  tree.getRoot());
		blockToNode.put(2, tree.getRoot().getChild(0));
		blockToNode.put(3, tree.getRoot().getChild(1));
		blockToNode.put(4, tree.getRoot().getChild(2));

		StringToIntNode rootCopy = StringToIntNode.fromBlock(3, tree.getRoot().toBlock(1024, nodeToBlock), blockToNode);

		System.out.println(rootCopy);*/
		
		HashMap<IntToStringNode, Integer> nodeToBlock = new HashMap<IntToStringNode, Integer>();
		HashMap<Integer, IntToStringNode> blockToNode = new HashMap<Integer, IntToStringNode>();

		nodeToBlock.put(tree.getRoot(), 1);
		nodeToBlock.put(tree.getRoot().getChild(0), 2);
		nodeToBlock.put(tree.getRoot().getChild(1), 3);
		nodeToBlock.put(tree.getRoot().getChild(2), 4);

		blockToNode.put(1,  tree.getRoot());
		blockToNode.put(2, tree.getRoot().getChild(0));
		blockToNode.put(3, tree.getRoot().getChild(1));
		blockToNode.put(4, tree.getRoot().getChild(2));

		//IntToStringNode rootCopy = IntToStringNode.fromBlock(3, tree.getRoot().toBlock(1024, nodeToBlock), blockToNode);

		//System.out.println(rootCopy);


		/*try{
			StringIntTreeIO io = new StringIntTreeIO(new BlockFile("test.txt", 1024));
			io.writeTree(tree);
			StringIntTreeIO ioIn = new StringIntTreeIO(new BlockFile("test.txt", 1024));
			BPlusTreeString60toInt newTree = ioIn.readTree();
			System.out.println("Success");
		}
		catch(IOException e){
			e.printStackTrace();
		}*/

		/*tree.put("H", 8);
		tree.put("B", 2);
		tree.put("D", 4);
		tree.put("I", 9);
		tree.put("C", 3);
		tree.put("G", 7);
		tree.put("J", 10);
		tree.put("K", 11);
		tree.put("L", 12);
		tree.put("Z", 0);
		tree.put("Y", -1);
		tree.put("M", 13);
		tree.put("N", 14);
		tree.put("O", 15);*/
	}
	
	public void matchText(){
		try {
			BufferedReader hosts = new BufferedReader(new FileReader(new File("dummy.txt")));
			BufferedReader iterated = new BufferedReader(new FileReader(new File("iterateall.txt")));
			
			int index = 1;
			String hostLine = hosts.readLine();
			String iterLine = iterated.readLine();
			while(hostLine != null || iterLine != null){
				if(!hostLine.equals(iterLine)) System.out.printf("Mismatch: %s - %s at %d\n", hostLine, iterLine, index);
				hostLine = hosts.readLine();
				iterLine = iterated.readLine();
				index++;
			}
			
			System.out.printf("No issues, read %d\n", index);
			
			hosts.close();
			iterated.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

	public void flipHosts(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File("dummy.txt")));
			PrintWriter writer = new PrintWriter(new File("dummyflipped.txt"));
			String line = reader.readLine();
			
			while(line != null){
				String[] split = line.split("\t");
				writer.println(split[1] + "\t" + split[0]);
				line = reader.readLine();
			}
			
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void testBits(){
		int first = Integer.MAX_VALUE;
		int second = 47382727;

		
		System.out.println(hashThem(first, second));

		
	}
	
	private double hashThem(int first, int second){
		double k1 = first;
		double k2 = second;
		double result = 1.0 / (2.0*(k1 + k2)*(k1 + k2 + 1.0) + k2);
		return result;
	}
	
	public static int add(Integer num){
		return ++num;
	}
	
	public static void main(String[] args){
		//new TestClass().matchText();
		Integer five = 5;
		
		System.out.println(TestClass.add(five));

	}
}
