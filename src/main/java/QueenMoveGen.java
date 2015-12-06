import java.util.ArrayList;

public class QueenMoveGen extends SlidingMoveGen {
	private static QueenMoveGen instance = new QueenMoveGen();

	private QueenMoveGen() {
	}

	public static QueenMoveGen getInstance() {
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
