package unimelb.bitbox;

public class test extends  Thread{
    public static void main(String args[]){
        test t =new test();
        t.start();
    }
    public void run(){
        for(int i = 0; i<10; i++){
            System.out.println(i);
            try {
                Thread.sleep(1*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
