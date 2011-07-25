package de.tudresden.inf.lat.uel.plugin.ui;

class LabelId implements Comparable<LabelId> {

	private int hashCode = 0;
	private String id = null;
	private String label = null;

	public LabelId(String label, String id) {
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
		if (o== null) {
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
		boolean ret = false;
		if (o instanceof LabelId) {
			LabelId other = (LabelId) o;
			ret = getLabel().equals(other.getLabel())
					&& getId().equals(other.getId());
		}

		return ret;
	}

	public String getId() {
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
		return getId() + " " + getLabel();
	}

}
