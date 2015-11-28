/*
 *
 */
package wblut.hemesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import wblut.geom.WB_Point;

/**
 * Creates the dual of a mesh. Vertices are replace with faces connecting all
 * face centers surrounding original vertex. The faces are replaced by vertices
 * at their center.
 *
 * @author Frederik Vanhoutte (W:Blut)
 *
 */
public class HEC_Dual extends HEC_Creator {
	/** Source mesh. */
	private HE_Mesh source;
	private boolean fixNonPlanarFaces;
	private boolean setCenter;
	private boolean keepBoundary;

	/**
	 * Instantiates a new HEC_Dual.
	 *
	 */
	public HEC_Dual() {
		super();
		override = true;
		toModelview = false;
	}

	/**
	 * Instantiates a new HEC_Dual.
	 *
	 * @param mesh
	 *            source mesh
	 */
	public HEC_Dual(final HE_Mesh mesh) {
		this();
		source = mesh;
		fixNonPlanarFaces = true;
	}

	/**
	 * Set source mesh.
	 *
	 * @param mesh
	 *            source mesh
	 * @return self
	 */
	public HEC_Dual setSource(final HE_Mesh mesh) {
		source = mesh;
		return this;
	}

	public HEC_Dual resetCenter(boolean b) {
		setCenter = b;
		return this;
	}
	
	public HEC_Dual setKeepBoundary(boolean b) {
		keepBoundary = b;
		return this;
	}

	public HEC_Dual setFixNonPlanarFaces(final boolean b) {
		fixNonPlanarFaces = b;
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see wblut.hemesh.HE_Creator#create()
	 */
	@Override
	public HE_Mesh createBase() {
		final HE_Mesh result = new HE_Mesh();
		if (source == null) {
			return result;
		}
		final HashMap<Long, Long> faceVertexCorrelation = new HashMap<Long, Long>();
		final Iterator<HE_Face> fItr = source.fItr();
		HE_Face f;
		while (fItr.hasNext()) {
			f = fItr.next();
			final HE_Vertex cv = new HE_Vertex(f.getFaceCenter());
			faceVertexCorrelation.put(f.key(), cv.key());
			result.add(cv);
		}
		HE_Halfedge he;
		if(keepBoundary){
		final Iterator<HE_Halfedge> heItr = source.heItr();
		
		while (heItr.hasNext()) {
			he = heItr.next();
			if (he.isOuterBoundary()) {
				HE_Vertex cv = new HE_Vertex(he.getEdgeCenter());
				faceVertexCorrelation.put(he.key(), cv.key());
				result.add(cv);
				cv = new HE_Vertex(he.getVertex());
				faceVertexCorrelation.put(he.getVertex().key(), cv.key());
				result.add(cv);
			}
		}
		}
		final Iterator<HE_Vertex> vItr = source.vItr();
		HE_Vertex v;
		final List<WB_Point> centers = new ArrayList<WB_Point>();
		while (vItr.hasNext()) {
			v = vItr.next();
			if (!v.isBoundary()) {

				he = v.getHalfedge();
				final List<HE_Halfedge> faceHalfedges = new ArrayList<HE_Halfedge>();
				final HE_Face nf = new HE_Face();
				final WB_Point p = new WB_Point();
				int n = 0;

				do {
					final HE_Halfedge hen = new HE_Halfedge();
					faceHalfedges.add(hen);
					hen.setFace(nf);
					final Long key = faceVertexCorrelation.get(he.getFace().key());
					hen.setVertex(result.getVertexWithKey(key));
					p.addSelf(hen.getVertex());
					n++;
					if (hen.getVertex().getHalfedge() == null) {
						hen.getVertex().setHalfedge(hen);
					}
					if (nf.getHalfedge() == null) {
						nf.setHalfedge(hen);
					}

					he = he.getNextInVertex();
				} while (he != v.getHalfedge());
				p.divSelf(n);
				centers.add(p);
				HE_Mesh.cycleHalfedges(faceHalfedges);
				result.addHalfedges(faceHalfedges);
				result.add(nf);

			} else if(keepBoundary){
				he = v.getHalfedge();
				while (!he.isOuterBoundary()) {
					he = he.getNextInVertex();
				}
				HE_Halfedge start = he;

				final List<HE_Halfedge> faceHalfedges = new ArrayList<HE_Halfedge>();
				final HE_Face nf = new HE_Face();
				final WB_Point p = new WB_Point();
				int n = 0;

				HE_Halfedge hen = new HE_Halfedge();
				faceHalfedges.add(hen);
				hen.setFace(nf);
				Long key = faceVertexCorrelation.get(v.key());
				hen.setVertex(result.getVertexWithKey(key));
				p.addSelf(hen.getVertex());
				n++;

				hen = new HE_Halfedge();
				faceHalfedges.add(hen);
				hen.setFace(nf);
				key = faceVertexCorrelation.get(he.key());
				hen.setVertex(result.getVertexWithKey(key));
				p.addSelf(hen.getVertex());
				n++;
				hen.getVertex().setHalfedge(hen);
				nf.setHalfedge(hen);
				he = he.getNextInVertex();
				do {
					hen = new HE_Halfedge();
					faceHalfedges.add(hen);
					hen.setFace(nf);
					key = (he.isOuterBoundary()) ? faceVertexCorrelation.get(he.key())
							: faceVertexCorrelation.get(he.getFace().key());
					hen.setVertex(result.getVertexWithKey(key));
					p.addSelf(hen.getVertex());
					n++;
					he = he.getNextInVertex();
				} while (he != start);

				he = he.getPrevInVertex();
				hen = new HE_Halfedge();
				faceHalfedges.add(hen);
				hen.setFace(nf);
				key = faceVertexCorrelation.get(he.getPair().key());
				hen.setVertex(result.getVertexWithKey(key));
				p.addSelf(hen.getVertex());
				n++;

				p.divSelf(n);
				centers.add(p);
				HE_Mesh.cycleHalfedges(faceHalfedges);
				result.addHalfedges(faceHalfedges);
				result.add(nf);

			}

		}

		result.pairHalfedges();
		result.capHalfedges();
		if (setCenter)
			result.moveTo(source.getCenter());
		result.flipAllFaces();
		final List<HE_Face> faces = result.getFaces();
		final int fs = faces.size();
		for (int i = 0; i < fs; i++) {
			if (!faces.get(i).isPlanar() && fixNonPlanarFaces) {
				HEM_TriSplit.splitFaceTri(result, faces.get(i), centers.get(i));
			}
		}
		return result;
	}
}
