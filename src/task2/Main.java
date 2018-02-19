package task2;

import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        boolean fair = true;
        int numBees = 5;
        Semaphore consumer = new Semaphore(0, fair);
        Semaphore producer = new Semaphore(1, fair);
        HoneyPot honeyPot = new HoneyPot();

        Bear bear = new Bear(consumer, producer, honeyPot);
        Bee[] bees = new Bee[numBees];
        for(int i = 0; i < bees.length; i++){
            bees[i] = new Bee(consumer, producer, honeyPot);
        }

        Thread bearThread = new Thread(bear);
        Thread[] beeThread = new Thread[bees.length];

        bearThread.start();
        for(int i = 0; i < beeThread.length; i++) {
            beeThread[i] = new Thread(bees[i]);
            beeThread[i].start();
        }
    }
}

class Bear implements Runnable{
    private Semaphore consumer;
    private Semaphore producer;
    private HoneyPot honeyPot;

    public Bear(Semaphore consumer, Semaphore producer, HoneyPot honeyPot){
        this.consumer = consumer;
        this.producer = producer;
        this.honeyPot = honeyPot;
    }

    @Override
    public void run(){
        while(true){
            try{
                consumer.acquire();
                honeyPot.eatAll();
                producer.release();
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

class Bee implements Runnable{
    private static int id = 0;
    private Semaphore consumer;
    private Semaphore producer;
    private HoneyPot honeyPot;
    private int myId;

    public Bee(Semaphore consumer, Semaphore producer, HoneyPot honeyPot){
        this.consumer = consumer;
        this.producer = producer;
        this.honeyPot = honeyPot;
        myId = id++;
    }

    public int getId(){
        return this.myId;
    }

    @Override
    public void run(){
        while(true) {
            try {
                producer.acquire();
                boolean refillPot = honeyPot.addFood(this);
                if (!refillPot) {
                    consumer.release();
                } else {
                    producer.release();
                }
                long sleep = (long) (Math.random() * 1000);
                System.out.printf("Bee #%d slept for %g seconds\n", myId, sleep / 1000.0);
                Thread.sleep(sleep);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

class HoneyPot{
    public static final int MAX_CAPACITY = 10;
    private int foodAmount = 0;

    public boolean addFood(Bee bee){
        if(foodAmount == MAX_CAPACITY){
            System.out.printf("I tried to bring more honey, but the honey pot was full!\n");
            return false;
        }
        foodAmount++;
        System.out.printf("Bee #%d brought in more honey, making the pot %.0f%% full.\n", bee.getId(), ((double)foodAmount/MAX_CAPACITY)*100);
        return true;
    }

    public void eatAll(){
        System.out.println("I ATE ALL THE FOOD\n");
        foodAmount = 0;
    }
}
