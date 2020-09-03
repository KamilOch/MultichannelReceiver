package com.kdk.MultichannelReceiver.controllerCharts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.mockito.junit.MockitoJUnitRunner;

import com.kdk.MultichannelReceiver.controllerCharts.PRK_4ZoomableLineChart_2c_clean.ReceivedDataWithTimeStamp;

import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextField;

/**
* Klasa testowa dla metod klasy PRK_4ZoomableLineChart_2c_clean
 * Klasa umożliwia testowanie interface JavaFX.
 * Klasa wymaga JUnit 4.9, ponieważ metoda JavaFXThreadingRule() inicjalizująca JavaFX wymaga tej wersji JUnit.
* @author Damian Garsta / damiangarsta@wp.pl
* @version 1.0
*/
//@RunWith(MockitoJUnitRunner.class)
public class PRK_4ZoomableLineChart_2c_cleanTEST {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
	
	PRK_4ZoomableLineChart_2c_clean obj;
	
	@Before
	public void setUp()
	{
		obj = new PRK_4ZoomableLineChart_2c_clean();// Mockito.mock(ReceiverDataConverter.class),Mockito.mock(VBox.class), Mockito.mock(SpectrumDataProcessor.class));
	}
	/**
	 * Metoda testowa dla metody getElementFromQueue().
	 * Testowane jest wybiorcze wyciaganie elementow z kolejki oraz fakt ze kolejka jest posortowana wg wartosci timeStamp
	 */
	@Test 
	public void getElementFromQueueTEST()
	{

		PriorityBlockingQueue<ReceivedDataWithTimeStamp> queueData = new PriorityBlockingQueue<>(150,
				PRK_4ZoomableLineChart_2c_clean.comparator_queueData);
		
		Random generator = new Random();
	
		//put random data to the queue with random timeStamp:
		double receivedData[][] = new double[20][5];
		double timeStamp[] = new double[20];
		double orderedTimeStamp[] = new double [20];
		for(int j=0; j<20;j++)
		{
			for(int i=0; i<5; i++) {
				receivedData[j][i]=generator.nextDouble()*10;
			}
			
			timeStamp[j] = generator.nextDouble()*10;
			//System.out.println("timeStamp[j]="+timeStamp[j]);
			queueData.put(  obj.new ReceivedDataWithTimeStamp(receivedData[j], timeStamp[j], 10, 2));
			//System.out.println("queueData.peek().timeStamp="+queueData.peek().timeStamp);
		}
		
		int w=0;
		//element in the queue are ordered with timeStamp value and can be read not only from head:
		for (int i=0; i< queueData.size(); i++) {
			int iElement = i+1;//generator.nextInt(queueData.size());
			ObservableList<XYChart.Data<Number, Number>> data = obj.getElementFromQueue(iElement, queueData);
			
//			System.out.println("data.size()="+data.size());
			
			boolean bFound = false;

			for(int z=0; z<20; z++)
			{
				bFound = true;
				for(int k=0;k<5;k++)
				{
//					System.out.println("data->"+data.get(k).getYValue().doubleValue()+" != receivedData->"+receivedData[z][k]);
					if(data.get(k).getYValue().doubleValue() != receivedData[z][k])
					{
						bFound = false;
					}
				}
				
				if(bFound)
				{
					//System.out.println("FOUND z="+z+" timeStamp[z]="+timeStamp[z]);
					orderedTimeStamp[w] = timeStamp[z];
					w++;
				}
			}
			
		}
		
		for(int i=0; i< orderedTimeStamp.length-1;i++)
		{
//			System.out.println("orderedTimeStamp[i]="+orderedTimeStamp[i]);
			assertTrue( orderedTimeStamp[i] <= orderedTimeStamp[i+1] );
		}


	}
	
	
	/**
	 * Metoda testowa dla metody updateMarkedFreq().
	 * Metoda sprawdza czy pole tekstowe xMarkedField jest prawidlowo uaktualniane po wywołaniu metody updateMarkedFreq().
	 */
	@Test
	public void updateMarkedFreqTEST()
	{
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		obj.chart = new LineChart<>(xAxis, yAxis);
		obj.chart.setData(obj.generateChartData());
		Random rng = new Random();
		
		obj.xField = new TextField();
		obj.xMarkedField = new TextField();
		
		for(int i=0; i< obj.NUM_DATA_POINTS; i++)
		{
			obj.xField.setText(""+i);
			
			obj.updateMarkedFreq();
			
//			System.out.println("xField -> xMarkedField == chart " +obj.xField.getText() +" -> "+obj.xMarkedField.getText() + " == " + obj.chart.getData().get(0).getData().get(i).getYValue().toString() );
			String chartValue = obj.chart.getData().get(0).getData().get(i).getYValue().toString();
			String textBoxValue = obj.xMarkedField.getText();
			assertTrue(chartValue.equals(textBoxValue));
		}
		
	}

	
}