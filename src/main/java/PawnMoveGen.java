import java.util.ArrayList;

public class PawnMoveGen extends MoveGen {
	public PawnMoveGen(Board board, boolean side) {
		super(board, side);
	}
	public ArrayList<Move> genMoves() {
		return null;
	}
	 
	public boolean isKingAttacked() {
		return false;
	}

	public static long genPawnPush(boolean side, long pawnPositions, long allPieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~allPieces & (pawnPositions << 8);
		} else {
			return ~allPieces & (pawnPositions >>> 8);
		}
	}

	public static long genDoublePawnPush(boolean side, long pawnPositions, long allPieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~allPieces & ((maskRank3 & genPawnPush(side, pawnPositions, allPieces)) << 8);
		} else {
			return ~allPieces & ((maskRank6 & genPawnPush(side, pawnPositions, allPieces)) >>> 8);
		}
	}

	public static long genPawnAttack(boolean side, long pawnPositions, long sidePieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~sidePieces & (((clearFileH & pawnPositions) << 9) |
				((clearFileA & pawnPositions) << 7));
		} else {
			return ~sidePieces & (((clearFileA & pawnPositions) >>> 9) |
				((clearFileH & pawnPositions) >>> 7));
		}
	}
}
