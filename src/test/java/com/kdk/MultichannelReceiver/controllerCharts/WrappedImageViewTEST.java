package com.kdk.MultichannelReceiver.controllerCharts;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.image.WritableImage;

/**
 * Klasa testowa dla WrappedImageView.
 * Klasa umożliwia testowanie interface JavaFX.
 * Klasa wymaga JUnit 4.9, ponieważ metoda JavaFXThreadingRule() inicjalizująca JavaFX wymaga tej wersji JUnit.
 * @author Damian Garsta / damiangarsta@wp.pl
 *
 */
public class WrappedImageViewTEST {

	//needs JUnit 4.9:
	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

	private WrappedImageView imageTest;
	LineChart<Number, Number> chart;
	NumberAxis axis1,axis2;
	
	@Before
	public void SetUp( ) {
		axis1 = new NumberAxis();
		axis1.setLowerBound(4);
		axis1.setUpperBound(300);
		
		axis1.setMinWidth(100);
		axis1.setPrefWidth(100);
		axis1.setMaxWidth(100);
		
		axis2 = new NumberAxis();
		axis2.setLowerBound(4);
		axis2.setUpperBound(300);
		
		chart = new LineChart<>(axis1, axis2);
		chart.setPrefSize(400,400);
		chart.setMaxSize(400,400);
		chart.setMinSize(400,400);
		
		imageTest = new WrappedImageView(axis1);
	}
	
	/**
	 * Metoda testowa dla metody minWidth().
	 * Metoda zwraca zawsze wartość 40.
	 */
	@Test
	public void minWidthTEST()
	{		
		assertEquals(40, imageTest.minWidth(1000));
	}
	
	/**
	 * Metoda testowa dla metody minHeight().
	 * Metoda zwraca zawsze wartość 40.
	 */
	@Test
	public void minHeightTEST()
	{		
		assertEquals(40, imageTest.minHeight(1000));
	}
	
	/**
	 * Metoda testowa dla metody resize(double width, double height).
	 * BEZ POKAZANIA SCENY NAJPRAWDOPODBNIEJ ROZMIARY KONTROLEK NIE MOGA BYC ZMIENIANE
	 * WIEC FUNKCJA NIE MOZE PRZETESTOWAC ZMIANY ROZMIARU.
	 * Wysokość ImageView taka jak wysokość obrazka Image
	 * a szerokość taka jak szerokość osi na wykresie.
	 */
	@Test
	public void resizeTEST_FilledInImageView()
	{		
		int axisWidth = 100;
		int prefHeight = 200;
		int imageWidth = 777;
		int imageHeight = 666;
		
		imageTest.setImage(new WritableImage(imageWidth,imageHeight));
		
		axis1.setMinWidth(axisWidth);
		axis1.setPrefWidth(axisWidth);
		axis1.setMaxWidth(axisWidth);
		
		axis1.setPrefHeight(prefHeight);
		
//		System.out.println( imageTest.getFitWidth() + " , " + imageTest.getFitHeight());
		
//		assertEquals(axisWidth, axis1.getWidth());
//		
//		imageTest.resize(555, imageHeight);
//		
//		System.out.println( imageTest.getFitWidth() + " , " + imageTest.getFitHeight());
//		assertEquals(imageHeight, imageTest.getFitHeight());
//		assertEquals(axisWidth, imageTest.getFitWidth());
	}
	

}
