package baked.potato;

public class TTEntry {
    public static final int flagPvNode = 1;
    public static final int flagAllNode = 2;
    public static final int flagCutNode = 4;


    public long positionKey = 0;
    public Move bestMove = null;
    public int score = 0;
    public byte depth = 0;
    public byte age = 0;
    public byte flag = 0;
}
