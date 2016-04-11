import java.util.ArrayList;

public class Tomasulo {

	static int addsub;
	static int ADDI;
	static int MULTD; // latencies
	static int DIVD;
	static int[] Add; // add and sub
	static int[] Load;
	static int[] Mult; // mult and div
	static int[] ADDDl;
	static int[] Store;
	static boolean stall;
	static int stallCycles;
	static int[] ExecuteFinish;
	static String regCheck;
	static int finishExecute;
	static int numOfways;
	static int currentLevel;
	static boolean towritethrough = false;
	static int valueofReg = 0; // value from register to be written in Memory
	static boolean tobranch;
	static int NAND;
	static int BEQ;
	static int JMP;
	static int JALR;
	static int RET;

	static int startExecute;

	static int loadLatency;
	static int buffersize;
	// static ArrayList<String> queue = new ArrayList<String>();
	static String[] queue;
	static int instTofetch = 0;

	static boolean finished = false;

	public Tomasulo(String latencies) {
		String[] latency = latencies.split(",");
		this.addsub = Integer.parseInt(latency[0]);
		this.ADDI = Integer.parseInt(latency[1]);
		this.MULTD = Integer.parseInt(latency[2]);
		this.DIVD = Integer.parseInt(latency[3]);
		this.NAND = Integer.parseInt(latency[4]);
		queue = new String[buffersize];
		stall = false;
		stallCycles = ReservationStation.cycles;
		finishExecute = ReservationStation.cycles;
		ExecuteFinish = new int[ReservationStation.number];
		this.currentLevel = 0;
		for (int m = 0; m < buffersize; m++) {
			queue[m] = "empty";
		}
		Add = new int[20];
		Load = new int[20];
		Mult = new int[20];
		ADDDl = new int[20];
		Store = new int[20];

	}

	public static void Fetch() {
		// ScoreBoard.scoreBoard[instTofetch][0];
		for (int l = 0; l < buffersize && l < ScoreBoard.size; l++) {
			if (queue[l].equalsIgnoreCase("empty")) {
				if (ReservationStation.PC < Memory.startData) {
					if (MainClass.c1.associativity == 1) {
						MainClass.searchDirect(ReservationStation.PC,
								MainClass.c1, 0, MainClass.levels);
					} else if (MainClass.c1.associativity == MainClass.c1.blocks) {
						MainClass.searchFullyAssoc(ReservationStation.PC,
								MainClass.c1, 0, MainClass.levels);
					} else {
						MainClass.searchSetAssoc(MainClass.c1,
								ReservationStation.PC, MainClass.levels, 0);
					}
					queue[l] = ScoreBoard.scoreBoard[l][0];
					ScoreBoard.scoreBoard[l][1] = "fetched";
					ScoreBoard.scoreBoard[l][3] = Integer
							.toString(ReservationStation.cycles);
					System.out.println("The instruction added in buffer is: "
							+ queue[l]);
					instTofetch++;
				}
				if (instTofetch == MainClass.c1.lineSize) {
					ReservationStation.PC++;
					instTofetch = 0;
				}
			}
		}
		instTofetch = 0;

	}

	public static void insert(String reg) {
		for (int i = 0; i < RegisterStatus.number; i++) {
			if (RegisterStatus.registerStatus[0][i].equalsIgnoreCase(reg)) {
				RegisterStatus.registerStatus[1][i] = Integer
						.toString(ReorderBuffer.tail);
			}
		}
	}

	public static boolean checkOp(String reg) {
		for (int i = 0; i < RegisterStatus.number; i++) {
			if (RegisterStatus.registerStatus[0][i].equalsIgnoreCase(reg)) {
				if (RegisterStatus.registerStatus[1][i].isEmpty()
						|| RegisterStatus.registerStatus[1][i]
								.equalsIgnoreCase("Empty")) {
					return true;
				} else {

					regCheck = RegisterStatus.registerStatus[1][i];
					return false;
				}
			}
		}
		return false;

	}

	public static void Issue() {
		String op;
		String[] operands = new String[3];
		String[] parts = new String[3];// to get all the split and send only the
										// regs to operands
		for (int i = 0; i < ScoreBoard.size - 1; i++) {
			if (!(ScoreBoard.scoreBoard[i][3].equalsIgnoreCase(Integer
					.toString(ReservationStation.cycles)))) {
				if (ScoreBoard.scoreBoard[i][1].equalsIgnoreCase("fetched")) {
					if (ReorderBuffer.ROB[ReorderBuffer.tail][0]
							.equalsIgnoreCase("Empty")) {
						op = ScoreBoard.scoreBoard[i][0].split(" ")[0]
								.split(",")[0];
						parts = ScoreBoard.scoreBoard[i][0].split(" ")[0]
								.split(",");
						operands[0] = parts[1];
						operands[1] = parts[2];
						operands[2] = parts[3];
						// System.out.println("The operations issued are: "+op);
						ScoreBoard.scoreBoard[i][1] = "Issued";
						ScoreBoard.scoreBoard[i][3] = Integer
								.toString(ReservationStation.cycles);
						ScoreBoard.scoreBoard[i][2] = ReorderBuffer.tail + "";// location
																				// of
																				// SB
																				// in
																				// ROB
						// System.out.println("The Score Board in Issue at instruction"+i+"is: "+ScoreBoard.scoreBoard[i][2]
						// );
						if (op.equalsIgnoreCase("LD")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.equalsIgnoreCase("load")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "LD";
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReservationStation.RS[j][8] = operands[2];
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "LD";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									insert(operands[0]);
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;
								}
							}
						} else if (op.equalsIgnoreCase("SD")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0]
										.contains("STORE")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "SD";
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									// if
									if (checkOp(operands[0])) {
										ReservationStation.RS[j][4] = operands[0];
									} else {
										ReservationStation.RS[j][6] = regCheck;
									}
									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReservationStation.RS[j][8] = operands[2];
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "SD";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;
								}
							}

						}

						else if (op.equalsIgnoreCase("Mult")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.equalsIgnoreCase("mult")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "Mult";
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									if (checkOp(operands[2])) {
										ReservationStation.RS[j][4] = operands[2];
									} else {
										ReservationStation.RS[j][6] = regCheck;
									}

									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "FP";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									insert(operands[0]);
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						}

						else if (op.equalsIgnoreCase("ADD")
								|| op.equalsIgnoreCase("Sub")
								|| op.equalsIgnoreCase("ADDI")
								|| op.equalsIgnoreCase("SUBI")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 3)
										.equalsIgnoreCase("ADD")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									if (op.equalsIgnoreCase("ADD")
											|| op.equalsIgnoreCase("ADDI")) {
										ReservationStation.RS[j][2] = "ADD";
									} else {
										ReservationStation.RS[j][2] = "SUB";
									}
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}

									if (checkOp(operands[2])
											|| (op.equalsIgnoreCase("ADDi") || op
													.equalsIgnoreCase("SubI"))) {
										ReservationStation.RS[j][4] = operands[2];
									} else {
										ReservationStation.RS[j][6] = regCheck;
									}

									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "INT";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									insert(operands[0]);
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						} else if (op.equalsIgnoreCase("NAND")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 3)
										.equalsIgnoreCase("ADD")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "NAND";
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}

									if (checkOp(operands[2])) {
										ReservationStation.RS[j][4] = operands[2];
									} else {
										ReservationStation.RS[j][6] = regCheck;
									}

									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "INT";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									insert(operands[0]);
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						} else if (op.equalsIgnoreCase("ADDD")
								|| op.equalsIgnoreCase("SUBD")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.equalsIgnoreCase("ADDD")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									if (op.equalsIgnoreCase("ADDD")) {
										ReservationStation.RS[j][2] = "add";
									} else {
										ReservationStation.RS[j][2] = "sub";
									}
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									if (checkOp(operands[2])) {
										ReservationStation.RS[j][4] = operands[2];
									} else {
										ReservationStation.RS[j][6] = regCheck;
									}

									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);

								}
							}
						} else if (op.equalsIgnoreCase("JMP")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.equalsIgnoreCase("add")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "JMP";
									if (checkOp(operands[0])) {
										ReservationStation.RS[j][3] = operands[0];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}

									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReservationStation.RS[j][8] = operands[2];
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "JMP";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						} else if (op.equalsIgnoreCase("BEQ")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.contains("ADD")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "BEQ";
									if (checkOp(operands[0])) {
										ReservationStation.RS[j][3] = operands[0];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][4] = operands[1];
									} else {
										ReservationStation.RS[j][6] = regCheck;
									}
									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReservationStation.RS[j][8] = operands[2];
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "BEQ";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = operands[0];
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						} else if (op.equalsIgnoreCase("JALR")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.equalsIgnoreCase("add")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "JALR";
									if (checkOp(operands[1])) {
										ReservationStation.RS[j][3] = operands[1];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReservationStation.RS[j][8] = operands[1];
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "JALR";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = "Mem";
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									insert(operands[0]);
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						} else if (op.equalsIgnoreCase("RET")) {
							for (int j = 0; j < ReservationStation.number; j++) {
								if (ReservationStation.RS[j][0].substring(0, 4)
										.equalsIgnoreCase("add")
										&& ReservationStation.RS[j][1]
												.equalsIgnoreCase("0")) {
									ReservationStation.RS[j][1] = "1";
									ReservationStation.RS[j][2] = "RET";
									if (checkOp(operands[0])) {
										ReservationStation.RS[j][3] = operands[0];
									} else {
										ReservationStation.RS[j][5] = regCheck;
									}
									ReservationStation.RS[j][7] = Integer
											.toString(ReorderBuffer.tail);
									ReservationStation.RS[j][8] = operands[1];
									ReorderBuffer.ROB[ReorderBuffer.tail][0] = "RET";
									ReorderBuffer.ROB[ReorderBuffer.tail][1] = "Mem";
									ReorderBuffer.ROB[ReorderBuffer.tail][3] = "N";
									ReorderBuffer.tail++;
									for (int m = 0; m < buffersize; m++) {
										if (ScoreBoard.scoreBoard[i][0]
												.equalsIgnoreCase(queue[m])) {
											queue[m] = "empty";
										}
									}
									return;

								}
							}
						}
					}
				}
			}
		}
	}

	public static void noOperation() {
		ReservationStation.cycles++;// msh howa kida kida by3mel ++ lel
									// clockcycle mn fo2?
	}

	public static void WriteResult() {
		String functionunit = ""; // gets the name of the functional unit for
									// the result
		char unitnumber;
		String DestinationRegister = ""; // gets the destination register
		int numberofROB; // to get the corresponding ROB index from ScoreBoard
		for (int i = 0; i < ScoreBoard.size - 1; i++) { // go through el Score
														// board
			if (!ScoreBoard.scoreBoard[i][3].equalsIgnoreCase(Integer
					.toString(ReservationStation.cycles))) { // board
				if (ScoreBoard.scoreBoard[i][1].equalsIgnoreCase("executed")) { // check
																				// if
					// Scorboard
					// = Execute
					numberofROB = Integer.parseInt(ScoreBoard.scoreBoard[i][2]);
					DestinationRegister = ReorderBuffer.ROB[numberofROB][1];

					for (int j = 0; j < ReservationStation.number; j++) {
						if (ReservationStation.RS[j][7] == "")
							continue;
						int destination = Integer
								.parseInt(ReservationStation.RS[j][7]);
						if (destination == numberofROB) { // checks if DEST =
															// no. in
															// ROB to get the
															// position in RS
							if (ScoreBoard.scoreBoard[i][0].contains("SD")) {
								if ((ReservationStation.RS[j][6].equals("")) // no
																				// dependencies
										&& (ExecuteFinish[j] == 0)) {// cycles
																		// when
																		// finish

									String Address = ReservationStation.RS[j][8];
									int cycles = WriteThrough(j, Address);
									ExecuteFinish[j] = ReservationStation.cycles
											+ cycles;
									continue;// to look for another instruction
												// waiting for writing

								}
								if (ExecuteFinish[j] != ReservationStation.cycles) {
									continue;
								}
							}
							RemoveDependenciesinRS(numberofROB + "");
							for (int k = 2; k < 9; k++) { // clears the RS row
								ReservationStation.RS[j][k] = "";
							}
							ExecuteFinish[j] = 0;
							ReservationStation.RS[j][1] = "0"; // Change busy in
																// RS to 0

							functionunit = ReservationStation.RS[j][0];
							unitnumber = functionunit.charAt(functionunit
									.length() - 1);
							functionunit = functionunit.substring(0,
									functionunit.length() - 1);
							placeinROBfromFU(functionunit, unitnumber,
									numberofROB, DestinationRegister); // ROB
																		// write
							ScoreBoard.scoreBoard[i][1] = "written"; // Change
							// Scoreboard to
							// written
							ScoreBoard.scoreBoard[i][3] = Integer
									.toString(ReservationStation.cycles);
							ReorderBuffer.ROB[numberofROB][3] = "1"; // 1 =
																		// available
																		// at
							// the
							// corresponding
							// ROB // value}
							return;
						}

					}
				}
			}
		}
	}

	// Address in the memory
	public static int WriteThrough(int RowatRS, String Address) { // helpermethod
																	// to write
																	// the Value
																	// in
																	// ScoreBoard
		String Register = ReservationStation.RS[RowatRS][3];// to get register
		int RegNumber = Integer.parseInt(Register.substring(1,
				Register.length()));
		valueofReg = RegisterFile.registers[RegNumber].getValue();
		towritethrough = true;
		int latency = Getcyclesforstore(Address);
		towritethrough = false;
		return latency;
	}

	public static int Getcyclesforstore(String Address) { // gets
															// cycles
															// needed
		// for the store
		MainClass.loadstorecycle = 0;
		int Addr = Integer.parseInt(Address);
		if (MainClass.c1.associativity == 1) {
			MainClass.searchDirectDcache(Addr, MainClass.c1, currentLevel,
					MainClass.levels);

		} else if (MainClass.c1.associativity == MainClass.c1.blocks) {
			MainClass.searchFullyAssocDcache(Addr, MainClass.c1, currentLevel,
					MainClass.levels);

		} else {
			MainClass.searchSetAssocDcache(MainClass.c1, Addr,
					MainClass.levels, currentLevel);
		}
		return MainClass.loadstorecycle;
	}

	// checks if any instruction is waiting for the value, removes from the Q,
	// place in V
	public static void RemoveDependenciesinRS(String DestinationRegister) {
		String Register = ReorderBuffer.ROB[Integer
				.parseInt(DestinationRegister)][1];
		for (int i = 0; i < ReservationStation.number; i++) {
			if (ReservationStation.RS[i][5].equals(DestinationRegister)) {
				ReservationStation.RS[i][3] = Register;
				ReservationStation.RS[i][5] = "";
			}
			if (ReservationStation.RS[i][6].equals(DestinationRegister)) {
				ReservationStation.RS[i][4] = Register;
				ReservationStation.RS[i][6] = "";
			}
		}
	}

	// gets the value from FU to write to ROB also it sets the value of the
	// register
	public static void placeinROBfromFU(String functionunit, char unitnumber,
			int numberofROB, String DestinationRegister) {
		int unit = Character.getNumericValue(unitnumber); // gets the Unit
															// Number
		char registernumber = DestinationRegister.charAt(DestinationRegister
				.length() - 1); // gets the dest. register number
		int valuetowrite = 0;
		if (functionunit.equalsIgnoreCase("Add")) {
			valuetowrite = Add[unit];
		} else if (functionunit.equalsIgnoreCase("Load")) {
			valuetowrite = Load[unit];
		} else if (functionunit.equalsIgnoreCase("Mult")) {
			valuetowrite = Mult[unit];
		} else if (functionunit.equalsIgnoreCase("ADDDl")) {
			valuetowrite = ADDDl[unit];
		} else if (functionunit.equalsIgnoreCase("Store")) {
			valuetowrite = Store[unit];
		}
		ReorderBuffer.ROB[numberofROB][2] = valuetowrite + "";
		unit = Character.getNumericValue(registernumber);
		RegisterFile.setVal(unit, valuetowrite);
	}

	public static int FindCorrespondingSB(String instruction) {
		int index;
		for (int i = 0; i < ScoreBoard.size; i++) {
			if (ScoreBoard.scoreBoard[i][0].equals(instruction)) {
				index = i;
				return index;
			}
		}
		return -1;

	}

	public static void Execute() {
		int result;
		int loadAddress;
		int storeAddress;
		int storeValue;
		String ROBDestination;
		int prediction;
		String Qj = "";
		String Qk = "";// gets the pos of ROB for dependencies
		int indexofregister = 0; // gets the index of R6 = 6
		Register firstOperand = null; // gets the Vj Register
		Register SecondOperand = null; // gets the Vk Register
		int Operand1 = 0; // gets the value of the first Register
		int Operand2 = 0; // gets the value of the second reg

		for (int i = 0; i < ReservationStation.number; i++) {

			// check if this station contains an instruction
			if (ReservationStation.RS[i][1] != "0") {
				String RSDestination = ReservationStation.RS[i][7];

				for (int j = 0; j < ScoreBoard.size - 1; j++) {
					if (ScoreBoard.scoreBoard[j][2].equals(RSDestination)) {
						if (ScoreBoard.scoreBoard[j][1]
								.equalsIgnoreCase("Issued")) {
							if (!ScoreBoard.scoreBoard[j][3]
									.equalsIgnoreCase(Integer
											.toString(ReservationStation.cycles))) {
								String name = ReservationStation.RS[i][0];

								// the index of the current functional unit in
								// the
								// reservation station.
								// i.e load1, index=1....load2, index=2.
								int index = Character.getNumericValue(name
										.charAt(name.length() - 1));
								// the name of the operation
								String operation = ReservationStation.RS[i][2];
								// 1st source operand
								String Vj = ReservationStation.RS[i][3];
								if (Vj == "") {
									Qj = ReservationStation.RS[i][5];

								}
								// Vj = "F0"
								else {
									indexofregister = Character
											.getNumericValue(Vj.charAt(Vj
													.length() - 1));
									firstOperand = RegisterFile.registers[indexofregister];
									// int Operand1 = Integer.parseInt(Vj);
									// lazm aroo7 agib el value ml data memory
									Operand1 = firstOperand.getValue();
								}
								String Vk = ReservationStation.RS[i][4];
								if (Vk == "") {
									Qk = ReservationStation.RS[i][6]; // tackling
																		// the
																		// Qk in
																		// LOAD
																		// !!!!!!!!!!!!!!

								} else {
									indexofregister = Character
											.getNumericValue(Vk.charAt(Vk
													.length() - 1));
									Register secondOperand = RegisterFile.registers[indexofregister];
									Operand2 = secondOperand.getValue();
								}

								if ((Qj != "" || Qk != "")) {

									stall = true;
									int startStall = ReservationStation.cycles;
									stallCycles++;

								} else {
									int startExecute = ReservationStation.cycles
											+ stallCycles;
									stall = false;
									if (operation.equalsIgnoreCase("add")) {
										if (ExecuteFinish[i] == 0) {
											result = FunctionalUnits.add(
													Operand1, Operand2);

											Add[index] = result;
											ExecuteFinish[i] = ReservationStation.cycles
													+ addsub - 1;
										}

										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}
									} else if (operation
											.equalsIgnoreCase("sub")) {
										if (ExecuteFinish[i] == 0) {
											result = FunctionalUnits.sub(
													Operand1, Operand2);
											Add[index] = result;
											ExecuteFinish[i] = ReservationStation.cycles
													+ addsub - 1;
										}
										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}

									} else if (operation
											.equalsIgnoreCase("mult")) {
										if (ExecuteFinish[i] == 0) {
											result = FunctionalUnits.mult(
													Operand1, Operand2);
											Mult[index] = result;
											ExecuteFinish[i] = ReservationStation.cycles
													+ MULTD - 1;
										}
										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}

									} else if (operation
											.equalsIgnoreCase("div")) {
										if (ExecuteFinish[i] == 0) {
											result = FunctionalUnits.div(
													Operand1, Operand2);
											Mult[index] = result;
											ExecuteFinish[i] = ReservationStation.cycles
													+ DIVD - 1;
										}
										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}

									} else if (operation.equalsIgnoreCase("LD")) {
										if (ExecuteFinish[i] == 0) {
											// Step1
											// value of Vj+A in RS

											ReservationStation.RS[i][8] = Operand1
													+ Integer
															.parseInt(ReservationStation.RS[i][8])
													+ "";

											// the Address i will access the
											// Data
											// memory
											// with
											loadAddress = Integer
													.parseInt(ReservationStation.RS[i][8]);
											// Step2
											// get from Memory
											MainClass.loadstorecycle = 0;
											if (MainClass.c1.associativity == 1) {
												MainClass.searchDirectDcache(
														loadAddress,
														MainClass.c1,
														currentLevel,
														MainClass.levels);

												result = Integer
														.parseInt(MainClass.bufferDcache
																.get(0));
												Load[index] = result;

											} else if (MainClass.c1.associativity == MainClass.c1.blocks) {
												MainClass
														.searchFullyAssocDcache(
																loadAddress,
																MainClass.c1,
																currentLevel,
																MainClass.levels);
												result = Integer
														.parseInt(MainClass
																.bufferDcache()
																.get(0));
												Load[index] = result;

											} else {
												MainClass.searchSetAssocDcache(
														MainClass.c1,
														loadAddress,
														MainClass.levels,
														currentLevel++);
												result = Integer
														.parseInt(MainClass
																.bufferDcache()
																.get(0));
												Load[index] = result;

											}

											ROBDestination = ReorderBuffer.ROB[Integer
													.parseInt(RSDestination)][1];
											char registerdestinationindex = ROBDestination
													.charAt(ROBDestination
															.length() - 1);
											Register DestinationReg = RegisterFile.registers[Character
													.getNumericValue(registerdestinationindex)];
											DestinationReg.setValue(result);
											ExecuteFinish[i] = ReservationStation.cycles
													+ MainClass.loadstorecycle;
										}

										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}
									}

									else if (operation.equalsIgnoreCase("SD")) {
										ReservationStation.RS[i][8] = Operand1
												+ Integer
														.parseInt(ReservationStation.RS[i][8])
												+ "";
										ScoreBoard.scoreBoard[j][1] = "Executed";
										ScoreBoard.scoreBoard[j][3] = Integer
												.toString(ReservationStation.cycles);

										// Address to store at in Data Memory
										// storeAddress =
										// Integer.parseInt(Operand1
										// + ReservationStation.RS[i][8]);
										// value to store at the memory
										// storeValue = Operand2;
										// write back will store it

									} else if (operation
											.equalsIgnoreCase("BEQ")) {
										tobranch = true;
										ReservationStation.totalBranch++;
										// String instruction = "BEQ";
										// check my prediction,taken or not
										prediction = ReservationStation.BranchPrediction[i];
										ScoreBoard.scoreBoard[j][1] = "Executed";
										ScoreBoard.scoreBoard[j][3] = Integer
												.toString(ReservationStation.cycles);

										// int indexReturned = ScoreBoard
										// .returnIndexBEQ(instruction, Vj, Vk);
										// pc+1
										// int BranchAddress =
										// ScoreBoard.startAddress
										// + indexReturned + 1;
										if (Operand1 - Operand2 == 0) {
											// ReservationStation.RS[i][8]+=
											// BranchAddress;
											int A = Integer
													.parseInt(ReservationStation.RS[i][8]);

											if (prediction == 1) {
												// my prediction was right
												// then calculate the new
												// address
												// ReservationStation.RS[i][8]
												// +=
												// BranchAddress;
												ReservationStation.PC = ReservationStation.PC
														+ 1 + A;
											} else {
												// else
												// reset the ROB
												ReservationStation.BranchMissPrediction++;
												resetROB();
											}

										} else {
											if (prediction == 0) {
												ReservationStation.PC = ReservationStation.PC + 1;

											} else {
												ReservationStation.BranchMissPrediction++;
												resetROB();
											}

										}

									} else if (operation
											.equalsIgnoreCase("JMP")) {
										// PC+1+regA+imm
										// String instruction = "JMP";
										// int indexReturned = ScoreBoard
										// .returnIndexJMP(instruction, Vj);
										// int JMPAddress =
										// ScoreBoard.startAddress
										// + indexReturned + 1;
										// int JMPAddress
										// =ReservationStation.PC;
										// ReservationStation.RS[i][8] +=
										// JMPAddress;
										int imm = Integer
												.parseInt(ReservationStation.RS[i][8]);
										ReservationStation.PC = ReservationStation.PC
												+ 1 + Operand1 + imm;

									} else if (operation
											.equalsIgnoreCase("RET")) {
										// operand1 is the value of Vj so its
										// the
										// address stored in it
										// but how to actually branch??
										ReservationStation.PC = Operand1;
									} else if (operation
											.equalsIgnoreCase("JALR")) {
										// JALR regA, regB
										// PC+1 in regA
										// branches unconditionally to address
										// in
										// regB
										ReservationStation.PC = ReservationStation.PC + 1;
										firstOperand
												.setValue(ReservationStation.PC);

									} else if (operation
											.equalsIgnoreCase("NAND")) {
										if (ExecuteFinish[i] == 0) {
											result = FunctionalUnits.nand(
													Operand1, Operand2);

											Add[index] = result;
											ExecuteFinish[i] = ReservationStation.cycles
													+ NAND - 1;
										}
										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}

									} else if (operation
											.equalsIgnoreCase("ADDI")) {
										if (ExecuteFinish[i] == 0) {
											result = FunctionalUnits.nand(
													Operand1, Operand2);

											Add[index] = result;
											ExecuteFinish[i] = ReservationStation.cycles
													+ ADDI - 1;
										}
										if (ReservationStation.cycles == ExecuteFinish[i]) {
											ScoreBoard.scoreBoard[j][1] = "Executed";
											ScoreBoard.scoreBoard[j][3] = Integer
													.toString(ReservationStation.cycles);

										}
									}
								}

							}

						}
					}

				}
			}
		}
	}

	public static void Commit() {
		for (int i = 0; i < ScoreBoard.size - 1; i++) {
			if (ScoreBoard.scoreBoard[i][1].equalsIgnoreCase("written")) {
				if (!(ScoreBoard.scoreBoard[i][3].equalsIgnoreCase(Integer
						.toString(ReservationStation.cycles)))) {

					if (canCommit(i)) {
						for (int j = 0; j < 4; j++) {
							ReorderBuffer.ROB[Integer
									.parseInt(ScoreBoard.scoreBoard[i][2])][j] = "Empty";
						}
						ScoreBoard.scoreBoard[i][1] = "Committed";
						ScoreBoard.scoreBoard[i][2] = "";
						ReorderBuffer.head++;
					}
					if (i == ScoreBoard.size - 2) {
						finished = true;
					}
				}

			} else {
				return;
			}
		}
	}

	public static boolean canCommit(int i) {
		for (int j = 0; j < i; j++) {
			if (!ScoreBoard.scoreBoard[j][1].equalsIgnoreCase("committed")) {
				return false;
			}
		}
		return true;
	}

	public static void resetROB() {
		for (int i = 0; i < ReorderBuffer.ROB.length; i++) {
			for (int j = 0; j < ReorderBuffer.ROB.length; j++) {
				ReorderBuffer.ROB[i][j] = "-1";
			}
		}
	}

}
