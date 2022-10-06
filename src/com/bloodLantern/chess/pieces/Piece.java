package com.bloodLantern.chess.pieces;

import java.awt.Image;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.bloodLantern.chess.Chess;
import com.bloodLantern.chess.Move;
import com.bloodLantern.chess.Tile;

/**
 * A Piece object represent what the name means in Chess. Therefore, it may be a
 * King, a Queen, a Pawn, etc...
 *
 * @author BloodLantern
 */
public abstract sealed class Piece permits Pawn,King,Queen,Bishop,Knight,Rook {

	public static final Image TEXTURE_WHITE_PAWN = getImage("white_pawn");
	public static final Image TEXTURE_BLACK_PAWN = getImage("black_pawn");
	public static final Image TEXTURE_WHITE_ROOK = getImage("white_rook");
	public static final Image TEXTURE_BLACK_ROOK = getImage("black_rook");
	public static final Image TEXTURE_WHITE_KNIGHT = getImage("white_knight");
	public static final Image TEXTURE_BLACK_KNIGHT = getImage("black_knight");
	public static final Image TEXTURE_WHITE_BISHOP = getImage("white_bishop");
	public static final Image TEXTURE_BLACK_BISHOP = getImage("black_bishop");
	public static final Image TEXTURE_WHITE_QUEEN = getImage("white_queen");
	public static final Image TEXTURE_BLACK_QUEEN = getImage("black_queen");
	public static final Image TEXTURE_WHITE_KING = getImage("white_king");
	public static final Image TEXTURE_BLACK_KING = getImage("black_king");

	private static Image getImage(String string) {
		try {
			return ImageIO.read(new FileInputStream("pieces/" + string + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Image getPieceImage(Piece piece) {
		if (piece.isWhite()) {
			if (piece instanceof Rook)
				return TEXTURE_WHITE_ROOK;
			else if (piece instanceof Knight)
				return TEXTURE_WHITE_KNIGHT;
			else if (piece instanceof Bishop)
				return TEXTURE_WHITE_BISHOP;
			else if (piece instanceof Queen)
				return TEXTURE_WHITE_QUEEN;
			else if (piece instanceof Pawn)
				return TEXTURE_WHITE_PAWN;
			else if (piece instanceof King)
				return TEXTURE_WHITE_KING;
		} else {
			if (piece instanceof Rook)
				return TEXTURE_BLACK_ROOK;
			else if (piece instanceof Knight)
				return TEXTURE_BLACK_KNIGHT;
			else if (piece instanceof Bishop)
				return TEXTURE_BLACK_BISHOP;
			else if (piece instanceof Queen)
				return TEXTURE_BLACK_QUEEN;
			else if (piece instanceof Pawn)
				return TEXTURE_BLACK_PAWN;
			else if (piece instanceof King)
				return TEXTURE_BLACK_KING;
		}
		return null;
	}

	/**
	 * Whether the piece is white or black. See {@link #isWhite()} for more
	 * information.
	 */
	protected final boolean white;

	/**
	 * For drag and drop movement.
	 */
	private int x;

	/**
	 * For drag and drop movement.
	 */
	private int y;

	/**
	 * The Tile on which this Piece is standing.
	 */
	protected Tile tile;

	/**
	 * The Image used to render this Piece.
	 */
	private final Image texture;

	/**
	 * Constructs a Piece with the selected color. See {@link #isWhite()} for more
	 * information.
	 *
	 * @param white   Whether the Piece should be white or black.
	 * @param texture The Image object graphically representing this Piece.
	 * @param tile    The Tile on which this Piece should be.
	 */
	public Piece(boolean white, Image texture, Tile tile) {
		this.white = white;
		this.texture = texture;
		setTile(tile, true);
		refreshTileProtection();
	}

	public Piece(Pawn pawn, Image texture) {
		white = pawn.isWhite();
		this.texture = texture;
	}

	/**
	 * Checks if this Piece can move to {@code moveTo}.
	 *
	 * @param moveTo The Tile to which this Piece should be moved.
	 * @return True if the movement is possible. False otherwise.
	 */
	public boolean checkMove(Tile moveTo) {
		if (moveTo != null)
			// If the Tile is protected
			if (getProtectedTiles().contains(moveTo))
				if (moveTo.getPiece() instanceof King)
					return false;
				else {
					// If the Tile doesn't hold any Piece or the Piece it holds is an enemy of this
					// one
					if (moveTo.getPiece() == null || moveTo.getPiece().isEnemy(white))
						// If this Piece's King isn't attacked
						if (!getKing().isInDanger()) {
							// If this Piece's King won't be attacked
							if (getKing().getTile().isEnemyPotentiallyProtected(white)) {
								return tryMove(moveTo);
							} else
								return true;
							// If this Piece's King is in check this Piece may move to protect it
						} else {
							// If this Piece's King is in check by only one enemy Piece
							if (getKing().getTile().getEnemyProtection(white).size() == 1)
								// If this Piece can attack the attacking one
								if (getKing().getTile().getEnemyProtection(white).get(0).getTile().equals(moveTo))
									return true;
								else
									// If this Piece can move between its King and the Piece that attacks it
									for (Tile t : getKing().getTile().getEnemyProtection(white).get(0)
											.getProtectedTiles())
										if (moveTo.equals(t))
											return tryMove(t);
						}
				}
		return false;
	}

	/**
	 * Tries to move this Piece to {@code moveTo} and checks if its King is in
	 * check. Then undo this movement.
	 *
	 * @param moveTo The Tile to check the move to.
	 * @return True if this Piece's King is in check after trying this move. False
	 *         otherwise.
	 */
	protected boolean tryMove(Tile moveTo) {
		Move move = new Move(this, moveTo);
		move.makeMove(false);
		if (getKing().isInDanger()) {
			move.unmakeMove(false);
			return false;
		}
		// Because it returned if true, the following statements are as in an else
		move.unmakeMove(false);
		return true;
	}

	/**
	 * Creates and returns an ArrayList containing every protected Tiles. Allied
	 * Pieces may also stand on some added Tiles (for the
	 * {@link King#checkMove(Tile) King movement check}).
	 *
	 * @return An ArrayList containing every protected Tiles.
	 */
	public abstract ArrayList<Tile> getProtectedTiles();

	/**
	 * Creates and returns an ArrayList containing every "potentially" protected
	 * Tiles. That means calling {@code checkMove} with one of those Tiles may
	 * return false. This method gets all the Tiles this Piece would protect if it
	 * was the only one on the board.
	 *
	 * @return An ArrayList containing every potentially protected Tiles.
	 */
	public abstract ArrayList<Tile> getPotentiallyProtectedTiles();

	/**
	 * Checks if {@code piece} is an enemy of this Piece. Hence these only 4
	 * different return possibilities:
	 * <ul>
	 * <li>False if this Piece is white and {@code piece} is also white</li>
	 * <li>False if this Piece is black and {@code piece} is also black</li>
	 * <li>True if this Piece is white and {@code piece} is black</li>
	 * <li>True if this Piece is black and {@code piece} is white</li>
	 * </ul>
	 *
	 * @param white The Piece object to which this one should be compared.
	 * @return As shown in the above Javadoc, true if the two Piece have the same
	 *         color and false otherwise.
	 */
	public boolean isEnemy(boolean white) {
		if (this.white && white)
			return false;
		else if (!this.white && !white)
			return false;
		return true;
	}

	/**
	 * Sets this Piece's X and Y coordinates back to its Tile's.
	 */
	public void replaceToTilePosition() {
		x = tile.getRow() * Chess.TILES_SIZE;
		y = tile.getLine() * Chess.TILES_SIZE;
	}

	/**
	 * Refreshes the {@code protectedWhite} and {@code protectedBlack} status of
	 * protected Tiles.
	 */
	public void refreshTileProtection() {
		for (Tile tile : getProtectedTiles())
			tile.getProtecting().add(this);
		for (Tile tile : getPotentiallyProtectedTiles())
			tile.getPotentiallyProtecting().add(this);
	}

	/**
	 * Gets the King of this Piece.
	 *
	 * @return The white King if this Piece is white. The black King otherwise.
	 */
	public King getKing() {
		if (white)
			return King.getWhiteKing();
		return King.getBlackKing();
	}

	/**
	 * Checks if this Piece is in danger. It means if its Tile is protected by an
	 * enemy Piece.
	 *
	 * @return True if {@code getTile().isEnemyProtected(this)} returns true. False
	 *         otherwise.
	 */
	public boolean isInDanger() {
		if (tile.isEnemyProtected(white))
			return true;
		return false;
	}

	/**
	 * Getter for the white value. White is true while black is false.
	 *
	 * @return The white to get.
	 */
	public boolean isWhite() {
		return white;
	}

	/**
	 * Getter for the x value.
	 *
	 * @return The x to get.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Setter for the x value.
	 *
	 * @param x The x to set.
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Getter for the y value.
	 *
	 * @return The y to get.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Setter for the y value.
	 *
	 * @param y The y to set.
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Getter for the tile value.
	 *
	 * @return The tile to get.
	 */
	public Tile getTile() {
		return tile;
	}

	/**
	 * Setter for the tile value. Also sets its previous Tile's Piece to null.
	 *
	 * @param tile     The tile to set.
	 * @param setXAndY Should this method also set the X and Y coordinates of this
	 *                 Piece according to {@code tile}'s position on the board ?
	 */
	public void setTile(Tile tile, boolean setXAndY) {
		if (this.tile != null)
			this.tile.setPiece(null, false);
		this.tile = tile;
		if (tile.getPiece() != this)
			tile.setPiece(this, true);
		if (setXAndY) {
			x = tile.getRow() * Chess.TILES_SIZE;
			y = tile.getLine() * Chess.TILES_SIZE;
		}
	}

	/**
	 * Getter for the texture value.
	 *
	 * @return The texture to get.
	 */
	public Image getTexture() {
		return texture;
	}

	/**
	 * Getter for the FEN value of this Piece.
	 *
	 * @return The FEN value of this Piece. This value is a {@code char}
	 *         corresponding to the first letter of each Piece.
	 */
	public abstract char getFENValue();

	/**
	 * 
	 */
	@Override
	public String toString() {
		return "white=" + white + ", tile=" + tile;
	}

}
