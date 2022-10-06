package com.bloodLantern.chess;

import java.util.List;
import java.util.Random;

import com.bloodLantern.chess.pieces.Bishop;
import com.bloodLantern.chess.pieces.King;
import com.bloodLantern.chess.pieces.Knight;
import com.bloodLantern.chess.pieces.Pawn;
import com.bloodLantern.chess.pieces.Piece;
import com.bloodLantern.chess.pieces.Queen;
import com.bloodLantern.chess.pieces.Rook;

/**
 * The AI class is used to instantiate Chess AIs to play against.
 *
 * @author BloodLantern
 */
public final class AI {

	private static final int pawnValue = 100;
	private static final int knightValue = 300;
	private static final int bishopValue = 300;
	private static final int rookValue = 500;
	private static final int queenValue = 900;
	private final boolean white;

	/**
	 * Constructs a new AI.
	 */
	public AI(boolean white) {
		this.white = white;
	}

	/**
	 * This method searches for the best Move.
	 */
	private int search(int depth, int startingDepth, int alpha, int beta) {
		if (depth == 0)
			return searchAllCaptures(alpha, beta);

		boolean turn = startingDepth == depth ? white : depth % 2 == startingDepth % 2;

		List<Move> moves = Chess.getInstance().getPossibleMoves(turn);
		if (moves.size() == 0) {
			if (turn) {
				if (King.getWhiteKing().isInDanger())
					return Integer.MIN_VALUE;
			} else if (King.getBlackKing().isInDanger())
				return Integer.MIN_VALUE;
			return 0;
		}

		for (Move move : moves) {
			move.makeMove(false);
			int evaluation = -search(depth - 1, startingDepth, -beta, -alpha);
			move.unmakeMove(false);
			if (evaluation >= beta)
				// Move was too good, opponent will avoid this position
				return beta;
			alpha = Math.max(alpha, evaluation);
		}

		return alpha;
	}

	private int searchAllCaptures(int alpha, int beta) {
		// Captures aren't typlically forced, so see what the eval is before making a
		// capture. Otherwise if only bad captures are available, the position will be
		// evaluated as bas, even if good non-capture moves exist.
		int evaluation = evaluate();
		if (evaluation >= beta)
			return beta;
		alpha = Math.max(alpha, evaluation);

		List<Move> captureMoves = Chess.getInstance().getPossibleMoves(white);
		for (Move move : captureMoves)
			if (!move.isCapture())
				captureMoves.remove(move);
		orderMoves(captureMoves);

		for (Move captureMove : captureMoves) {
			captureMove.makeMove(false);
			evaluation = -searchAllCaptures(-beta, -alpha);
			captureMove.unmakeMove(false);

			if (evaluation >= beta)
				return beta;
			alpha = Math.max(alpha, evaluation);
		}

		return alpha;
	}

	public void orderMoves(List<Move> moves) {
		for (Move move : moves) {
			int moveScoreGuess = 0;

			// Priotitize capturing opponent's most vavluable pieces with our least valuable
			// pieces
			if (move.getMoveToPiece() != null)
				moveScoreGuess = 10 * getPieceValue(move.getMoveToPiece()) - getPieceValue(move.getPiece());

			// Promoting a Pawn is likely to be good
			if (move.isPromotion())
				moveScoreGuess += getPieceValue(move.getPromotionPiece());

			// Penalize moving our pieces to a square attacked by an opponent pawn
			boolean _protected = false;
			for (Piece piece : move.getMoveTo().getEnemyProtection(white))
				if (piece instanceof Pawn)
					_protected = true;
			if (_protected)
				moveScoreGuess -= getPieceValue(move.getPiece());

			move.setScore(moveScoreGuess);
		}
	}

	public Piece choosePromotion(Pawn pawn) {
		return new Queen(pawn);
	}

	/**
	 * This method computes the different Move possibilities and returns the best
	 * one.
	 * 
	 * @return The current best Move to play for the AI.
	 */
	public Move chooseMove() {
		List<Move> moves = Chess.getInstance().getPossibleMoves(white);
		return moves.get(new Random().nextInt(0, moves.size()));
	}

	/**
	 * This method evaluates who is currently winning the game and returns how much
	 * the AI is winning. Note taht the returned value may be negative if the AI is
	 * currently losing.
	 * 
	 * @return An int value representing how much the AI is currently winning.
	 */
	private int evaluate() {
		if (white)
			return evaluate(true) - evaluate(false);
		else
			return evaluate(false) - evaluate(true);
	}

	/**
	 * This method is used by {@link #evaluate()} to get the winning value of each
	 * player.
	 * 
	 * @param white Evaluate white or black ?
	 * @return An int value representing how much {@code white} is currently
	 *         winning.
	 */
	private int evaluate(boolean white) {
		int result = 0;
		for (Tile[] tiles : Chess.getInstance().getTiles())
			for (Tile tile : tiles)
				if (tile.getPiece() != null)
					if (tile.getPiece().isWhite() == white)
						result += getPieceValue(tile.getPiece());
		return result;
	}

	private static int getPieceValue(Piece piece) {
		if (piece == null)
			throw new NullPointerException("Cannot return the value of a null Piece.");
		if (piece instanceof Pawn)
			return pawnValue;
		if (piece instanceof Knight)
			return knightValue;
		if (piece instanceof Bishop)
			return bishopValue;
		if (piece instanceof Rook)
			return rookValue;
		if (piece instanceof Queen)
			return queenValue;
		// piece instanceof King is true then
		return 0;
	}

	/**
	 * Getter for the white value.
	 * 
	 * @return The white to get.
	 */
	public boolean isWhite() {
		return white;
	}

}
