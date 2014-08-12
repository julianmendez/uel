package de.tudresden.inf.lat.uel.type.api;

import java.util.List;
import java.util.Map;

/**
 * An object implementing this interface computes unifiers.
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface UelProcessor {

	/**
	 * Computes the next unifier. Returns <code>true</code> if and only if the
	 * unifier has been successfully computed.
	 * 
	 * @return <code>true</code> if and only if the unifier has been
	 *         successfully computed
	 */
	boolean computeNextUnifier();

	/**
	 * Returns information about the last computation. This information can
	 * contain, for example, the processor's name or the number of (atom)
	 * variables required.
	 * 
	 * @return information about the last computation
	 */
	List<Map.Entry<String, String>> getInfo();

	/**
	 * Returns the input to be used to compute the next unifier.
	 * 
	 * @return the input to be used to compute the next unifier
	 */
	UelInput getInput();

	/**
	 * Returns the result of the last computation.
	 * 
	 * @throws IllegalStateException
	 *             if invoked before the first computation
	 * @return the result of the last computation
	 */
	UelOutput getUnifier();

}