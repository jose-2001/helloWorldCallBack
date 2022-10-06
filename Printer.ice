module Demo
{
    interface Callback{
	    void response(string rs, bool changeRunning);
    }
    
    interface Printer
    {
        void printString(string s, Callback* cl);
        void registerClient(string hostname, Callback* cl);
        void logOutClient(string hostname, Callback* cl);
        string fibonacci(int n);
    }
}