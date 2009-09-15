package com.example.android.snake

import _root_.android.content.Context
import _root_.android.content.res.TypedArray
import _root_.android.graphics.Bitmap
import _root_.android.graphics.Canvas
import _root_.android.graphics.Paint
import _root_.android.graphics.drawable.Drawable
import _root_.android.util.AttributeSet
import _root_.android.view.View

/** TileView: a View-variant designed for handling arrays of "icons" or other
 *  drawables.
 */
class TileView(context: Context, attrs: AttributeSet, defStyle: Int)
extends View(context, attrs, defStyle) {

  /** Parameters controlling the size of the tiles and their range within view.
   *  Width/Height are in pixels, and Drawables will be scaled to fit to these
   *  dimensions. X/Y Tile Counts are the number of tiles that will be drawn.
   */
  protected var mTileSize = 0

  protected var mXTileCount = 0
  protected var mYTileCount = 0

  private var mXOffset = 0
  private var mYOffset = 0


  /** A hash that maps integer handles specified by the subclasser to the
   *  drawable that will be used for that reference
   */
  private var mTileArray: Array[Bitmap] = null

  /** A two-dimensional array of integers in which the number represents the
   *  index of the tile that should be drawn at that locations
   */
  private var mTileGrid: Array[Array[Int]] = null

  private final val mPaint = new Paint()

  initTileView()

  private def initTileView() {
    val a = context.obtainStyledAttributes(attrs, R.styleable.TileView)
    mTileSize = a.getInt(R.styleable.TileView_tileSize, 12)
    a.recycle()
  }

  def this(context: Context, attrs: AttributeSet) =
    this(context, attrs, 0)
    
  /** Rests the internal array of Bitmaps used for drawing tiles, and
   *  sets the maximum index of tiles to be inserted
   * 
   * @param tilecount
   */
  def resetTiles(tilecount: Int) {
  	mTileArray = new Array[Bitmap](tilecount)
  }

  override protected def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    mXTileCount = Math.floor(w / mTileSize).toInt
    mYTileCount = Math.floor(h / mTileSize).toInt

    mXOffset = ((w - (mTileSize * mXTileCount)) / 2)
    mYOffset = ((h - (mTileSize * mYTileCount)) / 2)

    mTileGrid = new Array[Array[Int]](mXTileCount)
    for (x <- 0 until mXTileCount) mTileGrid(x) = new Array[Int](mYTileCount)
    clearTiles()
  }

  /** Function to set the specified Drawable as the tile for a particular
   *  integer key.
   * 
   *  @param key
   *  @param tile
   */
  def loadTile(key: Int, tile: Drawable) {
    val bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888)
    val canvas = new Canvas(bitmap)
    tile.setBounds(0, 0, mTileSize, mTileSize)
    tile draw canvas
    mTileArray(key) = bitmap
  }

  /** Resets all tiles to 0 (empty)
   */
  def clearTiles() {
    for (x <- 0 until mXTileCount; y <- 0 until mYTileCount) setTile(0, x, y)
  }

  /** Used to indicate that a particular tile (set with loadTile and referenced
   *  by an integer) should be drawn at the given x/y coordinates during the
   *  next invalidate/draw cycle.
   * 
   *  @param tileindex
   *  @param x
   *  @param y
   */
  def setTile(tileindex: Int, x: Int, y: Int) {
    mTileGrid(x)(y) = tileindex
  }

  override def onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    for (x <- 0 until mXTileCount; y <- 0 until mYTileCount) {
      val tileindex = mTileGrid(x)(y)
      if (tileindex > 0) {
        canvas.drawBitmap(mTileArray(tileindex), 
                          mXOffset + x * mTileSize,
                    	  mYOffset + y * mTileSize,
                    	  mPaint)
      }
    }
  }

}
