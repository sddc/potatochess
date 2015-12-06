import java.util.ArrayList;

public class BishopMoveGen extends SlidingMoveGen {
	private static BishopMoveGen instance = new BishopMoveGen();

	private BishopMoveGen() {
	}

	public static BishopMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(boolean side, Square fromSquare) {
		return getBishopMoves(fromSquare.intValue) & ~board.getSidePieces(side);
	}

	@Override
	public Piece sidePiece(boolean side) {
		if(side) {
			return Piece.WHITE_BISHOP;
		} else {
			return Piece.BLACK_BISHOP;
		}
	}

	@Override
	public boolean isKingInCheck(boolean side) {
		Square kingSquare = getOccupancyIndexes(board.getKingBitboard(side))[0];

		if((board.getBishopBitboard(!side) & getBishopMoves(kingSquare.intValue)) != 0L) {
			return true;
		}

		return false;
	}
	 
}
