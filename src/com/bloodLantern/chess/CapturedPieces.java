package com.bloodLantern.chess;

import com.bloodLantern.chess.pieces.Bishop;
import com.bloodLantern.chess.pieces.Knight;
import com.bloodLantern.chess.pieces.Pawn;
import com.bloodLantern.chess.pieces.Piece;
import com.bloodLantern.chess.pieces.Queen;
import com.bloodLantern.chess.pieces.Rook;

/**
 * Simple class containing the white and black taken Pieces count.
 *
 * @author BloodLantern
 */
public final class CapturedPieces {

	private final ColorPieces white = new ColorPieces();
	private final ColorPieces black = new ColorPieces();

	/**
	 * Simple class to count the number of Pieces of each type except the King one.
	 *
	 * @author BloodLantern
	 */
	class ColorPieces {
		int pawns = 0;
		int rooks = 0;
		int knights = 0;
		int bishops = 0;
		int queens = 0;

		void add(Piece piece) {
			if (piece instanceof Rook)
				rooks++;
			else if (piece instanceof Knight)
				knights++;
			else if (piece instanceof Bishop)
				bishops++;
			else if (piece instanceof Queen)
				queens++;
			else if (piece instanceof Pawn)
				pawns++;
		}

		void remove(Piece piece) {
			if (piece instanceof Rook)
				rooks--;
			else if (piece instanceof Knight)
				knights--;
			else if (piece instanceof Bishop)
				bishops--;
			else if (piece instanceof Queen)
				queens--;
			else if (piece instanceof Pawn)
				pawns--;
		}
	}

	/**
	 * Getter for the white value.
	 *
	 * @return The white to get.
	 */
	ColorPieces getWhite() {
		return white;
	}

	/**
	 * Getter for the black value.
	 *
	 * @return The black to get.
	 */
	ColorPieces getBlack() {
		return black;
	}

	public void add(Piece piece) {
		if (piece.isWhite())
			white.add(piece);
		else
			black.add(piece);
		if (Chess.getInstance().infoFrame != null)
			Chess.getInstance().infoFrame.repaint();
	}

	void remove(Piece piece) {
		if (piece.isWhite())
			white.remove(piece);
		else
			black.remove(piece);
		if (Chess.getInstance().infoFrame != null)
			Chess.getInstance().infoFrame.repaint();
	}

}
