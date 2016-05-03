package wff.lts;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import parse.Parse;
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
	private static Map<Integer,LTS> variablesLTS=null;
	private static LinkLTS wffRootLink=new LinkLTS(new LTS());
	private static LinkLTS termRootLink=new LinkLTS(new LTS());

	public static LTS toLTS(UnifiableFormulaElement thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof WFF) {
				return wffToLTS((WFF)thing);
			} else if (thing instanceof Term) {
				return termToLTS((Term)thing);
			} else throw new Exception("invalid option.");
		}
		return null;
	}
	
	private static LTS termToLTS(Term thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof Variable) {
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
				LTS n=constantsLTS.get(name);
				if (n==null) constantsLTS.put(name,n=new LTS());
				List<? extends UnifiableFormulaElement> args = thing.getArguments();
				Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
				return toLTS(wffRootLink.get(n),it);
			} else if (thing instanceof Implication) {
				
			} else if (thing instanceof Conjunction) {
				
			} else throw new Exception("invalid option.");
		}
		return null;
	}

	private static LTS toLTS(LinkLTS n, Iterator<? extends UnifiableFormulaElement> it) throws Exception {
		if (n!=null) {
			if (it==null || !it.hasNext()) return n.getElement();
			else {
				UnifiableFormulaElement thing = it.next();
				LTS aLTS=toLTS(thing);
				LinkLTS nextLink = n.get(aLTS);
				return toLTS(nextLink, it);
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		List<WFF> f=Parse.parse("(test 0.1 ?a ?b BILL)");
		long fl=toLTS(f.get(0));
	}

}
