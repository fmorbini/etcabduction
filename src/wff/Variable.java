package wff;

public class Variable extends Term {

	private String name;
	public Variable(String s) {
		if (!s.startsWith("?")) this.name="?"+s;
		else this.name=s;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
