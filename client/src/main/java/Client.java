import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import com.zeroc.Ice.SocketException;
import java.lang.InterruptedException;

public class Client
{
    public static boolean running = false;
    public static void main(String[] args)
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args,"config.client"))
        {
            Demo.PrinterPrx twoway = Demo.PrinterPrx.checkedCast(
                communicator.propertyToProxy("Printer.Proxy")).ice_twoway().ice_secure(false);
            Demo.PrinterPrx printer = twoway.ice_twoway();
            com.zeroc.Ice.ObjectAdapter adapter = null;
            boolean foundPort = false;
            while(!foundPort){ //Search for an available port
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
            Demo.CallbackPrx callPrx = Demo.CallbackPrx.uncheckedCast(objPrx);

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
                if(args.length >= 1){
                    msg = args[0];
                    Client.running = true;
                    printer.registerClient(hostname, callPrx);
                    waiting();
                    Client.running = true;
                    printer.printString(hostname+":"+msg, callPrx);
                    waiting();
                }else{
                    do{
                        System.out.println("Type ? for options");
                        msg = br.readLine();
                        if(!msg.equals("exit")){
                            switch(msg){
                                case "?":
                                    System.out.println("Type \"exit\" to stop");
                                    System.out.println("Type \"list clients\" to obtain the list of clients");
                                    System.out.println("Type \"to x:\" followed by your message (x being the hostname) to send a message to a specific client");
                                    System.out.println("Type \"BC\" and your message, separated by a space, to send a message to all clients");
                                    System.out.println("Type \"register\" to register\n");
                                    break;
                                case "register":
                                    Client.running = true;
                                    printer.registerClient(hostname, callPrx);
                                    waiting();
                                    break;
                                default:
                                    Client.running = true;
                                    printer.printString(hostname+":"+msg, callPrx);
                                    waiting();
                                    break;
                            }
                        }
                    }
                    while(!msg.equals("exit"));
                    Client.running = true;
                    printer.logOutClient(hostname, callPrx);
                    waiting();
                }
            }catch(UnknownHostException e){
                System.out.println(e);
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    public static void waiting(){
        if(running){
            System.out.println("Waiting the server...");
        }
        while(running){
            Thread.yield();
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