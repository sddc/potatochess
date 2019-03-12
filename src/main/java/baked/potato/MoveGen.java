package baked.potato;

public abstract class MoveGen {
	// wp, wr, wn, wb, wq, wk, bp, br, bn, bb, bq, bk
	protected static final int[] pieceValues = {1, 5, 3, 3, 9, 20, 1, 5, 3, 3, 9, 20};

	protected static MoveGen[] moveGens = {
	       PawnMoveGen.getInstance(),	
	       RookMoveGen.getInstance(),	
	       KnightMoveGen.getInstance(),	
	       BishopMoveGen.getInstance(),	
	       QueenMoveGen.getInstance(),	
	       KingMoveGen.getInstance()
	};

	public static Movelist getMoves(Board b, boolean captureMovesOnly) {
		Movelist ml = new Movelist();

		int kingSquare = Long.numberOfTrailingZeros(b.getKingBitboard(b.getActiveColor()));
		long kingAttackers = 0;

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
				movemask |= Mask.between(kingSquare, attackerSquare);
			}

			for (MoveGen mg : moveGens) {
				mg.generateMoves(b, ml, captureMovesOnly, true, movemask, getPinnedPieces(b, kingSquare));
			}
		} else if(numAttackers == 2) {
			// generate only king moves
			moveGens[5].generateMoves(b, ml, captureMovesOnly, true, -1, 0);
		} else {
			// generate moves normally
			for (MoveGen mg : moveGens) {
				mg.generateMoves(b, ml, captureMovesOnly, false, -1, getPinnedPieces(b, kingSquare));
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

	public void generateMoves(Board b, Movelist ml, boolean captureMovesOnly, boolean kingInCheck, long movemask, long pinned) {
		boolean side = b.getActiveColor();

		for(long fromBB = b.getPieceBitboard(sidePiece(side)); fromBB != 0; fromBB &= fromBB - 1) {
			int fromSquare = Long.numberOfTrailingZeros(fromBB);
			long fromMask = Long.lowestOneBit(fromBB);

			long moveBitboard = genMoveBitboard(b, fromSquare) & ~b.getSidePieces(side) & movemask;

			if((pinned & fromMask) != 0) {
				moveBitboard &= getPinnedMovemask(b, fromSquare);
			}

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

	public static long getPinnedPieces(Board b, int kingSquare) {
		long pinned = 0;
		long allPieces = b.getAllPieces();
		boolean side = b.getActiveColor();

		// find pinned pieces between rook or queen
		long potPinned = Magic.getRookMoves(kingSquare, allPieces) & b.getSidePieces(side);
		long pinners = Magic.getRookMoves(kingSquare, allPieces ^ potPinned);
		pinners &= b.getRookBitboard(!side) | b.getQueenBitboard(!side);
		while(pinners != 0) {
			int pinnerSq = Long.numberOfTrailingZeros(pinners);
			pinned |= potPinned & Mask.between(pinnerSq, kingSquare);
			pinners &= pinners - 1;
		}

		// find pinned pieces between bishop or queen
		potPinned = Magic.getBishopMoves(kingSquare, allPieces) & b.getSidePieces(side);
		pinners = Magic.getBishopMoves(kingSquare, allPieces ^ potPinned);
		pinners &= b.getBishopBitboard(!side) | b.getQueenBitboard(!side);
		while(pinners != 0) {
			int pinnerSq = Long.numberOfTrailingZeros(pinners);
			pinned |= potPinned & Mask.between(pinnerSq, kingSquare);
			pinners &= pinners - 1;
		}

		return pinned;
	}

	public static long getPinnedMovemask(Board b, int pinnedSq) {
		boolean side = b.getActiveColor();
		long pinnedSqMask = 1L << pinnedSq;
		int kingSq = Long.numberOfTrailingZeros(b.getKingBitboard(side));
		long allPieces = b.getAllPieces() ^ pinnedSqMask;
		long pinner = Magic.getRookMoves(kingSq, allPieces) | Magic.getBishopMoves(kingSq, allPieces);
		pinner &= (b.getRookBitboard(!side) | b.getBishopBitboard(!side) | b.getQueenBitboard(!side));

		// find correct pinner
		while(pinner != 0) {
			int pinnerSq = Long.numberOfTrailingZeros(pinner);
			long betweenMask = Mask.between(kingSq, pinnerSq);

			if((pinnedSqMask & betweenMask) != 0) {
				return Long.lowestOneBit(pinner) | betweenMask;
			}

			pinner &= pinner - 1;
		}

		return 0;
	}
}
