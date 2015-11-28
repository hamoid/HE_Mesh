/*
 *
 */
package wblut.hemesh;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import wblut.core.WB_ProgressCounter;
import wblut.geom.WB_Coord;
import wblut.geom.WB_GeometryFactory;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;

/**
 *
 */
public class HEM_Lattice extends HEM_Modifier {
	/**
	 *
	 */
	private static final WB_GeometryFactory gf = WB_GeometryFactory.instance();
	/**
	 *
	 */
	private double d;
	/**
	 *
	 */
	private double sew;
	/**
	 *
	 */
	private double hew;
	/**
	 *
	 */
	private double thresholdAngle;
	/**
	 *
	 */
	private boolean fuse;
	/**
	 *
	 */
	private double fuseAngle;
	/**
	 *
	 */
	private double ibulge, obulge;

	/**
	 *
	 */
	public HEM_Lattice() {
		super();
		d = 0;
		sew = 0;
		thresholdAngle = -1;
		fuseAngle = Math.PI / 36;
		fuse = false;
		ibulge = obulge = 0;
	}

	/**
	 *
	 *
	 * @param d
	 * @return
	 */
	public HEM_Lattice setDepth(final double d) {
		this.d = d;
		return this;
	}

	/**
	 *
	 *
	 * @param w
	 * @return
	 */
	public HEM_Lattice setWidth(final double w) {
		sew = 0.5 * w;
		hew = w;
		return this;
	}

	/**
	 *
	 *
	 * @param w
	 * @param hew
	 * @return
	 */
	public HEM_Lattice setWidth(final double w, final double hew) {
		sew = 0.5 * w;
		this.hew = hew;
		return this;
	}

	/**
	 *
	 *
	 * @param d
	 * @return
	 */
	public HEM_Lattice setBulge(final double d) {
		ibulge = obulge = d;
		return this;
	}

	/**
	 *
	 *
	 * @param inner
	 * @param outer
	 * @return
	 */
	public HEM_Lattice setBulge(final double inner, final double outer) {
		ibulge = inner;
		obulge = outer;
		return this;
	}

	/**
	 *
	 *
	 * @param b
	 * @return
	 */
	public HEM_Lattice setFuse(final boolean b) {
		fuse = b;
		return this;
	}

	/**
	 *
	 *
	 * @param a
	 * @return
	 */
	public HEM_Lattice setThresholdAngle(final double a) {
		thresholdAngle = a;
		return this;
	}

	/**
	 *
	 *
	 * @param a
	 * @return
	 */
	public HEM_Lattice setFuseAngle(final double a) {
		fuseAngle = a;
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see wblut.hemesh.HE_Modifier#apply(wblut.hemesh.HE_Mesh)
	 */
	@Override
	public HE_Mesh apply(final HE_Mesh mesh) {
		tracker.setStatus(this, "Starting HEM_Lattice.", +1);
		if (d == 0) {
			tracker.setStatus(this,
					"Can't create with zero thickness, use HEM_PunchHoles instead. Exiting HEM_Lattice.", -1);
			return mesh;
		}
		if (sew == 0) {
			tracker.setStatus(this, "Can't create with zero width. Exiting HEM_Lattice.", -1);
			return mesh;
		}
		final HEM_Extrude extm = new HEM_Extrude().setDistance(0).setRelative(false).setChamfer(sew).setFuse(fuse)
				.setHardEdgeChamfer(hew).setFuseAngle(fuseAngle).setThresholdAngle(thresholdAngle);
		mesh.modify(extm);
		tracker.setStatus(this, "Creating inner mesh.", 0);
		final HE_Mesh innerMesh = mesh.get();
		tracker.setStatus(this, "Shrinking inner mesh.", 0);
		final HEM_VertexExpand expm = new HEM_VertexExpand().setDistance(-d);
		innerMesh.modify(expm);
		WB_ProgressCounter counter = new WB_ProgressCounter(mesh.getNumberOfFaces(), 10);
		tracker.setStatus(this, "Creating face correlations.", counter);
		final HashMap<Long, Long> faceCorrelation = new HashMap<Long, Long>();
		final Iterator<HE_Face> fItr1 = mesh.fItr();
		final Iterator<HE_Face> fItr2 = innerMesh.fItr();
		HE_Face f1;
		HE_Face f2;
		while (fItr1.hasNext()) {
			f1 = fItr1.next();
			f2 = fItr2.next();
			faceCorrelation.put(f1.key(), f2.key());
			counter.increment();
		}
		counter = new WB_ProgressCounter(mesh.getNumberOfVertices(), 10);
		tracker.setStatus(this, "Creating boundary halfedge correlations.", counter);
		final HashMap<Long, Long> heCorrelation = new HashMap<Long, Long>();
		final Iterator<HE_Halfedge> heItr1 = mesh.heItr();
		final Iterator<HE_Halfedge> heItr2 = innerMesh.heItr();
		HE_Halfedge he1;
		HE_Halfedge he2;
		while (heItr1.hasNext()) {
			he1 = heItr1.next();
			he2 = heItr2.next();
			if (he1.getFace() == null) {
				heCorrelation.put(he1.key(), he2.key());
			}
			counter.increment();
		}
		innerMesh.flipAllFaces();
		final int nf = mesh.getNumberOfFaces();
		final HE_Face[] origFaces = mesh.getFacesAsArray();
		mesh.addVertices(innerMesh.getVerticesAsArray());
		mesh.addFaces(innerMesh.getFacesAsArray());
		mesh.addHalfedges(innerMesh.getHalfedgesAsArray());
		HE_Face fo;
		HE_Face fi;
		List<HE_Halfedge> hei;
		List<HE_Halfedge> heo;
		WB_Point[] viPos;
		WB_Polygon poly;
		HE_Halfedge heoc, heic, heon, hein, heio, heoi;
		HE_Face fNew;
		WB_Coord ni;
		WB_Coord no;
		counter = new WB_ProgressCounter(nf, 10);

		tracker.setStatus(this, "Connecting outer and inner faces.", counter);
		for (int i = 0; i < nf; i++) {
			fo = origFaces[i];
			final Long innerKey = faceCorrelation.get(fo.key());
			if (extm.extruded.contains(fo)) {
				fi = mesh.getFaceWithKey(innerKey);
				if (obulge != 0) {
					no = fo.getFaceNormal();
					fo.push(WB_Vector.mul(no, obulge));
				}
				if (ibulge != 0) {
					ni = fi.getFaceNormal();
					fi.push(WB_Vector.mul(ni, ibulge));
				}
				final int nvo = fo.getFaceOrder();
				final int nvi = fi.getFaceOrder();
				hei = fi.getFaceHalfedges();
				viPos = new WB_Point[nvi];
				for (int j = 0; j < nvi; j++) {
					viPos[j] = new WB_Point(hei.get(j).getVertex());
				}
				poly = gf.createSimplePolygon(viPos);
				heo = fo.getFaceHalfedges();
				for (int j = 0; j < nvo; j++) {
					heoc = heo.get(j);
					heon = heo.get((j + 1) % nvo);
					final int cic = poly.closestIndex(heoc.getVertex());
					final int cin = poly.closestIndex(heon.getVertex());
					heic = hei.get(cin);
					hein = hei.get(cic);
					heio = new HE_Halfedge();
					heoi = new HE_Halfedge();
					fNew = new HE_Face();
					heoi.setVertex(heon.getVertex());
					heio.setVertex(hein.getVertex());
					heoc.setNext(heoi);
					heoi.setPrev(heoc);
					heoc.setFace(fNew);
					if (cic == cin) {
						heoi.setNext(heio);
						heio.setPrev(heoi);
						heoi.setFace(fNew);
					} else {
						heoi.setNext(heic);
						heic.setPrev(heoi);
						heoi.setFace(fNew);
						heic.setNext(heio);
						heio.setPrev(heic);
						heic.setFace(fNew);
					}
					heio.setNext(heoc);
					heoc.setPrev(heio);
					heio.setFace(fNew);
					fNew.setHalfedge(heoc);
					mesh.add(heio);
					mesh.add(heoi);
					mesh.add(fNew);
					mesh.remove(fo);
					mesh.remove(fi);
				}
			}
			counter.increment();
		}
		counter = new WB_ProgressCounter(heCorrelation.size(), 10);

		tracker.setStatus(this, "Connecting outer and inner boundaries.", counter);
		final Iterator<Map.Entry<Long, Long>> it = heCorrelation.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<Long, Long> pairs = it.next();
			he1 = mesh.getHalfedgeWithKey(pairs.getKey());
			he2 = mesh.getHalfedgeWithKey(pairs.getValue());
			heio = new HE_Halfedge();
			heoi = new HE_Halfedge();
			mesh.add(heio);
			mesh.add(heoi);
			heio.setVertex(he1.getPair().getVertex());
			heoi.setVertex(he2.getPair().getVertex());
			he1.setNext(heio);
			heio.setNext(he2);
			he2.setNext(heoi);
			heoi.setNext(he1);
			heio.setPrev(he1);
			he2.setPrev(heio);
			heoi.setPrev(he2);
			he1.setPrev(heoi);
			fNew = new HE_Face();
			mesh.add(fNew);
			fNew.setHalfedge(he1);
			he1.setFace(fNew);
			he2.setFace(fNew);
			heio.setFace(fNew);
			heoi.setFace(fNew);
			counter.increment();
		}
		mesh.pairHalfedges();
		if (d < 0) {
			mesh.flipAllFaces();
		}
		tracker.setStatus(this, "Exiting HEM_Lattice.", -1);
		return mesh;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see wblut.hemesh.HE_Modifier#apply(wblut.hemesh.HE_Mesh)
	 */
	@Override
	public HE_Mesh apply(final HE_Selection selection) {
		tracker.setStatus(this, "Starting HEM_Lattice.", +1);
		if (d == 0) {
			tracker.setStatus(this,
					"Can't create with zero thickness, use HEM_PunchHoles instead. Exiting HEM_Lattice.", -1);
			return selection.parent;
		}
		if (sew == 0) {
			tracker.setStatus(this, "Can't create with zero width. Exiting HEM_Lattice.", -1);
			return selection.parent;
		}
		final HEM_Extrude extm = new HEM_Extrude().setDistance(0).setRelative(false).setChamfer(sew).setFuse(fuse)
				.setHardEdgeChamfer(hew).setFuseAngle(fuseAngle).setThresholdAngle(thresholdAngle);
		selection.parent.modifySelected(extm, selection);
		tracker.setStatus(this, "Creating inner mesh.", 0);
		final HE_Mesh innerMesh = selection.parent.get();
		tracker.setStatus(this, "Shrinking inner mesh.", 0);
		final HEM_VertexExpand expm = new HEM_VertexExpand().setDistance(-d);
		innerMesh.modify(expm);
		WB_ProgressCounter counter = new WB_ProgressCounter(selection.parent.getNumberOfFaces(), 10);

		tracker.setStatus(this, "Creating face correlations.", counter);
		final HashMap<Long, Long> faceCorrelation = new HashMap<Long, Long>();
		final Iterator<HE_Face> fItr1 = selection.parent.fItr();
		final Iterator<HE_Face> fItr2 = innerMesh.fItr();
		HE_Face f1;
		HE_Face f2;
		while (fItr1.hasNext()) {
			f1 = fItr1.next();
			f2 = fItr2.next();
			faceCorrelation.put(f1.key(), f2.key());
			counter.increment();
		}
		counter = new WB_ProgressCounter(selection.parent.getNumberOfHalfedges(), 10);

		tracker.setStatus(this, "Creating boundary halfedge correlations.", counter);
		final HashMap<Long, Long> heCorrelation = new HashMap<Long, Long>();
		final Iterator<HE_Halfedge> heItr1 = selection.parent.heItr();
		final Iterator<HE_Halfedge> heItr2 = innerMesh.heItr();
		HE_Halfedge he1;
		HE_Halfedge he2;
		while (heItr1.hasNext()) {
			he1 = heItr1.next();
			he2 = heItr2.next();
			if (he1.getFace() == null) {
				heCorrelation.put(he1.key(), he2.key());
			}
			counter.increment();
		}
		innerMesh.flipAllFaces();
		final int nf = selection.parent.getNumberOfFaces();
		final HE_Face[] origFaces = selection.parent.getFacesAsArray();
		selection.parent.addVertices(innerMesh.getVerticesAsArray());
		selection.parent.addFaces(innerMesh.getFacesAsArray());
		selection.parent.addHalfedges(innerMesh.getHalfedgesAsArray());
		HE_Face fo;
		HE_Face fi;
		List<HE_Halfedge> hei;
		List<HE_Halfedge> heo;
		WB_Point[] viPos;
		WB_Polygon poly;
		HE_Halfedge heoc, heic, heon, hein, heio, heoi;
		HE_Face fNew;
		WB_Coord ni, no;
		counter = new WB_ProgressCounter(nf, 10);

		tracker.setStatus(this, "Connecting outer and inner faces.", counter);
		for (int i = 0; i < nf; i++) {
			fo = origFaces[i];
			final Long innerKey = faceCorrelation.get(fo.key());
			if (extm.extruded.contains(fo)) {
				fi = selection.parent.getFaceWithKey(innerKey);
				if (obulge != 0) {
					no = fo.getFaceNormal();
					fo.push(WB_Vector.mul(no, obulge));
				}
				if (ibulge != 0) {
					ni = fi.getFaceNormal();
					fi.push(WB_Vector.mul(ni, ibulge));
				}
				final int nvo = fo.getFaceOrder();
				final int nvi = fi.getFaceOrder();
				hei = fi.getFaceHalfedges();
				viPos = new WB_Point[nvi];
				for (int j = 0; j < nvi; j++) {
					viPos[j] = new WB_Point(hei.get(j).getVertex());
				}
				poly = gf.createSimplePolygon(viPos);
				heo = fo.getFaceHalfedges();
				for (int j = 0; j < nvo; j++) {
					heoc = heo.get(j);
					heon = heo.get((j + 1) % nvo);
					final int cic = poly.closestIndex(heoc.getVertex());
					final int cin = poly.closestIndex(heon.getVertex());
					heic = hei.get(cin);
					hein = hei.get(cic);
					heio = new HE_Halfedge();
					heoi = new HE_Halfedge();
					fNew = new HE_Face();
					heoi.setVertex(heon.getVertex());
					heio.setVertex(hein.getVertex());
					heoc.setNext(heoi);
					heoi.setPrev(heoc);
					heoc.setFace(fNew);
					if (cic == cin) {
						heoi.setNext(heio);
						heio.setPrev(heoi);
						heoi.setFace(fNew);
					} else {
						heoi.setNext(heic);
						heic.setPrev(heoi);
						heoi.setFace(fNew);
						heic.setNext(heio);
						heio.setPrev(heic);
						heic.setFace(fNew);
					}
					heio.setNext(heoc);
					heoc.setPrev(heio);
					heio.setFace(fNew);
					fNew.setHalfedge(heoc);
					selection.parent.add(heio);
					selection.parent.add(heoi);
					selection.parent.add(fNew);
					selection.parent.remove(fo);
					selection.parent.remove(fi);
				}
			}
			counter.increment();
		}
		counter = new WB_ProgressCounter(heCorrelation.size(), 10);

		tracker.setStatus(this, "Connecting outer and inner boundaries.", counter);
		final Iterator<Map.Entry<Long, Long>> it = heCorrelation.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<Long, Long> pairs = it.next();
			he1 = selection.parent.getHalfedgeWithKey(pairs.getKey());
			he2 = selection.parent.getHalfedgeWithKey(pairs.getValue());
			heio = new HE_Halfedge();
			heoi = new HE_Halfedge();
			selection.parent.add(heio);
			selection.parent.add(heoi);
			heio.setVertex(he1.getPair().getVertex());
			heoi.setVertex(he2.getPair().getVertex());
			he1.setNext(heio);
			heio.setNext(he2);
			he2.setNext(heoi);
			heoi.setNext(he1);
			heio.setPrev(he1);
			he2.setPrev(heio);
			heoi.setPrev(he2);
			he1.setPrev(heoi);
			fNew = new HE_Face();
			selection.parent.add(fNew);
			fNew.setHalfedge(he1);
			he1.setFace(fNew);
			he2.setFace(fNew);
			heio.setFace(fNew);
			heoi.setFace(fNew);
			counter.increment();
		}
		selection.parent.pairHalfedges();
		if (d < 0) {
			selection.parent.flipAllFaces();
		}
		tracker.setStatus(this, "Exiting HEM_Lattice.", -1);
		return selection.parent;
	}
}
