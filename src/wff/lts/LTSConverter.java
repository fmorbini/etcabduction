package wff.lts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import wff.lts.link.BaseLTS;
import wff.lts.link.LinkLTS;

public class LTSConverter {
	
	private static final ConstantLTSRepo constantsLTS=new ConstantLTSRepo();
	
	public static final LinkLTS BasicWFF=new LinkLTS(null, new BaseLTS("WFF"));
	public static final LinkLTS ConjunctionWFF=new LinkLTS(null, new BaseLTS("AND"));
	public static final LinkLTS ImplicationWFF=new LinkLTS(null, new BaseLTS("=>"));
	public static final LinkLTS FunctionTerm=new LinkLTS(null, new BaseLTS("FUN"));
	public static final LinkLTS VariableTerm=new LinkLTS(null,new BaseLTS("VAR"));
	public static final LinkLTS ConstantTerm=new LinkLTS(null,new BaseLTS("CNST"));
	public static final LinkLTS OpName=new LinkLTS(null,new BaseLTS("NAME"));
	public enum Type {WFF,AND,IMPLY,CNST,VAR,FUNC,NAME};

	public static LinkLTS toLTS(UnifiableFormulaElement thing) throws Exception {
		List<Variable> vars=getAllArgs(thing);
		LinkLTS lts=toLTSinternal(thing);
		lts.setVariableAssignment(vars);
		return lts;
	}
	
	public static UnifiableFormulaElement fromLTS(LinkLTS link) {
		LinkLTS p=link;
		Type type=p.getType();
		LinkedList<UnifiableFormulaElement> args=null;
		UnifiableFormulaElement ret=null;
		int[] vars=null;
		switch (type) {
		case WFF:
		case FUNC:
			args=getArgumentsfromLTS(p);
			if (args!=null) {
				UnifiableFormulaElement name = args.pop();
				if (type==Type.WFF) {
					ret = new Predication(((Constant)name).getName());
					((Predication) ret).setArguments((List)args);
				} else {
					ret=new Function(((Constant)name).getName());
					((Function) ret).setArguments((List)args);
				}
			}
			vars = p.getVariableAssignment();
			if (vars!=null) renameVariables(ret,vars);
			return ret;
		case AND:
		case IMPLY:
			args=getArgumentsfromLTS(p);
			if (args!=null) {
				if (type==Type.AND) {
					ret=new Conjunction();
					((Conjunction)ret).setConjuncts((List)args);
				} else if (args.size()==2) {
					ret=new Implication((WFF)args.pop(), (Predication)args.pop());
				}
			}
			vars = p.getVariableAssignment();
			if (vars!=null) renameVariables(ret,vars);
			return ret;
		case CNST:
			return new Constant(p.getElement().toString());
		case VAR:
			return new Variable("?v");
		case NAME:
			return new Constant(p.toString());
		}
		return ret;
	}

	/**
	 * 
	 * @param vars an array that specifies for each argument, if it's a constant: -1 or which variable it is (integer>=0)
	 */
	public static void renameVariables(UnifiableFormulaElement u,int[] vars) {
		renameVariables(u,vars, 0); 
	}
	public static int renameVariables(UnifiableFormulaElement u,int[] vars,int p) {
		List<? extends UnifiableFormulaElement> args = u.getArguments();
		if (args!=null) {
			for(UnifiableFormulaElement a:args) {
				if (a.getArgCount()>0) {
					p=renameVariables(a,vars, p);
				} else {
					if (a instanceof Variable) ((Variable)a).setName("?v"+vars[p]);
					p++;
				}
			}
		}
		return p;
	}

	private static LinkedList<UnifiableFormulaElement> getArgumentsfromLTS(LinkLTS p) {
		LinkedList<UnifiableFormulaElement> args=null;
		if (p!=null) {
			do {
				UnifiableFormulaElement a=fromLTS(p.getElement());
				if (args==null) args=new LinkedList<>();
				args.push(a);
			} while ((p=p.getParent())!=null && !p.isRoot());
		}
		return args;
	}

	private static List<Variable> getAllArgs(UnifiableFormulaElement thing) {
		List<Variable> ret=null;
		LinkedList<UnifiableFormulaElement> s=new LinkedList<>();
		s.push(thing);
		while(!s.isEmpty()) {
			UnifiableFormulaElement t=s.pop();
			if (t!=null) {
				List<? extends UnifiableFormulaElement> args = t.getArguments();
				if (args!=null) s.addAll(0,args);
				else if (t instanceof Variable || t instanceof Constant) {
					if (ret==null) ret=new ArrayList<>();
					if (t instanceof Variable) ret.add((Variable) t);
					else ret.add(null);
				}
			}
		}
		return ret;
	}

	private static LinkLTS toLTSinternal(UnifiableFormulaElement thing) throws Exception {
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
	
	private static LinkLTS termToLTS(Term thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof Variable) {
				return toLTS(VariableTerm,null);
			} else if (thing instanceof Constant) {
				String name=((Constant) thing).getName();
				BaseLTS n=constantsLTS.get(name);
				return toLTS(ConstantTerm.get(n),null);
			} else if (thing instanceof Function) {
				String name=((Function) thing).getName();
				BaseLTS n=constantsLTS.get(name);
				LinkLTS nn=OpName.get(n);
				List<? extends UnifiableFormulaElement> args = thing.getArguments();
				Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
				return toLTS(FunctionTerm.get(nn),it);
			}
		}
		return null;
	}
	
	private static LinkLTS wffToLTS(WFF thing) throws Exception {
		if (thing!=null) {
			if (thing instanceof Predication) {
				String name=((Predication) thing).getName();
				List<? extends UnifiableFormulaElement> args = thing.getArguments();
				BaseLTS n=constantsLTS.get(name);
				LinkLTS nn=OpName.get(n);
				Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
				return toLTS(BasicWFF.get(nn),it);
			} else if (thing instanceof Implication) {
				List<? extends UnifiableFormulaElement> args = thing.getArguments();
				Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
				return toLTS(ImplicationWFF,it);
			} else if (thing instanceof Conjunction) {
				List<? extends UnifiableFormulaElement> args = thing.getArguments();
				Iterator<? extends UnifiableFormulaElement> it=args!=null?args.iterator():null;
				return toLTS(ConjunctionWFF,it);
			} else throw new Exception("invalid option.");
		}
		return null;
	}

	private static LinkLTS toLTS(LinkLTS n, Iterator<? extends UnifiableFormulaElement> it) throws Exception {
		if (n!=null) {
			if (it==null || !it.hasNext()) return n;
			else {
				UnifiableFormulaElement thing = it.next();
				LinkLTS aLTS=toLTSinternal(thing);
				LinkLTS nextLink = n.get(aLTS);
				return toLTS(nextLink, it);
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		List<WFF> f=Parse.parse("(p1 C)");
		LinkLTS f1=toLTS(f.get(0));
		System.out.println(f1.getArgCount());
		System.out.println(f1.getId()+": "+f1);
		UnifiableFormulaElement u=fromLTS(f1);
		System.out.println(u);
		
		f=Parse.parse("(p1 C)");
		LinkLTS f2=toLTS(f.get(0));
		System.out.println(f2.getId()+": "+f2);
		u=fromLTS(f2);
		System.out.println(u);
	}

}
