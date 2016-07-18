/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDAlgorithmResult {

	enum SNOMEDAlgorithmStatus {
		SUCCESS, FAILURE, TIMEOUT, ERROR
	}

	public SNOMEDAlgorithmStatus status = SNOMEDAlgorithmStatus.FAILURE;

	public String unificationAlgorithmName = "";

	public long preprocessing = 0;

	public long firstUnifier = 0;

	public long goalUnifier = 0;

	public long allUnifiers = 0;

	public long totalTime = 0;

	public int numberOfSolutions = 0;

	public SNOMEDAlgorithmResult(String unificationAlgorithmName) {
		this.unificationAlgorithmName = unificationAlgorithmName;
	}

}
