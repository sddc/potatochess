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
			long moveBitboard;

			if(!captureMovesOnly) {
				// pawn push
				moveBitboard = 1L << (fromSquare + (side ? 8 : -8)) & ~b.getAllPieces();
				if(moveBitboard != 0) {
					if((moveBitboard & movemask) != 0) {
						int toSquare = Long.numberOfTrailingZeros(moveBitboard);

						if ((moveBitboard & maskRank1) != 0 || (moveBitboard & maskRank8) != 0) {
							genPromotionMoves(b, side, false, fromSquare, toSquare, ml, pinned);
						} else {
							Move move = new Move(fromSquare, toSquare, sidePiece(side));

							if(((fromBB & -fromBB) & pinned) != 0) {
								if(isValidMove(b, side, move)) {
									ml.addMove(move);
								}
							} else {
								ml.addMove(move);
							}
						}
					}

					// double pawn push
					moveBitboard = (side ? (maskRank3 & moveBitboard) << 8 : (maskRank6 & moveBitboard) >>> 8) & ~b.getAllPieces();
					if((moveBitboard & movemask) != 0) {
						int toSquare = Long.numberOfTrailingZeros(moveBitboard);
						Move move = new Move(fromSquare, toSquare, sidePiece(side));
						move.setFlag(Flag.DOUBLE_PAWN_PUSH);

						if(((fromBB & -fromBB) & pinned) != 0) {
							if(isValidMove(b, side, move)) {
								ml.addMove(move);
							}
						} else {
							ml.addMove(move);
						}
					}
				}
			}

			// pawn attack
			long opponentPieces = b.getSidePieces(!side);
			if(b.lastMoveDPP()) {
				// add en passant square
				opponentPieces |= getSquareMask(b.getEpTargetSquare());
			}

			// todo: pawn attack lookup table
//			moveBitboard = opponentPieces & genPawnAttack(side, 1L << fromSquare, b.getSidePieces(side));
			moveBitboard = opponentPieces & pawnAttackMoves[side ? 0 : 1][fromSquare] & movemask;
			for(long toBB = moveBitboard; toBB != 0; toBB &= toBB - 1) {
				int toSquare = Long.numberOfTrailingZeros(toBB);
				Move move = new Move(fromSquare, toSquare, sidePiece(side));

				if(b.lastMoveDPP() && (toSquare == b.getEpTargetSquare().intValue)) {
					move.setFlag(Flag.EP_CAPTURE);
					move.score = 99;
					if(side == Board.WHITE) {
						move.setCapturePieceType(Piece.BLACK_PAWN);
					} else {
						move.setCapturePieceType(Piece.WHITE_PAWN);
					}
					if(isValidMove(b, side, move)) {
						ml.addMove(move);
					}
				} else {
					if((moveBitboard & maskRank1) != 0 || (moveBitboard & maskRank8) != 0) {
						genPromotionMoves(b, side, true, fromSquare, toSquare, ml, pinned);
					} else {
						// todo: mailbox
						Piece type = b.getPieceType(Square.toEnum(toSquare));
						move.setFlag(Flag.CAPTURE);
						move.score = pieceValues[type.intValue] * 100 - pieceValues[sidePiece(side).intValue];
						move.setCapturePieceType(type);
						if(((fromBB & -fromBB) & pinned) != 0) {
							if(isValidMove(b, side, move)) {
								ml.addMove(move);
							}
						} else {
							ml.addMove(move);
						}
					}
				}

			}
		}
	}

//	private boolean isPromotion(Square toSquare) {
//		if(((getSquareMask(toSquare) & maskRank1) != 0) || ((getSquareMask(toSquare) & maskRank8) != 0)) {
//			return true;
//		}
//
//		return false;
//	}

	private void genPromotionMoves(Board b, boolean side, boolean isAttack, int fromSquare, int toSquare, Movelist ml, long pinned) {
		Move[] promotion = {
			new Move(fromSquare, toSquare, sidePiece(side)),
			new Move(fromSquare, toSquare, sidePiece(side)),
			new Move(fromSquare, toSquare, sidePiece(side)),
			new Move(fromSquare, toSquare, sidePiece(side)),
		};

		promotion[0].setPromotionType(moveGens[1].sidePiece(side));
		promotion[1].setPromotionType(moveGens[2].sidePiece(side));
		promotion[2].setPromotionType(moveGens[3].sidePiece(side));
		promotion[3].setPromotionType(moveGens[4].sidePiece(side));

		for(Move p : promotion) {
			if(isAttack) {
				// todo: mailbox
				Piece type = b.getPieceType(Square.toEnum(toSquare));
				p.setCapturePieceType(type);
				p.setFlag(Flag.CAPTURE);
				p.score = pieceValues[type.intValue] * 100 - pieceValues[sidePiece(side).intValue];
			}
			p.setFlag(Flag.PROMOTION);
			if(((1L << fromSquare) & pinned) != 0) {
				if(isValidMove(b, side, p)) {
					ml.addMove(p);
				}
			} else {
				ml.addMove(p);
			}
		}
	}

	@Override
	public long genMoveBitboard(Board b, boolean side, int fromSquare) {
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
//		long opponentAttacks = genPawnAttack(!side, b.getPawnBitboard(!side), b.getSidePieces(!side));
//		if((opponentAttacks & position) != 0L) {
//			return true;
//		}
//		return false;

//		for(; position != 0; position &= position - 1) {
//			int square = Long.numberOfTrailingZeros(position);
//			if((pawnAttackMoves[side ? 0 : 1][square] & b.getPawnBitboard(!side)) != 0) {
//				return true;
//			}
//		}
//
//		return false;

		return (pawnAttackMoves[side ? 0 : 1][square] & b.getPawnBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return pawnAttackMoves[side ? 0 : 1][square] & b.getPawnBitboard(!side);
	}

	//	private static long genPawnPush(boolean side, long pawnPositions, long allPieces) {
//		// side:
//		// white = true
//		// black = false
//		if(side) {
//			return ~allPieces & (pawnPositions << 8);
//		} else {
//			return ~allPieces & (pawnPositions >>> 8);
//		}
//	}

//	private static long genDoublePawnPush(boolean side, long pawnPositions, long allPieces) {
//		// side:
//		// white = true
//		// black = false
//		if(side) {
//			return ~allPieces & ((maskRank3 & genPawnPush(side, pawnPositions, allPieces)) << 8);
//		} else {
//			return ~allPieces & ((maskRank6 & genPawnPush(side, pawnPositions, allPieces)) >>> 8);
//		}
//	}

//	private static long genPawnAttack(boolean side, long pawnPositions, long sidePieces) {
//		// side:
//		// white = true
//		// black = false
//		if(side) {
//			return ~sidePieces & (((clearFileH & pawnPositions) << 9) |
//					((clearFileA & pawnPositions) << 7));
//		} else {
//			return ~sidePieces & (((clearFileA & pawnPositions) >>> 9) |
//					((clearFileH & pawnPositions) >>> 7));
//		}
//	}

	public static long[][] genPawnAttackMoves() {
		long[][] genMoves = new long[2][64];
		long pawnPos = 1L;

		for(int i = 0; i < 64; i++) {
			long pawnAttackPos = pawnPos & (clearRank1 | clearRank8);

			// white pawn attacks
			genMoves[0][i] = (((clearFileH & pawnAttackPos) << 9) | ((clearFileA & pawnAttackPos) << 7));

			// black pawn attacks
			genMoves[1][i] = (((clearFileA & pawnAttackPos) >>> 9) | ((clearFileH & pawnAttackPos) >>> 7));

			pawnPos <<= 1;
		}

		return genMoves;
	}
}
