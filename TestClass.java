import java.io.IOException;
import java.util.HashMap;

public class TestClass {
	public TestClass(){

	}

	public void testTree(){
		BPlusTreeString60toInt tree = new BPlusTreeString60toInt();
		/*tree.put(1, "A");
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
		tree.put(-1, "Y");
		tree.put(13, "M");
		tree.put(14, "N");
		tree.put(15, "O");*/

		
		tree.put("A", 1);
		tree.put("B", 5);
		tree.put("C", 6);
		tree.put("D", 20);
		tree.put("E", 3);
		tree.put("F", 1);
		
		try{
			StringIntTreeIO io = new StringIntTreeIO(new BlockFile("test.txt", 1024));
			io.writeTree(tree);
			System.out.println(io.readTree());
		}
		catch(IOException e){e.printStackTrace();}
		
		System.out.println(tree);
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

	public void test(){
	}

	public static void main(String[] args){
		new TestClass().testTree();

	}
}
