package inference;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AbductionWorker extends Thread {

	int maxDepth=0;
	private boolean run=true;
	private ConcurrentLinkedQueue<AbductionNode> q;
	private Abduction a;
	
	public AbductionWorker(ConcurrentLinkedQueue<AbductionNode> q,Abduction a) {
		this.q=q;
		this.a=a;
	}
	
	@Override
	public void run() {
		while(run) {
			if (q!=null) {
				AbductionNode n=q.poll();
				if (n!=null) {
					try {
						List<AbductionNode> rs = a.doAbductionStep(n);
						if (rs!=null && !rs.isEmpty()) {
							for(AbductionNode an:rs) {
								if (an.getDepth()<maxDepth) q.add(an);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void kill() {
		run=false;
	}
	@Override
	public synchronized void start() {
		run=true;
		super.start();
	}

	public void setMaxDepth(int levelsToCrunch) {
		this.maxDepth=levelsToCrunch;
	}
}
