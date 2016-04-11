
public class ReorderBuffer {
	static int head;
	static int tail;
	static String ROB[][];
	static int numROB;

	public ReorderBuffer(int numROB) {
		ROB = new String[numROB][4];
		this.numROB = numROB;
		for (int i = 0; i < numROB; i++) {
			for (int j = 0; j < 4; j++) {
				ROB[i][j] = "Empty";
			}
		}

	}

	public static void printROB() {
		for (int i = 0; i < numROB; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.print(ROB[i][j] + " ");
			}
			System.out.println();

		}
	}

}