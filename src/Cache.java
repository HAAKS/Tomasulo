import java.util.ArrayList;
import java.util.HashMap;

public class Cache {

	HashMap<Integer, ArrayList<String>> Icache;
	HashMap<Integer, ArrayList<String>> Dcache;
	ArrayList<Integer> tags;
	int sizeOfCache;
	static int lineSize;
	int associativity;
	int numOfCycles;
	int blocks;
	int hit;
	int miss;

	public Cache(int sizeOfCache, int lineSize, int associativity,
			int numOfCycles) {
		Icache = new HashMap<Integer, ArrayList<String>>();
		Dcache = new HashMap<Integer, ArrayList<String>>();
		tags = new ArrayList<Integer>();
		for (int j = 0; j < 100; j++) {
			tags.add(-1);
		}
		this.sizeOfCache = sizeOfCache;
		this.lineSize = lineSize;
		this.associativity = associativity;
		this.numOfCycles = numOfCycles;
		this.blocks = sizeOfCache / lineSize;

	}
}
