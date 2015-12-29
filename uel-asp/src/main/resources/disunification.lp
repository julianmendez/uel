
%
% Local Disunification
%

% definitions
atom(A) :- diseqhasatom(A,_,_).
atom(A) :- diseqhasatom(exists(_,A),_,_).

{ lefttoright(E) } :- diseqhasatom(X,P,E).

1 { chosen(Y,E):diseqhasatom(Y,1,E) } :- diseqhasatom(_,_,E), lefttoright(E).
1 { chosen(Y,E):diseqhasatom(Y,0,E) } :- diseqhasatom(_,_,E), not lefttoright(E).

dissubs(X,Y) :- diseqhasatom(X,0,E), diseqhasatom(Y,1,E), chosen(Y,E), lefttoright(E).
dissubs(X,Y) :- diseqhasatom(X,1,E), diseqhasatom(Y,0,E), chosen(Y,E), not lefttoright(E).



% (VI) a
subs(cname(X),cname(X)) :- atom(cname(X)).

% (VI) e/2
subs(exists(R,X),exists(R,Y)) :- subs(X,Y), atom(exists(R,X)), atom(exists(R,Y)).

:- dissubs(C,D), subs(C,D).



%dissubs(C,D) :- triviallyfalse(C,D).

% "transitivity" for dissubs
%1 { dissubs(C,E);dissubs(E,D) } :- dissubs(C,D), atom(E).

% (II)
%:- diseq(C,D), subs(C,D), subs(D,C).
%1 { dissubs(C,D);dissubs(D,C) } :- diseq(C,D).

% (VIII)
%1 { counterexample(C,var(X),D):nonvaratom(D) } :- atom(C), atom(var(X)), dissubs(C,var(X)).
%subs(var(X),D) :- counterexample(C,var(X),D).
%dissubs(C,D) :- counterexample(C,var(X),D).



ok(C,D) :- dissubs(C,D), subs(D,D'), nonvaratom(D'), not subs(C,D'), not subs(C',D'):subs(C,C').
:- dissubs(C,D), not ok(C,D).

