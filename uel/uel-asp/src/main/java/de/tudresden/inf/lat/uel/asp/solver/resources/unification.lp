
%
% Unification in EL
%
% ASP encoding based on the SAT encoding in 
% F. Baader and B. Morawska: SAT Encoding of Unification in EL
%

atom(A) :- hasatom(A,_,_).
atom(A) :- hasatom(exists(_,A),_,_).

triviallyfalse(cname(X),cname(Y)) :- atom(cname(X)), atom(cname(Y)), X!=Y.

triviallyfalse(exists(R1,C1),exists(R2,C2)) :- atom(exists(R1,C1)),
                                               atom(exists(R2,C2)), R1!=R2. 

triviallyfalse(cname(C),exists(R,C1)) :- atom(cname(C)), atom(exists(R,C1)).
triviallyfalse(exists(R,C1),cname(C)) :- atom(cname(C)), atom(exists(R,C1)).

:- triviallyfalse(X,Y), subs(X,Y).

nonvaratom(X) :- atom(X), X!=var(V):atom(var(V)).

% corresponds to clauses (1) 1 and 2 
1 { subs(Y,X):hasatom(Y,|P-1|,E):not triviallyfalse(Y,X) } :- hasatom(X,P,E), nonvaratom(X).

% allow some subsumption propositions to be true, otherwise
% gringo will not ground some instances of the rule corresponding to clause (1) 3
% (because it will see a new atom in the body and the 
% simplification algorithm will set it to false)
%{ subs(X,C) } :- nonvaratom(C), hasatom(X,P,E), not hasatom(C,PX,E):hasatom(C,PX,E),
%                 not triviallyfalse(X,C).

% corresponds to clause (1) 3
1 { subs(Y,C):hasatom(Y,|P-1|,E):not triviallyfalse(Y,C) } :- subs(X,C), hasatom(X,P,E),
                  nonvaratom(C), not hasatom(C,PX,E):hasatom(C,PX,E).

% some subsumtion propositions occurring in clause (2) 3 do not occurr earlier
% this rule avoids simplification in grounding related to these propositions
%{ subs(C1,C2) } :- atom(exists(R,C1)), atom(exists(R,C2)), 
%                   not triviallyfalse(exists(R,C1),exist(R,C2)), C1!=C2.

% clause (2) 3
subs(X,Y) :- subs(exists(R,X),exists(R,Y)).

% clause (2) 5
subs(X,Z) :- subs(X,Y), subs(Y,Z), X!=Z, X!=Y, Z!=Y.

% generate greater atoms (clause (3) 2
greater(X,Y) :- subs(var(X),exists(R,var(Y))).

:- greater(X,X).
greater(X,Z) :- greater(X,Y), greater(Y,Z), X!=Y, X!=Z, Y!=Z.

%#minimize{ subs(var(V),C) : relevant(V) :  nonvaratom(C) }.

#hide.
#show subs(var(V),C) : relevant(V) : nonvaratom(C).
%#show greater/2.



