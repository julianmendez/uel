package de.tudresden.inf.lat.uel.rule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.tudresden.inf.lat.uel.type.api.UelInput;
import de.tudresden.inf.lat.uel.type.api.UelOutput;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;

/**
 * This class is used to solve a unification problem using a rule-based unification algorithm for
 * EL.
 * 
 * This algorithm is described in:
 * Franz Baader, Stefan Borgwardt, and Barbara Morawska. 'Uniﬁcation in the description logic EL
 * w.r.t. cycle-restricted TBoxes'. LTCS-Report 11-05, Chair for Automata Theory, Institute for
 * Theoretical Computer Science, Technische Universität Dresden, Dresden, Germany, 2011.
 * See http://lat.inf.tu-dresden.de/research/reports.html.
 * 
 * Based on the algorithm in:
 * Franz Baader and Barbara Morawska. 'Uniﬁcation in the description logic EL'. Logical Methods in
 * Computer Science, 6(3), 2010. Special Issue: 20th Int. Conf. on Rewriting Techniques and
 * Applications (RTA’09).
 * 
 * @author Stefan Borgwardt
 */
public class RuleProcessor implements UelProcessor {

	private List<EagerRule> staticEagerRules;
	private List<EagerRule> dynamicEagerRules;
	private List<Rule> nondeterministicRules;
	
	private UelInput input;
	private Goal goal;
	private Assignment assignment;
	
	private Deque<Result> searchStack = null;
	
	/**
	 * Initialize a new unification problem with goal subsumptions. 
	 * @param input a UelInput object that will return the subsumptions to be solved
	 */
	public RuleProcessor(UelInput input) {
		this.goal = new Goal(input);
		this.input = input;
		assignment = new Assignment();
		
		for (Subsumption sub : goal) {
			if (sub.getHead().isVariable()) {
				// subsumptions with a variable on the right-hand side are always solved
				sub.setSolved(true);
			}
		}
		
		initRules();
	}
	
	public UelInput getInput() {
		return input;
	}

	/**
	 * Initialize the rule lists according to the rule-based algorithm for unification in EL w.r.t.
	 * the empty TBox.
	 */
	private void initRules() {
		staticEagerRules = new ArrayList<EagerRule>();
		staticEagerRules.add(new EagerGroundSolvingRule());
		staticEagerRules.add(new EagerSolving1Rule());
		dynamicEagerRules = new ArrayList<EagerRule>();
		dynamicEagerRules.add(new EagerSolving2Rule());
		dynamicEagerRules.add(new EagerExtensionRule());
		nondeterministicRules = new ArrayList<Rule>();
		nondeterministicRules.add(new DecompositionRule());
		nondeterministicRules.add(new ExtensionRule());
	}
	
	/**
	 * If at least one unifier has already been computed, this method tries to compute the next
	 * unifier. If there are no more unifiers, 'false' is returned.
	 * @return true iff the current assignment represents a unifier of the goal subsumptions
	 */
	public boolean computeNextUnifier() {
		if (searchStack == null) {
			searchStack = new ArrayDeque<Result>();
			
			// apply eager rules to each unsolved subsumption
			Result res = applyEagerRules(goal, staticEagerRules, null);
			if (!res.wasSuccessful()) return false;
			for (Subsumption sub : res.getSolvedSubsumptions()) {
				sub.setSolved(true);
			}
			Assignment tmp = new Assignment();
			res = applyEagerRules(goal, dynamicEagerRules, tmp);
			if (!res.wasSuccessful()) return false;
			if (!commitResult(res, tmp)) return false;
			
			// exhaustively apply eager rules to the result of this initial iteration
			applyEagerRules(res);
		} else {
			// we already have a search stack --> try to backtrack from last solution
			if (!backtrack()) {
				return false;
			}
		}
		return solve();
	}
	
	public UelOutput getUnifier() {
		return new RuleOutput(assignment, input.getAtomManager());
	}
	
	private boolean solve() {
		while(true) {
			Subsumption sub = chooseUnsolvedSubsumption();
			if (sub == null) return true;
			if (applyNextNondeterministicRule(sub, null)) continue;
			if (!backtrack()) return false;
		}
	}
	
	private boolean backtrack() {
		while (!searchStack.isEmpty()) {
			Result res = searchStack.pop();
			rollBackResult(res);
			if (applyNextNondeterministicRule(res.getSubsumption(), res.getApplication())) {
				return true;
			}
		}
		return false;
	}
	
	private Subsumption chooseUnsolvedSubsumption() {
		for (Subsumption sub : goal) {
			if (!sub.isSolved()) return sub;
		}
		return null;
	}

	private Result applyEagerRules(Collection<Subsumption> subs, List<EagerRule> rules, Assignment currentAssignment) {
		Result res = new Result(null, null);
		for (Subsumption sub : subs) {
			if (!sub.isSolved()) {
				for (Rule rule : rules) {
					Result r = tryApplyRule(sub, rule, null, currentAssignment);
					if (r == null) continue;
					if (!r.wasSuccessful()) return r;
					res.getSolvedSubsumptions().add(sub);
					res.getNewSubsumers().addAll(r.getNewSubsumers());
					if (currentAssignment != null) {
						currentAssignment.addAll(r.getNewSubsumers());
					}
					break;
				}
			}
		}
		return res;
	}
	
	private boolean applyNextNondeterministicRule(Subsumption sub, Rule.Application previous) {
		Iterator<Rule> iter = nondeterministicRules.listIterator(
				(previous == null) ? 0 : nondeterministicRules.indexOf(previous.rule()));
		
		while (iter.hasNext()) {
			Rule rule = iter.next();
			while(true) {
				Result res = tryApplyRule(sub, rule, previous, assignment);
				if (res == null) break;
				previous = res.getApplication();
				if (!res.wasSuccessful()) continue;
				
				// now 'res' is the result of a successful nondeterministic rule application ->
				// apply eager rules, put result on the stack
				if (!commitResult(res, null)) {
					// application of static eager rules failed -> roll back changes and continue search
					rollBackResult(res);
					continue;
				}
				if (!applyEagerRules(res)) {
					// exhaustive application of eager rules failed
					rollBackResult(res);
					continue;
				}
				searchStack.push(res);
				return true;
			}
			previous = null;
		}
		return false;
	}

	/**
	 * Exhaustively apply all applicable eager rules to the goal subsumptions.
	 * 
	 * @param parent the previous result of a nondeterministic rule application to which the
	 *               results of the eager rule applications should be added; if it is 'null', then
	 *               no results are stored
	 * @return true iff all rule applications were successful
	 */
	private boolean applyEagerRules(Result parent) {
		Result currentResult = parent;
		Result nextResult = new Result(null, null);
		Assignment tmp = new Assignment(assignment);
		
		do {
			
			// apply dynamic eager rules to each new unsolved subsumption
			{
				Result res = applyEagerRules(currentResult.getNewUnsolvedSubsumptions(), dynamicEagerRules, tmp);
				if (!res.wasSuccessful()) return false;
				nextResult.getSolvedSubsumptions().addAll(res.getSolvedSubsumptions());
				nextResult.getNewSubsumers().addAll(res.getNewSubsumers());
			}
			
			// apply dynamic eager rules for each new assignment
			Assignment newSubsumers = currentResult.getNewSubsumers(); 
			for (Integer var : newSubsumers.getKeys()) {
				if (!newSubsumers.getSubsumers(var).isEmpty()) {
					Result res = applyEagerRules(goal.getSubsumptionsByLHSVariable(var), dynamicEagerRules, tmp);
					if (!res.wasSuccessful()) return false;
					nextResult.getSolvedSubsumptions().addAll(res.getSolvedSubsumptions());
					nextResult.getNewSubsumers().addAll(res.getNewSubsumers());
				}
			}
			
			boolean commitSuccessful = commitResult(nextResult, tmp); 
			parent.amend(nextResult);
			if (!commitSuccessful) return false;
			
			currentResult = nextResult;
			nextResult = new Result(null, null);
			tmp = new Assignment(assignment);
		} while (!currentResult.getNewSubsumers().isEmpty() || !currentResult.getNewUnsolvedSubsumptions().isEmpty());
		
		return true;
	}
	
	/**
	 * Try to apply a rule to a given subsumption.
	 * 
	 * @param rule the rule to be applied
	 * @param sub the considered subsumption
	 * @param previous the previous result or 'null' if this is the first try
	 * @return the result of the rule application or 'null' if no more rule applications are
	 *         possible
	 */
	private Result tryApplyRule(Subsumption sub, Rule rule, Rule.Application previous, Assignment currentAssignment) {
		Rule.Application next;
		if (previous == null) {
			next = rule.getFirstApplication(sub, currentAssignment);
		} else {
			next = rule.getNextApplication(sub, currentAssignment, previous);
		}
		if (next == null) return null;
		
		Result res = rule.apply(sub, currentAssignment, next);
		return res;
	}
	
	/**
	 * Adds the new unsolved subsumptions resulting from a rule application to the current goal and
	 * also applies the changes to the current assignment. In the process, the result is changed to
	 * reflect the exact changes that are made. For example, a created subsumption that is already
	 * in the goal is removed from the result. Additionally, the result of goal expansion is added
	 * to the result.
	 * 
	 * @param res the result to be considered; will be changed in-place
	 * @param newAssignment the new assignment that will replace the current assignment; if this is
	 * 'null', then the change will be computed from 'res.getNewSubsumers()'
	 */
	private boolean commitResult(Result res, Assignment newAssignment) {
		// solve subsumption that triggered the rule
		if (res.getSubsumption() != null) {
			res.getSubsumption().setSolved(true);
		}
		
		// add new unsolved subsumptions to the goal
		res.getNewUnsolvedSubsumptions().removeAll(goal);
		goal.addAll(res.getNewUnsolvedSubsumptions());
		for (Subsumption sub : res.getNewUnsolvedSubsumptions()) {
			if (sub.getHead().isVariable()) {
				// subsumptions with a variable on the right-hand side are always solved
				sub.setSolved(true);
				res.getNewSolvedSubsumptions().add(sub);
			}
		}
		res.getNewUnsolvedSubsumptions().removeAll(res.getNewSolvedSubsumptions());
		
//		{
//			Iterator<Subsumption> iter = res.getNewUnsolvedSubsumptions().iterator();
//			while (iter.hasNext()) {
//				Subsumption newUnsolvedSub = iter.next();
//				if (!goal.add(newUnsolvedSub)) {
//					// if 'newUnsolvedSub' is already in the goal, we can ignore it
//					iter.remove();
//				} else { 
//					if (newUnsolvedSub.getHead().isVariable()) {
//						// subsumptions with a variable on the right-hand side are always solved
//						iter.remove();
//						newUnsolvedSub.setSolved(true);
//						res.getNewSolvedSubsumptions().add(newUnsolvedSub);
//					}
//				}
//			}
//		}

		// goal expansion (I)
		for (Subsumption sub : res.getNewSolvedSubsumptions()) {
			/* we can assume that all new solved subsumptions have a variable on the right-hand
			 * side
			 */
			Integer var = sub.getHead().getConceptName();
			Set<Subsumption> newSubs = goal.expand(sub, assignment.getSubsumers(var));
			res.getNewUnsolvedSubsumptions().addAll(newSubs);
		}
		
		// solve subsumptions in 'res.solvedSubsumptions'
		for (Subsumption sub : res.getSolvedSubsumptions()) {
			sub.setSolved(true);
		}
		
		// update current assignment
		res.getNewSubsumers().removeAll(assignment);
		if (newAssignment == null) {
			assignment.addAll(res.getNewSubsumers());
		} else {
			assignment = newAssignment;
		}
		
		// goal expansion (II)
		Set<Subsumption> newSubs = goal.expand(res.getNewSubsumers());
		res.getNewUnsolvedSubsumptions().addAll(newSubs);
		
//		Assignment newSubsumers = res.getNewSubsumers();
//		if (!newSubsumers.isEmpty()) {
//			// add new subsumers to the current assignment
//			for (Integer var : newSubsumers.getKeys()) {
//				Iterator<FlatAtom> iter = newSubsumers.getSubsumers(var).iterator();
//				while (iter.hasNext()) {
//					FlatAtom newAtom = iter.next();
//					if (!assignment.add(var, newAtom)) {
//						// if 'newAtom' is already in the assignment, we can ignore it
//						iter.remove();
//					}
//				}
//				
//				// goal expansion (II)
//				Set<Subsumption> newSubs = goal.expand(var, newSubsumers.getSubsumers(var));
//				res.getNewUnsolvedSubsumptions().addAll(newSubs);
//			}
//			
//			/* check assignment for cycles again since this result might have come from a bulk
//			 * application of eager rules
//			 */
//			if (newSubsumers.getDomain().size() > 1) {
//				if (assignment.isCyclic()) return false;
//			}
//		}
		
		// try to solve new unsolved subsumptions by static eager rules
		Result eagerRes = applyEagerRules(res.getNewUnsolvedSubsumptions(), staticEagerRules, null);
		if (!eagerRes.wasSuccessful()) return false;
		for (Subsumption sub : eagerRes.getSolvedSubsumptions()) {
			sub.setSolved(true);
		}
		res.amend(eagerRes);
		return true;
	}
	
	/**
	 * Undo the changes made to the goal by a result.
	 * 
	 * @param res the result to undo
	 */
	private void rollBackResult(Result res) {
		
		assignment.removeAll(res.getNewSubsumers());
		goal.removeAll(res.getNewSolvedSubsumptions());
		goal.removeAll(res.getNewUnsolvedSubsumptions());
		
		for (Subsumption sub : res.getSolvedSubsumptions()) {
			sub.setSolved(false);
		}
		
		res.getSubsumption().setSolved(false);
	}
	
}
