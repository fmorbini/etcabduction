package wff.lts.link;

import wff.lts.Generator;

public class BaseLTS extends LinkLTS {
	
	private static Generator ltsId=new Generator(0);
	
	private long id=-1;
	private String name=null;
	
	public BaseLTS(String name) {
		super(null,null);
		this.id=ltsId.next();
		this.name=name;
	}
	public long getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getName() {
		return name;
	}
	
}
