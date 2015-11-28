package wblut.geom;

/**
 * Interface for random vector/point generators
 *
 * @author frederikvanhoutte
 *
 */
public interface WB_RandomPoint {
	/**
	 * Set the seed for the RNG
	 *
	 * @param seed
	 * @return this
	 */
	public WB_RandomPoint setSeed(final long seed);

	/**
	 * Get the next random point
	 *
	 * @return
	 */
	public WB_Point nextPoint();

	/**
	 * Get the next random vector
	 *
	 * @return
	 */
	public WB_Vector nextVector();

	/**
	 * Reset the RNG
	 */
	public void reset();

	/**
	 * Set point offset
	 * 
	 * @param offset
	 * @return
	 */
	public WB_RandomPoint setOffset(WB_Coord offset);

	/**
	 * Set point offset
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public WB_RandomPoint setOffset(double x, double y);

	/**
	 * Set point offset
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public WB_RandomPoint setOffset(double x, double y, double z);

}
