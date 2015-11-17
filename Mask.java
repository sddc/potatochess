public class Mask {
	public long squareMask;
	public int offset;

	public Mask(long squareMask, int offset) {
		this.squareMask = squareMask;
		this.offset = offset;
	}

	public long blockBitMask(int square) {
		long m = 0L;
		switch(offset) {
			case 1:
				m = MoveGen.plus1Mask[square];
				break;
			case 7:
				m = MoveGen.plus7Mask[square];
				break;
			case 8:
				m = MoveGen.plus8Mask[square];
				break;
			case 9:
				m = MoveGen.plus9Mask[square];
				break;
			case -1:
				m = MoveGen.minus1Mask[square];
				break;
			case -7:
				m = MoveGen.minus7Mask[square];
				break;
			case -8:
				m = MoveGen.minus8Mask[square];
				break;
			case -9:
				m = MoveGen.minus9Mask[square];
				break;
			default:
				break;
		}
		return m;
	}
}
