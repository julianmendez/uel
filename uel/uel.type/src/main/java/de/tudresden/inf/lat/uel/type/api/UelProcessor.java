package de.tudresden.inf.lat.uel.type.api;

import java.util.Map;

/**
 * 
 * @author Stefan Borgwardt
 * @author Julian Mendez
 */
public interface UelProcessor {

	public boolean computeNextUnifier();

	public Map<String, String> getInfo();

	public UelInput getInput();

	public UelOutput getUnifier();

}
