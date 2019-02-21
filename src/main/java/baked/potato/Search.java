package baked.potato;

import java.util.ArrayList;

public class Search {
    public static Move getBestMove(Board b, int depth, boolean side) {
        ScoredMove sm = negamax(b, depth, -1000000, 1000000, side);
        return sm.m;
    }

    private static ScoredMove negamax(Board b, int depth, int alpha, int beta, boolean side) {
        int color = side ? 1 : -1;

        if(depth == 0) {
            return new ScoredMove(null, Evaluation.score(b) * color);
        }

        ArrayList<Move> moves = MoveGen.getMoves(side);
        if(moves.size() == 0) {
            return new ScoredMove(null, Evaluation.score(b) * color);
        }
        
        Move bestMove = null;
        int bestScore = -1000000;
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
