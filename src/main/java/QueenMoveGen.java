import java.util.ArrayList;

public class QueenMoveGen extends SlidingMoveGen {
	private static QueenMoveGen instance = new QueenMoveGen();

	private QueenMoveGen() {
	}

	public static QueenMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(boolean side, Square fromSquare) {
		return (getRookMoves(fromSquare.intValue) | getBishopMoves(fromSquare.intValue)) & ~board.getSidePieces(side);
	}

	@Override
	public Piece sidePiece(boolean side) {
		if(side) {
			return Piece.WHITE_QUEEN;
		} else {
			return Piece.BLACK_QUEEN;
		}
	}

	@Override
	public boolean isKingInCheck(boolean side) {
		Square kingSquare = getOccupancyIndexes(board.getKingBitboard(side))[0];
		long queenAttackMask = getBishopMoves(kingSquare.intValue) | getRookMoves(kingSquare.intValue);

		if((board.getQueenBitboard(!side) & queenAttackMask) != 0L) {
			return true;
		}

		return false;
	}
	 
}
