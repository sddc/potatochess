public class Board {
	public static final int EMPTY = 0;
	public static final int WHITE_PAWN = 1;
	public static final int WHITE_ROOK = 2;
	public static final int WHITE_KNIGHT = 3;
	public static final int WHITE_BISHOP = 4;
	public static final int WHITE_QUEEN = 5;
	public static final int WHITE_KING = 6;
	public static final int BLACK_PAWN = -1;
	public static final int BLACK_ROOK = -2;
	public static final int BLACK_KNIGHT = -3;
	public static final int BLACK_BISHOP = -4;
	public static final int BLACK_QUEEN = -5;
	public static final int BLACK_KING = -6;

	private long WP = 0x000000000000FF00L;
	private long WR = 0x0000000000000081L;
	private long WN = 0x0000000000000042L;
	private long WB = 0x0000000000000024L;
	private long WQ = 0x0000000000000008L;
	private long WK = 0x0000000000000010L;

	private long BP = 0x00FF000000000000L;
	private long BR = 0x8100000000000000L;
	private long BN = 0x4200000000000000L;
	private long BB = 0x2400000000000000L;
	private long BQ = 0x0800000000000000L;
	private long BK = 0x1000000000000000L;

	private long whitePieces = WP | WR | WN | WB | WQ | WK;
	private long blackPieces = BP | BR | BN | BB | BQ | BK;
	private long allPieces = whitePieces | blackPieces;

	public void print() {
		int[][] temp = new int[8][8];
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				temp[i][j] = 0;
			}
		}

		long[] sets = {WP, WR, WN, WB, WQ, WK, BP, BR, BN, BB, BQ, BK};
		int setNumber = 1;
		int k = 63;

		for(long s : sets) {
			String setString = String.format("%64s", Long.toBinaryString(s)).replace(' ', '0');

			for(int i = 7; i >= 0; i--) { 
				for(int j = 0; j < 8; j++) {
					if(setString.charAt(k) == '1') {
						switch(setNumber) {
							case 1: temp[i][j] = WHITE_PAWN;
								break;
							case 2: temp[i][j] = WHITE_ROOK;
								break;
							case 3: temp[i][j] = WHITE_KNIGHT;
								break;
							case 4: temp[i][j] = WHITE_BISHOP;
								break;
							case 5: temp[i][j] = WHITE_QUEEN;
								break;
							case 6: temp[i][j] = WHITE_KING;
								break;
							case 7: temp[i][j] = BLACK_PAWN;
								break;
							case 8: temp[i][j] = BLACK_ROOK;
								break;
							case 9: temp[i][j] = BLACK_KNIGHT;
								break;
							case 10: temp[i][j] = BLACK_BISHOP;
								 break;
							case 11: temp[i][j] = BLACK_QUEEN;
								 break;
							case 12: temp[i][j] = BLACK_KING;
							default:
								 break;
						}
					}
					k--;
				}
			}
			k = 63;
			setNumber++;
		}

		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				switch(temp[i][j]) {
					case -6: System.out.print("K");
						break;
					case -5: System.out.print("Q");
						break;
					case -4: System.out.print("B");
						break;
					case -3: System.out.print("N");
						break;
					case -2: System.out.print("R");
						break;
					case -1: System.out.print("P");
						break;
					case 0: System.out.print("_");
						break;
					case 1: System.out.print("p");
						break;
					case 2: System.out.print("r");
						break;
					case 3: System.out.print("n");
						break;
					case 4: System.out.print("b");
						break;
					case 5: System.out.print("q");
						break;
					case 6: System.out.print("k");
						break;
					default:
						break;
				}
				System.out.print(", ");
			}
			System.out.print("\n");
		}

	}

}
