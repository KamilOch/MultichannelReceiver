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





/**
 * Klasa klienta UDP odbieraj¹cego dane z odbiornika i przekazuj¹ce odebrany i przepaklowany pakiet do klas s³uchaczy poprzez 
 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
 *
 */
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
    
    
    /**
     * Konstruktor Klasy ReceiverUDPClient.
     * @param String ipAddress - adres IP serwera UDP streamuj¹cego dane pomiarowe
     * @param int receivePort - numer portu na któey dane s¹ przey³ane
     * @param PacketConverter spectrumDataPacket - klasa pakietu z odebranymi danymi z odbiornika
     * @param ReceiverDataConverter dataConverter - 
     * @param ConcurrentLinkedQueue<PacketConverter> blockingQueue - (nieu¿ywana ze wzglêdu na s³ab¹ wydajnoœæ) kolejka FIFO z odebranymi pakiertami danych spectrumDataPacket z odbiornika doo dalszego przetwarzania
     * @param receivedRecordOneRawEntityRepository repozytorium zapisanych rekordÃ³w, zapis w 1 linii (krotce)
	 * @throws SocketException, UnknownHostException - rzucane wyj¹tki  
     */
    public ReceiverUDPClient(String ipAddress, int receivePort, PacketConverter spectrumDataPacket, ReceiverDataConverter dataConverter, ConcurrentLinkedQueue<PacketConverter> blockingQueue) throws SocketException, UnknownHostException {
		super();
		this.address = InetAddress.getByName(ipAddress);;
		this.receivePort = receivePort;
		this.sharedSpectrumDataPacket = spectrumDataPacket;//wspÃ³lna struktura z odebranymi danymi
		this.receiverDataConverter = dataConverter;
		this.blockingSpectrumDataQueue = blockingQueue;
		socket = new DatagramSocket(receivePort);
		socket.setReceiveBufferSize(32768);//maksymalny rozmiar bufora odbiorczego
        socket.setSoTimeout(500);//maksymalny czas oczekiwania na pakiet
        
	}
    
//	public ReceiverUDPClient(String ipAddress, int receivePort, PacketConverter spectrumDataPacket, ReceiverDataConverter dataConverter, BlockingQueue<PacketConverter> blockingQueue) throws SocketException, UnknownHostException {
//		super();
//		this.address = InetAddress.getByName(ipAddress);;
//		this.receivePort = receivePort;
//		this.sharedSpectrumDataPacket = spectrumDataPacket;//wspÃ³lna struktura z odebranymi danymi
//		this.receiverDataConverter = dataConverter;
//		this.blockingSpectrumDataQueue = blockingQueue;
//		socket = new DatagramSocket(receivePort);
//		socket.setReceiveBufferSize(32768);
//        socket.setSoTimeout(500);
//        
//	}
    /**
     * Uproszcony konstruktor Klasy ReceiverUDPClient.
     * @param String ipAddress - adres IP serwera UDP streamuj¹cego dane pomiarowe
	 * @throws SocketException, UnknownHostException - rzucane wyj¹tki  
     */ 
	public ReceiverUDPClient(String ipAddress) throws SocketException, UnknownHostException {
        socket = new DatagramSocket(4445);
        socket.setReceiveBufferSize(32768);
        socket.setSoTimeout(500);
        
        address = InetAddress.getByName(ipAddress);
        
    }
	
	/**
     * Metoda zwracaj¹ca adres IP swerwera odbiornika.
     * @return InetAddress address - zwraca odres IP serwera.  
     */ 
    public InetAddress getAddress() {
		return address;
	}

    /**
     * Metoda zwracaj¹ca numer portu do nas³uchu danych z serwera odbiornika.
     * @return int receivePort - zwraca numer portu na którym nas³uchuje danych z serwera odbiornika.  
     */ 
	public int getReceivePort() {
		return receivePort;
	}

	/**
     * Metoda wysy³aj¹ca pakiet UDP.
     * @param String msg - wysy³any pakiet typu String. 
     * @return String received - zwraca odebrany pakiet odpowiedzi.  
     */ 
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
    
    /**
     * Metoda odbieraj¹ca pakiety UDP z gniazda, wyci¹gaj¹ca dane z pakietu i notyfikuj¹ca wszystkich s³uchaczy klasy receiverDataConverter o odbierze danych.
     * @return boolean result - zwraca status odbioru (true jeœli rozmioar pakietu jest w³aœciwy).  
     */
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
            //wszystkie dane pobrane - przekazanie ich do czekajÄ…cych wÄ…tkÃ³w
            Platform.runLater(()-> receiverDataConverter.notifyData(spectrumData, rsequenceNumber, rtimeStamp, freqStart, freqStep ));//dziaÅ‚a ale sie lekko przycina
            
            //blockingSpectrumDataQueue.add(new PacketConverter(rMagicWord, rsequenceNumber, rtimeStamp, rdataSize, freqStart, freqStep, spectrumData));            
            //sharedSpectrumDataPacket.setPacket(rMagicWord, rsequenceNumber, rtimeStamp, rdataSize, freqStart, freqStep, spectrumData);

            //wyï¿½wietlenie testowe
            //System.out.println("MW: " + rMagicWord + " rsequenceNumber: " + rsequenceNumber + " rtimeStamp: " + rtimeStamp);
            //System.out.println(" rdataSize: " + rdataSize + " freqStart: " + freqStart + " freqStep: " + freqStep);
            result = true;
        }
        //System.out.println(" Received packet length: " + packet.getLength() + " Reomte port: " + packet.getPort());
        return result;

    }
    /**
     * Metoda zamykaj¹ca gniazdo 
     */
    public void close() { 		
        socket.close();
    }
    /**
     * Metoda run w¹tku odbiorczego klienta UDP.  
     */
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
				running = false;//wy³¹czamy w¹tek klienta odbiorczego po 10000 pakietach (tymczasowo)
			}
				

		}
		//koniec pracy 
    	sendMsg("end ");//przesy³amy polecenie wy³aczenia serwera odbiornika
		close();//zamykamy gniazdo
	}
}


