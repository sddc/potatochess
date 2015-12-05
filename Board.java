import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;

public class Board {
	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	
	/* castleStatus array 
	 * 0 = white kingside not rook moved?
	 * 1 = white queenside not rook moved?
	 * 2 = black kingside not rook moved?
	 * 3 = black queenside not rook moved?
	 */
	private boolean[] castleStatus;

	private boolean activeColor;
	
	// en passant checks
	private boolean lastMoveDoublePawnPush;
	private Square epTargetSquare;

	/* Bitboard Board Representation
	 * Square to bit mapping
	 * H8 = MSB
	 * A1 = LSB
	 */
	private long[] bitboards;
	private long whitePieces;
	private long blackPieces;
	
	private Deque<PreviousMove> previousMoves = new ArrayDeque<PreviousMove>();

	public Board(long[] bitboards, boolean[] castleStatus, boolean lastMoveDoublePawnPush, Square epTargetSquare, boolean activeColor) {
		this.bitboards = bitboards;
		this.castleStatus = castleStatus;
		this.lastMoveDoublePawnPush = lastMoveDoublePawnPush;
		this.epTargetSquare = epTargetSquare;
		this.activeColor = activeColor;
		whitePieces = genSidePieces(Board.WHITE);
		blackPieces = genSidePieces(Board.BLACK); 
	}

	public boolean getActiveColor() {
		return activeColor;
	}

	public boolean toggleActiveColor() {
		activeColor = !activeColor;
		return activeColor;
	}

	public Square getEpTargetSquare() {
		return epTargetSquare;
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

	public long getPiece(Piece piece) {
		return bitboards[piece.intValue];
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
	
	private void printPiece(Piece type) {
		switch(type) {
			case EMPTY: System.out.print(" ");
				break;
			case WHITE_PAWN: System.out.print("P");
				break;
			case WHITE_ROOK: System.out.print("R");
				break;
			case WHITE_KNIGHT: System.out.print("N");
				break;
			case WHITE_BISHOP: System.out.print("B");
				break;
			case WHITE_QUEEN: System.out.print("Q");
				break;
			case WHITE_KING: System.out.print("K");
				break;
			case BLACK_PAWN: System.out.print("p");
				 break;
			case BLACK_ROOK: System.out.print("r");
				 break;
			case BLACK_KNIGHT: System.out.print("n");
				 break;
			case BLACK_BISHOP: System.out.print("b");
				 break;
			case BLACK_QUEEN: System.out.print("q");
				 break;
			case BLACK_KING: System.out.print("k");
				 break;
			default:
				break;
		}
	}
/*
	public void print() {
		int rank = 8;
		System.out.println("     A   B   C   D   E   F   G   H");
		System.out.println("   +---+---+---+---+---+---+---+---+");
		for(int i = 56; i >= 0; i -= 8) {
			System.out.print(" " + rank + " |");
			for(int j = i; j < (i + 8); j++) {
				System.out.print(" ");
				printPiece(getPieceType(Square.toEnum(j)));
				System.out.print(" |");
			}
			System.out.print(" " + rank--);
			System.out.println("\n   +---+---+---+---+---+---+---+---+");
		}
		System.out.println("     A   B   C   D   E   F   G   H");
	}

	public Piece getPieceType(Square s) {
		long mask = get1BitMask(s);
		for(int i = 0; i < 12; i++ ) {
			if((mask & bitboards[i]) != 0) {
				return Piece.toEnum(i);
			}
		}
		return Piece.EMPTY;
	}
*/
	private void modify(Piece type, long modifier) {
		// type selects which bitboard to modify
		// modifier is bitmask or which bits to toggle
		if(type == Piece.EMPTY) {
			return;
		}
		if(type.intValue < 6) {
			bitboards[type.intValue] ^= modifier;
			whitePieces ^= modifier;
		} else {
			bitboards[type.intValue] ^= modifier;
			blackPieces ^= modifier;
		}
	}

	public void move(boolean side, Move m) {
		/*
		// assumes m is at least pseudo legal

		// save move to undo
		previousMoves.addFirst(new PreviousMove(m, castleStatus, lastMoveDoublePawnPush, epTargetSquare));

		Square fromSquare = m.getFromSquare();
		Square toSquare = m.getToSquare();
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		Piece pieceType = m.getPieceType();

		// move within bitboard
		modify(pieceType, fromMask | toMask);

		// remove bit from capture bitboard
		if(m.getFlag(Flag.CAPTURE)) {
			Piece capturePieceType = m.getCapturePieceType();
			modify(capturePieceType, toMask);

			// castling checks
			if(capturePieceType == Piece.WHITE_ROOK && toSquare == Square.H1) {
				castleStatus[0] = false;
			}	

			if(capturePieceType == Piece.WHITE_ROOK && toSquare == Square.A1) {
				castleStatus[1] = false;
			}	

			if(capturePieceType == Piece.BLACK_ROOK && toSquare == Square.H8) {
				castleStatus[2] = false;
			}	

			if(capturePieceType == Piece.BLACK_ROOK && toSquare == Square.A8) {
				castleStatus[3] = false;
			}
		}

		if(m.getFlag(Flag.DOUBLE_PAWN_PUSH)) {
			lastMoveDoublePawnPush = true;
			if(side == Board.WHITE) {
				epTargetSquare = Square.toEnum(toSquare.intValue-8);
			} else {
				epTargetSquare = Square.toEnum(toSquare.intValue+8);
			}
		} else {
			lastMoveDoublePawnPush = false;
		}

		if(m.getFlag(Flag.EP_CAPTURE)) {
			if(side == Board.WHITE) {
				modify(Piece.BLACK_PAWN, get1BitMask(Square.toEnum(toSquare.intValue-8)));
			} else {
				modify(Piece.WHITE_PAWN, get1BitMask(Square.toEnum(toSquare.intValue+8)));
			}
		}

		if(m.getFlag(Flag.CASTLE)) {
			if(m.getCastleType()) {
				// kingside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.H1) | get1BitMask(Square.F1));
					castleStatus[0] = false;
					castleStatus[1] = false;
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.H8) | get1BitMask(Square.F8));
					castleStatus[2] = false;
					castleStatus[3] = false;
				}
			} else {
				// queenside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.A1) | get1BitMask(Square.D1));
					castleStatus[0] = false;
					castleStatus[1] = false;
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.A8) | get1BitMask(Square.D8));
					castleStatus[2] = false;
					castleStatus[3] = false;
				}
			}
		}

		if(m.getFlag(Flag.PROMOTION)) {
			// remove bit from pawn bitboard
			modify(pieceType, toMask);

			// add bit to chosen piece
			modify(m.getPromotionType(), toMask);
		}

		// castling checks
		if(pieceType == Piece.WHITE_KING && fromSquare == Square.E1) {
			castleStatus[0] = false;
			castleStatus[1] = false;
		}

		if(pieceType == Piece.BLACK_KING && fromSquare == Square.E8) {
			castleStatus[2] = false;
			castleStatus[3] = false;
		}

		if(pieceType == Piece.WHITE_ROOK && fromSquare == Square.H1) {
			castleStatus[0] = false;
		}	

		if(pieceType == Piece.WHITE_ROOK && fromSquare == Square.A1) {
			castleStatus[1] = false;
		}	

		if(pieceType == Piece.BLACK_ROOK && fromSquare == Square.H8) {
			castleStatus[2] = false;
		}	

		if(pieceType == Piece.BLACK_ROOK && fromSquare == Square.A8) {
			castleStatus[3] = false;
		}	
		*/
	}

	public void undoMove(boolean side) {
		/*
		if(previousMoves.isEmpty()) {
			return;
		}

		// get move information for previous move
		PreviousMove pm = previousMoves.removeFirst();
		Move m = pm.move;
		castleStatus = pm.castleStatus;
		lastMoveDoublePawnPush = pm.lastMoveDoublePawnPush;
		epTargetSquare = pm.epTargetSquare;

		Square fromSquare = m.getFromSquare();
		Square toSquare = m.getToSquare();
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		Piece pieceType = m.getPieceType();

		// restore move within bitboard
		modify(pieceType, fromMask | toMask);

		// restore bit from capture bitboard
		if(m.getFlag(Flag.CAPTURE)) {
			modify(m.getCapturePieceType(), toMask);
		}

		// restore ep capture
		if(m.getFlag(Flag.EP_CAPTURE)) {
			if(side == Board.WHITE) {
				modify(Piece.BLACK_PAWN, get1BitMask(Square.toEnum(toSquare.intValue-8)));
			} else {
				modify(Piece.WHITE_PAWN, get1BitMask(Square.toEnum(toSquare.intValue+8)));
			}
		}

		// restore castle	
		if(m.getFlag(Flag.CASTLE)) {
			if(m.getCastleType()) {
				// kingside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.H1) | get1BitMask(Square.F1));
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.H8) | get1BitMask(Square.F8));
				}
			} else {
				// queenside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.A1) | get1BitMask(Square.D1));
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.A8) | get1BitMask(Square.D8));
				}
			}
		}

		// restore promotion	
		if(m.getFlag(Flag.PROMOTION)) {
			// restore bit from pawn bitboard
			modify(pieceType, toMask);

			// remove bit from chosen piece
			modify(m.getPromotionType(), toMask);
		}
		*/
	}
}
