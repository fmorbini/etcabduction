package wff.lts;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import parse.Parse;
import unify.Unify;
import wff.Conjunction;
import wff.Constant;
import wff.Function;
import wff.Implication;
import wff.Predication;
import wff.Term;
import wff.UnifiableFormulaElement;
import wff.Variable;
import wff.WFF;

public class LTSConverter {
	
	private static Map<String,LTS> constantsLTS=null;
	private static LTS variableLTS=new LTS();
	private static LinkLTS wffRootLink=new LinkLTS(new LTS());
	private static LinkLTS termRootLink=new LinkLTS(new LTS());

	public static LTS toLTS(UnifiableFormulaElement thing) throws Exception {
		List<Variable> vars=getAllVariableUses(thing);
		LTS lts=toLTS(thing);
		lts.setVariableAssignment(vars);
		return lts;
	}

	private static List<Variable> getAllVariableUses(UnifiableFormulaElement thing) {
		List<Variable> ret=null;
		Deque<UnifiableFormulaElement> s=new ArrayDeque<>();
		s.push(thing);
		while(!s.isEmpty()) {
			UnifiableFormulaElement t=s.pop();
			if (t!=null) {
				List<? extends UnifiableFormulaElement> args = t.getArguments();
				if (args!=null) for(UnifiableFormulaElement a:args) s.push(a);
				else if (Unify.isVariable(t)) {
					if (ret==null) ret=new ArrayList<>();
					ret.add((Variable) t);
				}
			}
		}
		return ret;
	}

	private static LTS toLTSinternal(UnifiableFormulaElement thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof WFF) {
				WFF wff=(WFF)thing;
				return wffToLTS(wff);
			} else if (thing instanceof Term) {
				Term term=(Term)thing;
				return termToLTS(term);
			} else throw new Exception("invalid option.");
		}
		return null;
	}
	
	private static LTS termToLTS(Term thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof Variable) {
				return variableLTS;
			} else if (thing instanceof Constant) {
				String name=((Constant) thing).getName();
				LTS n=constantsLTS.get(name);
				return n;
			} else if (thing instanceof Function) {
				String name=((Function) thing).getName();
				LTS n=constantsLTS.get(name);
				if (n==null) constantsLTS.put(name,n=new LTS());
				List<? extends UnifiableFormulaElement> args = thing.getArguments();
				Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
				return toLTS(termRootLink.get(n),it);
			}
		}
		return null;
	}
	
	private static LTS wffToLTS(WFF thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof Predication) {
				String name=((Predication) thing).getName();
				return namedOperatorWithArgumentsToLTS(name,thing.getArguments());
			} else if (thing instanceof Implication) {
				String name="if";
				return namedOperatorWithArgumentsToLTS(name,thing.getArguments());
			} else if (thing instanceof Conjunction) {
				String name="and";
				return namedOperatorWithArgumentsToLTS(name,thing.getArguments());
			} else throw new Exception("invalid option.");
		}
		return null;
	}

	private static LTS namedOperatorWithArgumentsToLTS(String name, List<? extends UnifiableFormulaElement> args) throws Exception {
		LTS n=constantsLTS.get(name);
		if (n==null) constantsLTS.put(name,n=new LTS());
		Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
		return toLTS(wffRootLink.get(n),it);
	}

	private static LTS toLTS(LinkLTS n, Iterator<? extends UnifiableFormulaElement> it) throws Exception {
		if (n!=null) {
			if (it==null || !it.hasNext()) return n.getElement();
			else {
				UnifiableFormulaElement thing = it.next();
				LTS aLTS=toLTSinternal(thing);
				LinkLTS nextLink = n.get(aLTS);
				return toLTS(nextLink, it);
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		List<WFF> f=Parse.parse("(test 0.1 ?a ?b BILL)");
		LTS fl=toLTS(f.get(0));
		System.out.println(1);
	}

}
