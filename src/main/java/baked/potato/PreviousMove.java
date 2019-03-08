package baked.potato;

public class PreviousMove {
	public Move move;
	int capture = Piece.EMPTY.intValue;
	public long castleRights;
	public int epSquare;
	int fiftyMove;
	int fullMove;
	long positionKey;
	int material;

	public PreviousMove(Move move, long castleRights, int epSquare, int fiftyMove, int fullMove, long positionKey) {
		this.move = move;
		this.castleRights = castleRights;
		this.epSquare = epSquare;
		this.fiftyMove = fiftyMove;
		this.fullMove = fullMove;
		this.positionKey = positionKey;
	}
}
