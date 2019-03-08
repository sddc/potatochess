package baked.potato;

public class QueenMoveGen extends SlidingMoveGen {
	private static QueenMoveGen instance = new QueenMoveGen();

	private QueenMoveGen() {
	}

	public static QueenMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
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
	public boolean squareAttacked(Board b, boolean side, int square) {
		return ((getRookMoves(b, square) | getBishopMoves(b, square)) & b.getQueenBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return (getRookMoves(b, square) | getBishopMoves(b, square)) & b.getQueenBitboard(!side);
	}
}
