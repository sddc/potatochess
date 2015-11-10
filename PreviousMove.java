public class PreviousMove {
	public int fromSquare;
	public int toSquare;
	public int fromPieceType;
	public int toPieceType;
	public boolean[] moved;
	public boolean lastMoveDoublePawnPush;
	public int behindSquare;

	public PreviousMove(int fromSquare, int toSquare, int fromPieceType, int toPieceType, boolean[] moved, boolean lastMoveDoublePawnPush, int behindSquare) {
		this.fromSquare = fromSquare;
		this.toSquare = toSquare;
		this.fromPieceType = fromPieceType;
		this.toPieceType = toPieceType;
		this.moved = moved.clone();
		this.lastMoveDoublePawnPush = lastMoveDoublePawnPush;
		this.behindSquare = behindSquare;
	}
}
