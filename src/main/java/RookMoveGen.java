import java.util.ArrayList;

public class RookMoveGen extends SlidingMoveGen {
	private static RookMoveGen instance = new RookMoveGen();

	private RookMoveGen() {
	}

	public static RookMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(boolean side, Square fromSquare) {
		return getRookMoves(fromSquare.intValue) & ~board.getSidePieces(side);
	}
	
	@Override
	public Piece sidePiece(boolean side) {
		if(side) {
			return Piece.WHITE_ROOK;
		} else {
			return Piece.BLACK_ROOK;
		}
	}

	@Override
	public boolean isKingInCheck(boolean side) {
		Square kingSquare = getOccupancyIndexes(board.getKingBitboard(side))[0];

		if((board.getRookBitboard(!side) & getRookMoves(kingSquare.intValue)) != 0L) {
			return true;
		}

		return false;
	}
	 
}
