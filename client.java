package jiwangclient;

import java.io.*;    
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
    
    
/**  
   UDP的报文格式：Seq no.+Ver+Systime+Srcport+Destport+Data;
   占用字节数为:2+1+10+2+2+196;
 */    
public class client {    
    private byte[] buffer = new byte[8192]; 
    //总共发的数据的数量
    private static int SUMDATA=12;
    static int sumdatanum=12;
    static String serverHost=null;
    static int serverPort=0;
    //发送端输入的内容
    static String content=null;
  //发送端输入的内容
    static String baowencontent=null;
    //UDP的socket
    private DatagramSocket ds = null;  
    //超时的时间
    static int setouttimeis=100;
    //丢包的次数
    static int recinum=0;
    //重发的次数
    static int resend=0;
    //是否重发过
    static boolean isresend=false;
    //开始时间
    static long starttime=0;
    //结束时间
    static long endtime=0;
    //开始时间
    static long serverstarttime=0;
    //结束时间
    static long serverendtime=0;
    //现在时间
    static String timenow=null;
    //序号
    static short seq=1;
    //版本号
    static byte ver=2;
    //序号
    static short srcport=0;
    //序号
    static short destport=0;
    //RTT序列
    static double[] RTTarr=new double[SUMDATA+5];
    
   
    public client() throws Exception {    
        ds = new DatagramSocket();    
    }    
    
    public static void gettime()
    {
		Date currentDate = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		timenow = formatter.format(currentDate);
    }
    
    public String getnooutprintcon(String ss)
    {
    	String [] arr = ss.split("\s+");
    	String info="序号:"+arr[0]+" ,系统时间:"+arr[1]+" ,IP地址:"+serverHost+" ,端口号"+serverPort;
    	return info;
    }
    
    public String getoutprintcon(String ss)
    {
    	String [] arr = ss.split("\s+");
    	String info="序号:"+arr[0]+" 系统时间:"+arr[1]+" 发生过丢包不计算RTT";
    	return info;
    }
    
    public void getbaowen()
    {
    	String a = null;
    	if(seq<10)
    	{
    		a="0";
    		a=a+Short.toString(seq);
    	}
    	else if (seq<100)
    	{
    		a=Integer.toString(seq);
    	}
		baowencontent=a+" "+ver+" "+timenow+" "+ds.getLocalPort()+" "+serverPort+" "+content;
    }
    
    public static double maxRTT()
    {
    	double max=0.0;
    	for(double t:RTTarr)
    	{
    		if(t!=0)
    		{
    			if(max<=t)
    			{
    				max=t;
    			}
    		}
    	}
    	return max;
    }
    
    public static double minRTT()
    {
    	double min=100;
    	for(double t:RTTarr)
    	{
    		if(t!=0)
    		{
    			if(min>=t)
    			{
    				min=t;
    			}
    		}
    	}
    	return min;
    }
    
    public static double aveRTT()
    {
    	double sum=0.0;
    	for(double t:RTTarr)
    	{
    		sum=sum+t;
    	}
    	double avea=(double)sum/SUMDATA;
    	return avea;
    }
    
    public static double stdRTT()
    {
    	double avea=aveRTT();
    	double sum=0.0;
    	for(double t:RTTarr)
    	{
    		sum=sum+(t-avea)*(t-avea);
    	}
    	double std=(double)Math.sqrt((double)sum/SUMDATA);
    	return std;
    }
    
    public void send(client clien,String con)
    {
    	try {
			if(!con.equals("重传"))
			{
				gettime();
				getbaowen();
		        DatagramPacket dp = new DatagramPacket((baowencontent).getBytes(), 
		        		(baowencontent).getBytes().length, InetAddress.getByName(serverHost), serverPort);    
		        ds.send(dp);
			}
			else
			{
				if(con.equals("关闭"))
				{
			        DatagramPacket dp = new DatagramPacket(("关闭").getBytes(), 
			        		("关闭").getBytes().length, InetAddress.getByName(serverHost), serverPort);    
			        ds.send(dp);
				}
				else
				{
					resend=resend+1;
					gettime();
					getbaowen();
			        DatagramPacket dp = new DatagramPacket((baowencontent).getBytes(), 
			        		(baowencontent).getBytes().length, InetAddress.getByName(serverHost), serverPort);    			    
			        ds.send(dp);
				}
			}
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}    
    }
    
    
    public void receive(final String lhost, final int lport,client clien) { 
        try {
        	ds.setSoTimeout(setouttimeis);
        	DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			ds.receive(dp);
			String info = new String(dp.getData(), 0, dp.getLength());    
            endtime=System.nanoTime();
		    long RTT=(endtime-starttime)/1000000;
		    recinum++;
		    if(RTT<100)
		    {
		    	String newinfo=getnooutprintcon(info);
		    	RTT=endtime-starttime;
		    	double RTTms=(double)RTT/1000000;
		    	RTTarr[seq]=RTTms;
		    	newinfo=newinfo+" RTT是"+RTTms+"ms";
		    	System.out.println(newinfo); 
		    }
		    else
		    {
		    	String newinfo=getoutprintcon(info);
		    	System.out.println(newinfo);
		    }
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			if(resend<2)
			{
				sumdatanum++;
				String a=null;
				if(seq<10)
				{
					a="0"+seq;
				}
				System.out.println(seq+" 超时");
				clien.send(clien, "重传");
				receive(lhost,lport,clien);
			}
			else if(resend>=2)
			{
				System.out.println("放弃");
			}
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
        client clien = new client();    
        Scanner sc = new Scanner(System.in);  
        System.out.print("请输入ServerIP:");  
        serverHost = sc.next();  
        System.out.print("请输入ServerPort: ");
        serverPort = sc.nextInt();
        int num=0;
        recinum=0;
        seq=1;
        while(num<SUMDATA)
        {
        	System.out.print("请输入聊天内容：");
            content = sc.next(); 
            resend=0;
            starttime = System.nanoTime();
            clien.send(clien,content);
            if(seq==1)
            {
            	serverstarttime=System.currentTimeMillis();
            }
            if(seq==SUMDATA)
            {
            	serverendtime=System.currentTimeMillis();
            }
            clien.receive(serverHost, serverPort,clien);  
            seq++;
            num++;
        } 
        System.out.println("--------------------------------------------------------------");
        System.out.println("接收到udp packets的数目为："+recinum);
        double vlostnum=1-(double)recinum/sumdatanum;
        System.out.println("丢包率为："+vlostnum*100+"%");
        System.out.println("最大RTT为："+maxRTT()+"ms");
        System.out.println("最小RTT为："+minRTT()+"ms");
        System.out.println("RTT的标准差为："+stdRTT()+"ms");
        System.out.println("server的整体响应时间是"+(serverendtime-serverstarttime)+"ms");
        content = sc.next(); 
        if(content=="关闭")
        {
//        	clien.send(clien,"关闭");
        	clien.close();
        }
    }    
}   





























































