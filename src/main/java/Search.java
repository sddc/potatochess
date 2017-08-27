import java.util.ArrayList;

public class Search {
    public static Move getBestMove(Board b, int depth, boolean side) {
        ScoredMove sm = negamax(b, depth, Integer.MIN_VALUE, Integer.MAX_VALUE, side);
        return sm.m;
    }

    private static ScoredMove negamax(Board b, int depth, int alpha, int beta, boolean side) {
        int color = side ? 1 : -1;

        if(depth == 0) {
            return new ScoredMove(null, Evaluation.materialScore(b) * color);
        }

        ArrayList<Move> moves = MoveGen.getMoves(side);
        if(moves.size() == 0) {
            return new ScoredMove(null, Evaluation.materialScore(b) * color);
        }
        
        Move bestMove = null;
        int bestScore= Integer.MIN_VALUE;
        int score;

        for(Move m : moves) {
            b.move(side, m);
            ScoredMove sm = negamax(b, depth - 1, -beta, -alpha, !side);
            b.undoMove(side);
            sm.score *= -1;

            if(sm.score > bestScore) {
                bestScore = sm.score;
                bestMove = m;
            }

            alpha = Math.max(sm.score, alpha);
            if(alpha >= beta) {
                break;
            }
        }

        return new ScoredMove(bestMove, bestScore);
    }
}
