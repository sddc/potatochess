package baked.potato;

import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;

public class Board {
	public static final boolean WHITE = true;
	public static final boolean BLACK = false;
	public static final boolean KINGSIDE = true;
	public static final boolean QUEENSIDE = false;
	public static final long WKS_CASTLE_MASK = 0x90L;
	public static final long WQS_CASTLE_MASK = 0x11L;
	public static final long BKS_CASTLE_MASK = 0x9000000000000000L;
	public static final long BQS_CASTLE_MASK = 0x1100000000000000L;


	private long castleRights; // square has lost castle rights if the corresponding bit is set

	private boolean activeColor;
	
	// en passant checks
	private boolean lastMoveDoublePawnPush;
	private Square epTargetSquare;

	/* Bitboard baked.potato.Board Representation
	 * baked.potato.Square to bit mapping
	 * H8 = MSB
	 * A1 = LSB
	 */
	private long[] bitboards;
	private int[] pieceBoard;
	private long whitePieces;
	private long blackPieces;
    private int[] pieceCounts;

    private long positionKey = 0;

    private int ply = 0;
    private long kingStorage = 0;
	
	private Deque<PreviousMove> previousMoves = new ArrayDeque<PreviousMove>();

	public static final TT tt = new TT(64);

	public Board(long[] bitboards, long castleRights, boolean lastMoveDoublePawnPush, Square epTargetSquare, boolean activeColor) {
		this.bitboards = bitboards;
		this.castleRights = castleRights;
		this.lastMoveDoublePawnPush = lastMoveDoublePawnPush;
		this.epTargetSquare = epTargetSquare;
		this.activeColor = activeColor;
		whitePieces = genSidePieces(Board.WHITE);
		blackPieces = genSidePieces(Board.BLACK); 

        // count pieces
        pieceCounts = new int[12];
        for(int i = 0; i < 12; i++) {
            pieceCounts[i] = Long.bitCount(bitboards[i]);
        }

		initPieceBoard();
		positionKeyInit();
        ply = 0;
	}

	private void positionKeyInit() {
		positionKey = 0;

		for(int i = 0; i < 64; i++) {
			int pieceIdx = getPieceType(Square.toEnum(i)).intValue;

			if(pieceIdx != 12) {
				positionKey ^= Zobrist.randSquare[pieceIdx][i];
			}
		}

		if(!activeColor) {
			// xor if black
			positionKey ^= Zobrist.randSide;
		}

		if(lastMoveDoublePawnPush) {
			positionKey ^= Zobrist.randEp[epTargetSquare.intValue];
		}

		positionKey ^= Zobrist.randCastle[getCastlePosKey(castleRights)];
	}

	private int getCastlePosKey(long castleRights) {
		int castleIdx = 0;

		if((WKS_CASTLE_MASK & castleRights) == 0) castleIdx += 8;
		if((WQS_CASTLE_MASK & castleRights) == 0) castleIdx += 4;
		if((BKS_CASTLE_MASK & castleRights) == 0) castleIdx += 2;
		if((BQS_CASTLE_MASK & castleRights) == 0) castleIdx += 1;

		return castleIdx;
	}

	public void hideKing(boolean side) {
		if(kingStorage != 0) {
			throw new UnsupportedOperationException("previous king hidden");
		}

		if(side) {
			kingStorage = bitboards[Piece.WHITE_KING.intValue];
			whitePieces ^= kingStorage;
		} else {
			kingStorage = bitboards[Piece.BLACK_KING.intValue];
			blackPieces ^= kingStorage;
		}
	}

	public void showKing(boolean side) {
		if(kingStorage == 0) {
			throw new UnsupportedOperationException("no king to show");
		}

		if(side) {
			whitePieces ^= kingStorage;
		} else {
			blackPieces ^= kingStorage;
		}

		kingStorage = 0;
	}

    public long getPositionKey() {
        return positionKey;
    }

    public int[] getPieceCounts() {
        return pieceCounts.clone();
    }

    public long[] getBitboards() {
        return bitboards.clone();
    }

	public boolean getActiveColor() {
		return activeColor;
	}

	public boolean toggleActiveColor() {
		activeColor = !activeColor;

		positionKey ^= Zobrist.randSide;

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

	public long getPawnBitboard(boolean side) {
		if(side) {
			return bitboards[0];
		} else {
			return bitboards[6];
		}
	}

	public long getRookBitboard(boolean side) {
		if(side) {
			return bitboards[1];
		} else {
			return bitboards[7];
		}
	}

	public long getKnightBitboard(boolean side) {
		if(side) {
			return bitboards[2];
		} else {
			return bitboards[8];
		}
	}

	public long getBishopBitboard(boolean side) {
		if(side) {
			return bitboards[3];
		} else {
			return bitboards[9];
		}
	}

	public long getQueenBitboard(boolean side) {
		if(side) {
			return bitboards[4];
		} else {
			return bitboards[10];
		}
	}

	public long getKingBitboard(boolean side) {
		if(side) {
			return bitboards[5];
		} else {
			return bitboards[11];
		}
	}

	public long getPieceBitboard(Piece p) {
		return bitboards[p.intValue];
	}

	public long getSidePieces(boolean side) {
		if(side == Board.WHITE) {
			return whitePieces;
		} else {
			return blackPieces;
		}
	}

	public long getAllPieces() {
		return whitePieces | blackPieces;
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

	private void initPieceBoard() {
		pieceBoard = new int[64];
		Arrays.fill(pieceBoard, 12);
		long mask = 1L;

		for(int i = 0; i < 64; i++) {
			for(int j = 0; j < 12; j++) {
				if((bitboards[j] & mask) != 0) {
					pieceBoard[i] = j;
					break;
				}
			}

			mask <<= 1;
		}
	}

	public Piece getPieceType(Square s) {
		return Piece.toEnum(pieceBoard[s.intValue]);
	}

	public long get1BitMask(Square s) {
		return 1L << s.intValue;
	}

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

	// todo: side
	public void move(boolean side, Move m) {
		ply++;

		// save move to undo
		previousMoves.addFirst(new PreviousMove(m, castleRights, lastMoveDoublePawnPush, epTargetSquare));

		Square fromSquare = m.getFromSquare();
		Square toSquare = m.getToSquare();
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		Piece pieceType = m.getPieceType();

		// update castle rights
		castleRights |= fromMask;

		// move within bitboard
		modify(pieceType, fromMask | toMask);
		pieceBoard[fromSquare.intValue] = 12; // empty
		pieceBoard[toSquare.intValue] = pieceType.intValue;

		// update position key
		positionKey ^= Zobrist.randSquare[pieceType.intValue][fromSquare.intValue];
		positionKey ^= Zobrist.randSquare[pieceType.intValue][toSquare.intValue];

		// remove bit from capture bitboard
		if(m.getFlag(Flag.CAPTURE)) {
			Piece capturePieceType = m.getCapturePieceType();
			modify(capturePieceType, toMask);
			positionKey ^= Zobrist.randSquare[capturePieceType.intValue][toSquare.intValue];
            pieceCounts[capturePieceType.intValue]--;

			// update castle rights
			castleRights |= toMask;
		}

		if(previousMoves.peekFirst().lastMoveDoublePawnPush) {
			positionKey ^= Zobrist.randEp[previousMoves.peekFirst().epTargetSquare.intValue];
		}

		if(m.getFlag(Flag.DOUBLE_PAWN_PUSH)) {
			lastMoveDoublePawnPush = true;
			if(side == Board.WHITE) {
				epTargetSquare = Square.toEnum(toSquare.intValue-8);
			} else {
				epTargetSquare = Square.toEnum(toSquare.intValue+8);
			}

			positionKey ^= Zobrist.randEp[epTargetSquare.intValue];
		} else {
			lastMoveDoublePawnPush = false;
		}

		if(m.getFlag(Flag.EP_CAPTURE)) {
			if(side == Board.WHITE) {
				modify(Piece.BLACK_PAWN, get1BitMask(Square.toEnum(toSquare.intValue-8)));
				pieceBoard[toSquare.intValue-8] = 12;
				positionKey ^= Zobrist.randSquare[Piece.BLACK_PAWN.intValue][toSquare.intValue-8];
                pieceCounts[Piece.BLACK_PAWN.intValue]--;
			} else {
				modify(Piece.WHITE_PAWN, get1BitMask(Square.toEnum(toSquare.intValue+8)));
				pieceBoard[toSquare.intValue+8] = 12;
				positionKey ^= Zobrist.randSquare[Piece.WHITE_PAWN.intValue][toSquare.intValue+8];
                pieceCounts[Piece.WHITE_PAWN.intValue]--;
			}
		}

		if(m.getFlag(Flag.CASTLE)) {
			if(m.getCastleType()) {
				// kingside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.H1) | get1BitMask(Square.F1));
					pieceBoard[Square.H1.intValue] = 12;
					pieceBoard[Square.F1.intValue] = Piece.WHITE_ROOK.intValue;
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.H1.intValue];
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.F1.intValue];
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.H8) | get1BitMask(Square.F8));
					pieceBoard[Square.H8.intValue] = 12;
					pieceBoard[Square.F8.intValue] = Piece.BLACK_ROOK.intValue;
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.H8.intValue];
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.F8.intValue];
				}
			} else {
				// queenside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.A1) | get1BitMask(Square.D1));
					pieceBoard[Square.A1.intValue] = 12;
					pieceBoard[Square.D1.intValue] = Piece.WHITE_ROOK.intValue;
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.A1.intValue];
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.D1.intValue];
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.A8) | get1BitMask(Square.D8));
					pieceBoard[Square.A8.intValue] = 12;
					pieceBoard[Square.D8.intValue] = Piece.BLACK_ROOK.intValue;
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.A8.intValue];
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.D8.intValue];
				}
			}
		}

		if(m.getFlag(Flag.PROMOTION)) {
			// remove bit from pawn bitboard
			modify(pieceType, toMask);
			positionKey ^= Zobrist.randSquare[pieceType.intValue][toSquare.intValue];
            pieceCounts[pieceType.intValue]--;

			// add bit to chosen piece
            Piece promotionType = m.getPromotionType();
			modify(promotionType, toMask);
			pieceBoard[toSquare.intValue] = promotionType.intValue;
			positionKey ^= Zobrist.randSquare[promotionType.intValue][toSquare.intValue];
            pieceCounts[promotionType.intValue]++;
		}

		int castleIdx = getCastlePosKey(castleRights);
		int prevCastleIdx = getCastlePosKey(previousMoves.peekFirst().castleRights);
		if(castleIdx != prevCastleIdx) {
			positionKey ^= Zobrist.randCastle[prevCastleIdx];
			positionKey ^= Zobrist.randCastle[castleIdx];
		}
	}

	// todo: side
	public void undoMove(boolean side) {
		ply--;
		if(previousMoves.isEmpty()) {
			return;
		}

		// get move information for previous move
		PreviousMove pm = previousMoves.removeFirst();
		Move m = pm.move;

		int prevCastleIdx = getCastlePosKey(castleRights);
		castleRights = pm.castleRights;
		int castleIdx = getCastlePosKey(castleRights);
		if(castleIdx != prevCastleIdx) {
			positionKey ^= Zobrist.randCastle[prevCastleIdx];
			positionKey ^= Zobrist.randCastle[castleIdx];
		}

		if(lastMoveDoublePawnPush) {
			positionKey ^= Zobrist.randEp[epTargetSquare.intValue];
		}

		lastMoveDoublePawnPush = pm.lastMoveDoublePawnPush;
		epTargetSquare = pm.epTargetSquare;

		if(lastMoveDoublePawnPush) {
			positionKey ^= Zobrist.randEp[epTargetSquare.intValue];
		}

		Square fromSquare = m.getFromSquare();
		Square toSquare = m.getToSquare();
		long fromMask = get1BitMask(fromSquare);
		long toMask = get1BitMask(toSquare);
		Piece pieceType = m.getPieceType();

		// restore move within bitboard
		modify(pieceType, fromMask | toMask);
		pieceBoard[fromSquare.intValue] = pieceType.intValue;
		pieceBoard[toSquare.intValue] = 12; // empty

		positionKey ^= Zobrist.randSquare[pieceType.intValue][fromSquare.intValue];
		positionKey ^= Zobrist.randSquare[pieceType.intValue][toSquare.intValue];

		// restore bit from capture bitboard
		if(m.getFlag(Flag.CAPTURE)) {
			Piece capturePieceType = m.getCapturePieceType();
			modify(capturePieceType, toMask);
			pieceBoard[toSquare.intValue] = capturePieceType.intValue;
			positionKey ^= Zobrist.randSquare[capturePieceType.intValue][toSquare.intValue];
            pieceCounts[capturePieceType.intValue]++;
		}

		// restore ep capture
		if(m.getFlag(Flag.EP_CAPTURE)) {
			if(side == Board.WHITE) {
				modify(Piece.BLACK_PAWN, get1BitMask(Square.toEnum(toSquare.intValue-8)));
				pieceBoard[toSquare.intValue-8] = Piece.BLACK_PAWN.intValue;
				positionKey ^= Zobrist.randSquare[Piece.BLACK_PAWN.intValue][toSquare.intValue-8];
                pieceCounts[Piece.BLACK_PAWN.intValue]++;
			} else {
				modify(Piece.WHITE_PAWN, get1BitMask(Square.toEnum(toSquare.intValue+8)));
				pieceBoard[toSquare.intValue+8] = Piece.WHITE_PAWN.intValue;
				positionKey ^= Zobrist.randSquare[Piece.WHITE_PAWN.intValue][toSquare.intValue+8];
                pieceCounts[Piece.WHITE_PAWN.intValue]++;
			}
		}

		// restore castle	
		if(m.getFlag(Flag.CASTLE)) {
			if(m.getCastleType()) {
				// kingside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.H1) | get1BitMask(Square.F1));
					pieceBoard[Square.H1.intValue] = Piece.WHITE_ROOK.intValue;;
					pieceBoard[Square.F1.intValue] = 12;
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.H1.intValue];
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.F1.intValue];
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.H8) | get1BitMask(Square.F8));
					pieceBoard[Square.H8.intValue] = Piece.BLACK_ROOK.intValue;
					pieceBoard[Square.F8.intValue] = 12;
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.H8.intValue];
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.F8.intValue];
				}
			} else {
				// queenside castle
				if(side == Board.WHITE) {
					modify(Piece.WHITE_ROOK, get1BitMask(Square.A1) | get1BitMask(Square.D1));
					pieceBoard[Square.A1.intValue] = Piece.WHITE_ROOK.intValue;
					pieceBoard[Square.D1.intValue] = 12;
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.A1.intValue];
					positionKey ^= Zobrist.randSquare[Piece.WHITE_ROOK.intValue][Square.D1.intValue];
				} else {
					modify(Piece.BLACK_ROOK, get1BitMask(Square.A8) | get1BitMask(Square.D8));
					pieceBoard[Square.A8.intValue] = Piece.BLACK_ROOK.intValue;
					pieceBoard[Square.D8.intValue] = 12;
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.A8.intValue];
					positionKey ^= Zobrist.randSquare[Piece.BLACK_ROOK.intValue][Square.D8.intValue];
				}
			}
		}

		// restore promotion	
		if(m.getFlag(Flag.PROMOTION)) {
			// restore bit from pawn bitboard
			modify(pieceType, toMask);
			positionKey ^= Zobrist.randSquare[pieceType.intValue][toSquare.intValue];
            pieceCounts[pieceType.intValue]++;

			// remove bit from chosen piece
            Piece promotionType = m.getPromotionType();
			modify(promotionType, toMask);
			positionKey ^= Zobrist.randSquare[promotionType.intValue][toSquare.intValue];
            pieceCounts[promotionType.intValue]--;
		}
	}

	public boolean castlingAvailable(boolean side, boolean squares) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long pieceMask;
		if(side == Board.WHITE) {
			if(squares == KINGSIDE) {
				// check if kingside castle available
				if((WKS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0x60L;
			} else {
				// check if queenside castle available
				if((WQS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0xEL;
			}
		} else {
			if(squares == KINGSIDE) {
				// check if kingside castle available
				if((BKS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0x6000000000000000L;
			} else {
				// check if queenside castle available
				if((BQS_CASTLE_MASK & castleRights) != 0) {
					return false;
				}
				pieceMask = 0xE00000000000000L;
			}
		}

		// check if any pieces between king and rook
		if((pieceMask & (whitePieces | blackPieces)) == 0L) {
			return true;
		} else {
			return false;
		}
	}

	public int getPly() {
		return ply;
	}
}
