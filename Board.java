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

	public static final int
		A8=56, B8=57, C8=58, D8=59, E8=60, F8=61, G8=62, H8=63, 
		A7=48, B7=49, C7=50, D7=51, E7=52, F7=53, G7=54, H7=55, 
		A6=40, B6=41, C6=42, D6=43, E6=44, F6=45, G6=46, H6=47, 
		A5=32, B5=33, C5=34, D5=35, E5=36, F5=37, G5=38, H5=39, 
		A4=24, B4=25, C4=26, D4=27, E4=28, F4=29, G4=30, H4=31, 
		A3=16, B3=17, C3=18, D3=19, E3=20, F3=21, G3=22, H3=23, 
		A2=8, B2=9, C2=10, D2=11, E2=12, F2=13, G2=14, H2=15, 
		A1=0, B1=1, C1=2, D1=3, E1=4, F1=5, G1=6, H1=7;

	/* Bitboard Board Representation
	 * Square to bit mapping
	 * H8 = MSB
	 * A1 = LSB
	 *
	 * A8=56 B8=57 C8=58 D8=59 E8=60 F8=61 G8=62 H8=63 
	 * A7=48 B7=49 C7=50 D7=51 E7=52 F7=53 G7=54 H7=55 
	 * A6=40 B6=41 C6=42 D6=43 E6=44 F6=45 G6=46 H6=47 
	 * A5=32 B5=33 C5=34 D5=35 E5=36 F5=37 G5=38 H5=39 
	 * A4=24 B4=25 C4=26 D4=27 E4=28 F4=29 G4=30 H4=31 
	 * A3=16 B3=17 C3=18 D3=19 E3=20 F3=21 G3=22 H3=23 
	 * A2=8  B2=9  C2=10 D2=11 E2=12 F2=13 G2=14 H2=15 
	 * A1=0  B1=1  C1=2  D1=3  E1=4  F1=5  G1=6  H1=7  
	 */

	/* White Pawns
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 11111111 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long WP = 0x000000000000FF00L;

	/* White Rooks
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 10000001 1
	 *     ABCDEFGH
	 */
	private long WR = 0x0000000000000081L;

	/* White Knights
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 01000010 1
	 *     ABCDEFGH
	 */
	private long WN = 0x0000000000000042L;

	/* White Bishops
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00100100 1
	 *     ABCDEFGH
	 */
	private long WB = 0x0000000000000024L;

	/* White Queen
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00010000 1
	 *     ABCDEFGH
	 */
	private long WQ = 0x0000000000000008L;

	/* White King
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00001000 1
	 *     ABCDEFGH
	 */
	private long WK = 0x0000000000000010L;

	/* Black Pawns
	 *     ABCDEFGH
	 *   8 00000000 8
	 *   7 11111111 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long BP = 0x00FF000000000000L;

	/* Black Rooks
	 *     ABCDEFGH
	 *   8 10000001 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long BR = 0x8100000000000000L;

	/* Black Knights
	 *     ABCDEFGH
	 *   8 01000010 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long BN = 0x4200000000000000L;

	/* Black Bishops
	 *     ABCDEFGH
	 *   8 00100100 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long BB = 0x2400000000000000L;

	/* Black Queen
	 *     ABCDEFGH
	 *   8 00010000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long BQ = 0x0800000000000000L;

	/* Black King
	 *     ABCDEFGH
	 *   8 00001000 8
	 *   7 00000000 7
	 *   6 00000000 6
	 *   5 00000000 5
	 *   4 00000000 4
	 *   3 00000000 3
	 *   2 00000000 2
	 *   1 00000000 1
	 *     ABCDEFGH
	 */
	private long BK = 0x1000000000000000L;

	/* Clear Files
	 * Ex:
	 *
	 * ClearAFile
	 *     ABCDEFGH
	 *   8 01111111 8
	 *   7 01111111 7
	 *   6 01111111 6
	 *   5 01111111 5
	 *   4 01111111 4
	 *   3 01111111 3
	 *   2 01111111 2
	 *   1 01111111 1
	 *     ABCDEFGH
	 */
	public static final long clearAFile = 0xFEFEFEFEFEFEFEFEL;
	public static final long clearBFile = 0xFDFDFDFDFDFDFDFDL;
	public static final long clearGFile = 0xBFBFBFBFBFBFBFBFL;
	public static final long clearHFile = 0x7F7F7F7F7F7F7F7FL;

	public long getWhitePieces() {
		return WP | WR | WN | WB | WQ | WK; 
	}

	public long getBlackPieces() {
		return BP | BR | BN | BB | BQ | BK; 
	}

	public long getAllPieces() {
		return getWhitePieces() | getBlackPieces();
	}

	public long getEmptySquares() {
		return ~getAllPieces();
	}

	public long getWhitePawns() {
		return WP;
	}

	public long getWhiteRooks() {
		return WR;
	}

	public long getWhiteKnights() {
		return WN;
	}

	public long getWhiteBishops() {
		return WB;
	}

	public long getWhiteQueen() {
		return WQ;
	}

	public long getWhiteKing() {
		return WK;
	}

	public long getBlackPawns() {
		return BP;
	}

	public long getBlackRooks() {
		return BR;
	}

	public long getBlackKnights() {
		return BN;
	}

	public long getBlackBishops() {
		return BB;
	}

	public long getBlackQueen() {
		return BQ;
	}

	public long getBlackKing() {
		return BK;
	}

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
