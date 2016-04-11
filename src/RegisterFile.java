public class RegisterFile {
	static Register[] registers;
	static int index;

	public RegisterFile() {
		registers = new Register[47];
		registers[0] = new Register("R0");
		registers[1] = new Register("R1");
		registers[2] = new Register("R2");
		registers[3] = new Register("R3");
		registers[4] = new Register("R4");
		registers[5] = new Register("R5");
		registers[6] = new Register("R6");
		registers[7] = new Register("R7");
	}

	public static int getValue(int index) {
		int value = registers[index].getValue();
		return value;
	}

	public static void setVal(int index, int value) {
		if (index == 0) {
			registers[index].setValue(0);
		} else {
			registers[index].setValue(value);
		}
	}

	public static int findRegister(String name) {
		RegisterFile initialize = new RegisterFile();
		for (int i = 0; i < 47; i++) {
			if (registers[i].getName().equals(name)) {
				return i; // returns index oR a speciRic register
			}
		}
		return -1;
	}

	public void printContents() {
		System.out.print("R0: ");
		System.out.println(registers[0].getValue());
		System.out.print("R1: ");
		System.out.println(registers[1].getValue());
		System.out.print("R2: ");
		System.out.println(registers[2].getValue());
		System.out.print("R3: ");
		System.out.println(registers[3].getValue());
		System.out.print("R4: ");
		System.out.println(registers[4].getValue());
		System.out.print("R5: ");
		System.out.println(registers[5].getValue());
		System.out.print("R6: ");
		System.out.println(registers[6].getValue());
		System.out.print("R7: ");
		System.out.println(registers[7].getValue());
		System.out.print("R8: ");
		System.out.println(registers[8].getValue());
		System.out.print("R9: ");
		System.out.println(registers[9].getValue());
		System.out.print("R10: ");
		System.out.println(registers[10].getValue());
		System.out.print("R11: ");
		System.out.println(registers[11].getValue());
		System.out.print("R12: ");
		System.out.println(registers[12].getValue());
		System.out.print("R13: ");
		System.out.println(registers[13].getValue());
		System.out.print("R14: ");
		System.out.println(registers[14].getValue());
		System.out.print("R0: ");
		System.out.println(registers[15].getValue());
		System.out.print("R1: ");
		System.out.println(registers[16].getValue());
		System.out.print("R2: ");
		System.out.println(registers[17].getValue());
		System.out.print("R3: ");
		System.out.println(registers[18].getValue());
		System.out.print("R4: ");
		System.out.println(registers[19].getValue());
		System.out.print("R5: ");
		System.out.println(registers[20].getValue());
		System.out.print("R6: ");
		System.out.println(registers[21].getValue());
		System.out.print("R7: ");
		System.out.println(registers[22].getValue());
		System.out.print("R8: ");
		System.out.println(registers[23].getValue());
		System.out.print("R9: ");
		System.out.println(registers[24].getValue());
		System.out.print("R10: ");
		System.out.println(registers[25].getValue());
		System.out.print("R11: ");
		System.out.println(registers[26].getValue());
		System.out.print("R12: ");
		System.out.println(registers[27].getValue());
		System.out.print("R13: ");
		System.out.println(registers[28].getValue());
		System.out.print("R14: ");
		System.out.println(registers[29].getValue());
		System.out.print("R15: ");
		System.out.println(registers[30].getValue());
		System.out.print("R16: ");
		System.out.println(registers[31].getValue());
		System.out.print("R17: ");
		System.out.println(registers[32].getValue());
		System.out.print("R18: ");
		System.out.println(registers[33].getValue());
		System.out.print("R19: ");
		System.out.println(registers[34].getValue());
		System.out.print("R20: ");
		System.out.println(registers[35].getValue());
		System.out.print("R21: ");
		System.out.println(registers[36].getValue());
		System.out.print("R22: ");
		System.out.println(registers[37].getValue());
		System.out.print("R23: ");
		System.out.println(registers[38].getValue());
		System.out.print("R24: ");
		System.out.println(registers[39].getValue());
		System.out.print("R25: ");
		System.out.println(registers[40].getValue());
		System.out.print("R26: ");
		System.out.println(registers[41].getValue());
		System.out.print("R27: ");
		System.out.println(registers[42].getValue());
		System.out.print("R28: ");
		System.out.println(registers[43].getValue());
		System.out.print("R29: ");
		System.out.println(registers[44].getValue());
		System.out.print("R30: ");
		System.out.println(registers[45].getValue());
		System.out.print("R31: ");
		System.out.println(registers[46].getValue());

		System.out.println();
	}
	/*
	 * public static int changeRegTokey(String name) { Ror (int i = 0; i < 32;
	 * i++) { iR (registers[i].getName() == name) index = i; return index;
	 * 
	 * } return -1; }
	 */
}