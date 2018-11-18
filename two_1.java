import java.util.concurrent.Semaphore;

class Clerk {
	private int clerk;

	public Clerk(int clerk) {
		this.clerk = clerk;
	}

	// Prints seats status
	public void display() {
		String seats = "";
		for (boolean booked : One.seats) {
			seats += booked ? " booked " : " free ";
		}
		System.out.println(seats);
	}

	// Checks if a seat is free
	public void free(int seat) {
		One.seats[seat] = false;
		System.out.println("Clerk " + this.clerk + " freed seat " + seat);
	}

	// Books a seat
	public boolean book(int seat) {
		if (!One.seats[seat]) {
			One.seats[seat] = true;
			System.out.println("Clerk " + this.clerk + " booked seat " + seat);
			return true;
		}
		else return false;
	}

	// Checks if a seat is available
	public boolean available(int seat) {
		return !One.seats[seat];
	}

	// Checks if all the seats are booked
	public boolean full() {
		int c = 0;
		for (boolean booked : One.seats) {
			if (booked) c++;
		}
		return c == One.seats.length;
	}
}

class Client implements Runnable {
	public void run() {
		int client = 0;	// Which client object in One
		synchronized (One.clientNum) {
			try {
				One.clientNum.acquire();
				client = One.clientNum.availablePermits();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Random clerk
		int whichClerk = (int)(Math.random() * One.clerks.length);
		One.delay();
		System.out.println("Client " + client + " goes to Clerk " + whichClerk);
		display(whichClerk, client);

		int whichSeat = whichSeat(whichClerk);
		One.delay();
		System.out.println("Client " + client + " picks seat " + whichSeat);

		// Booking a seat, picked again if its booked
		while (!One.clerks[whichClerk].book(whichSeat)) {
			if (One.clerks[whichClerk].full()) return;	// Client leaves if its sold out
			display(whichClerk, client);
			whichSeat = whichSeat(whichClerk);
			One.delay();
			System.out.println("Client " + client + " repicks, picks seat " + whichSeat);
		}

		One.delay();
		// Randomly pick to free the seat they booked
		int free = (int)(Math.random() * 2);
		if (free == 0) {
			One.clerks[whichClerk].free(whichSeat);
		}
	}

	// Shared resource display with Clerk and Client
	private void display(int clerk, int client) {
		System.out.println("Clerk " + clerk + " displays to Client " + client);
		One.clerks[clerk].display();
	}

	// Picks a random seat thats available
	private int whichSeat(int clerk) {
		int seat = (int)(Math.random() * One.seats.length);
		while (!One.clerks[clerk].available(seat)) {
			seat = (int)(Math.random() * One.seats.length);
		}
		return seat;
	}
}

public class One {
	public static Semaphore clientNum = new Semaphore(15);
	public static boolean[] seats = new boolean[10];
	public static Clerk[] clerks = new Clerk[3];
	public static Thread[] clients = new Thread[15];

	public static void main(String[] args) {
		for (int i=0; i<clerks.length; i++) {
			clerks[i] = new Clerk(i);
		}
		for (int i=0; i<clients.length; i++) {
			clients[i] = new Thread(new Client());
			clients[i].start();
		}
	}

	public static void delay() {
		try {
		 	Thread.sleep((int)((Math.random() * 4000) + 300));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}