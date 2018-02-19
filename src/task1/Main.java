import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        boolean fair = true;
        int numBabies = 5;
        int amountFood = Food.REFILL_AMOUNT;
        Semaphore producer = new Semaphore(0, fair);
        Semaphore consumer = new Semaphore(1, fair);
        Food food = new Food(amountFood);

        //Create the parent and children
        Parent parent = new Parent(producer, consumer, food);
        Baby[] babies = new Baby[numBabies];
        for(int i = 0; i < babies.length; i++){
            babies[i] = new Baby(producer, consumer, food);
        }

        //Create threads for the birds to run on
        Thread parentThread = new Thread(parent);
        Thread[] babyThread = new Thread[numBabies];

        for(int i = 0; i < babyThread.length; i++){
            babyThread[i] = new Thread(babies[i]);
            babyThread[i].start();
        }
        parentThread.start();
    }
}

class Parent implements Runnable{
    private final Semaphore producer;
    private final Semaphore consumer;
    private Food food;

    public Parent(Semaphore producer, Semaphore consumer, Food food){
        this.producer = producer;
        this.consumer = consumer;
        this.food = food;
    }

    @Override
    public void run(){
        while(true){
            try{
                producer.acquire();
                food.refill();
                consumer.release();
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

class Baby implements Runnable{
    private static int id;

    private final Semaphore producer;
    private final Semaphore consumer;
    private Food food;
    private int myId;

    public Baby(Semaphore producer, Semaphore consumer, Food food){
        this.producer = producer;
        this.consumer = consumer;
        this.food = food;
        myId = id++;
    }

    public int getId(){
        return this.myId;
    }

    @Override
    public void run(){
        while(true){
            try{
                consumer.acquire();
                boolean canEat = food.eat(this);
                if(!canEat){
                    System.out.printf("There was no worms left for baby #%d. CHIRP!\n", myId);
                    producer.release();
                }
                else{
                    consumer.release();
                }
                long sleep = (long) (Math.random() * 7000);
                System.out.printf("Baby bird #%d slept for %g seconds\n", myId, sleep/1000.0);
                Thread.sleep(sleep);
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}

class Food{
    public final static int REFILL_AMOUNT = 5;
    private int amount;

    public Food(int amount){
        this.amount = amount;
    }

    public boolean eat(Baby baby){
        if(amount <= 0){
            return false;
        }
        amount--;
        System.out.printf("Baby #%d ate. %d worms left.\n", baby.getId(), amount);
        return true;
    }

    public void refill(){
        amount += REFILL_AMOUNT;
        System.out.printf("Food refilled now, %d worms in the nest!\n", amount);
    }
}