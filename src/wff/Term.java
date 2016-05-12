package wff;

import java.util.List;

import edu.usc.ict.nl.util.StringUtils;

public abstract class Term implements UnifiableFormulaElement {
	public static Term create(Object t) throws Exception {
		Term ret=null;
		if (t!=null) {
			if (t instanceof String) {
				String s=(String)t;
				try {
					Float n=Float.parseFloat(s);
					ret=new NumericConstant(n);
				} catch (NumberFormatException e) {
					if (s.startsWith("?")) ret=new Variable(s);
					else if (StringUtils.isAllLowerCase(s)) ret=new Variable("?"+s);
					else ret=new Constant(s);
				}
			} else if (t instanceof List) {
				int s=((List)t).size();
				Object f=((List)t).get(0);
				if (f!=null && f instanceof String) {
					ret=Function.create((String)f,((List) t).subList(1, s));
				} else throw new Exception("invalid function name."+f);
			} else throw new Exception("Invalid term type."+t);
		}
		return ret;
	}
}
