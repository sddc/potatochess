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
	public void generateMoves(Board b, Movelist ml, boolean captureMovesOnly, boolean kingInCheck, long movemask) {
		boolean side = b.getActiveColor();
		long attacked = MoveGen.attackedSquares(b, !side);
		long fromBB = b.getKingBitboard(side);
		int fromSquare = Long.numberOfTrailingZeros(fromBB);
		long moveBitboard = genMoveBitboard(b, fromSquare) & ~b.getSidePieces(side) & ~attacked;

		if(captureMovesOnly) {
			moveBitboard &= b.getSidePieces(!side);
		}

		for(long toBB = moveBitboard; toBB != 0; toBB &= toBB - 1) {
			int toSquare = Long.numberOfTrailingZeros(toBB);
			Move move = new Move(fromSquare, toSquare);
			int type = b.getPieceType(toSquare);

			if(type != Piece.EMPTY.intValue) {
				move.score = pieceValues[type] * 100 - pieceValues[sidePiece(side).intValue];
			}

			ml.addMove(move);
		}

		if(!captureMovesOnly && !kingInCheck) {
			genCastlingMoves(b, ml, attacked);
		}
	}

	@Override
	public long genMoveBitboard(Board b, int fromSquare) {
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
	public static long kingMoves(long kingPos) {
		long moves = 0;

		moves |= (kingPos & Mask.clearFileA) << 7; // pos 1
		moves |= (kingPos & Mask.clearFileA) >>> 1; // pos 8
		moves |= (kingPos & Mask.clearFileA) >>> 9; // pos 7

		moves |= kingPos << 8; // pos 2
		moves |= kingPos >>> 8; // pos 6

		moves |= (kingPos & Mask.clearFileH) << 9; // pos 3
		moves |= (kingPos & Mask.clearFileH) << 1; // pos 4
		moves |= (kingPos & Mask.clearFileH) >>> 7; // pos 5

		return moves;
	}

	public static long[] genKingMoves() {
		long[] genMoves = new long[64];
		long kingPos = 0x0000000000000001L;

		for(int i = 0; i < 64; i++) {
			genMoves[i] = kingMoves(kingPos);
			kingPos = kingPos << 1;
		}

		return genMoves;
	}

	public boolean castlingSquaresAttacked(boolean side, boolean squares, long attackedSquares) {
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
		if((attackedSquares & attackMask) != 0) return true;
		return false;
	}

	private void genCastlingMoves(Board b, Movelist ml, long attackedSquares) {
		boolean side = b.getActiveColor();
		Move move;

		// check if kingside castling is available
		if(b.castlingAvailable(side, Board.KINGSIDE) && !castlingSquaresAttacked(side, Board.KINGSIDE, attackedSquares)) {
			if(side == Board.WHITE) {
				move = new Move(Square.E1, Square.G1);
				move.move |= Move.CASTLE_FLAG;
				ml.addMove(move);
			} else {
				move = new Move(Square.E8, Square.G8);
				move.move |= Move.CASTLE_FLAG;
				ml.addMove(move);
			}
		}

		// check if queenside castling is available
		if(b.castlingAvailable(side, Board.QUEENSIDE) && !castlingSquaresAttacked(side, Board.QUEENSIDE, attackedSquares)) {
			if(side == Board.WHITE) {
				move = new Move(Square.E1, Square.C1);
				move.move |= Move.CASTLE_FLAG;
				ml.addMove(move);
			} else {
				move = new Move(Square.E8, Square.C8);
				move.move |= Move.CASTLE_FLAG;
				ml.addMove(move);
			}
		}
	}
	
}
