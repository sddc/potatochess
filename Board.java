import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;

public class Board {
	public static final int EMPTY = -1;
	public static final int WHITE_PAWN = 0;
	public static final int WHITE_ROOK = 1;
	public static final int WHITE_KNIGHT = 2;
	public static final int WHITE_BISHOP = 3;
	public static final int WHITE_QUEEN = 4;
	public static final int WHITE_KING = 5;
	public static final int BLACK_PAWN = 6;
	public static final int BLACK_ROOK = 7;
	public static final int BLACK_KNIGHT = 8;
	public static final int BLACK_BISHOP = 9;
	public static final int BLACK_QUEEN = 10;
	public static final int BLACK_KING = 11;

	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	public static final boolean KINGSIDE = true;
	public static final boolean QUEENSIDE = false;
	
	/* castling moved array checks
	 * 0 = white king moved?
	 * 1 = black king moved?
	 * 2 = white kingside rook moved?
	 * 3 = white queenside rook moved?
	 * 4 = black kingside rook moved?
	 * 5 = black queenside rook moved?
	 */
	private boolean[] moved;

	private boolean activeColor;
	
	// en passant checks
	private boolean lastMoveDoublePawnPush;
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

	private long[] bitboards;
	private long whitePieces;
	private long blackPieces;
	
	public static final long clearAFile = 0xFEFEFEFEFEFEFEFEL;
	public static final long clearBFile = 0xFDFDFDFDFDFDFDFDL;
	public static final long clearGFile = 0xBFBFBFBFBFBFBFBFL;
	public static final long clearHFile = 0x7F7F7F7F7F7F7F7FL;

	public static final long maskRank2 = 0x000000000000FF00L;
	public static final long maskRank3 = 0x0000000000FF0000L;
	public static final long maskRank4 = 0x00000000FF000000L;
	public static final long maskRank5 = 0x000000FF00000000L;
	public static final long maskRank6 = 0x0000FF0000000000L;
	public static final long maskRank7 = 0x00FF000000000000L;

	private Deque<PreviousMove> previousMoves = new ArrayDeque<PreviousMove>();

	public Board(long[] bitboards, boolean[] moved, boolean lastMoveDoublePawnPush, int behindSquare, boolean activeColor) {
		this.bitboards = bitboards;
		this.moved = moved;
		this.lastMoveDoublePawnPush = lastMoveDoublePawnPush;
		this.behindSquare = behindSquare;
		this.activeColor = activeColor;
		whitePieces = genSidePieces(Board.WHITE);
		blackPieces = genSidePieces(Board.BLACK); 
	}

	public boolean[] getCastlingChecks() {
		return moved.clone();
	}

	public boolean getActiveColor() {
		return activeColor;
	}

	public boolean toggleActiveColor() {
		activeColor = !activeColor;
		return activeColor;
	}

	public int getBehindSquare() {
		return behindSquare;
	}

	public boolean lastMoveDPP() {
		return lastMoveDoublePawnPush;
	}

	private long genSidePieces(boolean side) {
		long result = 0L;

		if(side == Board.WHITE) {
			for(int i = 0; i < 6; i++) {
				result |= bitboards[i];
			}
		} else {
			for(int i = 6; i < 12; i++) {
				result |= bitboards[i];
			}
		}
		return result;
	}

	public long getSidePieces(boolean side) {
		if(side == Board.WHITE) {
			return whitePieces;
		} else {
			return blackPieces;
		}
	}

	public long getAllPieces() {
		return getSidePieces(Board.WHITE) | getSidePieces(Board.BLACK);
	}

	public long getEmptySquares() {
		return ~getAllPieces();
	}

	public long getPawns(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[0];
		} else {
			return bitboards[6];
		}
	}

	public long getRooks(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[1];
		} else {
			return bitboards[7];
		}
	}

	public long getKnights(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[2];
		} else {
			return bitboards[8];
		}
	}

	public long getBishops(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[3];
		} else {
			return bitboards[9];
		}
	}

	public long getQueen(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[4];
		} else {
			return bitboards[10];
		}
	}

	public long getKing(boolean side) {
		if(side == Board.WHITE) {
			return bitboards[5];
		} else {
			return bitboards[11];
		}
	}
	
	private void printPiece(int p) {
		switch(p) {
			case -1: System.out.print(" ");
				break;
			case 0: System.out.print("P");
				break;
			case 1: System.out.print("R");
				break;
			case 2: System.out.print("N");
				break;
			case 3: System.out.print("B");
				break;
			case 4: System.out.print("Q");
				break;
			case 5: System.out.print("K");
				break;
			case 6: System.out.print("p");
				 break;
			case 7: System.out.print("r");
				 break;
			case 8: System.out.print("n");
				 break;
			case 9: System.out.print("b");
				 break;
			case 10: System.out.print("q");
				 break;
			case 11: System.out.print("k");
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

		for(int i = 0; i < 12; i++ ) {
			if((mask & bitboards[i]) != 0) {
				return i;
			}
		}
		return Board.EMPTY;
	}

	private void modify(int type, long modifier) {
		if(type == Board.EMPTY) {
			return;
		}
		if(type < Board.BLACK_PAWN) {
			bitboards[type] ^= modifier;
			whitePieces ^= modifier;
		} else {
			bitboards[type] ^= modifier;
			blackPieces ^= modifier;
		}
	}

	public void move(boolean side, int fromSquare, int toSquare) {
		// assumes fromSquare, toSquare are at least pseudovalid for piece types
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		int fromPieceType = getPieceType(fromMask);
		int toPieceType = getPieceType(toMask);

		// save move to undo
		previousMoves.addFirst(new PreviousMove(fromSquare, toSquare, fromPieceType, toPieceType, moved, lastMoveDoublePawnPush, behindSquare));

		// XOR fromPieceType's bitboard with fromMask to toggle to 0
		modify(fromPieceType, fromMask);	
		// XOR fromPieceType's bitboard with toMask to toggle to 1
		modify(fromPieceType, toMask);	

		// if toPieceType is not empty, it is a capture
		if(toPieceType != EMPTY) {
			// XOR toPieceType's bitboard with toMask to toggle to 0
			modify(toPieceType, toMask);	
		}


		// en passant check and capture
		if((fromPieceType == WHITE_PAWN) || (fromPieceType == BLACK_PAWN)) {
			if((lastMoveDoublePawnPush == true) && (toSquare == behindSquare)) {
				// capture
				if(side == Board.WHITE) {
					modify(BLACK_PAWN, get1BitMask(toSquare-8));
				} else {
					modify(WHITE_PAWN, get1BitMask(toSquare+8));
				}	
				lastMoveDoublePawnPush = false;	
			} else {
				// check for double pawn push
				if( ((fromMask & maskRank2) != 0L && (toMask & maskRank4) != 0L) ||
				((fromMask & maskRank7) != 0L && (toMask & maskRank5) != 0L) ) {
					lastMoveDoublePawnPush = true;	
					if(side == Board.WHITE) {
						behindSquare = toSquare-8;
					} else {
						behindSquare = toSquare+8;
					}
				} else {
					lastMoveDoublePawnPush = false;	
				}
			}
		} else {
			lastMoveDoublePawnPush = false;	
		}

		// castling move and checks
		// if moved[0] is true, white king has already moved
		if(!moved[0] && (fromPieceType == WHITE_KING)) {
			moved[0] = true;
			// check if kingside castle
			if((fromSquare == Board.E1) && (toSquare == Board.G1)) {
				modify(WHITE_ROOK, Board.get1BitMask(Board.H1));	
				modify(WHITE_ROOK, Board.get1BitMask(Board.F1));	
				moved[2] = true;
			}	

			// check if queenside castle
			if((fromSquare == Board.E1) && (toSquare == Board.C1)) {
				modify(WHITE_ROOK, Board.get1BitMask(Board.A1));	
				modify(WHITE_ROOK, Board.get1BitMask(Board.D1));	
				moved[3] = true;
			}	
		}
		// if moved[1] is true, black king has already moved
		if(!moved[1] && (fromPieceType == BLACK_KING)) {
			moved[1] = true;
			// check if kingside castle
			if((fromSquare == Board.E8) && (toSquare == Board.G8)) {
				modify(BLACK_ROOK, Board.get1BitMask(Board.H8));	
				modify(BLACK_ROOK, Board.get1BitMask(Board.F8));	
				moved[4] = true;
			}	

			// check if queenside castle
			if((fromSquare == Board.E8) && (toSquare == Board.C8)) {
				modify(BLACK_ROOK, Board.get1BitMask(Board.A8));	
				modify(BLACK_ROOK, Board.get1BitMask(Board.D8));	
				moved[5] = true;
			}	
		}
		
		// rook castling checks
		if(!moved[2] && (fromPieceType == WHITE_ROOK) && (fromSquare == Board.H1)) {
			moved[2] = true;
		}

		if(!moved[3] && (fromPieceType == WHITE_ROOK) && (fromSquare == Board.A1)) {
			moved[3] = true;
		}

		if(!moved[4] && (fromPieceType == BLACK_ROOK) && (fromSquare == Board.H8)) {
			moved[4] = true;
		}

		if(!moved[5] && (fromPieceType == BLACK_ROOK) && (fromSquare == Board.A8)) {
			moved[5] = true;
		}
	}

	public void undoMove(boolean side) {
		if(previousMoves.isEmpty()) {
			return;
		}

		PreviousMove move = previousMoves.removeFirst();
		long fromMask = get1BitMask(move.fromSquare);
		long toMask = get1BitMask(move.toSquare);

		// restore checks
		moved = move.moved;
		lastMoveDoublePawnPush = move.lastMoveDoublePawnPush;
		behindSquare = move.behindSquare;

		// undo move
		modify(move.fromPieceType, fromMask);
		modify(move.fromPieceType, toMask);

		// undo capture
		if(move.toPieceType != EMPTY) {
			modify(move.toPieceType, toMask);
		}
		
		if((move.fromPieceType == WHITE_PAWN) || (move.fromPieceType == BLACK_PAWN)) {
			// undo en passant capture
			if((move.lastMoveDoublePawnPush == true) && (move.toSquare == move.behindSquare)) {
				if(side == Board.WHITE) {
					modify(BLACK_PAWN, get1BitMask(move.toSquare-8));
				} else {
					modify(WHITE_PAWN, get1BitMask(move.toSquare+8));
				}	
			}
		}

		// restore white castling rooks
		if(move.fromPieceType == WHITE_KING) {
			// undo kingside castle
			if((move.fromSquare == Board.E1) && (move.toSquare == Board.G1)) {
				modify(WHITE_ROOK, Board.get1BitMask(Board.H1));	
				modify(WHITE_ROOK, Board.get1BitMask(Board.F1));	
			}	

			// undo queenside castle
			if((move.fromSquare == Board.E1) && (move.toSquare == Board.C1)) {
				modify(WHITE_ROOK, Board.get1BitMask(Board.A1));	
				modify(WHITE_ROOK, Board.get1BitMask(Board.D1));	
			}	
		}
		
		// restore black castling rooks
		if(move.fromPieceType == BLACK_KING) {
			// undo kingside castle
			if((move.fromSquare == Board.E8) && (move.toSquare == Board.G8)) {
				modify(BLACK_ROOK, Board.get1BitMask(Board.H8));	
				modify(BLACK_ROOK, Board.get1BitMask(Board.F8));	
			}	

			// undo queenside castle
			if((move.fromSquare == Board.E8) && (move.toSquare == Board.C8)) {
				modify(BLACK_ROOK, Board.get1BitMask(Board.A8));	
				modify(BLACK_ROOK, Board.get1BitMask(Board.D8));	
			}	
		}
		/*

		// unpromote queen to pawn
		if((move.fromPieceType == WHITE_PAWN) || (move.fromPieceType == BLACK_PAWN)) {
			if(side == Board.WHITE) {
				if(move.toSquare >= Board.A8 && move.toSquare <= Board.H8) {
					modify(WHITE_PAWN, toMask);
					modify(WHITE_QUEEN, toMask);
				}
			} else {
				if(move.toSquare >= Board.A1 && move.toSquare <= Board.H1) {
					modify(BLACK_PAWN, toMask);
					modify(BLACK_QUEEN, toMask);
				}
			}
		}
		    */
	}

	public boolean castlingAvailable(boolean side, boolean squares, long attacks) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long pieceMask;
		long attackMask;
		if(side == Board.WHITE) {
			// check if king moved
			if(moved[0]) {
				return false;
			}

			if(squares == Board.KINGSIDE) {
				// check if kingside rook moved
				if(moved[2]) {
					return false;
				}
				pieceMask = 0x60L;
				attackMask = pieceMask;
			} else {
				// check if queenside rook moved
				if(moved[3]) {
					return false;
				}
				pieceMask = 0xEL;
				attackMask = 0xCL;
			}
		} else {
			// check if king moved
			if(moved[1]) {
				return false;
			}

			if(squares == Board.KINGSIDE) {
				// check if kingside rook moved
				if(moved[4]) {
					return false;
				}
				pieceMask = 0x6000000000000000L;
				attackMask = pieceMask;
			} else {
				// check if queenside rook moved
				if(moved[5]) {
					return false;
				}
				pieceMask = 0xE00000000000000L;
				attackMask = 0xC00000000000000L;
			}
		}

		// check if any pieces between king and rook. also check if opponent
		// is attacking squares king passes or ends up on
		if(((pieceMask & getAllPieces()) == 0L) && ((attackMask & attacks) == 0L)) {
			return true;
		} else {
			return false;
		}
	}
}
