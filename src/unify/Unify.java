package unify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import parse.Generator;
import wff.Conjunction;
import wff.Constant;
import wff.Function;
import wff.Implication;
import wff.Predication;
import wff.Term;
import wff.UnifiableFormulaElement;
import wff.Variable;
import wff.WFF;

public class Unify {
	public static boolean isVariable(UnifiableFormulaElement v) {
		return (v!=null && v instanceof Variable);
	}
	
	public static Map<Variable, Term> robinson(UnifiableFormulaElement x,UnifiableFormulaElement y) {
		return robinson(x,y,null);
	}
	public static Map<Variable, Term> robinson(UnifiableFormulaElement x,UnifiableFormulaElement y,Map<Variable,Term> theta) {
		x=subst(x,theta);
		y=subst(y,theta);
		if (!x.equals(y)) {
			if (isVariable(x)) {
				if (isVariable(y)) {
					if (theta==null) theta=new HashMap<>();
					if (((Variable) x).compareTo((Variable) y)<0) {
						theta.put((Variable)x, (Term) y);
					} else {
						theta.put((Variable)y, (Term)x);
					}
				} else if (occurCheck((Variable) x,y,theta)) {
					if (theta==null) theta=new HashMap<>();
					theta.put((Variable)x, (Term) y);
				} else return null;
			} else if (isVariable(y) && occurCheck((Variable) y, x, theta)) {
				if (theta==null) theta=new HashMap<>();
				theta.put((Variable)y, (Term)x);
			} else if (x instanceof Predication && y instanceof Predication) {
				String predX=((Predication)x).getPredicate();
				String predY=((Predication)y).getPredicate();
				if (predX.equals(predY) && ((Predication)x).getArgCount()>0 && ((Predication)x).getArgCount()==((Predication)y).getArgCount()) {
					Iterator<? extends UnifiableFormulaElement> itX=x.getArguments().iterator();
					Iterator<? extends UnifiableFormulaElement> itY=y.getArguments().iterator();
					while(itX.hasNext() && itY.hasNext()) {
						UnifiableFormulaElement a=itX.next(),b=itY.next();
						theta=robinson(a, b, theta);
						if (theta==null) return null;
					}
				}
			} else return null;
		}
		return theta;
	}
	
	private static boolean occurCheck(Variable var, UnifiableFormulaElement y, Map<Variable, Term> theta) {
		if (y!=null && var!=null) {
			Stack<UnifiableFormulaElement> terms=new Stack<>();
			terms.add(y);
			while(!terms.isEmpty()) {
				UnifiableFormulaElement thing=terms.pop();
				if (thing!=null) {
					if (thing instanceof Function) {
						terms.addAll(((Function)y).getArguments());
					} else if (thing instanceof Variable) {
						if (((Variable)thing).equals(var)) {
							return false;
						}
					} else if (thing instanceof Constant) {
					} else System.err.println("unknown options.");
				}
			}
		}
		return true;
	}

	public static UnifiableFormulaElement subst(UnifiableFormulaElement thing,Map<Variable,Term> theta) {
		if (thing!=null && theta!=null) {
			if (thing instanceof Variable) {
				if (theta.containsKey(thing)) {
					return theta.get(thing);
				}
			} else if (thing instanceof Function) {
				Function ret=(Function) thing;
				List<Term> as = ((Function)thing).getArguments(),nas=null;
				if (as!=null) {
					boolean changed=false;
					int i=0;
					for(Term t:as) {
						Term nt=(Term) subst(t,theta);
						if (nt!=t) {
							if (!changed) {
								ret=new Function(ret.getName());
								ret.setArguments(nas=new ArrayList<>());
								if (i-1>0) nas.addAll(as.subList(0, i));
								changed=true;
							}
							nas.add(nt);
						} else if (changed) {
							nas.add(nt);
						}
						i++;
					}
				}
				return ret;
			} else if (thing instanceof Predication) {
				Predication ret=(Predication) thing;
				List<Term> as = ((Predication)thing).getArguments(),nas=null;
				if (as!=null) {
					boolean changed=false;
					int i=0;
					for(Term t:as) {
						Term nt=(Term) subst(t,theta);
						if (nt!=t) {
							if (!changed) {
								ret=new Predication(ret.getName());
								ret.setArguments(nas=new ArrayList<>());
								if (i-1>0) nas.addAll(as.subList(0, i));
								changed=true;
							}
							nas.add(nt);
						} else if (changed) {
							nas.add(nt);
						}
						i++;
					}
				}
				return ret;
			} else if (thing instanceof Implication) {
				UnifiableFormulaElement ret=thing;
				WFF a=((Implication)thing).getAntecedent();
				WFF c=((Implication)thing).getConsequent();
				if (a!=null && c!=null) {
					WFF na=(WFF) Unify.subst(a, theta);
					WFF nc=(WFF) Unify.subst(c, theta);
					if (na!=a || nc!=c) {
						ret=new Implication(na, (Predication) nc);
					}
				}
				return ret;
			} else if (thing instanceof Conjunction) {
				Conjunction ret=(Conjunction) thing;
				List<WFF> as = ((Conjunction)thing).getArguments(),nas=null;
				if (as!=null) {
					boolean changed=false;
					int i=0;
					for(WFF t:as) {
						WFF nt=(WFF) subst(t,theta);
						if (nt!=t) {
							if (!changed) {
								ret=new Conjunction();
								ret.setConjuncts(nas=new ArrayList<>());
								if (i-1>0) nas.addAll(as.subList(0, i));
								changed=true;
							}
							nas.add(nt);
						} else if (changed) {
							nas.add(nt);
						}
						i++;
					}
				}
				return ret;
			}
		}
		return thing;
	}
		
	public static Map<Variable,Term> unify(UnifiableFormulaElement x,UnifiableFormulaElement y) {
		return robinson(x, y);
	}
	
	public static Set<Variable> allVariables(UnifiableFormulaElement sexp) {
		Set<Variable> ret=null;
		Stack<UnifiableFormulaElement> s=new Stack<>();
		s.push(sexp);
		while(!s.isEmpty()) {
			UnifiableFormulaElement t=s.pop();
			if (t!=null) {
				List<? extends UnifiableFormulaElement> args = t.getArguments();
				if (args!=null) s.addAll(args);
				else if (isVariable(t)) {
					if (ret==null) ret=new HashSet<>();
					ret.add((Variable) t);
				}
			}
		}
		return ret;
	}
	
	public static Object skolem(UnifiableFormulaElement sexp) {
		Set<Variable> vars=allVariables(sexp);
		Map<Variable,Term> unif=null;
		if (vars!=null) {
			for(Variable v:vars) {
				if (unif==null) unif=new HashMap<>();
				unif.put(v, Generator.skolemGenerator.next());
			}
		}
		if (unif!=null) return Unify.subst(sexp, unif);
		else return sexp;
	}
	
}
