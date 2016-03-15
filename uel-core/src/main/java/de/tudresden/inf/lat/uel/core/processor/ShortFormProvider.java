/**
 * 
 */
package de.tudresden.inf.lat.uel.core.processor;

/**
 * @author Stefan Borgwardt
 *
 */
public interface ShortFormProvider {

	public String getShortForm(String id);

	public void resetCache();

}
