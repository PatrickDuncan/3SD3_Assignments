import java.util.concurrent.Semaphore;

class East implements Runnable {
	// All it does is wait to be notified then add
	public void run() {
		while (true) {
			try {
				synchronized(Four.east) {
					Four.east.wait();
				}
				synchronized(Four.visitors) {					
					Four.visitors.release();
				}
				System.out.println("enter");
				Four.delay();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void begin() {
		(new Thread(new East())).start();
	}
}

class West implements Runnable {
	// All it does is wait to be notified then subtract
	public void run() {
		while (true) {
			try {
				synchronized(Four.west) {
					Four.west.wait();
				}					
				synchronized(Four.visitors) {
					Four.visitors.acquire();
				}
				System.out.println("exit");
				Four.delay();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void begin() {
		(new Thread(new West())).start();
	}
}

class Control implements Runnable {
	public void run() {
		while (true) {
			// if its open and not closing either enter or exit
			if (Four.open && !Four.closing) {
				// I added a 3:1 bias for people entering over exiting
				int enter = (int)(Math.random() * 4);
				// Can't enter if the museum is at capacity
				// Can't leave if there's no one in the museum
				if (enter < 2 && Four.visitors.availablePermits() < Four.CAPACITY) {
					synchronized (Four.east) {
						Four.east.notify();
					}
				} else if (enter == 3 && Four.visitors.availablePermits() > 0) {
					synchronized (Four.west) {
						Four.west.notify();
					}
				}
			} else if (Four.open && Four.closing && Four.visitors.availablePermits() > 0) {
				synchronized (Four.west) {
					Four.west.notify();
				}
			}
			// End state
			if (Four.closing && Four.visitors.availablePermits() == 0) {
				synchronized (Four.director) {
					Four.director.notify();
				}
			}
			Four.delay();
		}
	}

	public void begin() {
		(new Thread(new Control())).start();
	}
}

class Director implements Runnable {
	// open -> wait 10 seconds -> closing -> wait for empty museum -> closed (exit)
	public void run() {
		Four.open = true;
		System.out.println("Museum opened");
		try {
			Thread.sleep(10000);
			Four.closing = true;
			System.out.println("Museum closing");
			synchronized (Four.director) {
				Four.director.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Museum has now closed");
		System.exit(0);
	}

	public void begin() {
		(new Thread(new Director())).start();
	}
}

public class Four {
	public static final int CAPACITY = 10;
	public static Semaphore visitors = new Semaphore(0);
	public static boolean open = false, closing = false;
	public static East east = new East();
	public static West west = new West();
	public static Control control = new Control();
	public static Director director = new Director();

	public static void main(String[] args) {
		east.begin();
		west.begin();
		control.begin();
		director.begin();
	}

	public static void delay() {
		try {
		 	Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}