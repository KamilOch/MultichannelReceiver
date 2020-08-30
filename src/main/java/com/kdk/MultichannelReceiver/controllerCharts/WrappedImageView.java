package com.kdk.MultichannelReceiver.controllerCharts;

import javafx.scene.chart.Axis;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
/**
* Klasa nadpisujaca oryginalna klase JAVA ImageView
* wyswietla ona wykres Waterfall
* potrzebena jest aby umozliwic prawidlowa zmiane rozmiaru obrazka
* . . .
* @autor Damian Garsta / damiangarsta@wp.pl
* @version 1.0
*/
class WrappedImageView extends ImageView
{
	Axis<Number> xAxis;
	
	/**
	* Konstruktor. Ustala ze obrazek nie musi miec zawsze tych samych proporcji.
	* . . .
	* @param xAxis obiekt osi X
	*/
    WrappedImageView(Axis<Number> xAxis)
    {
        setPreserveRatio(false);
        this.xAxis = xAxis;
    }

	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia minimalnej szerokosci.
	* . . .
	* @param width minimalna szerokosc
	* @return zwraca zawsze okreslona wartosc
	*/
    @Override
    public double minWidth(double width)
    {
        return 40;
    }
    
	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia preferowanej szerokosci.
	* . . .
	* @param width preferowana szerokosc
	* @return zwraca szerokosc obrazka
	*/
    @Override
    public double prefWidth(double width)
    {
        Image I=getImage();
        if (I==null) return minWidth(width);
        return I.getWidth();
    }

	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia maksymalnej szerokosci.
	* . . .
	* @param width maksymalna szerokosc
	* @return zwraca zawsze okreslona wartosc
	*/
    @Override
    public double maxWidth(double width)
    {
        return 20000;
    }
    
	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia minimalnej wysokosci.
	* . . .
	* @param width minimalna wysokosc
	* @return zwraca zawsze okreslona wartosc
	*/
    @Override
    public double minHeight(double height)
    {
        return 40;
    }
    
	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia preferowanej wysokosci.
	* . . .
	* @param width preferowana wysokosc
	* @return zwraca wysokosc obrazka
	*/
    @Override
    public double prefHeight(double height)
    {
        Image I=getImage();
        if (I==null) return minHeight(height);
        return I.getHeight();
    }

	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia maksymalnej wysokosci.
	* . . .
	* @param width maksymalna wysokosc
	* @return zwraca zawsze okreslona wartosc
	*/
    @Override
    public double maxHeight(double height)
    {
        return 20000;
    }

	/**
	* Metoda nadpisujaca metode klasy ImageView dla okreslenia czy mozna zmieniac rozmiar obrazka.
	* . . .
	* @return zwraca zawsze wartosc true
	*/
    @Override
    public boolean isResizable()
    {
        return true;
    }
    
	/**
	* Metoda nadpisujaca metode klasy ImageView zmianiajaca rozmiar obrazka.
	* Okresla szerokosc obrazka na szerokosc osi X wykresu 1D.
	* . . .
	* @param width szerokosc obrazka ktora jest pomijana
	* @param height wysokosc obrazka
	*/
    @Override
    public void resize(double width, double height)
    {
        setFitWidth(xAxis.getWidth());//width);
        setFitHeight(height);
    }
    
    
}
