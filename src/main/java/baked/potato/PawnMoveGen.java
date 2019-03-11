package baked.potato;

import java.lang.UnsupportedOperationException;

public class PawnMoveGen extends MoveGen {
	private static PawnMoveGen instance = new PawnMoveGen();
	public static final long[][] pawnAttackMoves = genPawnAttackMoves();

	private PawnMoveGen() {
	}

	public static PawnMoveGen getInstance() {
		return instance;
	}

	@Override
	public void generateMoves(Board b, Movelist ml, boolean captureMovesOnly, boolean kingInCheck, long movemask, long pinned) {
		boolean side = b.getActiveColor();

		for(long fromBB = b.getPawnBitboard(side); fromBB != 0; fromBB &= fromBB - 1) {
			int fromSquare = Long.numberOfTrailingZeros(fromBB);
			long fromMask = Long.lowestOneBit(fromBB);
			long moveBitboard;
			long pinnedMovemask = -1;


			if((pinned & fromMask) != 0) {
				pinnedMovemask = getPinnedMovemask(b, fromSquare);
			}

			if(!captureMovesOnly) {
				// pawn push
				moveBitboard = 1L << (fromSquare + (side ? 8 : -8)) & ~b.getAllPieces();
				if(moveBitboard != 0) {
					if((moveBitboard & movemask & pinnedMovemask) != 0) {
						int toSquare = Long.numberOfTrailingZeros(moveBitboard);
						Move move = new Move(fromSquare, toSquare);

						if ((moveBitboard & Mask.maskRank1) != 0 || (moveBitboard & Mask.maskRank8) != 0) {
							for(int i = 0; i < 4; i++) {
								move = new Move(fromSquare, toSquare);
								move.move |= Move.PROMOTION_FLAG | (i << 14);
								ml.addMove(move);
							}
						} else {
							ml.addMove(move);
						}
					}

					// double pawn push
					moveBitboard = (side ? (Mask.maskRank3 & moveBitboard) << 8 : (Mask.maskRank6 & moveBitboard) >>> 8) & ~b.getAllPieces();
					if((moveBitboard & movemask & pinnedMovemask) != 0) {
						int toSquare = Long.numberOfTrailingZeros(moveBitboard);
						Move move = new Move(fromSquare, toSquare);
						ml.addMove(move);
					}
				}
			}

			// pawn attack
			long opponentPieces = b.getSidePieces(!side);

			long epMask = 0;
			if(b.getEpSquare() != Square.NO_SQ.intValue) {
				epMask = 1L << b.getEpSquare();
				opponentPieces |= epMask;
			}

			moveBitboard = opponentPieces & (pawnAttackMoves[side ? 0 : 1][fromSquare] & movemask & pinnedMovemask);
			for(long toBB = moveBitboard; toBB != 0; toBB &= toBB - 1) {
				int toSquare = Long.numberOfTrailingZeros(toBB);
				Move move = new Move(fromSquare, toSquare);

				if((epMask & (toBB & -toBB)) != 0) {
					move.move |= Move.EP_FLAG;
					move.score = 99;

					ml.addMove(move);
				} else {
					int type = b.getPieceType(toSquare);
					int score = pieceValues[type] * 100 - pieceValues[sidePiece(side).intValue];
					if ((moveBitboard & Mask.maskRank1) != 0 || (moveBitboard & Mask.maskRank8) != 0) {
						for(int i = 0; i < 4; i++) {
							move = new Move(fromSquare, toSquare);
							move.move |= Move.PROMOTION_FLAG | (i << 14);
							move.score = score;
							ml.addMove(move);
						}
					} else {
						move.score = score;
						ml.addMove(move);
					}
				}

			}
		}
	}

	@Override
	public long genMoveBitboard(Board b, int fromSquare) {
		throw new UnsupportedOperationException("not used for pawn movegen");
	}

	@Override
	public Piece sidePiece(boolean side) {
		if(side) {
			return Piece.WHITE_PAWN;
		} else {
			return Piece.BLACK_PAWN;
		}
	}

	@Override
	public boolean squareAttacked(Board b, boolean side, int square) {
		return (pawnAttackMoves[side ? 0 : 1][square] & b.getPawnBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return pawnAttackMoves[side ? 0 : 1][square] & b.getPawnBitboard(!side);
	}

	public static long genPawnAttack(boolean side, long pawnPositions) {
		// side:
		// white = true
		// black = false
		if(side) {
			return (((Mask.clearFileH & pawnPositions) << 9) |
					((Mask.clearFileA & pawnPositions) << 7));
		} else {
			return (((Mask.clearFileA & pawnPositions) >>> 9) |
					((Mask.clearFileH & pawnPositions) >>> 7));
		}
	}

	public static long[][] genPawnAttackMoves() {
		long[][] genMoves = new long[2][64];
		long pawnPos = 1L;

		for(int i = 0; i < 64; i++) {
			long pawnAttackPos = pawnPos & (Mask.clearRank1 | Mask.clearRank8);

			// white pawn attacks
			genMoves[0][i] = genPawnAttack(true, pawnAttackPos);

			// black pawn attacks
			genMoves[1][i] = genPawnAttack(false, pawnAttackPos);

			pawnPos <<= 1;
		}

		return genMoves;
	}
}
