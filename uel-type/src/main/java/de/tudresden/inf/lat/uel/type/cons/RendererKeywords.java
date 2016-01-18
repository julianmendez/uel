package de.tudresden.inf.lat.uel.type.cons;

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
	 * a string representing a line break
	 */
	String newLine = System.getProperty("line.separator");

	/**
	 * the string '('
	 */
	String open = "(";

	/**
	 * the string 'some'
	 */
	String some = "some";

	/**
	 * the string ' '
	 */
	String space = " ";

	/**
	 * the string 'top'
	 */
	String krssTop = "top";

	/**
	 * the string 'owl:Thing'
	 */
	String owlThing = "owl:Thing";

	/**
	 * the string 'ObjectSomeValuesFrom'
	 */
	String objectSomeValuesFrom = "ObjectSomeValuesFrom";

	/**
	 * the string 'ObjectIntersectionOf'
	 */
	String objectIntersectionOf = "ObjectIntersectionOf";
}
