/*
 *
 */
package wblut.hemesh;

import wblut.core.WB_ProgressCounter;
import wblut.geom.WB_Coord;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Plane;
import wblut.geom.WB_Point;
import wblut.math.WB_Epsilon;

/**
 *
 */
public class HEM_TriSplit extends HEM_Modifier {
	/**
	 *
	 */
	private double d;
	/**
	 *
	 */
	private HE_Selection selectionOut;

	/**
	 *
	 */
	public HEM_TriSplit() {
		super();
		d = 0;
	}

	/**
	 *
	 *
	 * @param d
	 * @return
	 */
	public HEM_TriSplit setOffset(final double d) {
		this.d = d;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wblut.hemesh.HE_Modifier#apply(wblut.hemesh.HE_Mesh)
	 */
	@Override
	public HE_Mesh apply(final HE_Mesh mesh) {
		tracker.setStatus(this, "Starting HEM_TriSplit.", +1);
		splitFacesTri(mesh.selectAllFaces(), d);
		tracker.setStatus(this, "Exiting HEM_TriSplit.", -1);
		return mesh;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see wblut.hemesh.HE_Modifier#apply(wblut.hemesh.HE_Mesh)
	 */
	@Override
	public HE_Mesh apply(final HE_Selection selection) {
		tracker.setStatus(this, "Starting HEM_TriSplit.", +1);
		splitFacesTri(selection, d);
		tracker.setStatus(this, "Exiting HEM_TriSplit.", -1);
		return selection.parent;
	}

	/**
	 * Tri split faces with offset along face normal.
	 *
	 * @param selection
	 *            face selection to split
	 * @param d
	 *            offset along face normal
	 * @return selection of new faces and new vertex
	 */
	private void splitFacesTri(final HE_Selection selection, final double d) {
		selectionOut = new HE_Selection(selection.parent);
		final HE_Face[] faces = selection.getFacesAsArray();
		final int n = selection.getNumberOfFaces();
		WB_ProgressCounter counter = new WB_ProgressCounter(n, 10);
		tracker.setStatus(this, "Splitting faces.", counter);
		for (int i = 0; i < n; i++) {
			selectionOut.add(splitFaceTri(faces[i], d, selection.parent));
			counter.increment();
		}
		selection.add(selectionOut);
	}

	/**
	 * Tri split face with offset along face normal.
	 *
	 * @param face
	 *            face
	 * @param d
	 *            offset along face normal
	 * @param mesh
	 * @return selection of new faces and new vertex
	 */
	private HE_Selection splitFaceTri(final HE_Face face, final double d, final HE_Mesh mesh) {
		return splitFaceTri(mesh, face, WB_Point.addMul(face.getFaceCenter(), d, face.getFaceNormal()));
	}

	/**
	 *
	 *
	 * @param mesh
	 * @param face
	 * @param p
	 * @return
	 */
	public static HE_Selection splitFaceTri(final HE_Mesh mesh, final HE_Face face, final WB_Coord p) {
		HE_Halfedge he = face.getHalfedge();
		final HE_Vertex vi = new HE_Vertex(p);
		vi.setInternalLabel(2);
		double u = 0;
		double v = 0;
		double w = 0;
		boolean hasTexture = true;
		do {
			if (!he.getVertex().hasUVW(face)) {
				hasTexture = false;
				break;
			}
			u += he.getVertex().getUVW(face).ud();
			v += he.getVertex().getUVW(face).vd();
			w += he.getVertex().getUVW(face).wd();
			he = he.getNextInFace();
		} while (he != face.getHalfedge());
		if (hasTexture) {
			final double ifo = 1.0 / face.getFaceOrder();
			vi.setUVW(u * ifo, v * ifo, w * ifo);
		}
		he = face.getHalfedge();
		final HE_Selection out = new HE_Selection(mesh);
		int c = 0;
		boolean onEdge = false;
		do {
			c++;
			final WB_Plane P = new WB_Plane(he.getHalfedgeCenter(), he.getHalfedgeNormal());
			final double d = WB_GeometryOp.getDistance3D(p, P);
			if (WB_Epsilon.isZero(d)) {
				onEdge = true;
				break;
			}
			he = he.getNextInFace();
		} while (he != face.getHalfedge());
		if (!onEdge) {
			mesh.add(vi);
			final HE_Halfedge[] he0 = new HE_Halfedge[c];
			final HE_Halfedge[] he1 = new HE_Halfedge[c];
			final HE_Halfedge[] he2 = new HE_Halfedge[c];
			c = 0;
			do {
				HE_Face f;
				if (c == 0) {
					f = face;
				} else {
					f = new HE_Face();
					f.copyProperties(face);
					mesh.add(f);
					out.add(f);
				}
				he0[c] = he;
				he.setFace(f);
				f.setHalfedge(he);
				he1[c] = new HE_Halfedge();
				he2[c] = new HE_Halfedge();
				mesh.add(he1[c]);
				mesh.add(he2[c]);
				if (he.getNextInFace().hasHalfedgeUVW()) {
					he1[c].setUVW(he.getNextInFace().getUVW());
				}
				he1[c].setVertex(he.getNextInFace().getVertex());
				he2[c].setVertex(vi);
				he1[c].setNext(he2[c]);
				he2[c].setNext(he);
				he2[c].setPrev(he1[c]);
				he.setPrev(he2[c]);
				he1[c].setFace(f);
				he2[c].setFace(f);
				c++;
				he = he.getNextInFace();
			} while (he != face.getHalfedge());
			vi.setHalfedge(he2[0]);
			for (int i = 0; i < c; i++) {
				he0[i].setNext(he1[i]);
				he1[i].setPrev(he0[i]);
				he1[i].setPair(he2[i == (c - 1) ? 0 : i + 1]);
				he2[i == (c - 1) ? 0 : i + 1].setPair(he1[i]);
			}
			out.add(vi);
			return out;
		}
		return null;
	}

	/**
	 *
	 *
	 * @return
	 */
	public HE_Selection getSplitFaces() {
		return this.selectionOut;
	}
}
