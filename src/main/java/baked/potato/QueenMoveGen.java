package baked.potato;

public class QueenMoveGen extends SlidingMoveGen {
	private static QueenMoveGen instance = new QueenMoveGen();

	private QueenMoveGen() {
	}

	public static QueenMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(boolean side, Square fromSquare) {
		return getRookMoves(side, fromSquare.intValue) | getBishopMoves(side, fromSquare.intValue);
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
	public boolean isPositionAttacked(boolean side, long position) {
		for(Square s : getOccupancyIndexes(position)) {
			long queenAttack = getBishopMoves(side, s.intValue) | getRookMoves(side, s.intValue);

			if((board.getQueenBitboard(!side) & queenAttack) != 0L) {
				return true;
			}
		}

		return false;
	}
	 
}
