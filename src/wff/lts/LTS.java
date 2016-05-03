package wff.lts;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import wff.Variable;

public class LTS {
	
	private static Generator ltsId=new Generator(0);
	
	private long id=-1;
	private int[] vars=null;
	
	public LTS() {
		this.id=ltsId.next();
	}
	public long getId() {
		return id;
	}
	public void setVariableAssignment(List<Variable> args) {
		if (args!=null && !args.isEmpty()) {
			this.vars=new int[args.size()];
			Set<Variable> uniqueVarsInOrder=new LinkedHashSet<>();
			int j=0;
			for(Variable x:args) {
				int i=0;
				uniqueVarsInOrder.add(x);
				if (uniqueVarsInOrder.size()==1) this.vars[j]=i;
				else {
					for(Variable y:uniqueVarsInOrder) {
						if (x.equals(y)) {
							this.vars[j]=i;
							break;
						}
						i++;
					}
				}
				j++;
			}
		}
	}
	
}
