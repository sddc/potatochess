package baked.potato;

import java.util.ArrayList;

public class KingMoveGen extends MoveGen {
	private static KingMoveGen instance = new KingMoveGen();
	public static final long[] kingMoves = genKingMoves();

	private KingMoveGen() {
	}

	public static KingMoveGen getInstance() {
		return instance;
	}

	@Override
	public ArrayList<Move> generateMoves(boolean side, boolean captureMovesOnly) {
		ArrayList<Move> moves = super.generateMoves(side, captureMovesOnly);

		if(!captureMovesOnly) {
			genCastlingMoves(side, moves);
		}

		return moves;
	}

	@Override
	public long genMoveBitboard(boolean side, Square fromSquare) {
		return kingMoves[fromSquare.intValue] & ~board.getSidePieces(side);
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
	public boolean isPositionAttacked(boolean side, long position) {
		for(Square s : getOccupancyIndexes(board.getKingBitboard(!side))) {
			long kingAttack = kingMoves[s.intValue] & ~board.getSidePieces(!side);

			if((kingAttack & position) != 0L) {
				return true;
			}
		}

		return false;
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

	public boolean castlingSquaresAttacked(boolean side, boolean squares) {
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
		
		for(MoveGen mg : moveGens) {
			if(mg.isPositionAttacked(side, attackMask)) {
				return true;
			}
		}
		return false;
	}

	private void genCastlingMoves(boolean side, ArrayList<Move> moves) {
		Move move;
		// check if king in check
		for(MoveGen mg : moveGens) {
			if(mg.isPositionAttacked(side, board.getKingBitboard(side))) {
				return;
			}
		}

		// check if kingside castling is available
		if(board.castlingAvailable(side, Board.KINGSIDE) && !castlingSquaresAttacked(side, Board.KINGSIDE)) {
			if(side == Board.WHITE) {
				move = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.KINGSIDE);
				moves.add(move);
			} else {
				move = new Move(Square.E8, Square.G8, Piece.BLACK_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.KINGSIDE);
				moves.add(move);
			}
		}

		// check if queenside castling is available
		if(board.castlingAvailable(side, Board.QUEENSIDE) && !castlingSquaresAttacked(side, Board.QUEENSIDE)) {
			if(side == Board.WHITE) {
				move = new Move(Square.E1, Square.C1, Piece.WHITE_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.QUEENSIDE);
				moves.add(move);
			} else {
				move = new Move(Square.E8, Square.C8, Piece.BLACK_KING);
				move.setFlag(Flag.CASTLE);
				move.setCastleType(Board.QUEENSIDE);
				moves.add(move);
			}
		}
	}
	
}
