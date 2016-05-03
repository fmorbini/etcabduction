package wff.lts;

import java.util.HashMap;
import java.util.Map;

public class LinkLTS {
	private LTS element;
	private Map<LTS,LinkLTS> children;

	public LinkLTS(LTS n) {
		this.element=n;
	}

	public LinkLTS get(LTS n) {
		if (children==null) children=new HashMap<>();
		LinkLTS link = children.get(n);
		if (link==null) children.put(n, link=new LinkLTS(n));
		return link;
	}

	public LTS getElement() {
		return element;
	}
}
