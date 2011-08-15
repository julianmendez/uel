package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.HashMap;
import java.util.Map;

import de.tudresden.inf.lat.uel.core.type.Goal;

/**
 * 
 * @author Julian Mendez
 */
public class StatInfo {

	private Integer clauseCount = 0;
	private Goal goal = null;
	private Map<String, String> idLabelMap = new HashMap<String, String>();
	private Integer literalCount = 0;

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
		this.idLabelMap = labels;
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

		String ret = this.idLabelMap.get(id);
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
