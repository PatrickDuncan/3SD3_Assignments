import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.LinkedList;

class Mix implements Runnable {
    public void run() {
        while (System.currentTimeMillis() - One.start < 30000) {  // Quit after 30 seconds
            int random = (int)(Math.random() * 2);

            if (random == 0) {   // produce
                int ranP = (int)(Math.random() * One.P);
                synchronized(One.producer[ranP]) {
                    One.producer[ranP].notify();
                }
            }
            // can only consume if there's something to consume 
            else if (!One.bufferID.isEmpty()) {  
                // Pop the most recent message ID off the stack
                int ranC = One.bufferID.remove().intValue();
                synchronized(One.consumer[ranC]) {
                    One.consumer[ranC].notify();
                }
            }
            One.delay();
        }
        System.out.println("30 seconds has passed, good bye!");
        System.exit(0);
    }
}

class Producer implements Runnable {
    public void run() {
        int num = ++One.pID;    // Which producer this thread is.
        while (true) {
            try {
                synchronized(One.producer[num]) {
                    One.producer[num].wait();
                }

                int ranC = (int)(Math.random() * One.C);    // randomly choose an ID
                int ranS = (int)(Math.random() * One.S);    // randomly choose a message

                One.bufferID.add(ranC);                      // produce the consumer ID
                One.bufferMsg.add(One.messages[ranS]);       // produce the message that goes along with it

                System.out.println("Producer(" + num +") produced message with ID(" + ranC + ")");
                One.delay();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Consumer implements Runnable {
    public void run() {
        int num = ++One.cID;    // Which consumer this thread is.
        while (true) {
            try {
                synchronized(One.consumer[num]) {
                    One.consumer[num].wait();
                }
                // Consume the message
                String message = One.bufferMsg.remove();

                System.out.println("Consumer(" + num +") consumed message:");
                System.out.println(message);
                One.delay();
            } catch (InterruptedException e) {
                e.printStackTrace();    
            }
        }
    }
}

public class One {
    public static final int C = 6, P = 4, S = 5;
    public static Queue<Integer> bufferID = new LinkedList<Integer>();
    public static Queue<String> bufferMsg = new LinkedList<String>();
    public static int cID = -1, pID = -1;
    public static long start = System.currentTimeMillis();
    public static String messages[] = new String[S];

    public static Thread mix = new Thread(new Mix());
    public static Thread[] producer = new Thread[P];
    public static Thread[] consumer = new Thread[C];

    public static void main(String[] args) {
        // initialize the messages
        messages[0] = "Hey there.";
        messages[1] = "SD33333.";
        messages[2] = "This is a message.";
        messages[3] = "I just produced.";
        messages[4] = "Consume my message!";
        // Create a bunch of producers
        for (int i = 0; i < producer.length; ++i) {
            producer[i] = new Thread(new Producer());
            producer[i].start();
        }
        // Create a bunch of consumers
        for (int i = 0; i < consumer.length; ++i) {
            consumer[i] = new Thread(new Consumer());
            consumer[i].start();
        }
        mix.start();    // Start the mix after the producers/consumers
    }

    public static void delay() {
        try {
            Thread.sleep((int)((Math.random() * 1000) + 300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
