
EL-Unifier Help File: readme.txt

USAGE: java -jar Unifier.jar [-options] <input file> [<ontology>]
USAGE: java -jar Unifier.jar -h 


Option is optional.
Input file should be in KRSS format. It should contain EL-definitions of two concepts  to be unified.
The definitions can contain variables. Variables are any concepts containing "VAR".

Example:

(define-concept Father (AND Man (SOME Has-child TOP)))
(define-concept Ancestor (AND Man (SOME Has-child VAR)))

If the program is called without options, the output is UNIFIABLE or UNUNIFIABLE printed to the stdout. No output file is created.
The third argument is optional. It should be a file with a set of definitions, a TBox. TBox should be non-cyclic.
If this is provided, the program performs EL unification modulo the definitions.  

OPTIONS.
If a letter t is appended to some options (at the end of the options string), an additional file <input file>.TBox is created. This file is necessary if you want
to check the result with Tester.

-h (or -H)         Prints the help file to the screen.

-w[t]				The output which is UNUNIFIABLE or UNIFIABLE and then a computed unifier,
					is written to the file <input file>.unif.

-a[t]				   If the goal is unifiable, then it is written to the file <input file>.unif.
				   The program then asks if next unifier should be computed. If "Y", then it tries to obtain another unifier.
				   If this is found, the unifier is appended to the file <input file>.unif.
				   The question is then repeated.
				   
-<i>[t]		   		Outputs UNIFIABLE or UNUNIFIABLE to stdout.
					Computes up to <i> unifiers, where i is an integer. The unifiers are written to the file
				  	 <input file>.unif. 
				   
-x[t]            Computes all local unifiers and outputs them to the file   <input file>.unif. This terminates, but can require a long time and
				can create a big output file.

-n			  Outputs UNIFIABLE or UNUNIFIABLE to stdout, and computes the number of all unifiers.


