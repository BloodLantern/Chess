package com.bloodLantern.chess.pieces;

import java.util.ArrayList;

import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Tile;

/**
 * A Queen is a Rook-Bishop mix: it can move on its diagonals plus its line and
 * row.
 *
 * @author BloodLantern
 */
public final class Queen extends Piece {

	public static final char FEN_VALUE = 'Q';

	/**
	 * @param white Whether the Piece should be white or black.
	 * @param tile  The Tile on which this Piece should be.
	 */
	public Queen(boolean white, Tile tile) {
		super(white, white ? Piece.TEXTURE_WHITE_QUEEN : Piece.TEXTURE_BLACK_QUEEN, tile);
	}

	public Queen(Pawn pawn) {
		super(pawn, pawn.isWhite() ? Piece.TEXTURE_WHITE_QUEEN : Piece.TEXTURE_BLACK_QUEEN);
	}

	@Override
	public ArrayList<Tile> getProtectedTiles() {
		// Remember that negative Y is on top
		ArrayList<Tile> list = new ArrayList<>();
		Tile[][] tiles = Chess.getInstance().getTiles();
		Tile tile = null;
		int row;
		int line;
		// Rook check
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
		// Bishop check
		// Checks for negative and positive X
		for (int x1 = -1; x1 < 3; x1 += 2)
			// Checks for negative and positive Y
			for (int y1 = -1; y1 < 3; y1 += 2)
				// Checks for all the diagonal
				for (int i = 1; i < 8; i++) {
					row = this.tile.getRow() + x1 * i;
					line = this.tile.getLine() + y1 * i;
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
		// Rook check
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
		// Bishop check
		// Checks for negative and positive X
		for (int x1 = -1; x1 < 3; x1 += 2)
			// Checks for negative and positive Y
			for (int y1 = -1; y1 < 3; y1 += 2)
				// Checks for all the diagonal
				for (int i = 1; i < 8; i++) {
					row = tile.getRow() + x1 * i;
					line = tile.getLine() + y1 * i;
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
		return "Queen [" + super.toString() + "]";
	}

}
