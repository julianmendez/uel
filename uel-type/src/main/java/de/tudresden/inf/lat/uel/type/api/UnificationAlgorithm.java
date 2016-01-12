package de.tudresden.inf.lat.uel.type.api;

import java.util.List;
import java.util.Map.Entry;

import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * An object implementing this interface computes unifiers.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface UnificationAlgorithm {

	/**
	 * Clean up used resources in case the algorithm thread was interrupted.
	 */
	void cleanup();

	/**
	 * Computes the next unifier. Returns <code>true</code> if and only if the
	 * unifier has been successfully computed.
	 * 
	 * @return <code>true</code> if and only if the unifier has been
	 *         successfully computed
	 * 
	 * @throws InterruptedException
	 *             if the process is interrupted
	 */
	boolean computeNextUnifier() throws InterruptedException;

	/**
	 * Returns information about the last computation. This information can
	 * contain, for example, the processor's name or the number of (atom)
	 * variables required.
	 * 
	 * @return information about the last computation
	 */
	List<Entry<String, String>> getInfo();

	/**
	 * Returns the input to be used to compute the next unifier.
	 * 
	 * @return the input to be used to compute the next unifier
	 */
	Goal getGoal();

	/**
	 * Returns the result of the last computation.
	 * 
	 * @return the result of the last computation
	 * 
	 * @throws IllegalStateException
	 *             if invoked before the first computation
	 */
	Unifier getUnifier();

}
