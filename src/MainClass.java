import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
	static Cache c1;
	static Cache c2;
	static Cache c3;
	static int hit = 0;
	static int miss = 0;
	static int cycles = 0;
	static Memory memory;
	static ArrayList<String> buffer = new ArrayList<String>();
	static ArrayList<String> bufferDcache = new ArrayList<String>();
	static int levels;
	static int loadstorecycle = 0;// to track cycles for load and store
	static int enterscoreboard = 0; // size of instructions before data for SC

	public static int logarithm(int size) {
		int logarithmvalue = (int) ((Math.log10(size)) / (Math.log10(2)));
		return logarithmvalue;
	}

	public static ArrayList<String> buffer() {

		return buffer;
	}

	public static ArrayList<String> bufferDcache() {

		return bufferDcache;
	}

	public static String inttobinary(int x) {
		String binary = Integer.toBinaryString(x);
		for (int i = binary.length(); i < 16; i++) {
			binary = "0" + binary;
		}
		return binary;
	}

	public static int binarytoint(String binary) {
		if (binary.isEmpty()) {
			return 0;
		}
		int value = Integer.parseInt(binary, 2);
		return value;

	}

	// Direct Mapping (subdivision/search/insert)
	public static int[] subdivDirect(int address, Cache c) {
		int[] subdivDirect = new int[3];
		String binary = inttobinary(address);
		int displacement = logarithm(c.lineSize);
		int index = logarithm(c.blocks);
		int tag = 16 - displacement - index;
		String dis = binary.substring((16 - displacement), 16);
		String ind = binary.substring((16 - displacement - index),
				(16 - displacement));
		String tag1 = binary.substring(0, tag);
		subdivDirect[0] = binarytoint(tag1);
		subdivDirect[1] = binarytoint(ind);
		if (dis.isEmpty())
			subdivDirect[2] = 0;

		else
			subdivDirect[2] = binarytoint(dis);
		return subdivDirect;

	}

	// i will call subdivision to specify the index
	public static void searchDirect(int address, Cache c, int currentLevel,
			int levels) {
		int[] sub = subdivDirect(address, c);
		int tagtocompare = c.tags.get(sub[1]);
		cycles += c.numOfCycles;
		if (sub[0] == tagtocompare) {
			c.hit++;
			buffer.add(c.Icache.get(sub[1]).get(sub[2]));
		} else {
			currentLevel++;
			c.miss++;
			insertDirect(address, c, Memory.memory.get(address));
			search(address, levels, currentLevel, sub[2]);
		}

	}

	public static void searchDirectDcache(int address, Cache c,
			int currentLevel, int levels) {
		int[] sub = subdivDirect(address, c);
		// if (!(c.tags.isEmpty())) {
		int tagtocompare = c.tags.get(sub[1]);
		cycles += c.numOfCycles;
		loadstorecycle += c.numOfCycles;
		if (sub[0] == tagtocompare) {
			c.hit++;
			bufferDcache.add(c.Dcache.get(sub[1]).get(sub[2]));
			if (Tomasulo.towritethrough) { // for writing through
				String valuefromRegister = Tomasulo.valueofReg + "";
				c.Dcache.get(sub[1]).add(sub[2], valuefromRegister);
				// get(sub[1]).get(sub[2]) =
				// }
			}
		} else {
			currentLevel++;
			c.miss++;
			insertDirectDcache(address, c, Memory.memory.get(address));
			searchDcache(address, levels, currentLevel, sub[2]);
		}
		// return sub[1];

	}

	public static void insertDirect(int address, Cache c,
			ArrayList<String> value) {
		int[] sub = subdivDirect(address, c);
		cycles += c.numOfCycles;
		c.Icache.put(sub[1], value);
		c.tags.add(sub[1], sub[0]);
	}

	public static void insertDirectDcache(int address, Cache c,
			ArrayList<String> value) {
		int[] sub = subdivDirect(address, c);
		cycles += c.numOfCycles;
		loadstorecycle += c.numOfCycles;
		c.Dcache.put(sub[1], value);
		c.tags.add(sub[1], sub[0]);
	}

	// Fully associative mapping(subdivision/search/insert)
	public static int[] subdivFullyAssoc(int address, Cache c) {
		String addressBinary = inttobinary(address);
		int[] subDivFully = new int[2];
		int d = logarithm(c.lineSize);
		int t = 16 - d;
		String tag = addressBinary.substring(0, t - 1);
		String displacement = addressBinary.substring(t, 16);

		subDivFully[0] = binarytoint(tag);
		subDivFully[1] = binarytoint(displacement);

		return subDivFully;

	}

	public static void searchFullyAssoc(int address, Cache c, int currentLevel,
			int levels) {
		int[] addressDiv = subdivFullyAssoc(address, c);
		int tag = addressDiv[0];
		cycles += c.numOfCycles;
		for (int i = 0; i < c.blocks; i++) {
			// c.tags[i] is the index
			if (c.tags.get(i) == tag) {
				c.hit++;
				buffer.add(c.Icache.get(i).get(addressDiv[1]));
				return;
			}

			currentLevel++;
			c.miss++;
			insertFullyAssoc(address, c, Memory.memory.get(address));
			search(address, levels, currentLevel, addressDiv[1]);

		}
		return;
	}

	public static void searchFullyAssocDcache(int address, Cache c,
			int currentLevel, int levels) {
		int[] addressDiv = subdivFullyAssoc(address, c);
		int tag = addressDiv[0];
		cycles += c.numOfCycles;
		loadstorecycle += c.numOfCycles;
		for (int i = 0; i < c.blocks; i++) {
			// c.tags[i] is the index
			if (c.tags.get(i) == tag) {
				c.hit++;
				bufferDcache.add(c.Dcache.get(i).get(addressDiv[1]));
				if (Tomasulo.towritethrough) { // for writing through
					String valuefromRegister = Tomasulo.valueofReg + "";
					c.Dcache.get(i).add(addressDiv[1], valuefromRegister);
					// get(sub[1]).get(sub[2]) =
				}
				return;
			}
		}
		currentLevel++;
		c.miss++;
		insertFullyAssocDcache(address, c, Memory.memory.get(address));
		searchDcache(address, levels, currentLevel, addressDiv[1]);

		return;
	}

	public static void insertFullyAssoc(int address, Cache c,
			ArrayList<String> value) {
		int[] addressDiv = subdivFullyAssoc(address, c);
		int tag = addressDiv[0];
		cycles += c.numOfCycles;
		for (int i = 0; i < c.blocks; i++) {
			if (c.tags.get(i) == null) {
				c.tags.add(tag);
				c.Icache.put(i, value);
				return;
			}
		}
		c.tags.add(0, tag);
		return;
	}

	public static void insertFullyAssocDcache(int address, Cache c,
			ArrayList<String> value) {
		int[] addressDiv = subdivFullyAssoc(address, c);
		int tag = addressDiv[0];
		cycles += c.numOfCycles;
		loadstorecycle += c.numOfCycles;
		for (int i = 0; i < c.blocks; i++) {
			if (c.tags.get(i) == null) {
				c.tags.add(tag);
				c.Dcache.put(i, value);
				return;
			}
		}
		c.tags.add(0, tag);
		return;
	}

	// Set associative mapping (subdivision/search/subdivision)
	public static int[] subdivSetAssoc(int address, int c, int m, int L) {
		String add = inttobinary(address);
		int index = (int) (Math.log(c / m) / Math.log(2));
		int disp = (int) (Math.log(L) / Math.log(2));
		int tag = 16 - disp - index;
		int[] map = { binarytoint(add.substring(0, tag)),
				binarytoint(add.substring(tag, tag + index)),
				binarytoint(add.substring(tag + index, 16)) };
		return map;

	}

	public static void searchSetAssoc(Cache a, int address, int levels,
			int currentLevel) {
		int[] map = subdivSetAssoc(address, a.blocks, a.associativity,
				a.lineSize);
		int index = map[1] * a.associativity;
		cycles += a.numOfCycles;
		for (int i = 0; i < a.associativity; i++) {
			if (a.tags.get(i + index) == map[0]) {
				a.hit++;
				buffer.add(a.Icache.get(index + i).get(0));
				return;
			}
		}
		currentLevel++;
		a.miss++;
		insertSetAssoc(a, index, map[0]);
		search(address, levels, currentLevel, map[2]);

	}

	public static void searchSetAssocDcache(Cache a, int address, int levels,
			int currentLevel) {
		int[] map = subdivSetAssoc(address, a.blocks, a.associativity,
				a.lineSize);
		int index = map[1] * a.associativity;
		cycles += a.numOfCycles;
		loadstorecycle += a.numOfCycles;
		for (int i = 0; i < a.associativity; i++) {
			if (a.tags.get(i + index) == map[0]) {
				a.hit++;
				if (Tomasulo.towritethrough) { // for writing through
					String valuefromRegister = Tomasulo.valueofReg + "";
					//System.out.println("OK"+a.Dcache.get(index+i));
					a.Dcache.get(index + i).add( valuefromRegister);
					// get(sub[1]).get(sub[2]) =
				}
				bufferDcache.add(a.Dcache.get(index + i).get(0));
				return;
			}
		}
		currentLevel++;
		a.miss++;
		insertSetAssocDcache(a, index, map[0]);
		searchDcache(address, levels, currentLevel, map[2]);

	}

	public static void insertSetAssoc(Cache a, int index, int tag) {
		cycles += a.numOfCycles;
		for (int i = 0; i < a.associativity; i++) {
			if (a.tags.get(index + i).equals(null)) {
				a.tags.set(index + i, tag);
				return;
			}
		}
		a.tags.set(index, tag);
	}

	public static void insertSetAssocDcache(Cache a, int index, int tag) {
		cycles += a.numOfCycles;
		loadstorecycle += a.numOfCycles;
		for (int i = 0; i < a.associativity; i++) {
			if (a.tags.get(index + i).equals(null)) {
				a.tags.set(index + i, tag);
				return;
			}
		}
		a.tags.set(index, tag);
	}

	// public static int calculateCyles() {
	// return hit;
	//
	// }

	// search method is called on a miss in any cache search, it checks if the
	// current level cache
	// is not the last level, then check the associativity of the next level to
	// call it's
	// search method. if the current level is the last level it will add the
	// instruction to the
	// buffer from the memory
	public static void search(int address, int levels, int currentLevel,
			int disp) {
		if (currentLevel == 1 && currentLevel < levels) {
			if (c2.associativity == 1) {
				searchDirect(address, c2, currentLevel, levels);
			} else if (c2.associativity == c2.blocks) {
				searchFullyAssoc(address, c2, currentLevel, levels);
			} else {
				searchSetAssoc(c2, address, levels, currentLevel);
			}
		} else if (currentLevel == 2 && currentLevel < levels) {
			if (c3.associativity == 1) {
				searchDirect(address, c3, currentLevel, levels);
			} else if (c3.associativity == c3.blocks) {
				searchFullyAssoc(address, c3, currentLevel, levels);
			} else {
				searchSetAssoc(c3, address, levels, currentLevel);
			}
		} else if (currentLevel == levels) {
			cycles += Memory.memorylatency;
			buffer.add(Memory.memory.get(address).get(disp));
			return;
		}

	}

	public static void searchDcache(int address, int levels, int currentLevel,
			int disp) {
		if (currentLevel == 1 && currentLevel < levels) {
			if (c2.associativity == 1) {
				searchDirectDcache(address, c2, currentLevel, levels);
			} else if (c2.associativity == c2.blocks) {
				searchFullyAssocDcache(address, c2, currentLevel, levels);
			} else {
				searchSetAssocDcache(c2, address, levels, currentLevel);
			}
		} else if (currentLevel == 2 && currentLevel < levels) {
			if (c3.associativity == 1) {
				searchDirectDcache(address, c3, currentLevel, levels);
			} else if (c3.associativity == c3.blocks) {
				searchFullyAssocDcache(address, c3, currentLevel, levels);
			} else {
				searchSetAssocDcache(c3, address, levels, currentLevel);
			}
		} else if (currentLevel == levels) {
			cycles += Memory.memorylatency;
			loadstorecycle += Memory.memorylatency;
			bufferDcache.add(Memory.memory.get(address).get(disp));
			if (Tomasulo.towritethrough) { // for writing through
				String valuefromRegister = Tomasulo.valueofReg + "";
				Memory.memory.get(address).set(disp, valuefromRegister);

			}

			// buffer.add(memory.memory.get(address));
			return;
		}

	}

	@SuppressWarnings("deprecation")
	// public static void main(String[] args) {
	// int lineSize = 0;
	// // create memory and caches
	// Scanner in = new Scanner(System.in);
	// System.out.println("Please enter the line size of the Memory ");
	// memory = new Memory(Integer.parseInt(in.nextLine()));
	// System.out.println("Please enter the pipline width ");
	// Tomasulo.numOfways = Integer.parseInt(in.nextLine());
	// System.out.println("Please enter the queue size ");
	// Tomasulo.buffersize = Integer.parseInt(in.nextLine());
	// System.out.println("Please enter the latency of the Memory");
	// Memory.memorylatency = Integer.parseInt(in.nextLine());
	// System.out.println("Please enter the number of cache levels");
	// int NumOfCaches = Integer.parseInt(in.nextLine());
	// levels = NumOfCaches;
	// int[] size = new int[NumOfCaches]; // initialize an array for each
	// // cache
	// // level
	// int[] m = new int[NumOfCaches];
	// int[] cycles = new int[NumOfCaches];
	// for (int i = 0; i < NumOfCaches; i++) {
	// System.out.println("Please enter the size of cache " + i);
	// size[i] = Integer.parseInt(in.nextLine());
	// System.out.println("Please enter the Line size of cache " + i);
	// lineSize = Integer.parseInt(in.nextLine());
	// System.out.println("Please enter the associativity type of cache "
	// + i);
	// m[i] = Integer.parseInt(in.nextLine());
	// System.out
	// .println("Please enter the number of cycles required to access data in cache "
	// + i);
	// cycles[i] = Integer.parseInt(in.nextLine());
	// }
	// if (NumOfCaches == 1) {
	// c1 = new Cache(size[0], lineSize, m[0], cycles[0]);
	// } else if (NumOfCaches == 2) {
	// c1 = new Cache(size[0], lineSize, m[0], cycles[0]);
	// c2 = new Cache(size[1], lineSize, m[1], cycles[1]);
	// } else if (NumOfCaches == 3) {
	// c1 = new Cache(size[0], lineSize, m[0], cycles[0]);
	// c2 = new Cache(size[1], lineSize, m[1], cycles[1]);
	// c3 = new Cache(size[2], lineSize, m[2], cycles[2]);
	// }
	//
	// System.out
	// .println("Please enter the number of functional units you want in the Reservation Station.");
	// int number = Integer.parseInt(in.nextLine());
	// System.out
	// .println("Please specify which functional units you want in the Reservation Station. ");
	// String units = in.nextLine();
	// System.out
	// .println("Please specify the latencies required for each Functional Unit in the following order -->:addSub,ADDI,MULTD,DIVD,NAND,BEQ,JMP,JALR,RET");
	// String latencies = in.nextLine();
	// System.out
	// .println("Please enter the number of entries in the Reorder Buffer.");
	// int numROB = Integer.parseInt(in.nextLine());
	// ReorderBuffer ROB = new ReorderBuffer(numROB);
	//
	// File file = new File("src/Data.txt");
	// FileInputStream fis = null;
	// BufferedInputStream bis = null;
	// DataInputStream dis = null;
	// BufferedReader reader = null;
	//
	// try {
	// fis = new FileInputStream(file);
	// reader = new BufferedReader(new InputStreamReader(fis));
	// // Here BufferedInputStream is added for fast reading.
	// bis = new BufferedInputStream(fis);
	// dis = new DataInputStream(bis);
	//
	// String line = reader.readLine();
	// ArrayList<String> instructions = new ArrayList<String>();
	// boolean startdata = false;
	// while (line != null) {
	// if (line.contains("Data")) {
	// startdata = true;
	// enterscoreboard = instructions.size();
	// }
	// if (startdata && enterscoreboard != 0) {
	// ScoreBoard.setInstructions(instructions);
	// ReservationStation station = new ReservationStation(units, number);
	// Tomasulo.setTomasulo(latencies);
	// startdata = false;
	// }
	// instructions.add(line);
	// line = reader.readLine();
	// }
	// Memory.insertFirstOnceIntoMemory(instructions);
	// // RegisterStatus.registersUsed(instructions);
	//
	// // dis.available() returns 0 if the file does not have more lines.
	// while (dis.available() != 0) {
	//
	// // this statement reads the line from the file and print it to
	// // the console.
	// System.out.println(dis.readLine());
	// }
	//
	// // dispose all the resources after using them.
	// fis.close();
	// bis.close();
	// dis.close();
	//
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// int cycless = Tomasulo.Getcyclesforstore("2");
	//
	// ReservationStation.ManageTomasulo();
	// }
	public static void main(String[] args) {

		int lineSize = 1;
		// create memory and caches

		memory = new Memory(lineSize);
		Tomasulo.numOfways = 2;
		Tomasulo.buffersize = 3;
		int NumOfCaches = 2;
		levels = NumOfCaches;
		int[] size = new int[NumOfCaches]; // initialize an array for each
		// cache
		// level
		int[] m = new int[NumOfCaches];
		int[] cycles = new int[NumOfCaches];

		size[0] = 60;
		m[0] = 1; // associativity
		cycles[0] = 1;
		Memory.memorylatency = 1;
		size[1] = 18;
		m[1] = 1;
		cycles[1] = 1;
		if (NumOfCaches == 1) {
			c1 = new Cache(size[0], lineSize, m[0], cycles[0]);
		} else if (NumOfCaches == 2) {
			c1 = new Cache(size[0], lineSize, m[0], cycles[0]);
			c2 = new Cache(size[1], lineSize, m[1], cycles[1]);
		} else if (NumOfCaches == 3) {
			c1 = new Cache(size[0], lineSize, m[0], cycles[0]);
			c2 = new Cache(size[1], lineSize, m[1], cycles[1]);
			c3 = new Cache(size[2], lineSize, m[2], cycles[2]);
		}

		int number = 2;

		String units = "LOAD1,STORE1";
		String latencies = ("1,1,1,1,1,1,1,1");

		int numROB = 3;
		ReorderBuffer ROB = new ReorderBuffer(numROB);

		File file = new File("src/Data.txt");
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		BufferedReader reader = null;

		try {
			fis = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(fis));
			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			String line = reader.readLine();
			ArrayList<String> instructions = new ArrayList<String>();
			boolean startdata = false;
			while (line != null) {
				if (line.contains("Data")) {
					startdata = true;
					enterscoreboard = instructions.size();
				}
				if (startdata && enterscoreboard != 0) {
					ScoreBoard.setInstructions(instructions);
					ReservationStation station = new ReservationStation(units,
							number);
					Tomasulo tomasulo = new Tomasulo(latencies);// to get the RS
																// number !!
					startdata = false;
				}
				instructions.add(line);
				line = reader.readLine();
			}
			Memory.insertFirstOnceIntoMemory(instructions);
			// RegisterStatus.registersUsed(instructions);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {

				// this statement reads the line from the file and print it to
				// the console.
				System.out.println(dis.readLine());
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		RegisterStatus.setRegisters("R0,R1,R2,R3,R4,R5,R6,R7");
		RegisterFile initiate = new RegisterFile();
		// RegisterStatus
		ReservationStation.ManageTomasulo();
	}
}