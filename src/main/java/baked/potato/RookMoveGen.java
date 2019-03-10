package baked.potato;

public class RookMoveGen extends MoveGen {
	private static RookMoveGen instance = new RookMoveGen();

	private RookMoveGen() {
	}

	public static RookMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
		return Magic.getRookMoves(fromSquare, b.getAllPieces());
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
		return (Magic.getRookMoves(square, b.getAllPieces()) & b.getRookBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		return Magic.getRookMoves(square, b.getAllPieces()) & b.getRookBitboard(!side);
	}
}
