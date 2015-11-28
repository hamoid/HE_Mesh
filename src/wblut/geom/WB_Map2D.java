/*
 * 
 */
package wblut.geom;

/**
 *
 * WB_Map2D is an interface for classes that transform between 3D coordinates
 * and 2D coordinates through some from of mapping or projection.
 *
 *
 */
public interface WB_Map2D extends WB_Map {

	/**
	 * Unmap 2D point.
	 *
	 * @param p
	 * @param result
	 *            object implementing the WB_MutableCoordinate interface to
	 *            receive the result;
	 */
	public void unmapPoint2D(WB_Coord p, WB_MutableCoord result);

	/**
	 * Unmap 2D point.
	 *
	 * @param u
	 * @param v
	 * @param result
	 *            object implementing the WB_MutableCoordinate interface to
	 *            receive the result;
	 */
	public void unmapPoint2D(double u, double v, WB_MutableCoord result);

	/**
	 * Unmap 2D vector.
	 *
	 * @param v
	 * @param result
	 *            object implementing the WB_MutableCoordinate interface to
	 *            receive the result;
	 */
	public void unmapVector2D(WB_Coord v, WB_MutableCoord result);

	/**
	 * Unmap 2D vector.
	 *
	 * @param u
	 * @param v
	 * @param result
	 *            object implementing the WB_MutableCoordinate interface to
	 *            receive the result;
	 */
	public void unmapVector2D(double u, double v, WB_MutableCoord result);
}
