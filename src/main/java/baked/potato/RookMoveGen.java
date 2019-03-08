package baked.potato;

public class RookMoveGen extends SlidingMoveGen {
	private static RookMoveGen instance = new RookMoveGen();

	private RookMoveGen() {
	}

	public static RookMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
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
	public boolean squareAttacked(Board b, boolean side, int square) {
		return (getRookMoves(b, square) & b.getRookBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return getRookMoves(b, square) & b.getRookBitboard(!side);
	}
}
