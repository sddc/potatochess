public class PawnMoveGen extends MoveGen {
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
			return ~allPieces & ((Board.maskRank3 & genPawnPush(side, pawnPositions, allPieces)) << 8);
		} else {
			return ~allPieces & ((Board.maskRank6 & genPawnPush(side, pawnPositions, allPieces)) >>> 8);
		}
	}

	public static long genPawnAttack(boolean side, long pawnPositions, long sidePieces) {
		// side:
		// white = true
		// black = false
		if(side) {
			return ~sidePieces & (((Board.clearHFile & pawnPositions) << 9) |
				((Board.clearAFile & pawnPositions) << 7));
		} else {
			return ~sidePieces & (((Board.clearAFile & pawnPositions) >>> 9) |
				((Board.clearHFile & pawnPositions) >>> 7));
		}
	}
}
