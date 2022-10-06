import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.lang.InterruptedException;

public class PrinterI implements Demo.Printer
{
    private ThreadPoolExecutor pool;
    private ArrayList<String> hostnames;
    private ArrayList<Demo.CallbackPrx> callbacks;
    private Semaphore sem;


    public PrinterI(){
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        hostnames = new ArrayList<>();
        callbacks = new ArrayList<>();
        sem = new Semaphore(1);

    }

    public void printString(String s, Demo.CallbackPrx  cl, com.zeroc.Ice.Current current)
    {
        String hostname = s.substring(0,s.indexOf(":"));
        int index = checkIfClientIsRegistered(hostname);
        if(index != -1){
            pool.submit(() -> {
                String msg="0";
                if(s.indexOf(":")==s.length()-1){
                    System.out.println(s);
                }else{
                    String text = s.substring(s.indexOf(":")+1);
                    try{
                        int n = Integer.parseInt(text);
                        if(n<1){
                            System.out.println(s);
                        }else{
                            System.out.print(hostname+": ");
                            msg = fibonacci(n, current);
                        }
                    }catch(NumberFormatException e){
                        if(text.startsWith("list clients")){
                            msg = "The clients registered in this moment are:\n";
                            for(int i = 0; i < hostnames.size(); i++){
                                msg += hostnames.get(i)+"\n";
                            }
                        }else if(text.startsWith("to ")){
                            if(!text.contains(":")){
                                System.out.println(s);
                            }else{
                                String toHostName = text.substring(3, text.indexOf(":"));
                                try{
                                    sem.acquire();
                                    int indexToHostName = checkIfClientIsRegistered(toHostName);
                                    if(indexToHostName != -1){
                                        String message = text.substring(text.indexOf(":")+1);
                                        callbacks.get(indexToHostName).response(hostname+":"+message, false);
                                        cl.response("The message was sent successfully to "+toHostName, false);
                                    }else{
                                        cl.response("In this moment, the host " + toHostName + " is not connected to the server", false);
                                    }
                                    sem.release();
                                }catch(InterruptedException ie){
                                    ie.printStackTrace();
                                }
                            }
                        }else if(text.startsWith("BC ")){
                            try{
                                sem.acquire();
                                String message = text.substring(3);
                                for(int i = 0; i < hostnames.size(); i++){
                                    callbacks.get(i).response(hostname + ": " + message, false);
                                }
                                sem.release();
                            }catch(InterruptedException ie){
                                ie.printStackTrace();
                            }
                        }else System.out.println(s);
                    }
                }
                cl.response(msg, true);
            });
        }else{
            cl.response("You are not registered. Please register\n", true);
        }
    }

    //Adapted from http://puntocomnoesunlenguaje.blogspot.com/2012/11/fibonacci-en-java.html
    public String fibonacci(int n, com.zeroc.Ice.Current current){
        BigInteger fibo1,fibo2;

        fibo1=BigInteger.ONE;
        fibo2=BigInteger.ONE;
        System.out.print(fibo1.toString() + " ");
        for(int i=2;i<=n;i++){
            System.out.print(fibo2.toString() + " ");
            fibo2 = fibo1.add(fibo2);
            fibo1 = fibo2.subtract(fibo1);
        }
        System.out.println();
        String msg = fibo1.toString();
        return msg;
    }

    private int checkIfClientIsRegistered(String hostname){
        boolean isRegistered = false;
        int index = -1;
        for(int i = 0; i < hostnames.size() && !isRegistered; i++){
            if(hostnames.get(i).equals(hostname)){
                isRegistered = true;
                index = i;
            }
        }
        return index;
    }

    public void registerClient(String hostname, Demo.CallbackPrx  cl, com.zeroc.Ice.Current current){
        int index = checkIfClientIsRegistered(hostname);
        if(index == -1){
            hostnames.add(hostname);
            callbacks.add(cl);
            cl.response("You were registered successfully\n", true);
        }else{
            cl.response("You were already registered\n", true);
        }
    }

    public void logOutClient(String hostname, Demo.CallbackPrx  cl, com.zeroc.Ice.Current current){
        pool.submit(() -> {
            try{
                sem.acquire();
                int index = checkIfClientIsRegistered(hostname);
                if(index != -1){
                    hostnames.remove(index);
                    callbacks.remove(index);
                }
                cl.response("Bye\n", true);
                sem.release();
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
        });
    }


}