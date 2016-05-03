package wff.lts;

public class Generator {
	
	public static final Generator ltsGenerator=new Generator(0);
	
	private long v=1;
	
	public Generator(long start) {
		this.v=start;
	}
	
	public synchronized long next() {
		return v++;
	}
}
