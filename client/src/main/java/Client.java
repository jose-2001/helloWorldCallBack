import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import com.zeroc.Ice.SocketException;

public class Client
{
    public static void main(String[] args)
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client"))
        {
            Demo.PrinterPrx twoway = Demo.PrinterPrx.checkedCast(
                communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);
            Demo.PrinterPrx printer = twoway.ice_twoway();
            

            //int availablePort = getAvailablePort(8000, 9000);
            //com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Callback", "default -p "+String.valueOf(availablePort));
            //System.out.println(args[1]);
            com.zeroc.Ice.ObjectAdapter adapter = null;
            boolean foundPort = false;
            while(!foundPort){
                try{
                    int port = getAvailablePort(8000, 9000);
                    adapter = communicator.createObjectAdapterWithEndpoints("Callback", "default -p "+String.valueOf(port));
                    foundPort = true;
                }catch(SocketException se){
                    foundPort = false;
                }
            }
            com.zeroc.Ice.Object object = new CallbackI();
            com.zeroc.Ice.ObjectPrx objPrx= adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("callback"));
            adapter.activate();

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
                        System.out.println("Type exit to stop \nType list clients to obtain the list of clients \nType to x followed by your message (x being the hostname) to send a message to a specific client \nType BC and your message to send a message to all clients");
                    }
                    msg = args[0];
                    Demo.CallbackPrx callPrx = Demo.CallbackPrx.uncheckedCast(objPrx);
                    printer.printString(hostname+":"+msg, callPrx);
                    communicator.waitForShutdown();
                    //String response = printer.printString(hostname+":"+msg);
                    //System.out.println(response);
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

    public static int getAvailablePort(int minPort, int maxPort){
        boolean found = false;
        int availablePort = -1;
        for(int i = minPort; i < maxPort && !found; i++){
            if(available(i)){
                found = true;
                availablePort = i;
            }
        }
        return availablePort;
    }

    //Retrieve from https://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
    private static boolean available(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }
}