package wff.lts;

import java.util.HashMap;
import java.util.Map;

import wff.lts.link.BaseLTS;

class ConstantLTSRepo {
	private Map<String,BaseLTS> constantsLTS=null;

	public BaseLTS get(String name) {
		if (name!=null) {
			if (constantsLTS==null) constantsLTS=new HashMap<>();
			BaseLTS r = constantsLTS.get(name);
			if (r==null) constantsLTS.put(name, r=new BaseLTS(name));
			return r;
		}
		return null;
	}

	public void put(String name, BaseLTS lts) {
		if (lts!=null) {
			if (constantsLTS==null) constantsLTS=new HashMap<>();
			constantsLTS.put(name,lts);
		}
	}
}