package com.bloodLantern.chess.pieces;

import java.util.ArrayList;

import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Tile;

/**
 * A Bishop can move on its diagonals.
 *
 * @author BloodLantern
 */
public final class Bishop extends Piece {

	public static final char FEN_VALUE = 'B';

	/**
	 * @param white Whether the Piece should be white or black.
	 * @param tile  The Tile on which this Piece should be.
	 */
	public Bishop(boolean white, Tile tile) {
		super(white, white ? Piece.TEXTURE_WHITE_BISHOP : Piece.TEXTURE_BLACK_BISHOP, tile);
	}

	public Bishop(Pawn pawn) {
		super(pawn, pawn.isWhite() ? Piece.TEXTURE_WHITE_BISHOP : Piece.TEXTURE_BLACK_BISHOP);
	}

	@Override
	public ArrayList<Tile> getProtectedTiles() {
		// Remember that negative Y is on top
		ArrayList<Tile> list = new ArrayList<>();
		Tile[][] tiles = Chess.getInstance().getTiles();
		Tile tile = null;
		int row;
		int line;
		// Checks for negative and positive X
		for (int x = -1; x < 3; x += 2)
			// Checks for negative and positive Y
			for (int y = -1; y < 3; y += 2)
				// Checks for all the diagonal
				for (int i = 1; i < 8; i++) {
					row = this.tile.getRow() + x * i;
					line = this.tile.getLine() + y * i;
					if (row < 0 || row > 7 || line < 0 || line > 7)
						break;
					list.add(tile = tiles[row][line]);
					if (tile.getPiece() != null)
						break;
				}
		return list;
	}

	@Override
	public ArrayList<Tile> getPotentiallyProtectedTiles() {
		// Remember that negative Y is on top
		ArrayList<Tile> list = new ArrayList<>();
		Tile[][] tiles = Chess.getInstance().getTiles();
		int row;
		int line;
		// Checks for negative and positive X
		for (int x = -1; x < 3; x += 2)
			// Checks for negative and positive Y
			for (int y = -1; y < 3; y += 2)
				// Checks for all the diagonal
				for (int i = 1; i < 8; i++) {
					row = tile.getRow() + x * i;
					line = tile.getLine() + y * i;
					if (row < 0 || row > 7 || line < 0 || line > 7)
						break;
					list.add(tiles[row][line]);
				}
		return list;
	}

	@Override
	public char getFENValue() {
		if (white)
			return FEN_VALUE;
		return Character.toLowerCase(FEN_VALUE);
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "Bishop [" + super.toString() + "]";
	}

}
