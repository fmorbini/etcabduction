package wff;

public class Constant extends Term {
	private String name=null;
	public Constant(String s) {
		this.name=s;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
