package baked.potato;

public abstract class MoveGen {
	public static final long clearFileA = 0xFEFEFEFEFEFEFEFEL;
	public static final long clearFileB = 0xFDFDFDFDFDFDFDFDL;
	public static final long clearFileG = 0xBFBFBFBFBFBFBFBFL;
	public static final long clearFileH = 0x7F7F7F7F7F7F7F7FL;

	public static final long clearRank1 = 0xFFFFFFFFFFFFFF00L;
	public static final long clearRank8 = 0xFFFFFFFFFFFFFFL;

	public static final long maskRank1 = 0x00000000000000FFL;
	public static final long maskRank2 = 0x000000000000FF00L;
	public static final long maskRank3 = 0x0000000000FF0000L;
	public static final long maskRank4 = 0x00000000FF000000L;
	public static final long maskRank5 = 0x000000FF00000000L;
	public static final long maskRank6 = 0x0000FF0000000000L;
	public static final long maskRank7 = 0x00FF000000000000L;
	public static final long maskRank8 = 0xFF00000000000000L;

	public static final long maskFileA = 0x101010101010101L;
	public static final long maskFileH = 0x8080808080808080L;

	// wp, wr, wn, wb, wq, wk, bp, br, bn, bb, bq, bk
	protected static final int[] pieceValues = {1, 5, 3, 3, 9, 20, 1, 5, 3, 3, 9, 20};

//	protected static Board board = null;

	protected static MoveGen[] moveGens = {
	       PawnMoveGen.getInstance(),	
	       RookMoveGen.getInstance(),	
	       KnightMoveGen.getInstance(),	
	       BishopMoveGen.getInstance(),	
	       QueenMoveGen.getInstance(),	
	       KingMoveGen.getInstance()
	};

//	public static void setBoard(Board board) {
//		MoveGen.board = board;
//	}

//	public static ArrayList<Move> getMoves(boolean side) {
//		if(board == null) {
//			// check if board is set
//			return null;
//		}
//
//		ArrayList<Move> moves = new ArrayList<Move>();
//
//		for(MoveGen mg : moveGens) {
//			moves.addAll(mg.generateMoves(side, false));
//		}
//
//		return moves;
//	}

	public static Movelist getMoves(Board b) {
		Movelist ml = new Movelist();
		boolean kingInCheck = isKingInCheck(b, b.getActiveColor());

		for(MoveGen mg : moveGens) {
			mg.generateMoves(b, ml, false, kingInCheck);
		}

		return ml;
	}

	public static Movelist getCaptureMoves(Board b) {
		Movelist ml = new Movelist();
		boolean kingInCheck = isKingInCheck(b, b.getActiveColor());

		for(MoveGen mg : moveGens) {
			mg.generateMoves(b, ml, true, kingInCheck);
		}

		return ml;
	}

//	public static ArrayList<Move> getCaptureMoves(boolean side) {
//		if(board == null) {
//			// check if board is set
//			return null;
//		}
//
//		ArrayList<Move> moves = new ArrayList<Move>();
//
//		for(MoveGen mg : moveGens) {
//			moves.addAll(mg.generateMoves(, side, true));
//		}
//
//		return moves;
//	}

	public void generateMoves(Board b, Movelist ml, boolean captureMovesOnly, boolean kingInCheck) {
		boolean side = b.getActiveColor();

		for(long fromBB = b.getPieceBitboard(sidePiece(side)); fromBB != 0; fromBB &= fromBB - 1) {
			int fromSquare = Long.numberOfTrailingZeros(fromBB);
			long moveBitboard = genMoveBitboard(b, side, fromSquare)& ~b.getSidePieces(side);

			if (captureMovesOnly) {
				moveBitboard &= b.getSidePieces(!side);
			}

			for(long toBB = moveBitboard; toBB != 0; toBB &= toBB - 1) {
				int toSquare = Long.numberOfTrailingZeros(toBB);
				Move move = new Move(fromSquare, toSquare, sidePiece(side));
				// todo: mailbox
				Piece type = b.getPieceType(Square.toEnum(toSquare));

				if (type != Piece.EMPTY) {
					move.setFlag(Flag.CAPTURE);
					move.setCapturePieceType(type);
					move.score = pieceValues[type.intValue] * 100 - pieceValues[sidePiece(side).intValue];
				}

				if (isValidMove(b, side, move)) {
					ml.addMove(move);
				}
			}
		}
	}

	public boolean isValidMove(Board b, boolean side, Move move) {
		b.move(side, move);

		for(MoveGen mg : moveGens) {
			if(mg.isPositionAttacked(b, side, b.getKingBitboard(side))) {
				b.undoMove(side);
				return false;
			}
		}

		b.undoMove(side);

		return true;
	}

	public static boolean isKingInCheck(Board b, boolean side) {
		for(MoveGen mg : moveGens) {
			if(mg.isPositionAttacked(b, side, b.getKingBitboard(side))) {
				return true;
			}
		}

		return false;
	}

	abstract public long genMoveBitboard(Board b, boolean side, int fromSquare);
	abstract public Piece sidePiece(boolean side);
	abstract public boolean isPositionAttacked(Board b, boolean side, long position);

	public static Square[] getOccupancyIndexes(long occupancy) {
		Square[] occupancyIndexes = new Square[Long.bitCount(occupancy)];
		int index = 0;

		for(int i = 0; i < 64; i++) {
			if((occupancy & 1L) != 0L) {
				occupancyIndexes[index++] = Square.toEnum(i);
			}
			occupancy = occupancy >>> 1;
		}
		return occupancyIndexes;
	}

	public static long getSquareMask(Square s) {
		return 1L << s.intValue;
	}

	public static void sortMoves(Movelist ml, Move pv) {
		for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
			Move m = ml.moves[mIdx];

			if(m.move == pv.move) {
				m.score = 10000;
			}
		}

		// selection sort
		for(int mIdx = 0; mIdx < ml.size(); mIdx++) {
			int max = ml.moves[mIdx].score;

			for(int mIdx2 = mIdx + 1; mIdx2 < ml.size(); mIdx2++) {
				if(ml.moves[mIdx2].score > max) {
					Move temp = ml.moves[mIdx];
					ml.moves[mIdx] = ml.moves[mIdx2];
					ml.moves[mIdx2] = temp;
					max = ml.moves[mIdx2].score;
				}
			}
		}
	}
}
