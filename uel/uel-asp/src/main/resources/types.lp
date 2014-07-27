
%
% Type Constraints
%

% (III) a,b
1 { hastype(X,T):type(T) } 1 :- atom(X).

% (III) c
hastype(cname(A),cname(A)) :- atom(cname(A)), type(cname(A)).

% (III) d
hastype(var(X),T) :- atom(var(X)), nonvaratom(D), type(T), subs(var(X),D), hastype(D,T).

% (IV)
1 { hastype(exists(R,C),T):domain(R,T) } 1 :- atom(exists(R,C)).

% (V)
1 { hastype(C,T):range(R,T) } 1 :- atom(exists(R,C)).
