import java.util.ArrayList;

public class BishopMoveGen extends SlidingMoveGen {
	private static BishopMoveGen instance = new BishopMoveGen();

	private BishopMoveGen() {
	}

	public static BishopMoveGen getInstance() {
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
