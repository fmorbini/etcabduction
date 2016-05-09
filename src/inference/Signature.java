package inference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import wff.Predication;
import wff.lts.LTSConverter;
import wff.lts.link.LinkLTS;

public class Signature implements Comparable<Signature> {

	long[] parts=null;
	boolean dirty=false;
	
	public void addToSignature(Predication[] ps) throws Exception {
		if (ps!=null) {
			int l=parts!=null?parts.length:0;
			if (parts==null) parts=new long[ps.length];
			else parts=Arrays.copyOf(parts, l+ps.length);
			for(Predication p:ps) {
				LinkLTS lts=LTSConverter.toLTS(p);
				long id=lts.getId();
				parts[l]=id;
				l++;
			}
			Arrays.sort(parts);
		}
	}
	public void addToSignature(Collection<LinkLTS> ps) {
		if (ps!=null) {
			int l=parts!=null?parts.length:0;
			if (parts==null) parts=new long[ps.size()];
			else parts=Arrays.copyOf(parts, l+ps.size());
			for(LinkLTS lts:ps) {
				long id=lts.getId();
				parts[l]=id;
				l++;
			}
			Arrays.sort(parts);
		}
	}
	
	public boolean getDirty() {return dirty;}
	public void setDirty(boolean v) {dirty=v;}

	@Override
	public int compareTo(Signature o) {
		if (parts==o.parts) return 0;
		else if (parts!=null && o.parts!=null) {
			if (parts.length==o.parts.length) {
				int l=parts.length;
				for(int i=0;i<l;i++) {
					long diff=parts[i]-o.parts[i];
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
		return Arrays.hashCode(parts);
	}

	
}
