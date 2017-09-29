package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.Function;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.google.common.collect.Sets;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.impl.DefinitionSet;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * Base class for all rendering purposes of UEL objects. Subclasses can
 * implement the rendering using one of two modes (or a combination of both).
 * The 'render...' methods are used to obtained renderings of the given objects,
 * by first calling 'initialize()', then the corresponding 'translate...'
 * method, and finally a 'finalize...' method, depending on the type of object
 * (concept term, role, or axioms). The 'translate...' methods recursively call
 * other necessary 'translate...' methods (for sub-objects). These methods can
 * either directly return the rendering, to be used as arguments in constructors
 * for more complex renderings, or simply update an internal variable, for
 * sequential renderings. In the latter case, only the 'finalize...' methods
 * constructs the final rendering.
 * 
 * For example, the instances of 'StringRenderer' update an internal
 * 'StringBuilder', which only yields the final string representation via a call
 * to 'finalize...'. In contrast, the 'OWLRenderer' needs the final renderings
 * of sub-objects in order to compute more complex renderings (e.g., the
 * renderings of a filler and a role are needed to obtain an existential
 * restriction (OWLObjectSomeValuesFrom)).
 * 
 * @author Stefan Borgwardt
 *
 * @param <ExpressionType>
 *            the type of a rendered class expression
 * @param <RoleType>
 *            the type of a rendered role
 * @param <AxiomsType>
 *            the type of a rendered set of axioms
 */
public abstract class Renderer<ExpressionType, RoleType, AxiomsType> {

	private static final String[] suffixes = new String[] { AtomManager.UNDEF_SUFFIX, AtomManager.ROLEGROUP_SUFFIX,
			AtomManager.VAR_SUFFIX };

	private final AtomManager atomManager;
	private final DefinitionSet background;
	private final ShortFormProvider provider;
	private final boolean restrictToUserVariables;

	/**
	 * Construct a new renderer.
	 * 
	 * @param atomManager
	 *            the atom manager
	 * @param provider
	 *            the short form provider
	 * @param background
	 *            (optional) a set of background definitions used for
	 *            abbreviating expressions
	 */
	protected Renderer(AtomManager atomManager, ShortFormProvider provider, DefinitionSet background) {
		this.atomManager = atomManager;
		this.provider = provider;
		this.background = background;
		this.restrictToUserVariables = (background != null);
	}

	private String addSuffixes(String id, String label) {
		for (String suffix : suffixes) {
			if (id.endsWith(suffix)) {
				label += suffix;
			}
		}
		return label;
	}

	private int compareDefinitions(Definition d1, Definition d2) {
		// compare definitions according to the lexicographic order of the
		// (IRIs or short forms of the) defined concept names
		return compareNames(d1.getDefiniendum(), d2.getDefiniendum());
	}

	private int compareNames(Integer id1, Integer id2) {
		return renderName(id1).compareTo(renderName(id2));
	}

	private int compareRoles(Integer r1, Integer r2) {
		return renderRole(r1).compareTo(renderRole(r2));
	}

	/**
	 * Finish the construction of an axiom or a set of axioms.
	 * 
	 * @return the rendered axiom(s)
	 */
	protected abstract AxiomsType finalizeAxioms();

	/**
	 * Finish the construction of a class expression.
	 * 
	 * @return the rendered expression
	 */
	protected abstract ExpressionType finalizeExpression();

	/**
	 * Get the short form of the given identifier.
	 * 
	 * @param id
	 *            a string representation of an entity, e.g. an IRI
	 * @return the short form of 'id'
	 */
	public String getShortForm(String id) {
		if (provider == null) {
			return id;
		}

		String label = stripSuffixes(id);
		String str = provider.getShortForm(label);
		if (str != null) {
			label = str;
		}
		label = addSuffixes(id, label);

		return "<" + label + ">";
	}

	/**
	 * Initialize the rendering of an expression or an axiom.
	 */
	protected abstract void initialize();

	/**
	 * Introduce a 'new line' to separate different parts of a rendered object
	 * from each other.
	 */
	protected abstract void newLine();

	/**
	 * Render the given atom.
	 * 
	 * @param atomId
	 *            the atom id
	 * @param expand
	 *            indicates whether the atom should be expanded if it is a
	 *            flattening variable
	 * @return the rendered atom
	 */
	public ExpressionType renderAtom(Integer atomId, boolean expand) {
		initialize();
		translateAtom(atomId, expand);
		return finalizeExpression();
	}

	/**
	 * Render the given list of atoms.
	 * 
	 * @param description
	 *            a description of the list
	 * @param atomIds
	 *            the atom ids
	 * @return the rendered atoms
	 */
	public ExpressionType renderAtomList(String description, Collection<Integer> atomIds) {
		initialize();
		translateAtomList(description, atomIds);
		return finalizeExpression();
	}

	/**
	 * Render the given axioms.
	 * 
	 * @param axioms
	 *            a set of axioms
	 * @return the rendered axioms
	 */
	public AxiomsType renderAxioms(Collection<? extends Axiom> axioms) {
		initialize();
		translateAxioms(axioms);
		return finalizeAxioms();
	}

	/**
	 * Render the given OWL axioms.
	 * 
	 * @param axioms
	 *            a set of axioms
	 * @param positive
	 *            indicates whether the axioms should be rendered as positive
	 *            axioms or as negative axioms
	 * @return the rendered axioms
	 */
	public AxiomsType renderAxioms(Collection<OWLAxiom> axioms, boolean positive) {
		initialize();
		translateAxioms(axioms, positive);
		return finalizeAxioms();
	}

	/**
	 * Render the given OWL entity.
	 * 
	 * @param entity
	 *            the entity
	 * @return the rendered entity
	 */
	public String renderEntity(OWLEntity entity) {
		return getShortForm(entity.toStringID());
	}

	/**
	 * Render the given unification problem.
	 * 
	 * @param input
	 *            a 'Goal' object representing the unification problem
	 * @param sorted
	 *            indicates whether the definitions and various lists should be
	 *            sorted
	 * @return the rendered problem, with axioms, variables, etc.
	 */
	public AxiomsType renderGoal(Goal input, boolean sorted) {
		initialize();
		translateAxioms(sortDefinitions(input.getDefinitions(), sorted));
		translateAxioms(input.getEquations());
		translateAxioms(input.getSubsumptions());
		translateAxioms(input.getDisequations());
		translateAxioms(input.getDissubsumptions());
		newLine();
		translateAtomList("Variables", sortNames(input.getAtomManager().getVariables(), sorted));
		translateAtomList("User variables", sortNames(input.getAtomManager().getUserVariables(), sorted));
		translateAtomList("Constants", sortNames(input.getAtomManager().getConstants(), sorted));
		newLine();
		Collection<Integer> types = sortNames(input.getTypes(), sorted);
		if (!types.isEmpty()) {
			translateAtomList("Types", types);
		}
		newLine();
		for (Integer type : types) {
			Integer supertype = input.getDirectSupertype(type);
			if (supertype != null) {
				translateAtomList("Direct supertype of " + renderName(type), Collections.singleton(supertype));
			}
		}
		newLine();
		for (Integer roleId : sortRoles(input.getDomains().keySet(), sorted)) {
			translateAtomList("Domain of " + renderRole(roleId), sortNames(input.getDomains().get(roleId), sorted));
		}
		newLine();
		for (Integer roleId : sortRoles(input.getRanges().keySet(), sorted)) {
			translateAtomList("Range of " + renderRole(roleId), sortNames(input.getRanges().get(roleId), sorted));
		}
		newLine();
		for (Integer conceptNameId : sortNames(
				Sets.union(input.getAtomManager().getVariables(), input.getAtomManager().getConstants()), sorted)) {
			if (input.getTypeAssignment().containsKey(conceptNameId)) {
				translateAtomList("Type of " + renderName(conceptNameId),
						Collections.singleton(input.getTypeAssignment().get(conceptNameId)));
			}
		}
		return finalizeAxioms();
	}

	/**
	 * Render the given class as a string.
	 * 
	 * @param atomId
	 *            the atom id representing the class
	 * @return the short form of the class' IRI
	 */
	public String renderName(Integer atomId) {
		return getShortForm(atomManager.printConceptName(atomId));
	}

	/**
	 * Render the given role as a string.
	 * 
	 * @param roleId
	 *            the role id
	 * @return the short form of the object property's IRI
	 */
	public String renderRole(Integer roleId) {
		return getShortForm(atomManager.getRoleName(roleId));
	}

	/**
	 * Render the top concept (not its alias).
	 * 
	 * @return the rendered top concept
	 */
	public ExpressionType renderTop() {
		initialize();
		translateTop();
		return finalizeExpression();
	}

	/**
	 * Render the given unifier, including definitions and optional type
	 * assignment.
	 * 
	 * @param unifier
	 *            the unifier
	 * @param typeInfo
	 *            indicates whether type information should be rendered
	 * @param sorted
	 *            indicates whether the definitions in the output should be
	 *            sorted by the label of the variable
	 * @return the rendered unifier
	 */
	public AxiomsType renderUnifier(Unifier unifier, boolean typeInfo, boolean sorted) {
		initialize();

		for (Definition definition : sortDefinitions(unifier.getDefinitions(), sorted)) {
			if (!restrictToUserVariables || atomManager.getUserVariables().contains(definition.getDefiniendum())) {
				if (!definition.getDefiniens().equals(Collections.singleton(definition.getDefiniendum())))
					translateAxiom(definition);
			}
		}

		if (typeInfo) {
			// optional: print type information used to obtain the unifier; this
			// is only meaningful in 'SNOMED mode'
			if (unifier.getTypeAssignment() != null) {
				for (Integer atomId : sortNames(unifier.getTypeAssignment().keySet(), sorted)) {
					// if (!unifier.getTypeAssignment().get(atomId).isEmpty()) {
					translateAtomList("Types of " + renderName(atomId), unifier.getTypeAssignment().get(atomId));
					// }
				}
			}
		}

		return finalizeAxioms();
	}

	private <T> Collection<T> sort(Collection<T> orig, Comparator<T> comparator, boolean sorted) {
		if (sorted) {
			Collection<T> sortedSet = new TreeSet<T>(comparator);
			sortedSet.addAll(orig);
			return sortedSet;
		} else {
			return orig;
		}
	}

	private Collection<Definition> sortDefinitions(DefinitionSet definitions, boolean sorted) {
		return sort(definitions.values(), this::compareDefinitions, sorted);
	}

	private Collection<Integer> sortNames(Collection<Integer> atomIds, boolean sorted) {
		return sort(atomIds, this::compareNames, sorted);
	}

	private Collection<Integer> sortRoles(Collection<Integer> roleIds, boolean sorted) {
		return sort(roleIds, this::compareRoles, sorted);
	}

	private String stripSuffixes(String id) {
		String label = id;
		for (String suffix : suffixes) {
			if (id.endsWith(suffix)) {
				label = id.substring(0, id.length() - suffix.length());
			}
		}
		return label;
	}

	/**
	 * Add an atom to the rendering under construction.
	 * 
	 * @param atomId
	 *            the atom id
	 * @param expand
	 *            indicates whether the atom should be expanded if it is a
	 *            flattening variable
	 * @return the rendered atom (for recursive translations)
	 */
	protected ExpressionType translateAtom(Integer atomId, boolean expand) {
		if (atomManager.getExistentialRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			Integer roleId = atomManager.getRoleId(atomId);
			return translateExistentialRestriction(roleId, childId, this::translateRole, t -> translateAtom(t, expand));
		} else {
			if (expand && restrictToUserVariables && atomManager.getFlatteningVariables().contains(atomId)) {
				return translateConjunction(background.getDefiniens(atomId));
			} else {
				return translateName(atomId);
			}
		}
	}

	/**
	 * Add an atom list to the rendering under construction.
	 * 
	 * @param description
	 *            a description of the list
	 * @param atomIds
	 *            the atom ids
	 * @return the rendered atom list (for recursive translations)
	 */
	protected abstract ExpressionType translateAtomList(String description, Collection<Integer> atomIds);

	/**
	 * Add an axiom to the rendering under construction.
	 * 
	 * @param axiom
	 *            the axiom
	 * @return the rendered axiom (for recursive translations)
	 */
	protected abstract AxiomsType translateAxiom(Axiom axiom);

	/**
	 * Add an OWL axiom to the rendering under construction.
	 * 
	 * @param axiom
	 *            the axiom
	 * @param positive
	 *            indicates whether the axiom should be rendered as a positive
	 *            axiom or a negative axiom
	 * @return the rendered axiom (for recursive translations)
	 */
	protected abstract AxiomsType translateAxiom(OWLAxiom axiom, boolean positive);

	/**
	 * Add the given axioms to the rendering under construction.
	 * 
	 * @param axioms
	 *            a set of axioms
	 * @return the rendered axioms (for recursive translations)
	 */
	protected AxiomsType translateAxioms(Collection<? extends Axiom> axioms) {
		AxiomsType ret = null;
		for (Axiom axiom : axioms) {
			ret = translateAxiom(axiom);
		}
		return ret;
	}

	/**
	 * Add the given OWL axioms to the rendering under construction.
	 * 
	 * @param axioms
	 *            a set of axioms
	 * @param positive
	 *            indicates whether the axioms should be rendered as positive
	 *            axioms or negative axioms
	 * @return the rendered axioms (for recursive translations)
	 */
	protected AxiomsType translateAxioms(Collection<OWLAxiom> axioms, boolean positive) {
		AxiomsType ret = null;
		for (OWLAxiom axiom : axioms) {
			ret = translateAxiom(axiom, positive);
		}
		return ret;
	}

	/**
	 * Add an OWL class to the rendering under construction.
	 * 
	 * @param cls
	 *            the class
	 * @return the rendered class (for recursive translation)
	 */
	protected abstract ExpressionType translateClass(OWLClass cls);

	/**
	 * Add an OWL class expression to the rendering under construction.
	 * 
	 * @param expression
	 *            the class expression
	 * @return the rendered class expression (for recursive translation)
	 */
	protected ExpressionType translateClassExpression(OWLClassExpression expression) {
		if (expression instanceof OWLClass) {
			return translateClass((OWLClass) expression);
		}
		if (expression instanceof OWLObjectIntersectionOf) {
			return translateConjunction(((OWLObjectIntersectionOf) expression).getOperands(),
					this::translateClassExpression);
		}
		if (expression instanceof OWLObjectSomeValuesFrom) {
			OWLObjectSomeValuesFrom someValuesFrom = ((OWLObjectSomeValuesFrom) expression);
			return translateExistentialRestriction(someValuesFrom.getProperty().asOWLObjectProperty(),
					someValuesFrom.getFiller(), this::translateObjectProperty, this::translateClassExpression);
		}
		throw new RuntimeException("Unsupported class expression: " + expression);
	}

	/**
	 * Add a conjunction to the rendering under construction. To render the
	 * individual atoms, the method 'translateAtom' is used.
	 * 
	 * @param conjuncts
	 *            a set of conjuncts
	 * @return the rendered conjunction (for recursive translation)
	 */
	protected ExpressionType translateConjunction(Collection<Integer> conjuncts) {
		return translateConjunction(conjuncts, t -> translateAtom(t, true));
	}

	/**
	 * Add a conjunction to the rendering under construction.
	 * 
	 * @param <T>
	 *            the conjunct type
	 * @param conjuncts
	 *            a set of conjuncts
	 * @param conjunctTranslator
	 *            a translation function for individual elements
	 * @return the rendered conjunction (for recursive translation)
	 */
	protected <T> ExpressionType translateConjunction(Collection<T> conjuncts,
			Function<T, ExpressionType> conjunctTranslator) {
		if (conjuncts.isEmpty()) {
			return translateTop();
		} else if (conjuncts.size() == 1) {
			return conjunctTranslator.apply(conjuncts.iterator().next());
		} else {
			return translateTrueConjunction(conjuncts, conjunctTranslator);
		}
	}

	/**
	 * Add an existential restriction to the rendering under construction.
	 * 
	 * @param<T> the
	 *               type of a role, e.g. a role id or an OWLObjectProperty
	 * @param<S> the
	 *               type of the filler, e.g. a concept name id or an
	 *               OWLClassExpression
	 * 
	 * @param role
	 *            the role
	 * @param filler
	 *            the filler
	 * @param roleTranslator
	 *            a translation function for roles
	 * @param fillerTranslator
	 *            a translation function for fillers
	 * @return the rendered existential restriction (for recursive translation)
	 */
	protected abstract <T, S> ExpressionType translateExistentialRestriction(T role, S filler,
			Function<T, RoleType> roleTranslator, Function<S, ExpressionType> fillerTranslator);

	/**
	 * Add a concept name to the rendering under construction.
	 * 
	 * @param atomId
	 *            the atom id of the concept name
	 * @return the rendered concept name (for recursive translation)
	 */
	protected abstract ExpressionType translateName(Integer atomId);

	/**
	 * Add an OWL object property to the rendering under construction.
	 * 
	 * @param prop
	 *            the object property
	 * @return the rendered object property (for recursive translation)
	 */
	protected abstract RoleType translateObjectProperty(OWLObjectProperty prop);

	/**
	 * Add a role to the rendering under construction.
	 * 
	 * @param roleId
	 *            the role id
	 * @return the rendered role (for recursive translation)
	 */
	protected abstract RoleType translateRole(Integer roleId);

	/**
	 * Add a list of roles to the rendering under construction.
	 * 
	 * @param description
	 *            a description of the list
	 * @param roleIds
	 *            a set of role ids
	 * @return the rendered role list (for recursive translation)
	 */
	protected abstract ExpressionType translateRoleList(String description, Collection<Integer> roleIds);

	/**
	 * Add the top concept to the rendering under construction.
	 * 
	 * @return the rendering of top
	 */
	protected abstract ExpressionType translateTop();

	/**
	 * Add a 'proper' conjunction between at least two conjuncts to the
	 * rendering under construction.
	 * @param<T> the
	 *               type of the conjuncts
	 * 
	 * @param conjuncts
	 *            a set of conjuncts
	 * @param conjunctTranslator
	 *            a translation function for individual conjuncts
	 * @return the rendered conjunction (for recursive translation)
	 */
	protected abstract <T> ExpressionType translateTrueConjunction(Collection<T> conjuncts,
			Function<T, ExpressionType> conjunctTranslator);

}
