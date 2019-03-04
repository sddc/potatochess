package baked.potato;

import java.util.ArrayList;
import java.util.Collections;

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

	protected static Board board = null;

	protected static MoveGen[] moveGens = {
	       PawnMoveGen.getInstance(),	
	       RookMoveGen.getInstance(),	
	       KnightMoveGen.getInstance(),	
	       BishopMoveGen.getInstance(),	
	       QueenMoveGen.getInstance(),	
	       KingMoveGen.getInstance()
	};

	public static void setBoard(Board board) {
		MoveGen.board = board;
	}

	public static ArrayList<Move> getMoves(boolean side) {
		if(board == null) {
			// check if board is set
			return null;
		}

		ArrayList<Move> moves = new ArrayList<Move>();

		for(MoveGen mg : moveGens) {
			moves.addAll(mg.generateMoves(side, false));
		}

		return moves;
	}

	public static ArrayList<Move> getCaptureMoves(boolean side) {
		if(board == null) {
			// check if board is set
			return null;
		}

		ArrayList<Move> moves = new ArrayList<Move>();

		for(MoveGen mg : moveGens) {
			moves.addAll(mg.generateMoves(side, true));
		}

		return moves;
	}

	public ArrayList<Move> generateMoves(boolean side, boolean captureMovesOnly) {
		ArrayList<Move> moves = new ArrayList<Move>();

		for(Square fromSquare : getOccupancyIndexes(board.getPieceBitboard(sidePiece(side)))) {
			long moveBitboard = genMoveBitboard(side, fromSquare);

			if(captureMovesOnly) {
				moveBitboard &= board.getSidePieces(!side);
			}

			for(Square toSquare : getOccupancyIndexes(moveBitboard)) {
				Move move = new Move(fromSquare, toSquare, sidePiece(side));
				Piece type = board.getPieceType(toSquare);

				if(type != Piece.EMPTY) {
					move.setFlag(Flag.CAPTURE);
					move.setCapturePieceType(type);
					move.score = pieceValues[type.intValue] * 100 - pieceValues[sidePiece(side).intValue];
				}
				
				if(isValidMove(side, move)) {
					moves.add(move);
				}
			}
		}

		return moves;
	}

	public boolean isValidMove(boolean side, Move move) {
		board.move(side, move);

		for(MoveGen mg : moveGens) {
			if(mg.isPositionAttacked(side, board.getKingBitboard(side))) {
				board.undoMove(side);
				return false;
			}
		}

		board.undoMove(side);

		return true;
	}

	public static boolean isKingInCheck(boolean side) {
		for(MoveGen mg : moveGens) {
			if(mg.isPositionAttacked(side, board.getKingBitboard(side))) {
				return true;
			}
		}

		return false;
	}

	abstract public long genMoveBitboard(boolean side, Square fromSquare);
	abstract public Piece sidePiece(boolean side);
	abstract public boolean isPositionAttacked(boolean side, long position);

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

	public static void sortMoves(ArrayList<Move> moves, Move pv) {
		int pvIdx = moves.indexOf(pv);
		if(pvIdx != -1) {
			moves.get(pvIdx).score = 10000;
			Collections.swap(moves, 0, pvIdx);
		}

		for(int i = 0; i < moves.size(); i++) {
			int max = moves.get(i).score;

			for(int j = i + 1; j < moves.size(); j++) {
				Move m = moves.get(j);

				if(moves.get(j).score > max) {
					Collections.swap(moves, i, j);
					max = m.score;
				}
			}
		}
	}
}
