package wff;

import java.util.List;

public class Constant extends Term {
	private String name=null;
	public Constant(String s) {
		this.name=s;
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
	public boolean equals(Object obj) {
		if (obj!=null && obj instanceof Constant) return name.equals(((Constant)obj).name);
		else return false;
	}
}
