package baked.potato;

public class QueenMoveGen extends SlidingMoveGen {
	private static QueenMoveGen instance = new QueenMoveGen();

	private QueenMoveGen() {
	}

	public static QueenMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, boolean side, int fromSquare) {
		return getRookMoves(b, fromSquare) | getBishopMoves(b, fromSquare);
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
	public boolean isPositionAttacked(Board b, boolean side, long position) {
		for(Square s : getOccupancyIndexes(position)) {
			long queenAttack = getBishopMoves(b, s.intValue) | getRookMoves(b, s.intValue);

			if((b.getQueenBitboard(!side) & queenAttack) != 0L) {
				return true;
			}
		}

		return false;
	}
	 
}
