package baked.potato;

public class BishopMoveGen extends MoveGen {
	private static BishopMoveGen instance = new BishopMoveGen();

	private BishopMoveGen() {
	}

	public static BishopMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
		return Magic.getBishopMoves(fromSquare, b.getAllPieces());
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
		return (Magic.getBishopMoves(square, b.getAllPieces()) & b.getBishopBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return Magic.getBishopMoves(square, b.getAllPieces()) & b.getBishopBitboard(!side);
	}
}
