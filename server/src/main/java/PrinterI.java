import java.math.BigInteger;
public class PrinterI implements Demo.Printer
{
    //private ExecutorService pool = Executors.newThreadPool(10);
    public String printString(String s, com.zeroc.Ice.Current current)
    {   
        String hostname = s.substring(0,s.indexOf(":"));
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

                }else if(text.startsWith("to ")){
    
                }else if(text.startsWith("BC")){
    
                }else System.out.println(s);
            }
            
        }
        return msg;
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
}