/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;

import de.tudresden.inf.lat.uel.core.processor.UelOptions;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDResult {

	enum SNOMEDGoalStatus {
		SUCCESS, TOO_LARGE, TIMEOUT, ERROR, COMPLETE
	}

	public OWLClass goalClass;

	public UelOptions options;

	public SNOMEDGoalStatus goalStatus = SNOMEDGoalStatus.ERROR;

	public long buildGoal = 0;

	public int goalSize = 0;

	public List<SNOMEDAlgorithmResult> algorithmResults = new ArrayList<SNOMEDAlgorithmResult>();

	public SNOMEDResult(OWLClass goalClass, UelOptions options) {
		this.goalClass = goalClass;
		this.options = options;
	}

}
