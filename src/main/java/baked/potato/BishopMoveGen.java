package baked.potato;

public class BishopMoveGen extends SlidingMoveGen {
	private static BishopMoveGen instance = new BishopMoveGen();

	private BishopMoveGen() {
	}

	public static BishopMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(boolean side, Square fromSquare) {
		return getBishopMoves(side, fromSquare.intValue);
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
	public boolean isPositionAttacked(boolean side, long position) {
		for(Square s : getOccupancyIndexes(position)) {
			if((board.getBishopBitboard(!side) & getBishopMoves(side, s.intValue)) != 0L) {
				return true;
			}
		}

		return false;
	}
	 
}
