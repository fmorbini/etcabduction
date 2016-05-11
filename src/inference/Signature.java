package inference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wff.Predication;
import wff.lts.LTSConverter;
import wff.lts.link.LinkLTS;

public class Signature implements Comparable<Signature> {
	List<Long> parts=null;
	
	public boolean addToSignature(Predication p) throws Exception {
		LinkLTS lts=LTSConverter.toLTS(p,true);
		return addToSignature(lts);
	}
	public boolean addToSignature(LinkLTS lts) throws Exception {
		if (lts!=null) {
			if (parts==null) parts=new ArrayList<>();
			long id=lts.getId();
			int l=parts.size();
			int insertAt;
			long atId=-1;
			for(insertAt=0;insertAt<l;insertAt++) {
				if ((atId=parts.get(insertAt))>=id) break;
			}
			if (atId<0 || atId!=id) {
				parts.add(insertAt, id);
				return true;
			}
		}
		return false;
	}
	public void addToSignature(Collection<LinkLTS> ps) throws Exception {
		if (ps!=null) for(LinkLTS p:ps) addToSignature(p);
	}
	
	@Override
	public int compareTo(Signature o) {
		if (parts==o.parts) return 0;
		else if (parts!=null && o.parts!=null) {
			int l=parts.size();
			if (l==o.parts.size()) {
				for(int i=0;i<l;i++) {
					long diff=parts.get(i)-o.parts.get(i);
					if (diff!=0) return (int) diff;
				}
				return 0;
			}
		} else if (parts==null) return -1; 
		return 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj!=null && obj instanceof Signature) return compareTo((Signature) obj)==0;
		else return false;
	}
	
	@Override
	public int hashCode() {
		return parts==null?0:parts.hashCode();
	}

	@Override
	protected Signature clone() throws CloneNotSupportedException {
		Signature ret=new Signature();
		ret.parts=new ArrayList<>(this.parts);
		return ret;
	}
}
