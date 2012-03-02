package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.Map;

import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;

/**
 * 
 * @author Julian Mendez
 */
public class StatInfo {

	private final Map<String, String> info;
	private final Map<String, String> mapIdLabel;
	private final PluginGoal pluginGoal;

	public StatInfo(PluginGoal g, Map<String, String> info,
			Map<String, String> labels) {
		if (g == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (info == null) {
			throw new IllegalArgumentException("Null argument.");
		}
		if (labels == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.pluginGoal = g;
		this.info = info;
		this.mapIdLabel = labels;
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = (this == o);
		if (!ret && o instanceof StatInfo) {
			StatInfo other = (StatInfo) o;
			ret = this.pluginGoal.equals(other.pluginGoal)
					&& this.info.equals(other.info);

		}
		return ret;
	}

	public Integer getAllVarCount() {
		return this.pluginGoal.getVariableSetSize();
	}

	public Map<String, String> getInfo() {
		return this.info;
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.mapIdLabel.get(id);

		if (ret == null) {
			if (id.endsWith(PluginGoal.UNDEF_SUFFIX)) {
				String origId = id.substring(0, id.length()
						- PluginGoal.UNDEF_SUFFIX.length());
				ret = this.mapIdLabel.get(origId);
				if (ret != null) {
					ret += PluginGoal.UNDEF_SUFFIX;
				}
			}
		}

		if (ret == null) {
			ret = id;
		}
		return ret;
	}

	public PluginGoal getPluginGoal() {
		return this.pluginGoal;
	}

	@Override
	public int hashCode() {
		return this.pluginGoal.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(getPluginGoal());
		sbuf.append("\n");
		sbuf.append(getInfo());
		sbuf.append("\n");
		return sbuf.toString();
	}

}
