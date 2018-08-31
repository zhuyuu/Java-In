package multithreadcore.extthread;

/**
 * 可重入锁
 * Created by liur on 17-5-21.
 */
public class Service {
    synchronized public void service1(){
        System.out.println("service1");
        service2();
    }

    synchronized public void service2(){
        System.out.println("service2");
        service3();
    }

    synchronized public void service3(){
        System.out.println("service3");
    }
}