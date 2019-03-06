package baked.potato;

public class PreviousMove {
	public Move move;
	public long castleRights;
	public boolean lastMoveDoublePawnPush;
	public Square epTargetSquare;

	public PreviousMove(Move move, long castleRights, boolean lastMoveDoublePawnPush, Square epTargetSquare) {
		this.move = move;
		this.castleRights = castleRights;
		this.lastMoveDoublePawnPush = lastMoveDoublePawnPush;
		this.epTargetSquare = epTargetSquare;
	}
}
