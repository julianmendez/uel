
%
% Unification in EL
%
% ASP encoding based on the SAT encoding in 
% F. Baader and B. Morawska: SAT Encoding of Unification in EL
%

% definitions
atom(A) :- eqhasatom(A,_,_).
atom(A) :- eqhasatom(exists(_,A),_,_).
nonvaratom(X) :- atom(X), X!=var(V):atom(var(V)).


% (VI) b
triviallyfalse(cname(X),cname(Y)) :- atom(cname(X)), atom(cname(Y)), X!=Y.

% (VI) c
triviallyfalse(exists(R1,C1),exists(R2,C2)) :- atom(exists(R1,C1)),
                                               atom(exists(R2,C2)), R1!=R2. 

% (VI) d
triviallyfalse(cname(C),exists(R,C1)) :- atom(cname(C)), atom(exists(R,C1)).
triviallyfalse(exists(R,C1),cname(C)) :- atom(cname(C)), atom(exists(R,C1)).

% (VI) e/1
subs(X,Y) :- subs(exists(R,X),exists(R,Y)).


% (I) a
1 { subs(Y,X):eqhasatom(Y,|P-1|,E),not triviallyfalse(Y,X) } :- eqhasatom(X,P,E), nonvaratom(X).

{ subs(X,C) } :- nonvaratom(C), eqhasatom(X,P,E), not eqhasatom(C,PX,E):eqhasatom(C,PX,E),
                 not triviallyfalse(X,C).

% (I) b
1 { subs(Y,C):eqhasatom(Y,|P-1|,E),not triviallyfalse(Y,C) } :- subs(X,C), eqhasatom(X,P,E),
                  nonvaratom(C), not eqhasatom(C,PX,E):eqhasatom(C,PX,E).


% (VII)
subs(X,Z) :- subs(X,Y), subs(Y,Z).

:- subs(X,Y), triviallyfalse(X,Y).


% (IX) c
greater(X,Y) :- subs(var(X),exists(R,var(Y))).

% (IX) a
:- greater(X,X).

% (IX) b
greater(X,Z) :- greater(X,Y), greater(Y,Z).
