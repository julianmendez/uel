
% TODO: compute the compatibility relation only from the _background_ equations (definitions)

% Compatibility is ...

% ... reflexive
compatible(var(X),var(X),var(X)) :- atom(var(X)).

% ... symmetric
compatible(var(X),var(Y),var(Z)) :- compatible(var(Y),var(X),var(Z)).

% ... inherited along definitions (downward)
compatible(var(F),var(G),var(Z)) :- compatible(var(F),_,var(X)), compatible(var(G),_,var(Y)),
                                    hasatom(var(X),P,E), hasatom(var(Y),P,E),
                                    hasatom(var(Z),1-P,E), eq(E),
                                    not hasatom(W,1-P,E):hasatom(W,1-P,E),W!=Z.
compatible(var(X),var(Z),var(Z)) :- hasatom(var(X),P,E), hasatom(var(Z),1-P,E), eq(E),
                                    not hasatom(W,1-P,E):hasatom(W,1-P,E),W!=Z.




% TODO: role number restrictions?

% Two variables in the same substitution set must be compatible
:- subs(var(X),var(Y)), subs(var(X),var(Z)), not compatible(var(Y),var(Z),_).


% Type constraints

rgtype(rg(X)) :- type(X).

% role group types must be compatible
:- subtype(var(X),rg(S)), subtype(var(X),rg(T)), not compatible(S,T,_).

% a variable cannot have both a role group type and a normal type (except top)
:- subtype(var(X),rg(T)), subs(var(X),S), type(S), not top(S).

% domain restrictions, possibly with role group types
1 { subtype(var(X),T):domain(R,T),rgtype(T); subs(var(X),T):domain(R,T),type(T) } :-
        subs(var(X),exists(R,C)), 1 { domain(R,T) }.

% range restrictions (no role group types)
1 { subs(var(X),T):range(R,T) } :- atom(exists(R,var(X))), 1 { range(R,T) }.

% TODO: add 'type', 'top', 'domain', 'range' to the input encoding