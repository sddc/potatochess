public class PreviousMove {
	public Move move;
	public boolean[] castleStatus;
	public boolean lastMoveDoublePawnPush;
	public Square epTargetSquare;

	public PreviousMove(Move move, boolean[] castleStatus, boolean lastMoveDoublePawnPush, Square epTargetSquare) {
		this.move = move;
		this.castleStatus = castleStatus.clone();
		this.lastMoveDoublePawnPush = lastMoveDoublePawnPush;
		this.epTargetSquare = epTargetSquare;
	}
}
