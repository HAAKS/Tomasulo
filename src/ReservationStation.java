public class ReservationStation {

	static String[][] RS;
	static int number;
	public static int cycles = 0;
	static int[] BranchPrediction;
	static int PC;
	static int BranchMissPrediction;
	static int totalBranch;

	public ReservationStation(String inputNames, int number) {
		RS = new String[number][9];
		BranchPrediction = new int[number];
		this.number = number;
		String[] parts = inputNames.split(",");
		for (int i = 0; i < parts.length; i++) {
			RS[i][0] = parts[i];
			RS[i][1] = "0";
			RS[i][2] = "";
			RS[i][3] = "";
			RS[i][4] = "";
			RS[i][5] = "";
			RS[i][6] = "";
			RS[i][7] = "";
			RS[i][8] = "";

		}
		PC = ScoreBoard.startAddress;
		cycles = 0;
	}

	public static void ManageTomasulo() {
		while (!Tomasulo.finished) {
			cycles++;
			// System.out.println(cycles);
			Tomasulo.Fetch();
			for (int i = 0; i < Tomasulo.numOfways; i++) {
				Tomasulo.Issue();
			}
			Tomasulo.Execute();
			Tomasulo.WriteResult();
			Tomasulo.Commit();
		}
		float IPC = ScoreBoard.size;
		float cycless = cycles;
		float answer = IPC / cycless;
		System.out.println("The total execution time=" + cycles);
		System.out.println("IPC= " + answer);
		float hit1 = ((MainClass.c1.hit));
		float miss1 = MainClass.c1.miss;
		float result1 = hit1 / (hit1 + miss1);

		if (MainClass.levels == 1) {
			System.out.println("Hit ratio for cache 1= " + result1);
		}
		if (MainClass.levels == 2) {
			float hit2 = MainClass.c2.hit;
			float miss2 = MainClass.c2.miss;
			float result2 = hit2 / (hit2 + miss2);
			System.out.println("Hit ratio for cache 1= " + result1);
			System.out.println("Hit ratio for cache 2= " + result2);
		}
		if (MainClass.levels == 3) {
			float hit2 = MainClass.c2.hit;
			float miss2 = MainClass.c2.miss;
			float result2 = hit2 / (hit2 + miss2);
			float hit3 = MainClass.c3.hit;
			float miss3 = MainClass.c3.miss;
			float result3 = hit3 / (hit3 + miss3);
			System.out.println("Hit ratio for cache 1= " + result1);
			System.out.println("Hit ratio for cache 2= " + result2);
			System.out.println("Hit ratio for cache 3= " + result3);
		}
		if ((totalBranch >= 1)) {
			System.out.print("the branch missprediction rate="
					+ BranchMissPrediction / totalBranch);
		}

	}

	public static void printRS() {
		for (int i = 0; i < number; i++) {
			for (int j = 0; j < 9; j++) {
				System.out.print(RS[i][j] + " ");
			}
			System.out.println();

		}
	}
}
