public abstract class MoveGen {
	public static final long clearAFile = 0xFEFEFEFEFEFEFEFEL;
	public static final long clearBFile = 0xFDFDFDFDFDFDFDFDL;
	public static final long clearGFile = 0xBFBFBFBFBFBFBFBFL;
	public static final long clearHFile = 0x7F7F7F7F7F7F7F7FL;

	public static final long maskRank1 = 0x00000000000000FFL;
	public static final long maskRank2 = 0x000000000000FF00L;
	public static final long maskRank3 = 0x0000000000FF0000L;
	public static final long maskRank4 = 0x00000000FF000000L;
	public static final long maskRank5 = 0x000000FF00000000L;
	public static final long maskRank6 = 0x0000FF0000000000L;
	public static final long maskRank7 = 0x00FF000000000000L;
	public static final long maskRank8 = 0xFF00000000000000L;

	public static ArrayList<Square> get1BitIndexes(long x) {
		long compare = 0x0000000000000001L;
		int index = 0;
		ArrayList<Square> indexes = new ArrayList<Square>();

		while(x != 0) {
			if((x & compare) == 1L) {
				indexes.add(Square.toEnum(index));
			}
			x = x >>> 1;
			index++;
		}
		
		return indexes;
	}

	public static long get1BitMask(Square s) {
		long mask = 0x0000000000000001L;
		if(s.intValue == 0) {
			return mask;
		} else {
			return mask << s.intValue;
		}
	}
}
