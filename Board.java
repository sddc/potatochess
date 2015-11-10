import java.util.ArrayList;

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
	public static final boolean WHITE = true;
	public static final boolean BLACK = false;

	// KS = Kingside, QS = Queenside
	private boolean whiteKSRookNotMoved = true;
	private boolean whiteQSRookNotMoved = true;
	private boolean blackKSRookNotMoved = true;
	private boolean blackQSRookNotMoved = true;
	private boolean whiteQueenNotMoved = true;
	private boolean blackQueenNotMoved = true;

	/* Bitboard Board Representation
	 * Square to bit mapping
	 * H8 = MSB
	 * A1 = LSB
	 */
	public static final int
		A8=56, B8=57, C8=58, D8=59, E8=60, F8=61, G8=62, H8=63, 
		A7=48, B7=49, C7=50, D7=51, E7=52, F7=53, G7=54, H7=55, 
		A6=40, B6=41, C6=42, D6=43, E6=44, F6=45, G6=46, H6=47, 
		A5=32, B5=33, C5=34, D5=35, E5=36, F5=37, G5=38, H5=39, 
		A4=24, B4=25, C4=26, D4=27, E4=28, F4=29, G4=30, H4=31, 
		A3=16, B3=17, C3=18, D3=19, E3=20, F3=21, G3=22, H3=23, 
		A2=8, B2=9, C2=10, D2=11, E2=12, F2=13, G2=14, H2=15, 
		A1=0, B1=1, C1=2, D1=3, E1=4, F1=5, G1=6, H1=7;


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
	
	public static final long clearAFile = 0xFEFEFEFEFEFEFEFEL;
	public static final long clearBFile = 0xFDFDFDFDFDFDFDFDL;
	public static final long clearGFile = 0xBFBFBFBFBFBFBFBFL;
	public static final long clearHFile = 0x7F7F7F7F7F7F7F7FL;

	public static final long maskRank3 = 0x0000000000FF0000L;
	public static final long maskRank6 = 0x0000FF0000000000L;

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
	
	private void printPiece(int p) {
		switch(p) {
			case -6: System.out.print("k");
				 break;
			case -5: System.out.print("q");
				 break;
			case -4: System.out.print("b");
				 break;
			case -3: System.out.print("n");
				 break;
			case -2: System.out.print("r");
				 break;
			case -1: System.out.print("p");
				 break;
			case 0: System.out.print(" ");
				break;
			case 1: System.out.print("P");
				break;
			case 2: System.out.print("R");
				break;
			case 3: System.out.print("N");
				break;
			case 4: System.out.print("B");
				break;
			case 5: System.out.print("Q");
				break;
			case 6: System.out.print("K");
				break;
			default:
				break;
		}
	}

	public void print() {
		int rank = 8;
		System.out.println("     A   B   C   D   E   F   G   H");
		System.out.println("   +---+---+---+---+---+---+---+---+");
		for(int i = 56; i >= 0; i -= 8) {
			System.out.print(" " + rank + " |");
			for(int j = i; j < (i + 8); j++) {
				System.out.print(" ");
				printPiece(getPieceType(get1BitMask(j)));
				System.out.print(" |");
			}
			System.out.print(" " + rank--);
			System.out.println("\n   +---+---+---+---+---+---+---+---+");
		}
		System.out.println("     A   B   C   D   E   F   G   H");
	}

	public static ArrayList<Integer> get1BitIndexes(long x) {
		long compare = 0x0000000000000001L;
		int index = 0;
		ArrayList<Integer> indexes = new ArrayList<Integer>();

		while(x != 0) {
			if((x & compare) == 1L) {
				indexes.add(index);
			}
			x = x >>> 1;
			index++;
		}
		
		return indexes;
	}

	public static long get1BitMask(int p) {
		long mask = 0x0000000000000001L;
		if(p == 0) {
			return mask;
		} else {
			return mask << p;
		}
	}

	public int getPieceType(long mask) {
		// if square int, run it through get1BitMask method
		// as argument.
		long[] whiteBitboards = {WP, WR, WN, WB, WQ, WK};
		long[] blackBitboards = {BP, BR, BN, BB, BQ, BK};

		for(int i = 0; i < 6; i++ ) {
			if((mask & whiteBitboards[i]) != 0) {
				return i+1;
			}
			if((mask & blackBitboards[i]) != 0) {
				return -(i+1);
			}
		}
		return 0;
	}

	private void modify(int type, long modifier) {
		switch(type) {
			case 1:
				WP ^= modifier;
				break;
			case 2:
				WR ^= modifier;
				break;
			case 3:
				WN ^= modifier;
				break;
			case 4:
				WB ^= modifier;
				break;
			case 5:
				WQ ^= modifier;
				break;
			case 6:
				WK ^= modifier;
				break;
			case -1:
				BP ^= modifier;
				break;
			case -2:
				BR ^= modifier;
				break;
			case -3:
				BN ^= modifier;
				break;
			case -4:
				BB ^= modifier;
				break;
			case -5:
				BQ ^= modifier;
				break;
			case -6:
				BK ^= modifier;
				break;
			case 0:
				break;
			default:
				break;
		}
	}

	public void pseudoMovePiece(int fromSquare, int toSquare) {
		// assumes from, to are pseudovalid for piece type
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		int fromPieceType = getPieceType(fromMask);
		int toPieceType = getPieceType(toMask);
	
		// XOR with from to mask to move bit	
		modify(fromPieceType, fromMask);	
		modify(fromPieceType, toMask);	

		// if bit is on one of opponents bitboard
		// it was captured. remove by XOR with to mask
		modify(toPieceType, toMask);	

		// castling checks
		if((whiteQueenNotMoved == true) && (fromPieceType == WHITE_QUEEN) && (fromSquare == Board.D1)) {
			whiteQueenNotMoved = false;
		}

		if((blackQueenNotMoved == true) && (fromPieceType == BLACK_QUEEN) && (fromSquare == Board.D8)) {
			blackQueenNotMoved = false;
		}

		if((whiteQSRookNotMoved == true) && (fromPieceType == WHITE_ROOK) && (fromSquare == Board.A1)) {
			whiteQSRookNotMoved = false;
		}

		if((whiteKSRookNotMoved == true) && (fromPieceType == WHITE_ROOK) && (fromSquare == Board.H1)) {
			whiteKSRookNotMoved = false;
		}

		if((blackQSRookNotMoved == true) && (fromPieceType == BLACK_ROOK) && (fromSquare == Board.A8)) {
			blackQSRookNotMoved = false;
		}

		if((blackKSRookNotMoved == true) && (fromPieceType == BLACK_ROOK) && (fromSquare == Board.H8)) {
			blackKSRookNotMoved = false;
		}
	}

	public boolean ksSquaresEmpty(boolean side, boolean squares) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long result;
		if(side) {
			result = getWhitePieces();
			if(squares) {
				result &= (get1BitMask(Board.F1) | get1BitMask(Board.G1));
			} else {
				result &= (get1BitMask(Board.B1) | get1BitMask(Board.C1) | get1BitMask(Board.D1));
			}
		} else {
			result = getBlackPieces();
			if(squares) {
				result &= (get1BitMask(Board.F8) | get1BitMask(Board.G8));
			} else {
				result &= (get1BitMask(Board.B8) | get1BitMask(Board.C8) | get1BitMask(Board.D8));
			}
		}

		if(result != 0L) {
			return false;
		} else {
			return true;
		}
	}
}
