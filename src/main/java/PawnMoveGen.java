import java.util.ArrayList;

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
/*
		if(chessboard.lastMoveDPP()) {
			epCapture = true;
			epCaptureSquare = chessboard.getEpTargetSquare();
			// add en passant square
			opponentPieces = chessboard.getSidePieces(!side);
			opponentPieces |= Board.get1BitMask(epCaptureSquare);
		} else {
			opponentPieces = chessboard.getSidePieces(!side);
		}

		// pawn attack
		for(Square i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = opponentPieces & MoveGen.genPawnAttack(side, Board.get1BitMask(i), chessboard.getSidePieces(side));
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, pawn);

				if(epCapture && (k == epCaptureSquare)) {
					m.setFlag(Flag.EP_CAPTURE);
					if(side == Board.WHITE) {
						m.setCapturePieceType(Piece.BLACK_PAWN);
					} else {
						m.setCapturePieceType(Piece.WHITE_PAWN);
					}
					validateAndAdd(side, m, moves);
				} else {
					if(((Board.get1BitMask(k) & Board.maskRank1) != 0) || 
							((Board.get1BitMask(k) & Board.maskRank8) != 0)) {
						Move[] promotion ={
							new Move(i, k, pawn),
							new Move(i, k, pawn),
							new Move(i, k, pawn),
							new Move(i, k, pawn)
						};
						promotion[0].setPromotionType(queen);
						promotion[1].setPromotionType(rook);
						promotion[2].setPromotionType(knight);
						promotion[3].setPromotionType(bishop);
						for(Move p : promotion) {
							type = chessboard.getPieceType(k);
							p.setCapturePieceType(type);
							p.setFlag(Flag.CAPTURE);
							p.setFlag(Flag.PROMOTION);
							validateAndAdd(side, p, moves);
						}
					} else {
						type = chessboard.getPieceType(k);
						m.setFlag(Flag.CAPTURE);
						m.setCapturePieceType(type);
						validateAndAdd(side, m, moves);
					}
				}

			}
		}

		// pawn push
		for(Square i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = MoveGen.genPawnPush(side, Board.get1BitMask(i), chessboard.getAllPieces());
			for(Square k : Board.get1BitIndexes(j)) {
				if(((Board.get1BitMask(k) & Board.maskRank1) != 0) || 
						((Board.get1BitMask(k) & Board.maskRank8) != 0)) {
					Move[] promotion ={
						new Move(i, k, pawn),
						new Move(i, k, pawn),
						new Move(i, k, pawn),
						new Move(i, k, pawn)
					};
					promotion[0].setPromotionType(queen);
					promotion[1].setPromotionType(rook);
					promotion[2].setPromotionType(knight);
					promotion[3].setPromotionType(bishop);
					for(Move p : promotion) {
						p.setFlag(Flag.PROMOTION);
						validateAndAdd(side, p, moves);
					}
				} else {
					m = new Move(i, k, pawn);
					validateAndAdd(side, m, moves);
				}
			}
		}

		// double pawn push
		for(Square i : Board.get1BitIndexes(chessboard.getPawns(side))) {
			j = MoveGen.genDoublePawnPush(side, Board.get1BitMask(i), chessboard.getAllPieces());
			for(Square k : Board.get1BitIndexes(j)) {
				m = new Move(i, k, pawn);
				m.setFlag(Flag.DOUBLE_PAWN_PUSH);
				validateAndAdd(side, m, moves);
			}
		}
*/
		return moves;
	}

	@Override
	public long genMoveBitboard(boolean side) {
		return 1L;
	}

	@Override
	public Piece sidePiece(boolean side) {
		return Piece.WHITE_PAWN;
	}

	@Override
	public boolean isKingInCheck(boolean side) {
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
