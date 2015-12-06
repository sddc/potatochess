import java.util.ArrayList;

public class RookMoveGen extends SlidingMoveGen {
	private static RookMoveGen instance = new RookMoveGen();

	private RookMoveGen() {
	}

	public static RookMoveGen getInstance() {
		return instance;
	}

	@Override	
	public long genMoveBitboard(boolean side) {
		return 1L;
	}
	
	@Override
	public Piece sidePiece(boolean side) {
		return Piece.WHITE_PAWN;
	}

	@Override
	public boolean isKingInCheck(boolean side) {
		return false;
	}
	 
}
