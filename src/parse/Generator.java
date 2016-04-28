package parse;

import wff.Constant;

public class Generator {
	
	public static final Generator skolemGenerator=new Generator("$");
	
	private int v=1;
	private String prefix=null;
	
	public Generator(String prefix) {
		this.prefix=prefix;
	}
	
	public Constant next() {
		return new Constant(prefix+(v++));
	}
}
