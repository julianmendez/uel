package de.tudresden.inf.lat.uel.plugin.ui;

import java.util.List;
import java.util.Map;

import de.tudresden.inf.lat.uel.core.processor.PluginGoal;
import de.tudresden.inf.lat.uel.core.type.AtomManager;

/**
 * An object of this class contains statistical information about a processor
 * after the computation of unifiers.
 * 
 * @author Julian Mendez
 */
public class StatInfo {

	private List<Map.Entry<String, String>> info;
	private final Map<String, String> mapIdLabel;
	private final PluginGoal pluginGoal;

	public StatInfo(PluginGoal g, List<Map.Entry<String, String>> info,
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
		setInfo(info);
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

	public List<Map.Entry<String, String>> getInfo() {
		return this.info;
	}

	public String getLabel(String id) {
		if (id == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String ret = this.mapIdLabel.get(id);

		if (ret == null) {
			if (id.endsWith(AtomManager.UNDEF_SUFFIX)) {
				String origId = id.substring(0, id.length()
						- AtomManager.UNDEF_SUFFIX.length());
				ret = this.mapIdLabel.get(origId);
				if (ret != null) {
					ret += AtomManager.UNDEF_SUFFIX;
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

	public void setInfo(List<Map.Entry<String, String>> info) {
		this.info = info;
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
