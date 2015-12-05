import java.util.ArrayList;

public class BishopMoveGen extends SlidingMoveGen {
	public BishopMoveGen(Board board, boolean side) {
		super(board, side);
	}
	public ArrayList<Move> genMoves() {
		return null;
	}
	 
	public boolean isKingAttacked() {
		return false;
	}
}
