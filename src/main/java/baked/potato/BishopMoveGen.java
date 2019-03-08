package baked.potato;

public class BishopMoveGen extends SlidingMoveGen {
	private static BishopMoveGen instance = new BishopMoveGen();

	private BishopMoveGen() {
	}

	public static BishopMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
		return getBishopMoves(b, fromSquare);
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
	public boolean squareAttacked(Board b, boolean side, int square) {
		return (getBishopMoves(b, square) & b.getBishopBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return getBishopMoves(b, square) & b.getBishopBitboard(!side);
	}
}
