package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Map;

import de.tudresden.inf.lat.uel.core.type.Goal;

/**
 * 
 * @author Julian Mendez
 */
public class StatInfo {

	private final Integer clauseCount;
	private final Goal goal;
	private final Integer literalCount;
	private final Map<String, String> mapIdLabel;

	public StatInfo(Goal g, int literals, int clauses,
			Map<String, String> labels) {
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.goal = g;
		this.literalCount = literals;
		this.clauseCount = clauses;
		this.mapIdLabel = labels;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof StatInfo) {
			StatInfo other = (StatInfo) o;
			ret = this.goal.equals(other.goal)
					&& this.literalCount.equals(other.literalCount)
					&& this.clauseCount.equals(other.clauseCount);

		}
		return ret;
	}

	public Integer getAllVarCount() {
		return this.goal.getVariables().size();
	}

	public Integer getClauseCount() {
		return this.clauseCount;
	}

	public Goal getGoal() {
		return this.goal;
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.mapIdLabel.get(id);

		if (ret == null) {
			if (id.endsWith(Goal.UNDEF_SUFFIX)) {
				String origId = id.substring(0,
						id.length() - Goal.UNDEF_SUFFIX.length());
				ret = this.mapIdLabel.get(origId);
				if (ret != null) {
					ret += Goal.UNDEF_SUFFIX;
				}
			}
		}

		if (ret == null) {
			ret = id;
		}
		return ret;
	}

	public Integer getLiteralCount() {
		return this.literalCount;
	}

	@Override
	public int hashCode() {
		return this.goal.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(getGoal());
		sbuf.append("\n");
		sbuf.append(getLiteralCount());
		sbuf.append("\n");
		sbuf.append(getClauseCount());
		sbuf.append("\n");
		return sbuf.toString();
	}

}
