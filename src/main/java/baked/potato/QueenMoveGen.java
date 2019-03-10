package baked.potato;

public class QueenMoveGen extends MoveGen {
	private static QueenMoveGen instance = new QueenMoveGen();

	private QueenMoveGen() {
	}

	public static QueenMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(Board b, int fromSquare) {
		long allPieces = b.getAllPieces();
		return Magic.getRookMoves(fromSquare, allPieces) | Magic.getBishopMoves(fromSquare, allPieces);
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
		long allPieces = b.getAllPieces();
		return ((Magic.getRookMoves(square, allPieces) | Magic.getBishopMoves(square, allPieces)) & b.getQueenBitboard(!side)) != 0;
	}

	@Override
	public long attackers(Board b, boolean side, int square) {
		long allPieces = b.getAllPieces();
		return (Magic.getRookMoves(square, allPieces) | Magic.getBishopMoves(square, allPieces)) & b.getQueenBitboard(!side);
	}
}
