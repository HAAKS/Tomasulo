import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Memory {
	static HashMap<Integer, ArrayList<String>> memory = new HashMap<Integer, ArrayList<String>>();
	static int blockSize;
	static int startingAddress;
	static int counter; // keep track at which instruction it is standing
	static int memorylatency;
	static int startData;//gets the line from which word data is present

	public Memory(int lineSize) {
		ArrayList<String> ins = new ArrayList<String>();
		this.blockSize = lineSize;
	}

	public static void insertFirstOnceIntoMemory(ArrayList<String> instruction) {
		// address
		ArrayList<String> Block = new ArrayList<String>();
		String in;
		startingAddress = Integer.parseInt(instruction.get(0));
		counter = 1;// goes through instructions in ArrayList
		for (int i = 1; i < instruction.size(); i += blockSize) {
			for (int j = 0; j < blockSize; j++) {
				in = instruction.get(counter);
				if (in.contains("Data")) {
					startData = startingAddress;
					counter++;
					in = instruction.get(counter++);
					startingAddress = Integer.parseInt(in); // addr. of data
					insertDatainMemory(instruction);
					System.out.println("The start of data: "+startData);
					//print();
					return;
				} else {
					Block.add(j, in);

					counter++;
				}

			}
			addInstruction(Block);
		

		}
	}

	public static void insertDatainMemory(ArrayList<String> instruction) {
		String in;
		List temp = new ArrayList();
		int variableholder = counter;
		; // to get a sublist from the main array
		ArrayList<String> toplaceinMem = new ArrayList<String>();
		in = instruction.get(counter);
		for (int i = variableholder; i < instruction.size() && in != null; i++) {
			for (int j = 0; j < blockSize && in != null; j++) {
				toplaceinMem.add(j, in);
				counter++;
				if (counter < instruction.size())
					in = instruction.get(counter);

			}
			addInstruction(toplaceinMem);
		}
	}

	public static void addInstruction(ArrayList<String> Array) {
		List temp = new ArrayList();
		temp = new ArrayList<String>(Array.subList(0, blockSize));
		memory.put(startingAddress, (ArrayList<String>) temp);
		startingAddress++;
	}
	public static void print(){
		
			System.out.println(memory.get(96));
		
			System.out.println(memory.get(2));
	}

}
