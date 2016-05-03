package wff.lts;

public class LTS {
	
	private static Generator ltsId=new Generator(0);
	
	private long id;
	
	public LTS() {
		this.id=ltsId.next();
	}
	public long getId() {
		return id;
	}
	
}
