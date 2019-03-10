package baked.potato;

public abstract class MoveGen {
	// wp, wr, wn, wb, wq, wk, bp, br, bn, bb, bq, bk
	protected static final int[] pieceValues = {1, 5, 3, 3, 9, 20, 1, 5, 3, 3, 9, 20};

	public static long getBetweenMask(int kingSq, int attackerSq) {
		int kingSqRank = kingSq / 8;
		int kingSqFile = kingSq % 8;
		int attackerSqRank = attackerSq / 8;
		int attackerSqFile = attackerSq % 8;
		long betweenMask = 0;
		int shift = 0;

		if(attackerSqRank > kingSqRank) {
			if(attackerSqFile < kingSqFile) {
				shift = 7;
			} else if(attackerSqFile > kingSqFile) {
				shift = 9;
			} else {
				shift = 8;
			}
		} else if(attackerSqRank < kingSqRank) {
			if(attackerSqFile < kingSqFile) {
				shift = -9;
			} else if(attackerSqFile > kingSqFile) {
				shift = -7;
			} else {
				shift = -8;
			}
		} else {
			shift = attackerSqFile < kingSqFile ? -1 : 1;
		}

		long kingBB = 1L << kingSq;
		long attackerBB = 1L << attackerSq;

		kingBB = shift < 0 ? kingBB >>> Math.abs(shift) : kingBB << shift;

		while(kingBB != attackerBB) {
			betweenMask |= kingBB;
			kingBB = shift < 0 ? kingBB >>> Math.abs(shift) : kingBB << shift;
		}

		return betweenMask;
	}

	protected static MoveGen[] moveGens = {
	       PawnMoveGen.getInstance(),	
	       RookMoveGen.getInstance(),	
	       KnightMoveGen.getInstance(),	
	       BishopMoveGen.getInstance(),	
	       QueenMoveGen.getInstance(),	
	       KingMoveGen.getInstance()
	};

	public static Movelist getMoves(Board b) {
		Movelist ml = new Movelist();

		int kingSquare = Long.numberOfTrailingZeros(b.getKingBitboard(b.getActiveColor()));
		long kingAttackers = 0;
		long pinned = moveGens[4].genMoveBitboard(b, kingSquare) & b.getSidePieces(b.getActiveColor());

		for(MoveGen mg : moveGens) {
			if(mg instanceof KingMoveGen) continue;
			kingAttackers |= mg.attackers(b, b.getActiveColor(), kingSquare);
		}

		int numAttackers = Long.bitCount(kingAttackers);
		if(numAttackers == 1) {
			long movemask = kingAttackers;
			int attackerSquare = Long.numberOfTrailingZeros(kingAttackers);
			Piece attackerPieceType = Piece.toEnum(b.getPieceType(attackerSquare));

			if(
					attackerPieceType == Piece.WHITE_ROOK ||
					attackerPieceType == Piece.WHITE_BISHOP ||
					attackerPieceType == Piece.WHITE_QUEEN ||
					attackerPieceType == Piece.BLACK_ROOK ||
					attackerPieceType == Piece.BLACK_BISHOP ||
					attackerPieceType == Piece.BLACK_QUEEN
			) {
				movemask |= getBetweenMask(kingSquare, attackerSquare);
			}

			for (MoveGen mg : moveGens) {
				mg.generateMoves(b, ml, false, true, movemask);
			}
		} else if(numAttackers == 2) {
			// generate only king moves
			moveGens[5].generateMoves(b, ml, false, true, -1);
		} else {
			// generate moves normally
			for (MoveGen mg : moveGens) {
				mg.generateMoves(b, ml, false, false, -1);
			}
		}

		return ml;
	}

	public static long attackedSquares(Board b, boolean side) {
		long squares = 0;

		for(MoveGen mg : moveGens) {
			if(mg instanceof PawnMoveGen) {
				squares |= ((PawnMoveGen) mg).genPawnAttack(side, b.getPawnBitboard(side));
			} else if(mg instanceof KnightMoveGen) {
				squares |= ((KnightMoveGen) mg).knightMoves(b.getKnightBitboard(side));
			} else if(mg instanceof KingMoveGen) {
				squares |= ((KingMoveGen) mg).kingMoves(b.getKingBitboard(side));
			} else {
				for(long fromBB = b.getPieceBitboard(mg.sidePiece(side)); fromBB != 0; fromBB &= fromBB - 1) {
					int fromSquare = Long.numberOfTrailingZeros(fromBB);
					long moveBitboard = 0;

					b.hideKing(!side);
					moveBitboard = mg.genMoveBitboard(b, fromSquare);
					b.showKing(!side);

					squares |= moveBitboard;
				}
			}
		}

		return squares & ~b.getSidePieces(side);
	}

	public void generateMoves(Board b, Movelist ml, boolean captureMovesOnly, boolean kingInCheck, long movemask) {
		boolean side = b.getActiveColor();

		for(long fromBB = b.getPieceBitboard(sidePiece(side)); fromBB != 0; fromBB &= fromBB - 1) {
			int fromSquare = Long.numberOfTrailingZeros(fromBB);
			long moveBitboard = genMoveBitboard(b, fromSquare) & ~b.getSidePieces(side) & movemask;

			if (captureMovesOnly) {
				moveBitboard &= b.getSidePieces(!side);
			}

			for(long toBB = moveBitboard; toBB != 0; toBB &= toBB - 1) {
				int toSquare = Long.numberOfTrailingZeros(toBB);
				Move move = new Move(fromSquare, toSquare);
				int type = b.getPieceType(toSquare);

				if (type != Piece.EMPTY.intValue) {
					move.score = pieceValues[type] * 100 - pieceValues[sidePiece(side).intValue];
				}

				ml.addMove(move);
			}
		}
	}

	public static boolean isKingInCheck(Board b, boolean side) {
		for(MoveGen mg : moveGens) {
			if(mg.squareAttacked(b, side, Long.numberOfTrailingZeros(b.getKingBitboard(side)))) {
				return true;
			}
		}

		return false;
	}

	abstract public long genMoveBitboard(Board b, int fromSquare);
	abstract public Piece sidePiece(boolean side);
	abstract public boolean squareAttacked(Board b, boolean side, int square);
	abstract public long attackers(Board b, boolean side, int square);

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

	public static void sortMoves(Movelist ml, Move pv) {
		if(pv != null) {
			for (int i = 0; i < ml.size(); i++) {
				Move m = ml.moves[i];

				if (m.move == pv.move) {
					m.score = 10000;
				}
			}
		}

		// selection sort
		for(int i = 0; i < ml.size(); i++) {
			int max = ml.moves[i].score;

			for(int j = i + 1; j < ml.size(); j++) {
				if(ml.moves[j].score > max) {
					max = ml.moves[j].score;

					Move temp = ml.moves[i];
					ml.moves[i] = ml.moves[j];
					ml.moves[j] = temp;
				}
			}
		}
	}
}
