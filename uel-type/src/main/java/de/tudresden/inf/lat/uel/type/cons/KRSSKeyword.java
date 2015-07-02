package de.tudresden.inf.lat.uel.type.cons;

/**
 * This interface contains the main required keywords of KRSS (Knowledge
 * Representation System Specification).
 * 
 * @author Julian Mendez
 */
public interface KRSSKeyword {

	/**
	 * the string 'and'
	 */
	String and = "and";

	/**
	 * the string ')'
	 */
	String close = ")";

	/**
	 * the string 'defconcept'
	 */
	String defconcept = "defconcept";

	/**
	 * the string 'define-concept'
	 */
	String define_concept = "define-concept";

	/**
	 * the string 'define-primitive-concept'
	 */
	String define_primitive_concept = "define-primitive-concept";

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
	String top = "top";

}
