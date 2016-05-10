package wff;

import java.util.List;

public class Variable extends Term implements Comparable<Variable> {

	private String name;
	public Variable(String s) {
		if (!s.startsWith("?")) this.name="?"+s;
		else this.name=s;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public List<? extends UnifiableFormulaElement> getArguments() {
		return null;
	}
	
	@Override
	public int getArgCount() {
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj!=null && obj instanceof Variable) return name.equals(((Variable)obj).name);
		else return false;
	}

	@Override
	public int compareTo(Variable o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
