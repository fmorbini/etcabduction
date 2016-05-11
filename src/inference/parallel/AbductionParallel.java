package inference.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import inference.Abduction;
import inference.AbductionNode;
import inference.AbductionWorker;
import wff.Implication;
import wff.Predication;

public class AbductionParallel extends Abduction {
	private List<AbductionWorker> workers=null;
	private ConcurrentLinkedQueue<AbductionNode> jobQueue=null;

	public AbductionParallel(List<Predication> obs,List<Implication> kb,boolean skolemize,int workers) {
		super(obs,kb,skolemize);
		this.jobQueue=new ConcurrentLinkedQueue<>();
		if (this.workers==null) this.workers=new ArrayList<>();
		this.workers.clear();
		for(int i=0;i<workers;i++) this.workers.add(new AbductionWorker(jobQueue, this,etcSolutions));
	}
	
	private void runParallel(int levelsToCrunch) throws Exception {
		runParallel(getInitialNode(), levelsToCrunch);
	}
	private void runParallel(AbductionNode start,int levelsToCrunch) throws InterruptedException {
		long startTime=System.currentTimeMillis();
		if (workers!=null && !workers.isEmpty()) {
			System.out.println("waiting for queue to be empty");
			while(!jobQueue.isEmpty()) {Thread.sleep(100);}
			for(AbductionWorker w:workers) {
				w.setMaxDepth(levelsToCrunch);
				w.start();
			}
			System.out.println("adding start node to queue");
			jobQueue.add(start);
			while(!jobQueue.isEmpty()) {Thread.sleep(100);}
		}
		long endTime=System.currentTimeMillis();
		System.out.println("runtime="+(endTime-startTime)+" ms");
	}

	private void killWorkers() {
		if (workers!=null) for(AbductionWorker w:workers) w.kill();
	}

}
