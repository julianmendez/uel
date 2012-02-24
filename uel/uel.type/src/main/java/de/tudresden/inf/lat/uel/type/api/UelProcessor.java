package de.tudresden.inf.lat.uel.type.api;

/**
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface UelProcessor {

	public boolean computeNextUnifier();

	public UelInput getInput();

	public UelOutput getUnifier();

}
