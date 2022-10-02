import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client
{
    public static void main(String[] args)
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client"))
        {
            //com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10000");
            Demo.PrinterPrx twoway = Demo.PrinterPrx.checkedCast(
                communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);
            //Demo.PrinterPrx printer = Demo.PrinterPrx.checkedCast(base);
            Demo.PrinterPrx printer = twoway.ice_twoway();

            if(printer == null)
            {
                throw new Error("Invalid proxy");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String msg = "";
            try{ 
                //Get hostname
                InetAddress address = InetAddress.getLocalHost();
                String hostname = address.getHostName();
                do{
                    System.out.println("Type ? for options");
                    //msg = br.readLine();
                    if(msg.equals("?")){
                        System.out.println("Type exit to stop \nType list clients to obtain the list of clients \nType to x followed by your message (x being the hostname) to send a message to a specific client \nType BC and your message to send a message to all clients")
                    }
                    msg = args[0];
                    String response = printer.printString(hostname+":"+msg);
                    System.out.println(response);
                    msg="exit";
                }
                while(!msg.equals("exit"));
                   
            }catch(UnknownHostException e){
                System.out.println(e);
            }catch (IOException ioe){ 
                    ioe.printStackTrace();
            }
            
        }
    }
}