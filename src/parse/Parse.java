package parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.usc.ict.nl.util.StringUtils;
import wff.WFF;

public class Parse {
	
	public static final File kb=new File("C:\\Users\\morbini\\EtcAbductionPy\\tricopa\\tricopa-kb.lisp");
	
	public static List<WFF> parse(File f) throws Exception {
		List<WFF> res=null;
		BufferedReader r=new BufferedReader(new FileReader(f));
		String l;
		Stack<List> literals=new Stack<>(); 
		int level=0;
		while((l=r.readLine())!=null) {
			l=decomment(l);
			if (!StringUtils.isEmptyString(l)) {
				String[] ts=tokenize(l);
				for(String t:ts) {
					List literal=null;
					t=StringUtils.removeLeadingAndTrailingSpaces(t);
					if (!StringUtils.isEmptyString(t)) {
						if (t.equals("(")) {
							literal=new ArrayList<>();
							if (level>0) {
								List parentLiteral=literals.pop();;
								parentLiteral.add(literal);
								literals.push(parentLiteral);
							} else {
								assert(literals.isEmpty());
							}
							literals.push(literal);
							level++;
						} else if (t.equals(")")) {
							level--;
							literal=literals.pop();
							if (level<=0) {
								if (res==null) res=new ArrayList<>();
								WFF wff=WFF.create(literal);
								res.add(wff);
								level=0;
							}
						} else {
							if (!StringUtils.isEmptyString(t)) {
								literal=literals.pop();
								literal.add(t);
								literals.push(literal);
							}
						}
					}
				}
			}
		}
		r.close();
		return res;
	}

	private static void variabilize(List literal) {
		if (literal!=null && !literal.isEmpty()) {
			for(int i=0;i<literal.size();i++) {
				Object t=literal.get(i);
				if (t!=null && t instanceof String) {
					if (StringUtils.isAllLowerCase((String)t)) {
						literal.set(i, "?"+t);
					}
				}
			}
		}
	}

	private static String decomment(String l) {
		String[] rs=l.split(";");
		return rs[0];
	}

	private static String[] tokenize(String l) {
		l=l.replaceAll("\\(", " ( ");
		l=l.replaceAll("\\)", " ) ");
		return l.split("[\\s]");
	}
	
	public static String getPredicate(Object l) {
		if (l!=null && l instanceof List && ((List)l).size()>0) {
			Object pred=((List)l).get(0); 
			if (pred!=null && pred instanceof String) return (String)pred;
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		List<WFF> content = parse(kb);
		System.out.println(content);
	}

}
