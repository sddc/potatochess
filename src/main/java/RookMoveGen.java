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
		return getRookMoves(side, fromSquare.intValue);
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
	public boolean isPositionAttacked(boolean side, long position) {
		for(Square s : getOccupancyIndexes(position)) {
			if((board.getRookBitboard(!side) & getRookMoves(side, s.intValue)) != 0L) {
				return true;
			}
		}

		return false;
	}
	 
}
