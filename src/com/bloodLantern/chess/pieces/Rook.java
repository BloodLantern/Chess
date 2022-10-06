package com.bloodLantern.chess.pieces;

import java.util.ArrayList;

import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Tile;

/**
 * A Rook can move on its line and row.
 *
 * @author BloodLantern
 */
public final class Rook extends Piece implements IfNotMoved {

	public static final char FEN_VALUE = 'R';

	private boolean moved = false;

	/**
	 * @param white Whether the Piece should be white or black.
	 * @param tile  The Tile on which this Piece should be.
	 */
	public Rook(boolean white, Tile tile) {
		super(white, white ? Piece.TEXTURE_WHITE_ROOK : Piece.TEXTURE_BLACK_ROOK, tile);
	}

	public Rook(Pawn pawn) {
		super(pawn, pawn.isWhite() ? Piece.TEXTURE_WHITE_ROOK : Piece.TEXTURE_BLACK_ROOK);
	}

	@Override
	public ArrayList<Tile> getProtectedTiles() {
		// Remember that negative Y is on top
		ArrayList<Tile> list = new ArrayList<>();
		Tile[][] tiles = Chess.getInstance().getTiles();
		Tile tile = null;
		int row;
		int line;
		int x = 0;
		int y = 0;
		for (int i = 0; i < 4; i++) {
			switch (i) {
			case 0:
				x = -1;
				break;
			case 1:
				x = 0;
				y = -1;
				break;
			case 2:
				x = 1;
				y = 0;
				break;
			case 3:
				x = 0;
				y = 1;
				break;
			}
			for (int ii = 1; ii < 8; ii++) {
				row = this.tile.getRow() + x * ii;
				line = this.tile.getLine() + y * ii;
				if (row < 0 || row > 7 || line < 0 || line > 7)
					break;
				list.add(tile = tiles[row][line]);
				if (tile.getPiece() != null)
					break;
			}
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
		int x = 0;
		int y = 0;
		for (int i = 0; i < 4; i++) {
			switch (i) {
			case 0:
				x = -1;
				break;
			case 1:
				x = 0;
				y = -1;
				break;
			case 2:
				x = 1;
				y = 0;
				break;
			case 3:
				x = 0;
				y = 1;
				break;
			}
			for (int ii = 1; ii < 8; ii++) {
				row = tile.getRow() + x * ii;
				line = tile.getLine() + y * ii;
				if (row < 0 || row > 7 || line < 0 || line > 7)
					break;
				list.add(tiles[row][line]);
			}
		}
		return list;
	}

	/**
	 * Getter for the moved value.
	 *
	 * @return The moved to get.
	 */
	@Override
	public boolean isMoved() {
		return moved;
	}

	/**
	 * Setter for the moved value.
	 *
	 * @param moved The moved to set.
	 */
	@Override
	public void setMoved(boolean moved) {
		this.moved = moved;
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
		return "Rook [moved=" + moved + ", " + super.toString() + "]";
	}
}
