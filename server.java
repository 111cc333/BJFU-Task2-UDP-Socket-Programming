package jiwangserver;
import java.io.IOException;    
import java.net.DatagramPacket;    
import java.net.DatagramSocket;    
import java.net.InetAddress;    
import java.net.InetSocketAddress;    
import java.net.SocketException;
import java.util.Random;    
    

public class server {    
	//要是字节数为1024那么多余1024的字节将被丢弃所以选大一些的
    private byte[] buffer = new byte[8192];    
    private DatagramSocket ds = null;    
    private DatagramPacket packet = null;    
    private InetSocketAddress socketAddress = null;    
    private String orgIp; 
    String content=null;
    static boolean rb=true;
    static boolean isout=false;
    static String printcontent=null;
    static String baowencontent=null;
     
    public server(String host, int port) throws Exception {    
        socketAddress = new InetSocketAddress(host, port);    
        ds = new DatagramSocket(socketAddress);    
        System.out.println("服务端启动!");    
    }    
    

    public void getbaowen(String ss)
    {
    	String [] arr = ss.split("\s+");
    	String info=arr[0]+" "+arr[2]+" "+"接收到了";
    	baowencontent=info;
    }
    
    public void getprintcontent(String ss)
    {
    	String [] arr = ss.split("\s+");
    	String info="序号-"+arr[0]+" "+"版本号-"+arr[1]+" "+"系统时间-"+arr[2]+" "+"信息-";
    	for(int i=5;i<arr.length;i++)
    	{
    		info=info+arr[i];
    	}
    	printcontent=info;
    }
    
    public void receive(){   
        try {
        	Random random = new Random();
         	rb = random.nextBoolean();
            packet = new DatagramPacket(buffer, buffer.length);    
            ds.receive(packet);    
            orgIp = packet.getAddress().getHostAddress();    
            String info = new String(packet.getData(), 0, packet.getLength());
            getbaowen(info);
            getprintcontent(info);
            if(rb)
            {
            	System.out.println("接收信息["+printcontent+"]");
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }   
    }    
      
    public final void response(String info) throws IOException {    
    	if(rb)
    	{  
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length, packet    
                    .getAddress(), packet.getPort());    
            dp.setData(baowencontent.getBytes());    
            ds.send(dp);
    	}    
    }    
       
    public final void close() {    
        try {    
            ds.close();    
        } catch (Exception ex) {    
            ex.printStackTrace();    
        }    
    }    
    
  
    public static void main(String[] args) throws Exception {    
        String serverHost = "127.0.0.1";    
        int serverPort = 2428;    
        server udpServerSocket = new server(serverHost, serverPort);    
        int num=0;
        while (true) {  
            udpServerSocket.receive();
            udpServerSocket.response("接收到了"); 
            num=num+1;
        }
    }    
}   