import java.util.ArrayList;

public abstract class SlidingMoveGen extends MoveGen {
	public static final long[] plus1Mask = genRayMask(1);
	public static final long[] plus7Mask = genRayMask(7);
	public static final long[] plus8Mask = genRayMask(8);
	public static final long[] plus9Mask = genRayMask(9);
	public static final long[] minus1Mask = genRayMask(-1);
	public static final long[] minus7Mask = genRayMask(-7);
	public static final long[] minus8Mask = genRayMask(-8);
	public static final long[] minus9Mask = genRayMask(-9);
	
	public static int firstOneBit(long x) {
		// =64 when x is 0
		return Long.numberOfTrailingZeros(x);
	}

	public static int lastOneBit(long x) {
		// =-1 when x is 0
		return (int)(63L - Long.numberOfLeadingZeros(x));
	}
	
	public static long genSlidingPieceMoves(Piece piece, Square square, long allPieces, long sidePieces) {
		long moves = 0x0000000000000000L;
		long m;
		int blockBit;
		ArrayList<Mask> masks = new ArrayList<Mask>();

		switch(piece) {
			case WHITE_BISHOP:
			case BLACK_BISHOP:
				masks.add(new Mask(plus7Mask[square.intValue], 7));
				masks.add(new Mask(minus7Mask[square.intValue], -7));
				masks.add(new Mask(plus9Mask[square.intValue], 9));
				masks.add(new Mask(minus9Mask[square.intValue], -9));
				break;
			case WHITE_QUEEN:
			case BLACK_QUEEN:
				masks.add(new Mask(plus7Mask[square.intValue], 7));
				masks.add(new Mask(minus7Mask[square.intValue], -7));
				masks.add(new Mask(plus9Mask[square.intValue], 9));
				masks.add(new Mask(minus9Mask[square.intValue], -9));
			case WHITE_ROOK:
			case BLACK_ROOK:
				masks.add(new Mask(plus1Mask[square.intValue], 1));
				masks.add(new Mask(minus1Mask[square.intValue], -1));
				masks.add(new Mask(plus8Mask[square.intValue], 8));
				masks.add(new Mask(minus8Mask[square.intValue], -8));
				break;
			default:
				break;
		}

		for(Mask i : masks) {
			m = i.squareMask & allPieces;
			if(m != 0L) {
				if(i.offset > 0) {
					blockBit = firstOneBit(m);
				} else {
					blockBit = lastOneBit(m);
				}

				moves |= (i.squareMask ^ i.blockBitMask(blockBit));
			} else {
				moves |= i.squareMask;
			}
		}

		return moves & ~sidePieces;
	}

	// +1, +7, +8, +9, -1, -7, -8, -9
	public static long shiftBits(int direction, long x) {
		long result = 0L;
		switch(direction) {
			case 7:
			case 8:
			case 9: 
			case 1:
				result = x << direction;
				break;
			case -7:
			case -8:
			case -9: 
			case -1:
				result = x >>> -direction;
				break;	
			default:
				break;

		}
		return result;
	}

	public static long[] genRayMask(int direction) {
		long[] genMask = new long[64];
		long pos = 0x0000000000000001L;
		long clearFile = 0xFFFFFFFFFFFFFFFFL;

		switch(direction) {
			case 7:
			case -1: 
			case -9:
				clearFile = Board.clearAFile;
				break;
			case 9:
			case 1:
			case -7:
				clearFile = Board.clearHFile;
				break;
			default:
				break;

		}

		for(int i = 0; i < 64; i++) {
			/* posNext initial position is LSB.
			 * Shift left or right x bits.
			 * If in file that will over/underflow to the next rank
			 * or the previous rank
			 * when shifted, AND it with the appropriate clearFile
			 * to end while loop and move pos to next bit.
			 */

			long posNext = shiftBits(direction, pos & clearFile);
			while(posNext != 0L) {
				genMask[i] = genMask[i] | posNext;
				posNext = shiftBits(direction, posNext & clearFile);
			}

			pos = pos << 1;
		}

		return genMask;
	}
}
