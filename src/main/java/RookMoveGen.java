import java.util.ArrayList;

public class RookMoveGen extends SlidingMoveGen {
	public RookMoveGen(Board board, boolean side) {
		super(board, side);
	}
	public ArrayList<Move> genMoves() {
		return null;
	}
	 
	public boolean isKingAttacked() {
		return false;
	}
}
