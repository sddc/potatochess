import java.util.ArrayList;
import java.lang.UnsupportedOperationException;

public class PawnMoveGen extends MoveGen {
	private static PawnMoveGen instance = new PawnMoveGen();

	private PawnMoveGen() {
	}

	public static PawnMoveGen getInstance() {
		return instance;
	}

	@Override
	public ArrayList<Move> generateMoves(boolean side) {
		ArrayList<Move> moves = new ArrayList<Move>();

		for(Square fromSquare : getOccupancyIndexes(board.getPawnBitboard(side))) {
			long moveBitboard;

			// double pawn push
			moveBitboard = genDoublePawnPush(side, getSquareMask(fromSquare), board.getAllPieces());
			for(Square toSquare : getOccupancyIndexes(moveBitboard)) {
				Move move = new Move(fromSquare, toSquare, sidePiece(side));
				move.setFlag(Flag.DOUBLE_PAWN_PUSH);

				if(isValidMove(side, move, board.getKingBitboard(side))) {
					moves.add(move);
				}
			}

			// pawn push
			moveBitboard = genPawnPush(side, getSquareMask(fromSquare), board.getAllPieces());
			for(Square toSquare : getOccupancyIndexes(moveBitboard)) {
				if(isPromotion(toSquare)) {
					genPromotionMoves(side, false, fromSquare, toSquare, moves);
				} else {
					Move move = new Move(fromSquare, toSquare, sidePiece(side));
					if(isValidMove(side, move, board.getKingBitboard(side))) {
						moves.add(move);
					}
				}
			}

			// pawn attack
			long opponentPieces = board.getSidePieces(!side);
			if(board.lastMoveDPP()) {
				// add en passant square
				opponentPieces |= getSquareMask(board.getEpTargetSquare());
			}

			moveBitboard = opponentPieces & genPawnAttack(side, getSquareMask(fromSquare), board.getSidePieces(side));
			for(Square toSquare : getOccupancyIndexes(moveBitboard)) {
				Move move = new Move(fromSquare, toSquare, sidePiece(side));

				if(board.lastMoveDPP() && (toSquare == board.getEpTargetSquare())) {
					move.setFlag(Flag.EP_CAPTURE);
					if(side == Board.WHITE) {
						move.setCapturePieceType(Piece.BLACK_PAWN);
					} else {
						move.setCapturePieceType(Piece.WHITE_PAWN);
					}
					if(isValidMove(side, move, board.getKingBitboard(side))) {
						moves.add(move);
					}
				} else {
					if(isPromotion(toSquare)) {
						genPromotionMoves(side, true, fromSquare, toSquare, moves);
					} else {
						Piece type = board.getPieceType(toSquare);
						move.setFlag(Flag.CAPTURE);
						move.setCapturePieceType(type);
						if(isValidMove(side, move, board.getKingBitboard(side))) {
							moves.add(move);
						}
					}
				}

			}
		}
		return moves;
	}

	private boolean isPromotion(Square toSquare) {
		if(((getSquareMask(toSquare) & maskRank1) != 0) || ((getSquareMask(toSquare) & maskRank8) != 0)) {
			return true;
		}

		return false;
	}

	private void genPromotionMoves(boolean side, boolean isAttack, Square fromSquare, Square toSquare, ArrayList<Move> moves) {
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
				Piece type = board.getPieceType(toSquare);
				p.setCapturePieceType(type);
				p.setFlag(Flag.CAPTURE);
			}
			p.setFlag(Flag.PROMOTION);
			if(isValidMove(side, p, board.getKingBitboard(side))) {
				moves.add(p);
			}
		}
	}

	@Override
	public long genMoveBitboard(boolean side, Square fromSquare) {
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
	public boolean isPositionAttacked(boolean side, long position) {
		long opponentAttacks = genPawnAttack(!side, board.getPawnBitboard(!side), board.getSidePieces(!side));
		if((opponentAttacks & position) != 0L) {
			return true;
		}
		return false;
	}

	private static long genPawnPush(boolean side, long pawnPositions, long allPieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~allPieces & (pawnPositions << 8);
		} else {
			return ~allPieces & (pawnPositions >>> 8);
		}
	}

	private static long genDoublePawnPush(boolean side, long pawnPositions, long allPieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~allPieces & ((maskRank3 & genPawnPush(side, pawnPositions, allPieces)) << 8);
		} else {
			return ~allPieces & ((maskRank6 & genPawnPush(side, pawnPositions, allPieces)) >>> 8);
		}
	}

	private static long genPawnAttack(boolean side, long pawnPositions, long sidePieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~sidePieces & (((clearFileH & pawnPositions) << 9) |
					((clearFileA & pawnPositions) << 7));
		} else {
			return ~sidePieces & (((clearFileA & pawnPositions) >>> 9) |
					((clearFileH & pawnPositions) >>> 7));
		}
	}
}
