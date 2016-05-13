package de.tudresden.inf.lat.uel.plugin.ui;

/**
 * This interface provides messages for the user interface.
 * 
 * @author Julian Mendez
 */
interface Message {

	String buttonAcceptVar = ">";
	String buttonFirst = "|<";
	String buttonLast = ">|";
	String buttonMakeCons = "<<<";
	String buttonMakeVar = ">>>";
	String buttonNext = ">";
	String buttonOpen = "Open";
	String buttonPrevious = "<";
	String buttonRejectVar = "<";
	String buttonSave = "Save";
	String buttonSaveGoal = "Save";
	String buttonSelectVariables = ">";
	String buttonShowStatInfo = "Stat";
	String textConstants = "Constants:";
	String titleError = "Error";
	String textExpandPrimitiveDefinitions = "Expand primitive definitions";
	String textOntologyBg00 = "First background ontology:";
	String textOntologyBg01 = "Second background ontology:";
	String textOntologyPos = "Goal subsumptions and equations:";
	String textOntologyNeg = "Goal dissubsumptions and disequations:";
	String textRefineExplanation = "Select the atoms that should not subsume the respective variable:";
	String textSnomedMode = "Load SNOMED type information";
	String textVariables = "Variables:";
	String tooltipAcceptVar = "use the selected entities as variables";
	String tooltipComboBoxOntologyBg00 = "select first background ontology";
	String tooltipComboBoxOntologyBg01 = "select second background ontology";
	String tooltipComboBoxOntologyPos = "select ontology containing subsumptions and equations";
	String tooltipComboBoxOntologyNeg = "select ontology containing dissubsumptions and disequations";
	String tooltipConstants = "constants";
	String tooltipFirst = "shows the first unifier";
	String tooltipGoal = "goal";
	String tooltipLast = "computes all unifiers and shows the last unifier";
	String tooltipMakeCons = "defines the selected items as constants";
	String tooltipMakeVar = "defines the selected items as variables";
	String tooltipNext = "shows the next unifier, it unifies the chosen class names using the selected variables";
	String tooltipOpen = "opens an ontology";
	String tooltipPrevious = "shows the previous unifier";
	String tooltipRecompute = "extend the negative ontology and recompute unifiers";
	String tooltipRefine = "refine unifier using dissubsumptions";
	String tooltipRefineAtoms = "choose atoms that should not subsume the variable";
	String tooltipRejectVar = "cancels this selection of variables";
	String tooltipSave = "saves the current unifier into a file";
	String tooltipSaveDissub = "save extended negative ontology and recompute unifiers";
	String tooltipSaveGoal = "saves the goal to a file";
	String tooltipSelectAlgorithm = "selects the unification algorithm";
	String tooltipSelectVariables = "gets candidates for variables";
	String tooltipShowStatInfo = "shows additional information about the computation";
	String tooltipSubsumedByTop = "no subsumers";
	String tooltipTextInfo = "statistical information about the unification algorithm";
	String tooltipUndoRefine = "undo last refinement operation";
	String tooltipUnifier = "unifier";
	String tooltipUnifierId = "unifier id";
	String tooltipVariableName = "subsumed variable";
	String tooltipVariables = "variables";

}
