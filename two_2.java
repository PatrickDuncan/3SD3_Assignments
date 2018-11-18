import java.util.concurrent.Semaphore;

class Savage implements Runnable {
	public void run() {
		int num = Two.cookNum++;	// Which savage this thread is.
		while (Two.fillings < Two.END || Two.pot.availablePermits() > 0) {
			try {
				// If the pot is empty notify the cook and then wait for the cook
				if (Two.pot.availablePermits() == 0) {
					synchronized(Two.cook) {
						Two.cook.notify();
					}
				}
				// If the pot is not empty acquire a serving
				synchronized(Two.pot) {
					Two.pot.acquire();
				}
				System.out.println("get_serving (savage " + num + ")");
				Two.delay();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Savages go to sleep.");
		System.exit(0);
	}
}

class Cook implements Runnable {
	public void run() {
		while (Two.fillings < Two.END) {
			try {
				// If the pot is not empty notify all savages
				if (Two.pot.availablePermits() != 0) {
					synchronized(Two.cook) {
						Two.cook.wait();
					}
				}
				// Else Fill the pot and increment the # of fillings counter
				System.out.println("fill_pot");
				Two.pot.release(Two.M);
				++Two.fillings;
				Two.delay();
			} catch (InterruptedException e) {
				e.printStackTrace();	
			}
		}
		for (int i=0; i<Two.savage.length; i++) {
			synchronized(Two.savage[i]) {
				Two.savage[i].notify();
			}
		}
	}
}

public class Two {
	public static final int M = 3, END = 5;
	public static Semaphore pot = new Semaphore(0);
	public static int fillings = 0, cookNum = 0;
	public static Thread cook = new Thread(new Cook());
	public static Thread[] savage = new Thread[4];

	public static void main(String[] args) {
		cook.start();
		// Create a bunch of savages
		for (int i=0; i<savage.length; i++) {
			savage[i] = new Thread(new Savage());
			savage[i].start();
		}
	}

	public static void delay() {
		try {
		 	Thread.sleep((int)((Math.random() * 1000) + 300));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
