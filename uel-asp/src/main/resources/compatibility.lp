
%
% Type and compatibility restrictions for UEL
%
%

%% Compatibility is ...
%
%% ... reflexive
%compatible(var(X),var(X),var(X)) :- atom(var(X)).
%
%% ... symmetric
%compatible(var(X),var(Y),var(Z)) :- compatible(var(Y),var(X),var(Z)).
%
%% ... inherited along definitions (downward)
%compatible(var(F),var(G),var(Z)) :- compatible(var(F),_,var(X)), compatible(var(G),_,var(Y)),
%                                    hasatom(var(X),P,E), hasatom(var(Y),P,E),
%                                    hasatom(var(Z),1-P,E), eq(E).
%%                                    not hasatom(W,1-P,E):hasatom(W,1-P,E),W!=var(Z).
%compatible(var(X),var(Z),var(Z)) :- hasatom(var(X),P,E), hasatom(var(Z),1-P,E), eq(E),
%                                    not hasatom(W,1-P,E):hasatom(W,1-P,E),W!=var(Z).
%
%compatible(X,Y) :- compatible(X,Y,_).

% TODO: arbitrary role number restrictions
related(C,D) :-
        number(R,1), subs(var(X),exists(R,C)), subs(var(X),exists(R,D)), C!=D.
related(C,D) | related(C,E) | related(D,E) :-
        number(R,2), subs(var(X),exists(R,C)), subs(var(X),exists(R,D)), subs(var(X),exists(R,E)), C!=D, D!=E, C!=E.
related(C,D) | related(C,E) | related(D,E) | related(C,F) | related(D,F) | related(E,F) :-
        number(R,3), subs(var(X),exists(R,C)), subs(var(X),exists(R,D)), subs(var(X),exists(R,E)),
        subs(var(X),exists(R,F)), C!=D, D!=E, C!=E, E!=F, D!=F, C!=F.
:- related(C,D), not compatible(C,D).
subs(C,D) | subs(D,C) :- related(C,D).


%:- number(R,1), subs(var(X),exists(R,C)), subs(var(X),exists(R,D)),
%        not subs(C,D):compatible(C,D); not subs(D,C):compatible(D,C).
%:- number(R,2), subs(var(X),exists(R,C)), subs(var(X),exists(R,D)), subs(var(X),exists(R,E)),
%        not subs(C,D):compatible(C,D); not subs(D,C):compatible(D,C); not subs(C,E):compatible(C,E);
%        not subs(E,C):compatible(E,C); not subs(D,E):compatible(D,E); not subs(E,D):compatible(E,D).
%{ subs(var(C),var(D)) } :- subs(var(X),exists(R,var(C))), subs(var(X),exists(R,var(D))), C!=D.


% Two variables in the same substitution set must be compatible
:- subs(var(X),var(Y)), subs(var(X),var(Z)), not compatible(var(Y),var(Z)).


% UNDEF context restrictions
subs(var(X),Y) :- subs(var(X),U), undef(Y,U).

% Type constraints

rgtype(rg(X)) :- type(X).

% role group types must be compatible
:- subtype(var(X),rg(S)), subtype(var(X),rg(T)), not compatible(S,T).

% a variable cannot have both a role group type and a normal type
:- subtype(var(X),rg(T)), subs(var(X),S), type(S).

% domain restrictions, possibly with role group types
1 { subtype(var(X),T):domain(R,T),rgtype(T); subs(var(X),T):domain(R,T),type(T) } :-
        subs(var(X),exists(R,C)), 1 { domain(R,T) }.

% range restrictions (no role group types)
1 { subs(var(X),T):range(R,T) } :- atom(exists(R,var(X))), 1 { range(R,T) }.

% 'RoleGroup' translates between role group types and normal types
subs(var(X),T) :- rolegroup(R), subs(var(X),exists(R,C)), rgtype(rg(T)), subtype(C,rg(T)).

% variables inside 'RoleGroup' must have a role group type
1 { subtype(var(X),T):rgtype(T) } :- rolegroup(R), atom(exists(R,var(X))).
