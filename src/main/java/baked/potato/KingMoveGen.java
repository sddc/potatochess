package baked.potato;

public class KingMoveGen extends MoveGen {
	private static KingMoveGen instance = new KingMoveGen();
	public static final long[] kingMoves = genKingMoves();

	private KingMoveGen() {
	}

	public static KingMoveGen getInstance() {
		return instance;
	}

	@Override
	public void generateMoves(Board b, Movelist ml, boolean captureMovesOnly, boolean kingInCheck, long movemask, long pinned) {
		boolean side = b.getActiveColor();
		long attacked = MoveGen.attackedSquares(b, !side);
		long fromBB = b.getKingBitboard(side);
		int fromSquare = Long.numberOfTrailingZeros(fromBB);
		long moveBitboard = genMoveBitboard(b, side, fromSquare) & ~b.getSidePieces(side) & ~attacked;

		if(captureMovesOnly) {
			moveBitboard &= b.getSidePieces(!side);
		}

		for(long toBB = moveBitboard; toBB != 0; toBB &= toBB - 1) {
			int toSquare = Long.numberOfTrailingZeros(toBB);
			Move move = new Move(fromSquare, toSquare, sidePiece(side));
			// todo: mailbox
			Piece type = b.getPieceType(Square.toEnum(toSquare));

			if(type != Piece.EMPTY) {
				move.setFlag(Flag.CAPTURE);
				move.setCapturePieceType(type);
				move.score = pieceValues[type.intValue] * 100 - pieceValues[sidePiece(side).intValue];
			}

			ml.addMove(move);
		}

		if(!captureMovesOnly && !kingInCheck) {
			genCastlingMoves(b, ml, attacked);
		}
	}

	@Override
	public long genMoveBitboard(Board b, boolean side, int fromSquare) {
		return kingMoves[fromSquare];
	}

	@Override
	public Piece sidePiece(boolean side) {
		if(side) {
			return Piece.WHITE_KING;
		} else {
			return Piece.BLACK_KING;
		}
	}

	@Override
	public boolean squareAttacked(Board b, boolean side, int square) {
//		for(Square s : getOccupancyIndexes(b.getKingBitboard(!side))) {
//			long kingAttack = kingMoves[s.intValue] & ~b.getSidePieces(!side);
//
//			if((kingAttack & square) != 0L) {
//				return true;
//			}
//		}
//
//		return false;
		return (kingMoves[square] & b.getKingBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		throw new UnsupportedOperationException("not used for king movegen");
	}

	/*
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 * 1 2 3 _ _ _ _ _
	 * 8 x 4 _ _ _ _ _
	 * 7 6 5 _ _ _ _ _
	 * _ _ _ _ _ _ _ _
	 *
	 */
	public static long[] genKingMoves() {
		long[] genMoves = new long[64];
		long kingPos = 0x0000000000000001L;

		for(int i = 0; i < 64; i++) {
			long pos1 = (kingPos & clearFileA) << 7;
			long pos8 = (kingPos & clearFileA) >>> 1;
			long pos7 = (kingPos & clearFileA) >>> 9;

			long pos2 = kingPos << 8;
			long pos6 = kingPos >>> 8;

			long pos3 = (kingPos & clearFileH) << 9;
			long pos4 = (kingPos & clearFileH) << 1;
			long pos5 = (kingPos & clearFileH) >>> 7;

			genMoves[i] = pos1 | pos2 | pos3 | pos4 | pos5 | pos6 | pos7 | pos8;
			kingPos = kingPos << 1;
		}

		return genMoves;
	}

	public boolean castlingSquaresAttacked(Board b, boolean side, boolean squares, long attackedSquares) {
		// side: true = white, false = black
		// squares: true = kingside, false = queenside
		long attackMask;
		if(side == Board.WHITE) {
			if(squares == Board.KINGSIDE) {
				attackMask = 0x60L;
			} else {
				attackMask = 0xCL;
			}
		} else {
			if(squares == Board.KINGSIDE) {
				attackMask = 0x6000000000000000L;
			} else {
				attackMask = 0xC00000000000000L;
			}
		}

		// check if opponent is attacking squares king passes or ends up on
		
//		for(MoveGen mg : moveGens) {
//			if(mg.squareAttacked(b, side, attackMask)) {
//				return true;
//			}
//		}
		if((attackedSquares & attackMask) != 0) return true;
		return false;
	}

	private void genCastlingMoves(Board b, Movelist ml, long attackedSquares) {
		boolean side = b.getActiveColor();
		Move move;
//		// check if king in check
//		for(MoveGen mg : moveGens) {
//			if(mg.squareAttacked(b, side, b.getKingBitboard(side))) {
//				return;
//			}
//		}

		// check if kingside castling is available
		if(b.castlingAvailable(side, Board.KINGSIDE) && !castlingSquaresAttacked(b, side, Board.KINGSIDE, attackedSquares)) {
			if(side == Board.WHITE) {
				move = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.KINGSIDE);
				ml.addMove(move);
			} else {
				move = new Move(Square.E8, Square.G8, Piece.BLACK_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.KINGSIDE);
				ml.addMove(move);
			}
		}

		// check if queenside castling is available
		if(b.castlingAvailable(side, Board.QUEENSIDE) && !castlingSquaresAttacked(b, side, Board.QUEENSIDE, attackedSquares)) {
			if(side == Board.WHITE) {
				move = new Move(Square.E1, Square.C1, Piece.WHITE_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.QUEENSIDE);
				ml.addMove(move);
			} else {
				move = new Move(Square.E8, Square.C8, Piece.BLACK_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.QUEENSIDE);
				ml.addMove(move);
			}
		}
	}
	
}
