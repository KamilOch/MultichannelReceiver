package com.kdk.MultichannelReceiver.controllerCharts;

import javafx.scene.chart.Axis;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class WrappedImageView extends ImageView
{
	Axis<Number> xAxis;
	
    WrappedImageView(Axis<Number> xAxis)
    {
        setPreserveRatio(false);
        this.xAxis = xAxis;
    }

    @Override
    public double minWidth(double width)
    {
        return 40;
    }

    @Override
    public double prefWidth(double width)
    {
        Image I=getImage();
        if (I==null) return minWidth(width);
        return I.getWidth();
    }

    @Override
    public double maxWidth(double width)
    {
        return 16384;
    }

    @Override
    public double minHeight(double height)
    {
        return 40;
    }

    @Override
    public double prefHeight(double height)
    {
        Image I=getImage();
        if (I==null) return minHeight(height);
        return I.getHeight();
    }

    @Override
    public double maxHeight(double height)
    {
        return 16384;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public void resize(double width, double height)
    {
        setFitWidth(xAxis.getWidth());//width);
        setFitHeight(height);
    }
    
//    @Override
//    public DoubleProperty layoutXProperty()
//    {
//    	
//    }
    
}
