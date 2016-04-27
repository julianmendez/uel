package de.tudresden.inf.lat.uel.core.renderer;

/**
 * This interface contains the keywords for rendering DL objects (existential
 * restrictions, conjunctions, ...) in various string representations.
 * 
 * @author Julian Mendez
 * @author Stefan Borgwardt
 */
public interface RendererKeywords {

	/**
	 * the string 'and'
	 */
	String and = "and";

	/**
	 * the string ')'
	 */
	String close = ")";

	/**
	 * the string ': '
	 */
	String colon = ": ";

	/**
	 * the string ', '
	 */
	String comma = ", ";

	/**
	 * the string 'top'
	 */
	String krssTop = "top";

	/**
	 * the string 'ObjectIntersectionOf'
	 */
	String objectIntersectionOf = "ObjectIntersectionOf";

	/**
	 * the string 'ObjectSomeValuesFrom'
	 */
	String objectSomeValuesFrom = "ObjectSomeValuesFrom";

	/**
	 * the string '('
	 */
	String open = "(";

	/**
	 * the string 'owl:Thing'
	 */
	String owlThing = "owl:Thing";

	/**
	 * the string 'some'
	 */
	String some = "some";

	/**
	 * the string ' '
	 */
	String space = " ";
}
