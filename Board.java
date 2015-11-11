import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;

public class Board {
	public static final int EMPTY = 0;
	public static final int WHITE_PAWN = 1;
	public static final int WHITE_ROOK = 2;
	public static final int WHITE_KNIGHT = 3;
	public static final int WHITE_BISHOP = 4;
	public static final int WHITE_QUEEN = 5;
	public static final int WHITE_KING = 6;
	public static final int BLACK_PAWN = 7;
	public static final int BLACK_ROOK = 8;
	public static final int BLACK_KNIGHT = 9;
	public static final int BLACK_BISHOP = 10;
	public static final int BLACK_QUEEN = 11;
	public static final int BLACK_KING = 12;

	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	public static final boolean KINGSIDE = true;
	public static final boolean QUEENSIDE = false;
	
	/* castling moved array checks
	 * 0 = white queen moved?
	 * 1 = black queen moved?
	 * 2 = white kingside rook moved?
	 * 3 = white queenside rook moved?
	 * 4 = black kingside rook moved?
	 * 5 = black queenside rook moved?
	 */
	private boolean[] moved = {false, false, false, false, false, false};
	
	// en passant checks
	private boolean lastMoveDoublePawnPush = false;
	private int behindSquare;

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

	private long[] bitboards = {
		0L,
		0x000000000000FF00L, // white pawns
		0x0000000000000081L, // white rooks
		0x0000000000000042L, // white knights
		0x0000000000000024L, // white bishops
		0x0000000000000008L, // white queen
		0x0000000000000010L, // white king
		0x00FF000000000000L, // black pawns
		0x8100000000000000L, // black rooks
		0x4200000000000000L, // black knights
		0x2400000000000000L, // black bishops
		0x0800000000000000L, // black queen
		0x1000000000000000L  // black king
	};
	
	public static final long clearAFile = 0xFEFEFEFEFEFEFEFEL;
	public static final long clearBFile = 0xFDFDFDFDFDFDFDFDL;
	public static final long clearGFile = 0xBFBFBFBFBFBFBFBFL;
	public static final long clearHFile = 0x7F7F7F7F7F7F7F7FL;

	public static final long maskRank3 = 0x0000000000FF0000L;
	public static final long maskRank6 = 0x0000FF0000000000L;

	private Deque<PreviousMove> previousMoves = new ArrayDeque<PreviousMove>();

	public boolean[] getCastlingChecks() {
		return moved.clone();
	}

	public int getBehindSquare() {
		return behindSquare;
	}

	public boolean lastMoveDPP() {
		return lastMoveDoublePawnPush;
	}

	public long getSidePieces(boolean side) {
		long result = 0L;

		if(side == Board.WHITE) {
			for(int i = 1; i <= 6; i++) {
				result |= bitboards[i];
			}
		} else {
			for(int i = 7; i <= 12; i++) {
				result |= bitboards[i];
			}
		}
		return result;
	}

	public long getAllPieces() {
		return getSidePieces(Board.WHITE) | getSidePieces(Board.BLACK);
	}

	public long getEmptySquares() {
		return ~getAllPieces();
	}

	public long getPawns(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[1];
		} else {
			return bitboards[7];
		}
	}

	public long getRooks(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[2];
		} else {
			return bitboards[8];
		}
	}

	public long getKnights(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[3];
		} else {
			return bitboards[9];
		}
	}

	public long getBishops(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[4];
		} else {
			return bitboards[10];
		}
	}

	public long getQueen(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[5];
		} else {
			return bitboards[11];
		}
	}

	public long getKing(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[6];
		} else {
			return bitboards[12];
		}
	}
	
	private void printPiece(int p) {
		switch(p) {
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
			case 7: System.out.print("p");
				 break;
			case 8: System.out.print("r");
				 break;
			case 9: System.out.print("n");
				 break;
			case 10: System.out.print("b");
				 break;
			case 11: System.out.print("q");
				 break;
			case 12: System.out.print("k");
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

		for(int i = 0; i < 13; i++ ) {
			if((mask & bitboards[i]) != 0) {
				return i;
			}
		}
		return 0;
	}

	private void modify(int type, long modifier) {
		if(type == 0) {
			return;
		}
		bitboards[type] ^= modifier;
	}

	public void move(boolean side, int fromSquare, int toSquare) {
		// assumes from, to are at least pseudovalid for piece types
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		int fromPieceType = getPieceType(fromMask);
		int toPieceType = getPieceType(toMask);

		// en passant capture
		if(
		(lastMoveDoublePawnPush == true) && 
		((fromPieceType == WHITE_PAWN) || (fromPieceType == BLACK_PAWN)) &&
		(toSquare == behindSquare)) {
			if(side == Board.WHITE) {
				modify(BLACK_PAWN, get1BitMask(toSquare+8));
			} else {
				modify(WHITE_PAWN, get1BitMask(toSquare-8));
			}	
		}

		// XOR with from to mask to move bit	
		modify(fromPieceType, fromMask);	
		modify(fromPieceType, toMask);	

		// if bit is on one of opponents bitboard
		// it was captured. remove by XOR with to mask
		if(toPieceType != EMPTY) {
			modify(toPieceType, toMask);	
		}

		// save move to undo
		previousMoves.addFirst(new PreviousMove(fromSquare, toSquare, fromPieceType, toPieceType, moved, lastMoveDoublePawnPush, behindSquare));

		// en passant check
		if((fromPieceType == WHITE_PAWN) || (fromPieceType == BLACK_PAWN)) {
			if(

			((fromSquare >= Board.A2 && fromSquare <= Board.H2) &&
			 (toSquare >= Board.A4 && toSquare <= Board.H4)) ||
			((fromSquare >= Board.A2 && fromSquare <= Board.H2) &&
			 (toSquare >= Board.A4 && toSquare <= Board.H4))

			) {
				lastMoveDoublePawnPush = true;	
				if(side == Board.WHITE) {
					behindSquare = toSquare-8;
				} else {
					behindSquare = toSquare+8;
				}
			} else {
				lastMoveDoublePawnPush = false;	
			}
		} else {
			lastMoveDoublePawnPush = false;	
		}

		// castling checks
		if((moved[0] == false) && (fromPieceType == WHITE_QUEEN) && (fromSquare == Board.D1)) {
			moved[0] = true;
		}

		if((moved[1] == false) && (fromPieceType == BLACK_QUEEN) && (fromSquare == Board.D8)) {
			moved[1] = true;
		}

		if((moved[2] == false) && (fromPieceType == WHITE_ROOK) && (fromSquare == Board.H1)) {
			moved[2] = true;
		}

		if((moved[3] == false) && (fromPieceType == WHITE_ROOK) && (fromSquare == Board.A1)) {
			moved[3] = true;
		}

		if((moved[4] == false) && (fromPieceType == BLACK_ROOK) && (fromSquare == Board.H8)) {
			moved[4] = true;
		}

		if((moved[5] == false) && (fromPieceType == BLACK_ROOK) && (fromSquare == Board.A8)) {
			moved[5] = true;
		}
		
		// kingside castling move
		if( ((fromPieceType == WHITE_KING) && (fromSquare == Board.E1) && (toSquare == Board.G1)) ||
		    ((fromPieceType == BLACK_KING) && (fromSquare == Board.E8) && (toSquare == Board.G8)) ) {
			if(side == Board.WHITE) {
				move(side, Board.H1, Board.F1);
			} else {
				move(side, Board.H8, Board.F8);
			}
		}	

		// queenside castling move
		if( ((fromPieceType == WHITE_KING) && (fromSquare == Board.E1) && (toSquare == Board.C1)) ||
		    ((fromPieceType == BLACK_KING) && (fromSquare == Board.E8) && (toSquare == Board.C8)) ) {
			if(side == Board.WHITE) {
				move(side, Board.A1, Board.D1);
			} else {
				move(side, Board.A8, Board.D8);
			}
		}
	}

	public void undoMove(boolean side) {
		if(previousMoves.size() == 0) {
			return;
		}

		PreviousMove move = previousMoves.removeFirst();
		long fromMask = get1BitMask(move.fromSquare);
		long toMask = get1BitMask(move.toSquare);
		modify(move.fromPieceType, toMask);
		modify(move.fromPieceType, fromMask);
	
		//restore en passant capture opportunity	
		if(
		(move.lastMoveDoublePawnPush == true) && 
		((move.fromPieceType == WHITE_PAWN) || (move.fromPieceType == BLACK_PAWN)) &&
		(move.toSquare == move.behindSquare)) {
			if(side == Board.WHITE) {
				modify(BLACK_PAWN, get1BitMask(move.toSquare+8));
			} else {
				modify(WHITE_PAWN, get1BitMask(move.toSquare-8));
			}	
		}

		// undo opponent capture
		if(move.toPieceType != EMPTY) {
			modify(move.toPieceType, toMask);
		}

		moved = move.moved;
		lastMoveDoublePawnPush = move.lastMoveDoublePawnPush;
		behindSquare = move.behindSquare;

		// if rook move was involved in castling, undo the king move
		if( ((move.fromPieceType == WHITE_ROOK) && (move.fromSquare == Board.H1) && (move.toSquare == Board.F1)) ||
		    ((move.fromPieceType == WHITE_ROOK) && (move.fromSquare == Board.A1) && (move.toSquare == Board.D1)) ||
		    ((move.fromPieceType == BLACK_ROOK) && (move.fromSquare == Board.H8) && (move.toSquare == Board.F8)) ||
		    ((move.fromPieceType == BLACK_ROOK) && (move.fromSquare == Board.A8) && (move.toSquare == Board.D8)) ) {
			undoMove(side);
		    }
	}

	public boolean castlingAvailable(boolean side, boolean squares, long attacks) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long result = getSidePieces(side);
		if(side == Board.WHITE) {
			// check if queen moved
			if(moved[0]) {
				return false;
			}

			if(squares == Board.KINGSIDE) {
				// check if kingside rook moved
				if(moved[2]) {
					return false;
				}
				result &= (get1BitMask(Board.F1) | get1BitMask(Board.G1));
			} else {
				// check if queenside rook moved
				if(moved[3]) {
					return false;
				}
				result &= (get1BitMask(Board.B1) | get1BitMask(Board.C1) | get1BitMask(Board.D1));
			}
		} else {
			// check if queen moved
			if(moved[1]) {
				return false;
			}

			if(squares == Board.KINGSIDE) {
				// check if kingside rook moved
				if(moved[4]) {
					return false;
				}
				result &= (get1BitMask(Board.F8) | get1BitMask(Board.G8));
			} else {
				// check if queenside rook moved
				if(moved[5]) {
					return false;
				}
				result &= (get1BitMask(Board.B8) | get1BitMask(Board.C8) | get1BitMask(Board.D8));
			}
		}

		// check if squares attacked
		result &= attacks;

		if(result != 0L) {
			return false;
		} else {
			return true;
		}
	}
}
