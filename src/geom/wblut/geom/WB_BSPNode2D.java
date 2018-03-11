/**
 * 
 */
package wblut.geom;

import org.eclipse.collections.impl.list.mutable.FastList;

/**
 * The Class WB_BSPNode2D.
 *
 * @author Frederik Vanhoutte, W:Blut
 */
public class WB_BSPNode2D {
	
	/** The partition. */
	protected WB_Line							partition;
	
	/** The segments. */
	protected FastList<WB_Segment>	segments;
	
	/** The pos. */
	protected WB_BSPNode2D						pos	= null;
	
	/** The neg. */
	protected WB_BSPNode2D						neg	= null;

	/**
	 * Instantiates a new w b_ bsp node2 d.
	 */
	public WB_BSPNode2D() {
		segments = new FastList<WB_Segment>();
	}

}