public class RegisterStatus {

	static String[][] registerStatus;
	static int number;

	public static void setRegisters(String registers) {
		String[] registersUsed = registers.split(",");
		number = registersUsed.length;
		registerStatus = new String[2][number];

		for (int i = 0; i < number; i++) {
			registerStatus[0][i] = registersUsed[i];
			registerStatus[1][i] = "Empty";
		}

	}

	public static void printRegS() {
		for (int i = 0; i < number; i++) {
			System.out.print(registerStatus[0][i] + " ");
		}
		System.out.println();
		for (int i = 0; i < number; i++) {
			System.out.print(registerStatus[1][i] + " ");
		}
	}
	/*
	 * public static void registersUsed(ArrayList<String> instructions){ int
	 * size = instructions.size(); String [] instruction = new String[size];
	 * String [] splittedInstruction;
	 * 
	 * for(int i=0; i<size; i++ ){ instruction[i] = instructions.get(i);
	 * 
	 * splittedInstruction=instruction[i].split(","); for(int j=0;
	 * j<splittedInstruction.length;j++){ if(splittedInstruction[0] == "LD" ) {
	 * 
	 * } } } }
	 */

	// String[] parts = registers.split(",");
	// int length = parts.length;
}