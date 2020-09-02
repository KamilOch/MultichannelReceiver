/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package udp_socket;

import util.UsefulConvertFunctions;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.sql.Timestamp;




/**
 * UDPDemoServer to symulator serwera odbiornika radiowego. Funkcjonalno�� - generacja danych widma z odbiornika radiowego zgodnie z jego formatem danych i streamowanie danych widma do Klienta UDP na port 4445.
 *  header:
 *  int -  magicWord 4 bytes (0xAAAA5667)
 *  int - sequenceNumber 4 bytes
 *  double - timeStamp 8 bytes
 *  double - dummy value 8 bytes
 *  int - spectrumDataSize 4 bytes
 *  double - freqStart 8 bytes
 *  double - freqStep 8 bytes
 *  end of header
 *  spectrum data: 
 *  double[spectrumDataSize * 8 bytes] 
 *  
 * @author Kamil Wilgucki k.wilgucki@wil.waw.pl 
 */





public class UDPDemoServer extends Thread {

	private DatagramSocket socket;
	private boolean running;
	private byte[] buf = new byte[32768];

	public UDPDemoServer() {
		try {
			socket = new DatagramSocket(4444);
			socket.setSendBufferSize(8192);
			socket.setSoTimeout(5);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			System.out.println("UDPDemoServer: " + e.getMessage());
		}

	}

	/**
	 * Metoda run() g��wnego w�tku symulatora serwera odbiornika radiowwego, generuje losowe dane i wysy�a pakiet UDP.
	 */
	public void run() {
		running = true;
		// do generacji
		int repetitionCounter = 0;
		int fixedFreq = 0;
		//boolean initialized = false;
		InetAddress address = null;
                try {
                    address = InetAddress.getByName("localhost");
                } catch (UnknownHostException ex) {
                   ;
                   //Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
                }
		int port = 4445;
		int sequenceNumber = 0;
		//
                System.out.println("UDPDemoServer started");

		while (running) {

			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				address = packet.getAddress();
				port = packet.getPort();

				packet = new DatagramPacket(buf, buf.length, address, port);
				String received = new String(packet.getData(), 0, packet.getLength());

				System.out.println("Server received: " + received);
				if (received.startsWith("end")) {
					running = false;
					System.out.println("Server shutdown");
					continue;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("UDPDemoServer: " + e.getMessage());
			}

			// wysylanie pakietu z symulowanymi danymi
			try {
				int headerSize = 44;
				int spectrumDataSize = 256;
				int out_tab_lenght = headerSize + spectrumDataSize * 8;

				int magicWord = 0xAAAA5667;
				sequenceNumber += 1;
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				double timeStamp = timestamp.getTime();
				double freqStart = 30000000;// 30M
				double freqStep = 10000;// 10k
				double[] receivedData = new double[spectrumDataSize];

				
				for (int i = 0; i < receivedData.length; i++) {
					double rndnumb = 10 * Math.random();
					receivedData[i] = (rndnumb > 9.95) ? rndnumb + 90 * Math.random() : rndnumb;
                                        receivedData[i] = receivedData[i] -  120;

					if (i == 45) {
						if (repetitionCounter < 20) {
							receivedData[i] = 90 + 5 * Math.random();// sta�y sygna� na tej samej cz�stotliwos�i
                                                        receivedData[i] = receivedData[i] -  120;
							repetitionCounter++;
						} else { // losowanie
							if (100 * Math.random() > 95) {
								repetitionCounter = 0;
								fixedFreq = (int) Math.abs(Math.round(spectrumDataSize * Math.random()));
							}
						}
					}
					if (i == fixedFreq) {
						if (repetitionCounter < 20) {
							receivedData[i] = 90 + 5 * Math.random();// sta�y sygna� na tej samej cz�stotliwos�i
                                                        receivedData[i] = receivedData[i] -  120;
						}

					}

				}
				 

				byte[] out_tab = new byte[out_tab_lenght];
				int bufferPos = 0;
				// header
				System.arraycopy(UsefulConvertFunctions.intToBytes(magicWord, ByteOrder.BIG_ENDIAN), 0, out_tab,
						bufferPos, 4);// magicWord 4 bytes
				bufferPos += 4;
				System.arraycopy(UsefulConvertFunctions.intToBytes(sequenceNumber, ByteOrder.BIG_ENDIAN), 0, out_tab,
						bufferPos, 4); // sequenceNumber 4 bytes
				bufferPos += 4;
				System.arraycopy(UsefulConvertFunctions.doubleToBytes(timeStamp, ByteOrder.BIG_ENDIAN), 0, out_tab,
						bufferPos, 8);// timeStamp 8 bytes
				bufferPos += 8;
				System.arraycopy(UsefulConvertFunctions.doubleToBytes(0, ByteOrder.BIG_ENDIAN), 0, out_tab, bufferPos,
						8); // dummy value
				bufferPos += 8;
				System.arraycopy(UsefulConvertFunctions.intToBytes(spectrumDataSize, ByteOrder.BIG_ENDIAN), 0, out_tab,
						bufferPos, 4);// spectrumDataSize 4 bytes
				bufferPos += 4;
				System.arraycopy(UsefulConvertFunctions.doubleToBytes(freqStart, ByteOrder.BIG_ENDIAN), 0, out_tab,
						bufferPos, 8); // freqStart - 8 bytes
				bufferPos += 8;
				System.arraycopy(UsefulConvertFunctions.doubleToBytes(freqStep, ByteOrder.BIG_ENDIAN), 0, out_tab,
						bufferPos, 8); // freqStep - 8 bytes
				bufferPos += 8;
				// end of header
				// pectrum data
				for (int i = 0; i < spectrumDataSize; i++) {
					System.arraycopy(UsefulConvertFunctions.doubleToBytes(receivedData[i], ByteOrder.BIG_ENDIAN), 0,
							out_tab, bufferPos, 8);
					bufferPos += 8;
				}

				DatagramPacket test_packet = new DatagramPacket(out_tab, out_tab_lenght, address, port);
				socket.send(test_packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("UDPDemoServer: " + e.getMessage());
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				System.out.println("UDPDemoServer: " + e1.getMessage());
			}

		}
		socket.close();
	}
}
