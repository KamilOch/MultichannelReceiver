package com.kdk.MultichannelReceiver.model.utils;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Zawiera zbi�r statycznych metod do konwersji i oblicze�
 *
 * @author Robert Urban <r.urban@wil.waw.pl>
 * @author Kamil Wilgucki <k.wilgucki@wil.waw.pl>
 * 
 */
public class UsefulConvertFunctions {

    public static short twoBytesToShort(byte[] myArray) {
        short result = ByteBuffer.wrap(myArray).getShort();
        return result;
    }
    public static int GetIntFromLEBuffer(byte[] myArray, int StartPos, int Count){
        int result = 0;    
        byte[] tempIntBytes = {0,0,0,0};                       
        System.arraycopy(myArray, StartPos, tempIntBytes, 0, 4);
        
        result = fourBytesToIntLE(tempIntBytes);        
        return result;    
    }
    
    public static int GetIntFromBEBuffer(byte[] myArray, int StartPos, int Count){
        int result = 0;    
        byte[] tempIntBytes = {0,0,0,0};                       
        System.arraycopy(myArray, StartPos, tempIntBytes, 0, 4);
        
        result = fourBytesToIntBE(tempIntBytes);        
        return result;    
    }
    
    public static int GetIntFromBuffer(byte[] bytes, int bufferPos, int CountBytes, ByteOrder ByteOrderValue) {
    	int result = 0;    
        byte[] tempIntBytes =new byte[CountBytes];                       
        System.arraycopy(bytes, bufferPos, tempIntBytes, 0, CountBytes);
        
        
        result = ByteBuffer.wrap(tempIntBytes).order(ByteOrderValue).getInt();
        
        return result;	
    	
    }
    public static short GetShortFromBuffer(byte[] bytes, int bufferPos, int CountBytes, ByteOrder ByteOrderValue) {
    	short result = 0;    
        byte[] tempIntBytes =new byte[CountBytes];                       
        System.arraycopy(bytes, bufferPos, tempIntBytes, 0, CountBytes);
        
        
        result = ByteBuffer.wrap(tempIntBytes).order(ByteOrderValue).getShort();
        
        return result;	
    	
    }
    public static short Get1ByteFromBuffer(byte[] bytes, int bufferPos, int CountBytes, ByteOrder ByteOrderValue) {
    	short result = 0;    
        byte[] tempIntBytes =new byte[CountBytes];                       
        System.arraycopy(bytes, bufferPos, tempIntBytes, 0, CountBytes);
        
        result = ByteBuffer.wrap(tempIntBytes).order(ByteOrderValue).get(0);//index 0
        
        return result;	
    	
    }
    public static boolean getBooleanFromBuffer(byte[] bytes, int bufferPos, int CountBytes, ByteOrder ByteOrderValue) {
    	boolean result = false;    
        byte[] tempIntBytes =new byte[CountBytes];                       
        System.arraycopy(bytes, bufferPos, tempIntBytes, 0, CountBytes);
        result = tempIntBytes[0] > 0;      

        return result;	
    	
    }
    
    public static double GetDoubleFromBuffer(byte[] bytes, int bufferPos, int CountBytes, ByteOrder ByteOrderValue) {
    	double result = 0;    
        byte[] tempDoubleBytes = {0,0,0,0,0,0,0,0};                       
        System.arraycopy(bytes, bufferPos, tempDoubleBytes, 0, CountBytes);        
        
        result = ByteBuffer.wrap(tempDoubleBytes).order(ByteOrderValue).getDouble();
        
        return result;  	
    	
    }  
    
    
      
    
    public static int fourBytesToIntBE(byte[] myArray){
        int result = (myArray[0] << 24) & 0xff000000;
        result |= (myArray[1] << 16) & 0x00ff0000;
        result |= (myArray[2] << 8) & 0x0000ff00;
        result |= (myArray[3] & 0x000000ff);

        return result; 
    }

    public static int fourBytesToIntLE(byte[] myArray){
        int result = (myArray[3] << 24) & 0xff000000;
        result |= (myArray[2] << 16) & 0x00ff0000;
        result |= (myArray[1] << 8) & 0x0000ff00;
        result |= (myArray[0] & 0x000000ff);

        return result; 
    }

    public static int threeBytesToInt(byte[] myArray) {

        int result = (myArray[0] << 16) & 0x00ff0000;
        result |= (myArray[1] << 8) & 0x0000ff00;
        result |= (myArray[2] & 0x000000ff);
        //int temp = (myArray[0] & 0x000000ff);

//        int result =0;
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(2);
//        byteBuffer.order(byteOrder);
//        byteBuffer.put(myArray);
//        byteBuffer.flip();
//        result = byteBuffer.getShort();
        return result;
    }
    
    public static byte[] intToBytes(int myInt, ByteOrder byteOrder) {
        byte[] result = ByteBuffer.allocate(4).order(byteOrder).putInt(myInt).array();
        return Arrays.copyOfRange(result, 0, 4);
    }

    public static byte[] shortToBytes(int myShort, ByteOrder byteOrder) {
        byte[] result = ByteBuffer.allocate(4).order(byteOrder).putInt(myShort).array();
        return Arrays.copyOfRange(result, 0, 2);
    }

    public static byte[] shortToBytes(int myShort) {
        return shortToBytes(myShort, ByteOrder.LITTLE_ENDIAN);
    }

    public static byte[] doubleToBytes(double myDouble, ByteOrder byteOrder) {
        byte[] result = ByteBuffer.allocate(8).order(byteOrder).putDouble(myDouble).array();
        return Arrays.copyOfRange(result, 0, 8);
    }
    
    public static int convertAzimuthToMilRadian(double degree) {
        double result = 0;
        result = degree * (Math.PI * 10000 / 180);
        return (int) Math.round(result);//(int)Math.round(result);
    }

    public static int convertElevationToMilRadian(double degree) {
        if (degree < 0) {
            degree += 360;
        }
        return convertAzimuthToMilRadian(degree);

    }

    public static int convertAzimuthToDegree(int milRadian) {
        double result = 0;
        result = milRadian * (180 / (Math.PI * 10000));
        return (int) Math.round(result);
    }

    public static int convertElevationToDegree(int milRadian) {
        if (milRadian > 58000) {
            milRadian -= 62831;
        }
        return convertAzimuthToDegree(milRadian);
    }
    
    public static byte[] CalcCheckSumBEndian(byte[] buffer, int buffer_size){
        
        int CHSPos = buffer_size - 4;
        int CHS = 0;
        for(int i = 0; i < CHSPos ; i++)
            CHS+= buffer[i] & 0x000000ff;
        
        System.arraycopy(UsefulConvertFunctions.intToBytes(CHS, ByteOrder.BIG_ENDIAN), 0, buffer, CHSPos, 4);     
        
        return buffer;
    }

    public static byte[] intToBytes(boolean myBoolean, ByteOrder byteOrder) {
        byte boleanValue = 0;
        if(myBoolean == true)
            boleanValue = 1;
            
        byte[] result = ByteBuffer.allocate(1).order(byteOrder).put(boleanValue).array();             

        return Arrays.copyOfRange(result, 0, 1);
    }

    public static double GetDoubleFromBEBuffer(byte[] bytes, int bufferPos, int CountBytes) {
    	double result = 0;    
        byte[] tempIntBytes = {0,0,0,0,0,0,0,0};                       
        System.arraycopy(bytes, bufferPos, tempIntBytes, 0, 8);        
        
        result = ByteBuffer.wrap(tempIntBytes).order(ByteOrder.BIG_ENDIAN).getDouble();
        
        return result;
    }

    public static double GetDoubleFromLEBuffer(byte[] bytes, int bufferPos, int CountBytes) {
    	double result = 0;    
        byte[] tempIntBytes = {0,0,0,0,0,0,0,0};                       
        System.arraycopy(bytes, bufferPos, tempIntBytes, 0, 8);
        
        
        result = ByteBuffer.wrap(tempIntBytes).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        
        return result;
    	
    	
    }
    
    
    public static double[] ByteArrayToDoubleArray(byte[] byteArray, ByteOrder ByteOrderValue){
        
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[byteArray.length / times];
        
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(byteArray, i*times, times).order(ByteOrderValue).getDouble();
        }
        return doubles;
    }
    public static double[] ByteArrayToDoubleArray(byte[] byteArray, int bufferPos, int doubleArraySize, ByteOrder ByteOrderValue){
        
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[doubleArraySize];
        
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(byteArray, bufferPos + i*times, times).order(ByteOrderValue).getDouble();
        }
        return doubles;
    }

    public static int[] ByteArrayToIntArray(byte[] byteArray, int bufferPos, int intArraySize, ByteOrder ByteOrderValue) {
        int times = 4;//int size 
        int[] ints = new int[intArraySize];
        
        for(int i=0;i<ints.length;i++){
            ints[i] = ByteBuffer.wrap(byteArray, bufferPos + i*times, times).order(ByteOrderValue).getInt();
        }
        return ints;
    }
    
    public static char[] byteArrayToCharArray(byte[] byteArray) {
    
        char[] charBuffer = new char[byteArray.length];
        
        for (int i = 0; i < byteArray.length; i++) {
            charBuffer[i] = (char) (byteArray[i] & 0xff);
        }
        
        return charBuffer;
    }
    public static byte[] charArrayToByteArray(char[] charArray) {
        byte[] byteArray = new byte[charArray.length];
        
        for(int i =0;i<charArray.length;i++){
                byte[] tempByte = UsefulConvertFunctions.shortToBytes(charArray[i], ByteOrder.LITTLE_ENDIAN);//gdy przetwarzamy na procesorze INTEL
                byteArray[i] = tempByte [0];
        }
        return byteArray;
    }
    

}


//To get a readable String back from a byte[], use:
//String string = new String(byte[] bytes, Charset charset);

 //String example = "Convert Java String";
  //byte[] bytes = example.getBytes();


//return new String(byteout.toByteArray(Charset.forName("UTF-8")))

 //byte[] byteValueAscii= letters.getBytes("US-ASCII");
 //   System.out.println(Arrays.toString(byteValueAscii));

/*
public static byte[] asBytes (String s) {                   
           String tmp;
           byte[] b = new byte[s.length() / 2];
           int i;
           for (i = 0; i < s.length() / 2; i++) {
             tmp = s.substring(i * 2, i * 2 + 2);
             b[i] = (byte)(Integer.parseInt(tmp, 16) & 0xff);
           }
           return b;                                            //return bytes
    }

This decodes hex-encoded byte array.
*/

/*
I've following scala code to convert (short, int, long, float, double,bigint) to byte array.
def getByteArray(value: Any, of_type: String) = {
    of_type match {
      case "short" => ByteBuffer.allocate(2).putShort(value.asInstanceOf[Short]).array()
      case "int" => ByteBuffer.allocate(4).putInt(value.asInstanceOf[Int]).array()
      case "long" => ByteBuffer.allocate(8).putLong(value.asInstanceOf[Long]).array()
      case "float" => ByteBuffer.allocate(4).putFloat(value.asInstanceOf[Float]).array()
      case "double" => ByteBuffer.allocate(8).putDouble(value.asInstanceOf[Double]).array()
      case "bigint" => BigInt(value.toString).toByteArray
    }
  }
*/


        /*
             System.arraycopy(sourceArray, 
                 sourceStartIndex,
                 targetArray,
                 targetStartIndex,
                 length);

                Example,

                String[] source = { "alpha", "beta", "gamma" };
                String[] target = new String[source.length];
                System.arraycopy(source, 0, target, 0, source.length);         
     
        */
