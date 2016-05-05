package wff.lts.link;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wff.Variable;
import wff.lts.LTSConverter;
import wff.lts.LTSConverter.Type;

public class LinkLTS {
	private LinkLTS element=null;
	private Map<LinkLTS,LinkLTS> children=null;
	private int[] vars=null;
	private LinkLTS parent=null;

	public LinkLTS(LinkLTS parent,LinkLTS n) {
		this.element=n;
		this.parent=parent;
	}

	public LinkLTS get(LinkLTS n) {
		if (children==null) children=new HashMap<>();
		LinkLTS link = children.get(n);
		if (link==null) children.put(n, link=new LinkLTS(this,n));
		return link;
	}

	public LinkLTS getElement() {
		return element;
	}
	
	public void setVariableAssignment(List<Variable> args) {
		if (args!=null && !args.isEmpty()) {
			this.vars=new int[args.size()];
			Set<Variable> uniqueVarsInOrder=new LinkedHashSet<>();
			int j=0;
			for(Variable x:args) {
				int i=0;
				if (x==null) this.vars[j]=-1;
				else {
					uniqueVarsInOrder.add(x);
					if (uniqueVarsInOrder.size()==1) this.vars[j]=i;
					else {
						for(Variable y:uniqueVarsInOrder) {
							if (x.equals(y)) {
								this.vars[j]=i;
								break;
							}
							i++;
						}
					}
				}
				j++;
			}
		}
	}
	public int[] getVariableAssignment() {
		return vars;
	}

	public LinkLTS getParent() {
		return parent;
	}
	
	public boolean isList() {
		LinkLTS p = getParent();
		return (p!=null && !p.isRoot());
	}

	@Override
	public String toString() {
		StringBuffer ret=new StringBuffer();
		LinkLTS p=this;
		boolean close=false;
		if (p.isList()) {
			close=true;
			ret.append("(");
		}
		boolean first=true;
		do {
			Type type=p.getRootType();
			if (type==null || type==Type.VAR) {
				LinkLTS link=p.getElement();
				ret.append((first?"":" ")+link);
				first=false;
			}
		} while ((p=p.getParent())!=null);
		if (close) ret.append(")");
		return ret.toString();
	}

	public boolean isRoot() {
		return getRootType()!=null;
	}
	public Type getRootType() {
		if (this==LTSConverter.BasicWFF) return Type.WFF;
		else if (this==LTSConverter.ConjunctionWFF) return Type.AND;
		else if (this==LTSConverter.ConstantTerm) return Type.CNST;
		else if (this==LTSConverter.FunctionTerm) return Type.FUNC;
		else if (this==LTSConverter.ImplicationWFF) return Type.IMPLY;
		else if (this==LTSConverter.VariableTerm) return Type.VAR;
		else if (this==LTSConverter.OpName) return Type.NAME;
		else return null;
	}

	public LinkLTS getRoot() {
		LinkLTS p = this;
		while(p!=null && p.getRootType()==null) {
			p=p.getParent();
		}
		return p;
	}
	
	public Type getType() {
		LinkLTS r = getRoot();
		return r!=null?r.getRootType():null;
	}
}
