package inference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Choose<T> implements Iterator<T[]> {

	private int n=-1;
	private List<T> l;
	private T[] response=null;
	private int[] state=null;
	public Choose(Collection<T> ss, int r) throws Exception {
		int size=ss.size();
		if (r>size) throw new Exception("combination size larger than available elements.");
		n=length(size, r);
		l=new ArrayList<>(ss);
		response=(T[]) Array.newInstance(l.get(0).getClass(), r);
		state=new int[r];
		for(int i=0;i<r;i++) state[i]=i;
	}

	@Override
	public boolean hasNext() {
		return n>0;
	}

	@Override
	public T[] next() {
		readState();
		getNextState();
		return response;
	}

	private void getNextState() {
		if (n>0) incrementPos(state.length-1);
	}
	
	private void incrementPos(int pos) {
		int start=state[pos];
		int end=getMaxPosForBit(pos);
		if (start<end-1) {
			state[pos]++;
		} else if (pos>0) {
			incrementPos(pos-1);
			state[pos]=state[pos-1]+1;
			assert(state[pos]<end);
		} else n=-1;
	}

	private int getMaxPosForBit(int pos) {
		int d=state.length-(pos+1);
		if (d<0) return l.size();
		else return l.size()-d;
	}

	private T[] readState() {
		if (n>0) {
			int r=state.length;
			for(int i=0;i<r;i++) response[i]=l.get(state[i]);
			n--;
			return response;
		} else {
			response=null;
			return null;
		}
	}

	public int length() {
		return length(l.size(),state.length);
	}
	public static int length(int size, int rep) {
		int nCk = 1;
		for (int k = 0; k < rep; k++) {
			nCk = nCk * (size-k) / (k+1);
		}
		return nCk;
	}
	
	public static void main(String[] args) throws Exception {
		Choose<Integer> c = new Choose<Integer>(Arrays.asList(new Integer[]{1,2,3,4,5,6,7,8}), 4);
		System.out.println(c.length());
		while(c.hasNext()) {
			System.out.println(Arrays.toString(c.next()));
		}
		System.out.println(Arrays.toString(c.next()));
		System.out.println(Arrays.toString(c.next()));
	}
}
