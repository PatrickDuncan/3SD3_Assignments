import java.util.concurrent.Semaphore;
import java.util.Stack;

class Barrier {
	private static Stack<Integer> accessed = new Stack<Integer>();
	private static int stackLength = 0;

	public static synchronized void sync(int process) {
		// If the thread has not called sync yet
		if (accessed.search(process) < 0) {
			accessed.push(process);
			++stackLength;
			// Once all threads have have called sync
			if (stackLength == Three.N) {
				++Three.times;
				System.out.println("All threads synced, notifying all threads.");
				// Empty stack and then reset length
				while (!accessed.empty()) {
					accessed.pop();
				}
				stackLength = 0;
				// Notify all threads that they can proceed with that they're doing
				for (int i=0; i<Three.N; i++) {
					synchronized(Three.process[i]) {
						Three.process[i].notify();
					}
				}			
			}
		}
	}

	public static synchronized boolean synced(int process) {
		return accessed.search(process) > 0;
	}
}

class Process implements Runnable {
	public void run() {
		int num = Three.processNum++;		// to know which process it is
		while (Three.times < Three.END) {
			try {
				// Sleep to slow down the execution of the program
				Thread.sleep((int)(Math.random() * 2000 + 500));
				System.out.println("Thread " + num + " called sync");
				Barrier.sync(num);
				// This is a fix for the last thread that calls sync not being notified
				// becasuse it never hits wait because its in the sync block
				if (Barrier.synced(num)) {
					synchronized(Three.process[num]) {
						Three.process[num].wait();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Thread would do something interesting
		}
	}
}

public class Three {
	public static final int N = 10, END = 5;
	public static int times = 0, processNum = 0;
	public static Thread[] process = new Thread[N];

	public static void main(String[] args) {
		for (int i=0; i<N; i++) {
			process[i] = new Thread(new Process());
			process[i].start();
		}
	}
}
