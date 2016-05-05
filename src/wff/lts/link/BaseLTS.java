package wff.lts.link;

import wff.lts.Generator;

public class BaseLTS extends LinkLTS {
	
	
	private String name=null;
	
	public BaseLTS(String name) {
		super(null,null);
		this.name=name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public String getName() {
		return name;
	}
	
}
