import java.util.ArrayList;

public class ScoreBoard {

	static String[][] scoreBoard;
	static int size;
	static int startAddress;

	public static void setInstructions(ArrayList<String> instructions) {
		size = instructions.size();
		scoreBoard = new String[size][4]; // ScoreBoard size changed to 2

		startAddress = Integer.parseInt(instructions.get(0));
		for (int i = 0; i < size-1; i++) {

			scoreBoard[i][0] = instructions.get(i + 1);
			scoreBoard[i][1] = "None";
			scoreBoard[i][2] = "-1";// num in ROB
			scoreBoard[i][3] = "-1";// start cycles of fetch/issue/execute/write
			ReservationStation.cycles++;
		}

	}

	public static void printScoreB() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.print(scoreBoard[i][j] + " ");
			}
			System.out.println();

		}
	}

	// return index for BEQ only
	public static int returnIndexBEQ(String instruction, String Vj, String Vk) {

		for (int i = 0; i < scoreBoard.length; i++) {
			String[] compare = scoreBoard[i][0].split(",");
			if (compare[0] == instruction && compare[1] == Vj
					&& compare[2] == Vk)
				return i;
		}
		return -1;
	}

	public static int returnIndexJMP(String instruction, String Vj) {
		for (int i = 0; i < scoreBoard.length; i++) {
			String[] compare = scoreBoard[i][0].split(",");
			if (compare[0] == instruction && compare[1] == Vj)
				return i;
		}
		return -1;
	}

}