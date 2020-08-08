package com.kdk.MultichannelReceiver.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.kdk.MultichannelReceiver.model.utils.PacketConverter;
import com.kdk.MultichannelReceiver.model.utils.UsefulConvertFunctions;

import javafx.application.Platform;






public class ReceiverUDPClient extends Thread{
    private DatagramSocket socket;
    private InetAddress address;
    private int receivePort;
    private boolean running;
    private byte[] buf;
    private PacketConverter sharedSpectrumDataPacket;
    private ReceiverDataConverter receiverDataConverter;
    //private BlockingQueue<PacketConverter> blockingSpectrumDataQueue;
    private ConcurrentLinkedQueue<PacketConverter> blockingSpectrumDataQueue;
    
    public ReceiverUDPClient(String ipAddress, int receivePort, PacketConverter spectrumDataPacket, ReceiverDataConverter dataConverter, ConcurrentLinkedQueue<PacketConverter> blockingQueue) throws SocketException, UnknownHostException {
		super();
		this.address = InetAddress.getByName(ipAddress);;
		this.receivePort = receivePort;
		this.sharedSpectrumDataPacket = spectrumDataPacket;//wspólna struktura z odebranymi danymi
		this.receiverDataConverter = dataConverter;
		this.blockingSpectrumDataQueue = blockingQueue;
		socket = new DatagramSocket(receivePort);
		socket.setReceiveBufferSize(32768);
        socket.setSoTimeout(500);
        
	}
//	public ReceiverUDPClient(String ipAddress, int receivePort, PacketConverter spectrumDataPacket, ReceiverDataConverter dataConverter, BlockingQueue<PacketConverter> blockingQueue) throws SocketException, UnknownHostException {
//		super();
//		this.address = InetAddress.getByName(ipAddress);;
//		this.receivePort = receivePort;
//		this.sharedSpectrumDataPacket = spectrumDataPacket;//wspólna struktura z odebranymi danymi
//		this.receiverDataConverter = dataConverter;
//		this.blockingSpectrumDataQueue = blockingQueue;
//		socket = new DatagramSocket(receivePort);
//		socket.setReceiveBufferSize(32768);
//        socket.setSoTimeout(500);
//        
//	}
 
	public ReceiverUDPClient(String ipAddress) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(4445);
        socket.setReceiveBufferSize(32768);
        socket.setSoTimeout(500);
        
        address = InetAddress.getByName(ipAddress);
        
    }
	
    public InetAddress getAddress() {
		return address;
	}


	public int getReceivePort() {
		return receivePort;
	}

 
    public String sendMsg(String msg) {
        String received = "";
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4444);        
        try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Client1: " + e.getMessage());
		}
        
        packet = new DatagramPacket(buf, buf.length);
        try {
			socket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Client1: " + e.getMessage());
		}
        
        int data_size = packet.getLength();
        if(data_size<2136) {
        	received = new String(packet.getData(), 0, packet.getLength());           
        }       
        return received;
    }
    public boolean receive() throws IOException, InterruptedException {
    	
    	boolean result = false;
    	buf = new byte[32768];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        
    
        
        int data_size = packet.getLength();
        if (data_size >= 2092) {
            int bufferPos = 0;
            byte[] dataReceived = packet.getData();
            int rMagicWord = UsefulConvertFunctions.GetIntFromBEBuffer(dataReceived, bufferPos, 4);
            bufferPos += 4;
            int rsequenceNumber = UsefulConvertFunctions.GetIntFromBEBuffer(dataReceived, bufferPos, 4);
            bufferPos += 4;
            double rtimeStamp = UsefulConvertFunctions.GetDoubleFromBuffer(dataReceived, bufferPos, 8, ByteOrder.BIG_ENDIAN);
            bufferPos += 8;
            bufferPos += 8;//dummy
            int rdataSize = UsefulConvertFunctions.GetIntFromBEBuffer(dataReceived, bufferPos, 4);
            bufferPos += 4;
            double freqStart = UsefulConvertFunctions.GetDoubleFromBuffer(dataReceived, bufferPos, 8, ByteOrder.BIG_ENDIAN);
            bufferPos += 8;;
            double freqStep = UsefulConvertFunctions.GetDoubleFromBuffer(dataReceived, bufferPos, 8, ByteOrder.BIG_ENDIAN);
            bufferPos += 8;
            double[] spectrumData = new double[rdataSize];
            
            for(int i = 0; i<rdataSize; i++) {
            	spectrumData[i] = UsefulConvertFunctions.GetDoubleFromBuffer(dataReceived, bufferPos, 8, ByteOrder.BIG_ENDIAN);
                bufferPos += 8;
                //System.out.println(spectrumData[i]);
            }
            //wszystkie dane pobrane - przekazanie ich do czekających wątków
            Platform.runLater(()-> receiverDataConverter.notifyData(spectrumData, rsequenceNumber, rtimeStamp, freqStart, freqStep ));//działa ale sie lekko przycina
            
            //blockingSpectrumDataQueue.add(new PacketConverter(rMagicWord, rsequenceNumber, rtimeStamp, rdataSize, freqStart, freqStep, spectrumData));            
            //sharedSpectrumDataPacket.setPacket(rMagicWord, rsequenceNumber, rtimeStamp, rdataSize, freqStart, freqStep, spectrumData);

            //wy�wietlenie testowe
            //System.out.println("MW: " + rMagicWord + " rsequenceNumber: " + rsequenceNumber + " rtimeStamp: " + rtimeStamp);
            //System.out.println(" rdataSize: " + rdataSize + " freqStart: " + freqStart + " freqStep: " + freqStep);
            result = true;
        }
        //System.out.println(" Received packet length: " + packet.getLength() + " Reomte port: " + packet.getPort());
        return result;

    }
 
    public void close() { 		
        socket.close();
    }
    public void run() {
		running = true;
		int packetCounter = 0;
		
		while (running) {

			if (packetCounter < 10000) {

				try {
					boolean receiveResult = false;
					receiveResult = receive();
					System.out.println("Client received: " + receiveResult);
					if(receiveResult)
						packetCounter++;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Client1: " + e.getMessage());
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Client1: " + e.getMessage());
					//running = false;
				}

				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					System.out.println("Client1: " + e1.getMessage());
					running = false;
				}
				
				
			}
			else {
				running = false;//wy�aczmy iorach 10 odblientapo
			}
				

		}
		//koniec pracy 
    	sendMsg("end ");
		close();
	}
}


