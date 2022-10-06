public class CallbackI implements Demo.Callback
{
    public void response(String msg, boolean changeRunning, com.zeroc.Ice.Current current){
		System.out.println(msg);
		if(changeRunning){
			Client.running = false;
		}
	}
}
