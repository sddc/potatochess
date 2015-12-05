import java.util.ArrayList;

public class QueenMoveGen extends SlidingMoveGen {
	public QueenMoveGen(Board board, boolean side) {
		super(board, side);
	}
	public ArrayList<Move> genMoves() {
		return null;
	}
	 
	public boolean isKingAttacked() {
		return false;
	}
}
