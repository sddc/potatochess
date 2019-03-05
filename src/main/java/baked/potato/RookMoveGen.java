package baked.potato;

public class RookMoveGen extends SlidingMoveGen {
	private static RookMoveGen instance = new RookMoveGen();

	private RookMoveGen() {
	}

	public static RookMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, boolean side, int fromSquare) {
		return getRookMoves(b, fromSquare);
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
	public boolean isPositionAttacked(Board b, boolean side, long position) {
		for(Square s : getOccupancyIndexes(position)) {
			if((b.getRookBitboard(!side) & getRookMoves(b, s.intValue)) != 0L) {
				return true;
			}
		}

		return false;
	}
	 
}
