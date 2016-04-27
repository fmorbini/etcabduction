package parse;

public class Generator {
	
	public static final Generator skolemGenerator=new Generator("$");
	
	private int v=1;
	private String prefix=null;
	
	public Generator(String prefix) {
		this.prefix=prefix;
	}
	
	public String next() {
		return prefix+(v++);
	}
}
