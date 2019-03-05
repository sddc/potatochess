package baked.potato;

public class BishopMoveGen extends SlidingMoveGen {
	private static BishopMoveGen instance = new BishopMoveGen();

	private BishopMoveGen() {
	}

	public static BishopMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, boolean side, int fromSquare) {
		return getBishopMoves(b, side, fromSquare);
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
	public boolean isPositionAttacked(Board b, boolean side, long position) {
		for(Square s : getOccupancyIndexes(position)) {
			if((b.getBishopBitboard(!side) & getBishopMoves(b, side, s.intValue)) != 0L) {
				return true;
			}
		}

		return false;
	}
	 
}
