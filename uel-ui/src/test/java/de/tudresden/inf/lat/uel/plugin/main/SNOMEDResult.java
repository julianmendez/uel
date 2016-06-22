/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import org.semanticweb.owlapi.model.OWLClass;

import de.tudresden.inf.lat.uel.core.processor.UelOptions;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDResult {

	enum SNOMEDStatus {
		SUCCESS, FAILURE, TOO_LARGE, TIMEOUT, ERROR
	}

	public OWLClass goalClass;

	public UelOptions options;

	public SNOMEDStatus status = SNOMEDStatus.FAILURE;

	public long buildGoal = 0;

	public int goalSize = 0;

	public long preprocessing = 0;

	public long firstUnifier = 0;

	public long goalUnifier = 0;

	public long allUnifiers = 0;

	public int numberOfSolutions = 0;

	public SNOMEDResult(OWLClass goalClass, UelOptions options) {
		this.goalClass = goalClass;
		this.options = options;
	}

}
