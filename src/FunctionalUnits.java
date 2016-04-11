public class FunctionalUnits {
	public static int add(int x, int y) {
		return (x + y);
	}

	public static int sub(int x, int y) {
		return (x - y);
	}

	public static int nand(int x, int y) {
		return ~(x & y);
	}

	public static int mult(int x, int y) {
		return (x * y);
	}

	public static int div(int x, int y) {
		return (x / y);
	}
}