package wblut.hemesh;

import java.util.HashMap;
import java.util.List;

import javolution.util.FastTable;
import wblut.geom.WB_HasData;

public class HE_Loop extends HE_Element implements WB_HasData {

	private HE_PathHalfedge _loopHalfedge;

	private HashMap<String, Object> _data;

	private boolean _sorted;

	public HE_Loop() {
		super();
	}

	/**
	 * Get key.
	 * 
	 * @return key
	 */
	public long key() {
		return super.getKey();
	}

	public int getLoopOrder() {
		int result = 0;
		if (_loopHalfedge == null) {
			return 0;
		}
		HE_PathHalfedge he = _loopHalfedge;
		do {
			result++;
			he = he.getNextInPath();
		} while (he != _loopHalfedge);
		return result;
	}

	public List<HE_Halfedge> getLoopHalfedges() {
		if (!_sorted) {
			sort();
		}
		final List<HE_Halfedge> fhe = new FastTable<HE_Halfedge>();
		if (_loopHalfedge == null) {
			return fhe;
		}
		HE_PathHalfedge he = _loopHalfedge;
		do {
			if (!fhe.contains(he.getHalfedge())) {
				fhe.add(he.getHalfedge());
			}
			he = he.getNextInPath();
		} while (he != _loopHalfedge);
		return fhe;
	}

	public List<HE_Edge> getLoopEdges() {
		if (!_sorted) {
			sort();
		}
		final List<HE_Edge> fe = new FastTable<HE_Edge>();
		if (_loopHalfedge == null) {
			return fe;
		}
		HE_PathHalfedge he = _loopHalfedge;
		do {
			if (!fe.contains(he.getHalfedge().getEdge())) {
				fe.add(he.getHalfedge().getEdge());
			}
			he = he.getNextInPath();
		} while (he != _loopHalfedge);
		return fe;
	}

	public HE_PathHalfedge getLoopHalfedge() {
		return _loopHalfedge;
	}

	public void setLoopHalfedge(final HE_PathHalfedge halfedge) {
		_loopHalfedge = halfedge;
		_sorted = false;
	}

	public void clearLoopHalfedge() {
		_loopHalfedge = null;
		_sorted = false;
	}

	public void sort() {
		if (_loopHalfedge != null) {
			HE_PathHalfedge he = _loopHalfedge;
			HE_PathHalfedge leftmost = he;
			do {
				he = he.getNextInPath();
				if (he.getHalfedge().getVertex()
						.compareTo(leftmost.getHalfedge().getVertex()) < 0) {
					leftmost = he;
				}
			} while (he != _loopHalfedge);
			_loopHalfedge = leftmost;
			_sorted = true;
		}
	}

	public List<HE_Face> getInnerFaces() {
		if (!isSorted()) {
			sort();
		}
		final List<HE_Face> ff = new FastTable<HE_Face>();
		if (getLoopHalfedge() == null) {
			return ff;
		}
		HE_PathHalfedge lhe = _loopHalfedge;
		HE_Halfedge he;
		do {
			he = lhe.getHalfedge();
			if (he.getFace() != null) {
				if (!ff.contains(he.getFace())) {
					ff.add(he.getFace());
				}
			}
			lhe = lhe.getNextInPath();
		} while (lhe != _loopHalfedge);
		return ff;
	}

	public List<HE_Face> getOuterFaces() {
		if (!isSorted()) {
			sort();
		}
		final List<HE_Face> ff = new FastTable<HE_Face>();
		if (getLoopHalfedge() == null) {
			return ff;
		}
		HE_PathHalfedge lhe = _loopHalfedge;
		HE_Halfedge hep;
		do {
			hep = lhe.getHalfedge().getPair();
			if (hep.getFace() != null) {
				if (!ff.contains(hep.getFace())) {
					ff.add(hep.getFace());
				}
			}
			lhe = lhe.getNextInPath();
		} while (lhe != _loopHalfedge);
		return ff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wblut.geom.Point3D#toString()
	 */
	@Override
	public String toString() {
		String s = "HE_Loop key: " + key() + ". Connects " + getLoopOrder()
				+ " vertices: ";
		HE_PathHalfedge he = _loopHalfedge;
		for (int i = 0; i < getLoopOrder() - 1; i++) {
			s += he.getHalfedge().getVertex()._key + "-";
			he = he.getNextInPath();
		}
		s += he.getHalfedge().getVertex()._key + ".";
		return s;
	}

	public boolean isSorted() {
		return _sorted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wblut.core.WB_HasData#setData(java.lang.String, java.lang.Object)
	 */
	public void setData(final String s, final Object o) {
		if (_data == null) {
			_data = new HashMap<String, Object>();
		}
		_data.put(s, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wblut.core.WB_HasData#getData(java.lang.String)
	 */
	public Object getData(final String s) {
		return _data.get(s);
	}

}
