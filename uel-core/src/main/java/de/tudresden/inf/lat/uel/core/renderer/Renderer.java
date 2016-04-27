package de.tudresden.inf.lat.uel.core.renderer;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.tudresden.inf.lat.uel.core.processor.ShortFormProvider;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.Axiom;
import de.tudresden.inf.lat.uel.type.api.Definition;
import de.tudresden.inf.lat.uel.type.api.Goal;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

/**
 * Base class for all rendering purposes of UEL objects.
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
abstract class Renderer<ExpressionType, RoleType, AxiomsType> {

	private static final String[] suffixes = new String[] { AtomManager.UNDEF_SUFFIX, AtomManager.ROLEGROUP_SUFFIX,
			AtomManager.VAR_SUFFIX };

	private final AtomManager atomManager;
	private final Set<Definition> background;
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
	protected Renderer(AtomManager atomManager, ShortFormProvider provider, Set<Definition> background) {
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

	private Set<Integer> getDefinition(Integer atomId) {
		for (Definition definition : background) {
			if (definition.getDefiniendum().equals(atomId)) {
				return definition.getRight();
			}
		}
		throw new IllegalArgumentException("Atom has no definition.");
	}

	private String getShortForm(String id) {
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
	 * Add the given atom to the rendering.
	 * 
	 * @param atomId
	 *            the atom id
	 * @return the rendered atom
	 */
	public ExpressionType renderAtom(Integer atomId) {
		initialize();
		translateAtom(atomId);
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
	public ExpressionType renderAtomList(String description, Set<Integer> atomIds) {
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
	public AxiomsType renderAxioms(Set<? extends Axiom> axioms) {
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
	public AxiomsType renderAxioms(Set<OWLAxiom> axioms, boolean positive) {
		initialize();
		translateAxioms(axioms, positive);
		return finalizeAxioms();
	}

	public String renderEntity(OWLEntity entity) {
		return getShortForm(entity.toStringID());
	}

	/**
	 * Render the given unification problem.
	 * 
	 * @param input
	 *            a 'Goal' object representing the unification problem
	 * @return the rendered problem, with axioms, variables, etc.
	 */
	public AxiomsType renderGoal(Goal input) {
		initialize();
		translateAxioms(input.getDefinitions());
		translateAxioms(input.getEquations());
		translateAxioms(input.getSubsumptions());
		translateAxioms(input.getDisequations());
		translateAxioms(input.getDissubsumptions());
		newLine();
		translateAtomList("Variables", input.getAtomManager().getVariables());
		translateAtomList("User variables", input.getAtomManager().getUserVariables());
		translateAtomList("Constants", input.getAtomManager().getConstants());
		newLine();
		if (!input.getTypes().isEmpty()) {
			translateAtomList("Types", input.getTypes());
		}
		newLine();
		for (Integer type : input.getTypes()) {
			Integer supertype = input.getDirectSupertype(type);
			if (supertype != null) {
				translateAtomList("Direct supertype of " + renderName(type), Collections.singleton(supertype));
			}
		}
		newLine();
		for (Integer roleId : input.getDomains().keySet()) {
			translateAtomList("Domain of " + renderRole(roleId), input.getDomains().get(roleId));
		}
		newLine();
		for (Integer roleId : input.getRanges().keySet()) {
			translateAtomList("Range of " + renderRole(roleId), input.getRanges().get(roleId));
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
	 * @param identity
	 *            indicates whether identity assignments should be rendered
	 * @param typeInfo
	 *            indicates whether type information should be rendered
	 * @return the rendered unifier
	 */
	public AxiomsType renderUnifier(Unifier unifier, boolean identity, boolean typeInfo) {
		initialize();
		for (Definition definition : unifier.getDefinitions()) {
			if (!restrictToUserVariables || atomManager.getUserVariables().contains(definition.getDefiniendum())) {
				if (!identity || !definition.getRight().equals(Collections.singleton(definition.getDefiniendum()))) {
					translateAxiom(definition);
				}
			}
		}
		if (typeInfo) {
			if (unifier.getTypeAssignment() != null) {
				for (Integer atomId : unifier.getTypeAssignment().keySet()) {
					translateAtomList("Types of " + renderName(atomId), unifier.getTypeAssignment().get(atomId));
				}
			}
		}
		return finalizeAxioms();
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
	 * @return the rendered atom (for recursive translations)
	 */
	protected ExpressionType translateAtom(Integer atomId) {
		if (atomManager.getExistentialRestrictions().contains(atomId)) {
			Integer childId = atomManager.getChild(atomId);
			Integer roleId = atomManager.getExistentialRestriction(atomId).getRoleId();
			return translateExistentialRestriction(roleId, childId, this::translateRole, this::translateChild);
		} else {
			return translateName(atomId);
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
	protected abstract ExpressionType translateAtomList(String description, Set<Integer> atomIds);

	/**
	 * Add an axiom to the rendering under construction.
	 * 
	 * @param axiom
	 *            the axiom
	 * @return the rendered axiom (for recursive translations)
	 */
	protected abstract AxiomsType translateAxiom(Axiom axiom);

	protected abstract AxiomsType translateAxiom(OWLAxiom axiom, boolean positive);

	/**
	 * Add the given axioms to the rendering under construction.
	 * 
	 * @param axioms
	 *            a set of axioms
	 * @return the rendered axioms (for recursive translations)
	 */
	protected AxiomsType translateAxioms(Set<? extends Axiom> axioms) {
		AxiomsType ret = null;
		for (Axiom axiom : axioms) {
			ret = translateAxiom(axiom);
		}
		return ret;
	}

	protected AxiomsType translateAxioms(Set<OWLAxiom> axioms, boolean positive) {
		AxiomsType ret = null;
		for (OWLAxiom axiom : axioms) {
			ret = translateAxiom(axiom, positive);
		}
		return ret;
	}

	/**
	 * Add the filler of an existential restriction to the rendering under
	 * construction, possibly expanding a flattening variable.
	 * 
	 * @param childId
	 *            the atom id of the filler
	 * @return the rendered filler (for recursive translation)
	 */
	protected ExpressionType translateChild(Integer childId) {
		if (restrictToUserVariables && atomManager.getFlatteningVariables().contains(childId)) {
			return translateConjunction(getDefinition(childId));
		} else {
			return translateName(childId);
		}
	}

	protected abstract ExpressionType translateClass(OWLClass cls);

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

	protected ExpressionType translateConjunction(Set<Integer> conjuncts) {
		return translateConjunction(conjuncts, this::translateAtom);
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
	protected <T> ExpressionType translateConjunction(Set<T> conjuncts,
			Function<T, ExpressionType> conjunctTranslator) {
		if (conjuncts.isEmpty()) {
			return translateTop();
		} else if (conjuncts.size() == 1) {
			return conjunctTranslator.apply(conjuncts.iterator().next());
		} else {
			return translateTrueConjunction(conjuncts, conjunctTranslator);
		}
	}

	protected abstract <T, S> ExpressionType translateExistentialRestriction(T role, S filler,
			Function<T, RoleType> roleTranslator, Function<S, ExpressionType> fillerTranslator);

	protected abstract ExpressionType translateName(Integer atomId);

	protected abstract RoleType translateObjectProperty(OWLObjectProperty prop);

	protected abstract RoleType translateRole(Integer roleId);

	protected abstract ExpressionType translateRoleList(String description, Set<Integer> roleIds);

	protected abstract ExpressionType translateTop();

	protected abstract <T> ExpressionType translateTrueConjunction(Set<T> conjuncts,
			Function<T, ExpressionType> conjunctTranslator);

}
