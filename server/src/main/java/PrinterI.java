import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.ArrayList;

public class PrinterI implements Demo.Printer
{
    private ThreadPoolExecutor pool;
    private ArrayList<String> hostnames;
    private ArrayList<Demo.CallbackPrx> callbacks;


    public PrinterI(){
        pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        hostnames = new ArrayList<>();
        callbacks = new ArrayList<>();
    }

    public void printString(String s, Demo.CallbackPrx  cl, com.zeroc.Ice.Current current)
    {   
        String hostname = s.substring(0,s.indexOf(":"));
        boolean isRegistered = checkIfClientIsRegistered(hostname);
        if(isRegistered){
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
                            msg=hostname.toString();
                        }else if(text.startsWith("to ")){
            
                        }else if(text.startsWith("BC")){
            
                        }else System.out.println(s);
                    }
                    
                }
                cl.response(msg+"\n");
            });
        }else{
            cl.response("You are not registered. Please register\n");
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

    private boolean checkIfClientIsRegistered(String hostname){
        boolean isRegistered = false;
        for(int i = 0; i < hostnames.size() && !isRegistered; i++){
            if(hostnames.get(i).equals(hostname)){
                isRegistered = true;
            }
        }
        return isRegistered;
    }

    public void registerClient(String hostname, Demo.CallbackPrx  cl, com.zeroc.Ice.Current current){
        boolean isRegistered = checkIfClientIsRegistered(hostname);
        if(!isRegistered){
            hostnames.add(hostname);
            callbacks.add(cl);
            cl.response("You were registered successfully\n");
        }else{
            cl.response("You were already registered\n");
        }
    }
}