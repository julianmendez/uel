package de.tudresden.inf.lat.uel.plugin.ui;

/**
 * An object of this class is a pair of label-identifier that is sorted first by
 * label and second by identifier.
 * 
 * @author Julian Mendez
 */
class LabelId implements Comparable<LabelId> {

	private int hashCode = 0;
	private Integer id = null;
	private String label = null;

	/**
	 * Constructs a new pair label-identifier.
	 * 
	 * @param label
	 *            label
	 * @param id
	 *            identifier
	 */
	public LabelId(String label, Integer id) {
		if (label == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.label = label;
		this.id = id;
		this.hashCode = label.hashCode() + 31 * id.hashCode();
	}

	@Override
	public int compareTo(LabelId o) {
		if (o == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		int ret = getLabel().compareTo(o.getLabel());
		if (ret == 0) {
			ret = getId().compareTo(o.getId());
		}
		return ret;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof LabelId) {
			LabelId other = (LabelId) o;
			ret = getLabel().equals(other.getLabel()) && getId().equals(other.getId());
		}

		return ret;
	}

	public Integer getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toString() {
		return getLabel();
	}

}
