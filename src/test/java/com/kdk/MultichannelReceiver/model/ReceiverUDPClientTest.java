package com.kdk.MultichannelReceiver.model;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import javafx.application.Platform;
import udp_socket.UDPDemoServer;

import org.junit.BeforeClass;
//import org.testfx.api.FxRobot;
//import org.testfx.api.FxToolkit;

import static org.junit.Assert.*;

//import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;

/**
 * Klasa testowa dla ReceiverUDPClient - testowanie zdarzenia onDataReceived z klasy receiverDataConverter (notifyData)
 * Do prawidłowego działania klasa wymaga uruchomienia wcześniej symulatora serwera odbiornika SymulatorOdbiornika2.exe
 * @author Kamil Wilgucki k.wilgucki@wil.waw.pl
 *
 */
public class ReceiverUDPClientTest implements ReceiverDataConverterListener{
		
	//wątek odbiorczy
	public ReceiverUDPClient udpClientThread;
	public ReceiverDataConverter dataConverter;
	private	boolean bReception = false;
	int tempSeqNumb = 0;
	int firstSeqNumb = 0;
	
	
	
	@BeforeEach
	@Timeout(5)
	public void setup( )throws Exception {
		//WaitForAsyncUtils.waitForFxEvents();
		System.out.println("setup");
       
		Thread.sleep(1250);
		dataConverter = new ReceiverDataConverter(null);  
		dataConverter.addListener(this);
		new UDPDemoServer().start();
		
//		try {
//
//			udpClientThread = new ReceiverUDPClient("localhost", 4445, null, dataConverter, null);
//			System.out.println("UDPClient started");
//			//udpClientThread.start();
//
//		}
//
//		catch (SocketException | UnknownHostException e1) {
//			// TODO Auto-generated catch block
//			System.out.println("UDP_Test1: " + e1.getMessage());
//		}
	}
	
	
		
	
//	@Test
//    void connectToUDPServerAndReceiveOnePacket(){
//		
//		
//		
//		try {
//			
//			udpClientThread = new ReceiverUDPClient("localhost", 4445, null, dataConverter, null);
//			System.out.println("UDPClient started");
//			;
//			try {
//
//				assertTrue(udpClientThread.receiveTest(true));
//				
//				System.out.println("getReceivePort: " + udpClientThread.getReceivePort() );
//				
//				assertTrue(udpClientThread.getReceivePort() == 4445);
//				
//				System.out.println("getAddress: " + udpClientThread.getAddress() );
//				
//				InetAddress address = InetAddress.getByName("localhost");
//				
//				//byte[] addr = new byte[] { 127, 0, 0, 1 };  
//				//InetAddress address2 = InetAddress.getByAddress(addr);
//				//assertTrue(udpClientThread.getAddress() == address2);
//
//				
//			} catch (IOException | InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//
//			
//
//			
//	} 
//		catch (SocketException | UnknownHostException e1) {
//			// TODO Auto-generated catch block
//			System.out.println("UDP_Test1: " + e1.getMessage());
//		}
//
//		
//		
//	}
	
	
	@Test
    void connectToUDPServerAndReceive(){		
		
		try {
			Thread.sleep(1250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			
			udpClientThread = new ReceiverUDPClient("localhost", 4445, null, dataConverter, null);
			//udpClientThread.start();//uruchomienie wątka odbiorczego (z Fx)
			udpClientThread.runTest();//uruchomienie wątka odbiorczego (bez Fx'a) - odbiór 100 pakietów
			System.out.println("UDPClient thread started");
//
			bReception = true;
			
			
			System.out.println("getReceivePort: " + udpClientThread.getReceivePort() );
			
			assertTrue(udpClientThread.getReceivePort() == 4445);
			
			System.out.println("getAddress: " + udpClientThread.getAddress() );
			
			InetAddress address = InetAddress.getByName("localhost");
			

			
		}
		catch (SocketException | UnknownHostException e1) {
			// TODO Auto-generated catch block
			System.out.println("UDP_Test1: " + e1.getMessage());
		}	
		
		
	}
	
	@AfterEach
    void tearDown(){	
		System.out.println("tearDown");
		
		bReception = false;
		System.out.println("UDPClient stopped");		
		
	}







	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		
		//testujemy zdarzenie onDataReceived z aktualnymi danymi z odbiornika
		System.out.println("onDataReceived");
		System.out.println("rsequenceNumber: " + seqNumber + " rtimeStamp: " + timeStamp);
    	System.out.println(" rdataSize: " + dataSize + " freqStart: " + freqStart + " freqStep: " + freqStep);

    	
    	//sprawdamy zakres wartości danych przychodzących
    	for(int i =0; i< receivedData.length ;i++) {
    		assertTrue(receivedData[i] > -160);
    		assertTrue(receivedData[i] < 40);
    	}
    	
    	// sprawdzamy rozmiar danych przychodzących 
    	assertTrue(dataSize >= 256);
    	assertTrue(dataSize <= 8192);
    	assertTrue(receivedData.length == dataSize);
    	
    	//sprawdzamy numer sekwencyjny
    	assertTrue(seqNumber >= 0);
   	
    	if(tempSeqNumb > 0) {//pomijamy pierwszy pakiet z niezainicjalizowanym tempSeqNumb
    		assertTrue(seqNumber  - tempSeqNumb == 1); //zmiana numeru sekwencyjnego o jeden   	
    		
    	}
    	else
    		firstSeqNumb = seqNumber;
    		
    	tempSeqNumb = seqNumber;
    	
    	//sprawdzamy TimeStamp
    	assertTrue(timeStamp >= 0);
    	
    	//sparawdzamy wartoć częsctotliwości poczatkowej
    	assertTrue(freqStart >= 250000);
    	
    	//sparawdzamy wartoć kroku częsctotliwości 
    	assertTrue(freqStep >= 1);   
    	
    	

    	
    		
    		
		
	}



	
	

}
