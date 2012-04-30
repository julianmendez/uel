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
	public static final String and = "and";

	/**
	 * the string ')'
	 */
	public static final String close = ")";

	/**
	 * the string 'defconcept'
	 */
	public static final String defconcept = "defconcept";

	/**
	 * the string 'define-concept'
	 */
	public static final String define_concept = "define-concept";

	/**
	 * the string 'define-primitive-concept'
	 */
	public static final String define_primitive_concept = "define-primitive-concept";

	/**
	 * a string representing a line break
	 */
	public static final String newLine = System.getProperty("line.separator");

	/**
	 * the string '('
	 */
	public static final String open = "(";

	/**
	 * the string 'some'
	 */
	public static final String some = "some";

	/**
	 * the string ' '
	 */
	public static final String space = " ";

	/**
	 * the string 'top'
	 */
	public static final String top = "top";

}
