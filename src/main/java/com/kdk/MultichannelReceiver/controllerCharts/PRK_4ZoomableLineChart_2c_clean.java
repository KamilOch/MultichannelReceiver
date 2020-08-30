package com.kdk.MultichannelReceiver.controllerCharts;


import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

import com.kdk.MultichannelReceiver.model.ReceiverDataConverter;
import com.kdk.MultichannelReceiver.model.ReceiverDataConverterListener;
import com.kdk.MultichannelReceiver.model.SpectrumDataProcessor;
import com.kdk.MultichannelReceiver.model.SpectrumWaterfall;
import com.kdk.MultichannelReceiver.model.SpectrumWaterfallListener;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class PRK_4ZoomableLineChart_2c_clean /*extends Application*/ implements SpectrumWaterfallListener, ReceiverDataConverterListener {

	public VBox chartsVbox;
    public static final int NUM_DATA_POINTS = 250;
    public static final int X_MIN = 30000000;
    public static final int X_MAX = 30000000+ 10500*NUM_DATA_POINTS;    
    public static final int Y_MIN = -150;
    public static final int Y_MAX = 50;

    //private static final int X_LABEL_HEIGHT = 50;
    //private static final int TITLE_HEIGHT = 30;
    Task<Void> task;
    static LineChart<Number, Number> chart;
    Series<Number, Number> series;
    boolean bStop = false;
    static int nrTimeStamp=-1;
    
    private TextField xField;
    private TextField yField;//to samo co tresholdField
    
    private String xFieldPrzed;
    private String yFieldPrzed;
    
    private TextField xMarkedField;
//    ObservableList<XYChart.Data<Number, Number>> data;
    double xMarked;
    
    Spinner<Integer> spinner;
    
    StackPane waterfallContainer;
    private static ImageView imageView;
    SpectrumWaterfall spectrumWaterfall = new SpectrumWaterfall(NUM_DATA_POINTS) ;
    SpectrumDataProcessor spectrumProcessor;
    
    static Comparator<ReceivedDataWithTimeStamp> comparator_queueData = (o1, o2) -> {
    	double ts1 = o1.timeStamp;
    	double ts2 = o2.timeStamp;

    	return Double.compare(ts1, ts2);
    };
    private static PriorityBlockingQueue<ReceivedDataWithTimeStamp> queueData = new PriorityBlockingQueue<>(150,comparator_queueData);
    ReceiverDataConverter dataConverter;//new ReceiverDataConverter();
    		
    //konstruktor:
    public PRK_4ZoomableLineChart_2c_clean(ReceiverDataConverter dataConverter, VBox vboxCharts,SpectrumDataProcessor spectrumProcessor)
    {
    	this.dataConverter = dataConverter;
    	this.spectrumProcessor = spectrumProcessor;
    	//vboxCharts;
    	vboxCharts.getChildren().remove(0, vboxCharts.getChildren().size());
    	start(vboxCharts);
    }
	public void start(VBox vboxCharts) {
		
		dataConverter.addListener(this);
		
		chart = createChart();
	
		final StackPane chartContainer = new StackPane();
		chartContainer.getChildren().add(chart);
		
		final Rectangle zoomRect = new Rectangle();
		zoomRect.setManaged(false);
		zoomRect.setFill(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
		
		final Line lineVert = new Line();
		lineVert.setManaged(false);
		lineVert.setStroke(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
		lineVert.setStrokeWidth(3);
	
		final Line lineHor = new Line();
		lineHor.setManaged(false);
		lineHor.setStroke(Color.AQUAMARINE.deriveColor(0, 1, 1, 0.5));
		lineHor.setStrokeWidth(3);
		
		chartContainer.getChildren().addAll(zoomRect, lineVert, lineHor);
		
		setUpZooming(zoomRect, chart, lineVert, lineHor);
		
		setUpVertLine(lineVert, chart);
		
		setUpHorLine(lineHor, chart);
		
		
		
//		data = FXCollections.observableArrayList();
//		series.setData(data);
//		Random generator = new Random();
//		for (int i =0; i<NUM_DATA_POINTS;i++)
//		{
//			data.add( new XYChart.Data<>( i, generator.nextDouble()*900) );
//		}
		
		final HBox controls = new HBox(10);
		controls.setPadding(new Insets(0,0,0,50));
		controls.setAlignment(Pos.CENTER_RIGHT);
		

//		IntegerProperty lacznik = new SimpleIntegerProperty();
		/*final Spinner<Integer> */spinner = new Spinner<Integer>(1,150,1);
		spinner.setPrefWidth(100);
		spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
			if(Vscroll.getValue()!=spinner.getValue())
				Vscroll.setValue(spinner.getValue());
		});
		
		/*
		 * StackPane stackPane = new StackPane();
		 * stackPane.getChildren().addAll(controls,spinner );
		 * StackPane.setAlignment(spinner,Pos.CENTER_LEFT); StackPane.setMargin(spinner,
		 * new Insets(0,0,0,10));//margines od lewej
		 * StackPane.setAlignment(controls,Pos.CENTER);
		 */		
		HBox controlsHBox = new HBox(10);
		controlsHBox.setPadding(new Insets(0,0,10,0));//bottom padding
		controlsHBox.getChildren().addAll(spinner,controls);
		
		
//		lacznik.bindBidirectional((Property<Number>)spinner.getValueFactory().valueProperty());
//		lacznik.bindBidirectional(Vscroll.valueProperty());
//		Vscroll.valueProperty().bindBidirectional((Property<Number>)spinner.getValueFactory().valueProperty());
		
		final Button startButton = new Button("Start/Stop");
		
		final Button zoomButton = new Button("Zoom");
		final Button zoomInButton = new Button("+");
		final Button zoomOutButton = new Button("-");
		final Button resetButton = new Button("Reset");
		/*final TextField */xField = new TextField("");
		/*final TextField */yField = new TextField("");//tresholdField;//
		
		xMarkedField = new TextField("");
		xMarkedField.setDisable(true);
		
		setxyFields(xField, yField, chart, lineVert,lineHor);

		startButton.setOnAction((ActionEvent event) -> {
			
				startChangingChart(bStop);
				bStop = !bStop;
				
				startButton.setText(bStop?"Stop":"Start");
        });
		
		zoomButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doZoom(zoomRect, chart);
            }
        });
		
		zoomInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

            	double lowerBound = xAxis.getLowerBound();
            	double upperBound = xAxis.getUpperBound();
            	double range = upperBound- lowerBound;
            	xAxis.setLowerBound(lowerBound + range*0.25);
            	xAxis.setUpperBound(upperBound - range*0.25);
            	
            	doZoomWaterfall();
            }
        });
		
		zoomOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

            	double lowerBound = xAxis.getLowerBound();
            	double upperBound = xAxis.getUpperBound();
            	double range = upperBound- lowerBound;
            	double newLowerBound = lowerBound - range*0.25;
            	double newUpperBound = upperBound + range*0.25;
            	
            	if(newLowerBound < X_MIN)
            		newLowerBound =X_MIN;
            	if(newUpperBound > X_MAX)
            		newUpperBound=X_MAX;
            	
            	xAxis.setLowerBound(newLowerBound);
            	xAxis.setUpperBound(newUpperBound);
            	
            	doZoomWaterfall();
            }
        });
		
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final NumberAxis xAxis = (NumberAxis)chart.getXAxis();
                xAxis.setLowerBound(X_MIN);
                xAxis.setUpperBound(X_MAX);//NUM_DATA_POINTS);
                final NumberAxis yAxis = (NumberAxis)chart.getYAxis();
                yAxis.setLowerBound(Y_MIN);
                yAxis.setUpperBound(Y_MAX);
                
                zoomRect.setWidth(0);
                zoomRect.setHeight(0);
                
                doZoomWaterfall();
            }
        });
		
		final BooleanBinding disableControls = 
		        zoomRect.widthProperty().lessThan(5)
		        .or(zoomRect.heightProperty().lessThan(5));
		zoomButton.disableProperty().bind(disableControls);
		
		controls.getChildren().addAll(startButton, zoomButton, zoomInButton,zoomOutButton, resetButton, new Label("Hz ="),xField, new Label("Threshold ="),yField, new Label("Amplitude ="),xMarkedField);
		
//		controls.setAlignment(spinner,Pos.BASELINE_LEFT);
		//++++++++++++++
		spectrumWaterfall.addListener(this);
		
///		waterfallContainer = new StackPane();
		imageView = new WrappedImageView(chart.getXAxis());//new ImageView();
///		waterfallContainer.getChildren().add(imageView);
		
		VBox temp = initView();
		//waterfallContainer.setAlignment(Pos.BASELINE_RIGHT);
		//waterfallContainer.getChildren().get(0).layoutXProperty().bind(chart.getXAxis().layoutXProperty());
		//waterfallContainer.paddingProperty().bind(chart.getXAxis().layoutXProperty());
		
		//VBox vbox = new VBox ();
		//vboxCharts.setManaged(false);
//		vboxCharts.setMargin(chartContainer, new Insets(0,0,0,0));
		vboxCharts.getChildren().addAll(chartContainer, controlsHBox/*, waterfallContainer*/,temp);
		//vbox.setFillWidth(true);
		//vbox.getChildren().get(1).layoutXProperty().bind(chart.getXAxis().layoutXProperty());
		//imageView.xProperty().bind(chart.getXAxis().layoutXProperty());
//		imageView.relocate(arg0, arg1);
		//++++++++++++++
//		final BorderPane root = new BorderPane();
//		root.setCenter(vbox);
//		root.setCenter(chartContainer);
//		root.setBottom(controls);
		
//		final Scene scene = new Scene(root, 800, 400);
//		primaryStage.setScene(scene);
//		primaryStage.show();
		
///		VBox.setMargin(temp/*waterfallContainer*/, new Insets(0,0,0,chart.getYAxis().getWidth()));//chart.getXAxis().getLayoutX()));

///		dataConverter.addListener(this);		
		dataConverter.addListener(spectrumWaterfall);
		//imageView.fitWidthProperty().bind(chart.getXAxis().widthProperty());//primaryStage.widthProperty());//.//vbox.widthProperty());//chart.getXAxis().widthProperty());
		//vbox.fitWidthProperty().bind(chart.getXAxis().widthProperty());//primaryStage.widthProperty());//.//vbox.widthProperty());//chart.getXAxis().widthProperty());
		
	
	}
	
	private void startChangingChart(boolean bStop){
		
		Random generator = new Random();
		long t1 = System.currentTimeMillis();
	
		if (bStop)
		{
			task.cancel();
			System.out.println("STOP");
			return;
		}
		//   ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
			
		task = new Task<Void>() {
			long ii =0;
			boolean done = true;
			 //  series.getData().addAll(data);
			   
			@Override
			public Void call() /*throws IOException*/ {
				try {
					while (true) {
						if (isCancelled()) {
							return null;
						}
						//TWORZONA JEST LISTA ZA KAZDYM RAZEM OD NOWA W PETLI WHILE() 
						//WIEC NIE MA PROBLEMU Z TYM ZE W RUNLATER() JEST POBIERANA TA LISTA 
						//ALE CHYBA DUZO OBIEKTOW JEST TWORZONYCH BEZ POTRZEBY?
						   //List<XYChart.Data<Integer,Double>> data = new ArrayList<XYChart.Data<Integer,Double> >();
   ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
						   
					//	series.getData().remove(0, NUM_DATA_POINTS);
						
						
//						if(!data.isEmpty())
//						{
//							data.remove(0,NUM_DATA_POINTS);
//						}
						
							for (int i =0; i<NUM_DATA_POINTS;i++)
							{
								data.add( new XYChart.Data<>( i, generator.nextDouble()*900) );
							}
						   
							//queueData.put(data);
							
							if(!done)
							{
								//System.out.println("jeszcze rysuje...");
								ii++;
							}
							else
							{
								done = false;
								
								
				///				dataConverter.convertData();
								
						Platform.runLater( new Runnable () {
							@Override
							public void run() {
								long t2 = System.currentTimeMillis();
								//ii++;
								//jezeli nie zakonczyl sie poprzedni watek
								//wyswietlania danych to nie wyswietlaj nowych:
							//	synchronized(this)
							//	{	
	//								if (!done)
	//								{
	//									ii++;
	//									System.out.println("ff");
	//									return;
	//								}
							//	}
							//	synchronized(this)
							//	{	
							//	done = false;
								
								chart.setTitle(
										String.format("czas dzialania %.3f [sec] - ilosc probek utraconych: %d",
												(float)(t2-t1)/1000
												,ii));
								//W SOBOTE ZAUWAZYLEM ZE JEZELI JEST POZA WATKIEM TA FUNKCJA
								//TO WYSKAKUJE BLAD PRZY ODSWIEZANIU WATERFALL:
								//BO CHYBA CHCE ODSWIEZYC ELEMENT JAVAFX WIEC MUSI BYC W runLater:
								dataConverter.convertData();

///&&								updateChart();
///&&								updateMarkedFreq();
									
								done = true;
								
							//	}
							}
						});//runLater()
							}//else
						Thread.sleep(100);
					}//while
				} catch (/*IOException |*/ InterruptedException ex) {
				
					System.out.println("error");
					
					if (isCancelled()) {
						return null;
					}
				}
				return null;
			}
		};
		
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
	
	private void updateChart()
	{
		
		series.getData().remove(0, series.getData().size());//NUM_DATA_POINTS);
		///			series.getData().addAll(data);
					
		//W SOBOTE ZAUWAZYLEM ZE CHYBA SZEREKUJE W KOLEJCE ELEMENTY TAK ZE GDY WEZMIE JEDEN ELEMENT,
		//TO NASTEPNY ELEMENT ZASTEPUJE POPRZEDNI WIEC CIAGLE TEN SAM ELEMENT NA POZYCJI 10:
		
		int nrTimeStampTEMP;
		if(nrTimeStamp==-1)
		{//gdy jeszcze ani razu nie przemiescilismy kursorem pionowym Vscroll
			nrTimeStampTEMP = queueData.size();
		}else
		{
			nrTimeStampTEMP=nrTimeStamp;
		}
		
		series.getData().addAll(getElementFromQueue(nrTimeStampTEMP,queueData) );
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++CHART UPDATED!!");
	}
	private void updateMarkedFreq()//ObservableList<XYChart.Data<Number, Number>> data)
	{
		//numbers including scientific notation:
		if (xField.getText().matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
			double x = Double.parseDouble(xField.getText());
			
			
			
			ObservableList<XYChart.Data<Number, Number>> data = chart.getData().get(0).getData();
			
			//System.out.println("cccccccccccccc x="+x+" data(0)="+data.get(0).getYValue().toString()+" size="+ data.size());
			
			//System.out.println("data.size()=" + data.size());
			for(int i=0; i< data.size(); i++) {
				if(data.get(i).getXValue().doubleValue() >= x)
				{
					xMarkedField.setText(data.get(i).getYValue().toString());
					return;
				}
			}
		}
		
	}
	
	private LineChart<Number, Number> createChart() {
	    final NumberAxis xAxis = createXAxis();
	    final NumberAxis yAxis = createYAxis();	    
	    final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
	    chart.setAnimated(false);
	    chart.setCreateSymbols(false);
	    chart.setData(generateChartData());	
	    
	    xAxis.setTickLabelsVisible(true);
	    xAxis.setTickMarkVisible(false);
	    xAxis.setMinorTickVisible(false);
	    xAxis.setTickUnit((X_MAX-X_MIN)/10);
	  
	    yAxis.setTickLabelsVisible(true);
	    yAxis.setTickMarkVisible(false);
	    yAxis.setMinorTickVisible(false);
	    yAxis.setTickUnit((Y_MAX-Y_MIN)/5);
    	
	    chart.setLegendVisible(false);
	    
	    return chart ;
	}

    private NumberAxis createXAxis() {
        final NumberAxis xAxis = new NumberAxis();
	    xAxis.setAutoRanging(false);
	    xAxis.setLowerBound(X_MIN);
	    xAxis.setUpperBound(X_MAX);//NUM_DATA_POINTS);
        return xAxis;
    }
    private NumberAxis createYAxis() {
        final NumberAxis yAxis = new NumberAxis();
	    yAxis.setAutoRanging(false);
	    yAxis.setLowerBound(Y_MIN);
	    yAxis.setUpperBound(Y_MAX);
        return yAxis;
    }
    private ObservableList<Series<Number, Number>> generateChartData() {
        series = new Series<>();
        series.setName("Data");
        final Random rng = new Random();
        for (int i=0; i<NUM_DATA_POINTS; i++) {
            Data<Number, Number> dataPoint = new Data<Number, Number>(i, rng.nextInt(1000));
            series.getData().add(dataPoint);
        }
        return FXCollections.observableArrayList(Collections.singleton(series));
    }
    
    private void setUpZooming(final Rectangle rect, final Node zoomingNode, final Line lineVert, final Line lineHor) {
    	
    	rect.setCursor(Cursor.HAND);
    	
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        
        zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                rect.setWidth(0);
                rect.setHeight(0);
                
            }
        });
        
        zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                rect.setX(Math.min(x, mouseAnchor.get().getX()));
                rect.setY(Math.min(y, mouseAnchor.get().getY()));
                rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
                
                chart.setCursor(Cursor.CROSSHAIR);
            }
        });
        
        rect.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	
            	if(event.getClickCount() == 1)
            		return;
            	
            	System.out.println("double click for zoom.");
         
				doZoom(rect, chart);

            	//xField.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, true, true, true, true));
            }
        });
        
        //CZEKANIE NA UAKTUALNIENIE SKALI PO ZOOMIE, aby odswiezyc linie, nie od razu po zoomie sa prawidlowe wartosci z funkcji getScale():
        ChangeListener<Number> scaleListener = (obs, oldValue, newValue) -> {
        	if(!xField.getText().isEmpty())
        		update_lineVert(lineVert);
        	
        	if(!yField.getText().isEmpty())
        		update_lineHor(lineHor);
        	
        	double lowerBoundX = ((NumberAxis) chart.getXAxis()).getLowerBound();
        	double upperBoundX = ((NumberAxis) chart.getXAxis()).getUpperBound();
        	double tickUnitX = (upperBoundX-lowerBoundX)/10;
        	((NumberAxis) chart.getXAxis()).setTickUnit(tickUnitX);
        //	System.out.println("tickUnit="+tickUnit);
        	
        	double lowerBoundY = ((NumberAxis) chart.getYAxis()).getLowerBound();
        	double upperBoundY = ((NumberAxis) chart.getYAxis()).getUpperBound();
        	double tickUnitY = (upperBoundY-lowerBoundY)/5;
        	((NumberAxis) chart.getYAxis()).setTickUnit(tickUnitY);
        	//waterfall:
        	//waterfallContainer
     //   	System.out.println("chart.getLayoutX()="+chart.getXAxis().getLayoutX());
      //  	waterfallContainer.setLayoutX(chart.getXAxis().getLayoutX());
        //	imageView.setFitWidth(chart.getWidth()-5);
    	//	imageView.setFitHeight(chart.getHeight());
        	//imageView.relocate(100, 100);
        };
        ((NumberAxis) chart.getXAxis()).scaleProperty().addListener(scaleListener);
        ((NumberAxis) chart.getYAxis()).scaleProperty().addListener(scaleListener);
        ((NumberAxis) chart.getXAxis()).lowerBoundProperty().addListener(scaleListener);
        ((NumberAxis) chart.getYAxis()).lowerBoundProperty().addListener(scaleListener);
        
        ChangeListener<Number> widthListener = (obs, oldValue, newValue) -> {
          Vscroll.setMaxHeight(imageView.getFitHeight());
          Vscroll.setMinHeight(imageView.getFitHeight());
//  		Hscroll.setMax(width);
//          Hscroll.setMaxWidth(imageView.getFitWidth());
//          Hscroll.setMinWidth(imageView.getFitWidth());
        };
        ((NumberAxis) chart.getXAxis()).widthProperty().addListener(widthListener);
        ((NumberAxis) chart.getYAxis()).heightProperty().addListener(widthListener);
    }
    
    private void doZoom(Rectangle zoomRect, LineChart<Number, Number> chart) {
    	//zoomRect.setX(zoomRect.localToParent(0,0).getX());
    	//zoomRect.setY(zoomRect.localToParent(0,0).getY());
    	//Rectangle zoomRectInScene = new Rectangle(zoomRect);
    	
    	zoomRect.setX(zoomRect.localToScene(zoomRect.getX(), 0, true).getX());
    	zoomRect.setY(zoomRect.localToScene(0, zoomRect.getY(), true).getY());
    	
        Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());
        
        Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(), zoomRect.getY() + zoomRect.getHeight());
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene/*Parent*/(0, 0,true);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene/*Parent*/(0, 0, true);
        double xOffset = zoomTopLeft.getX() - yAxisInScene.getX() - yAxis.getWidth();
        double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();//- xAxis.getHeight();
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();
        
        System.out.println("******zoomTopLeft = "+zoomRect.getX() + ", "+ zoomRect.getY() +" xOffset="+xOffset);
        System.out.println("******x,yAxisInScene = "+xAxisInScene + ", "+ yAxisInScene);
        
        xAxis.setLowerBound(xAxis.getLowerBound() + xOffset / xAxisScale);
        xAxis.setUpperBound(xAxis.getLowerBound() + zoomRect.getWidth() / xAxisScale);
        yAxis.setLowerBound(yAxis.getLowerBound() + yOffset / yAxisScale);
        yAxis.setUpperBound(yAxis.getLowerBound() - zoomRect.getHeight() / yAxisScale);
        System.out.println(yAxis.getLowerBound() + " " + yAxis.getUpperBound());
        zoomRect.setWidth(0);
        zoomRect.setHeight(0);
        
        chart.setCursor(Cursor.DEFAULT);
        
        doZoomWaterfall();
        
        //uaktulaniam recznie skale bo chyba nie uaktualnia od razu po zmianie .setLowerBound:
        //xAxisScale = xAxisScale * (xAxis.getUpperBound()-xAxis.getLowerBound());
        //yAxisScale = yAxisScale * (yAxis.getUpperBound()-yAxis.getLowerBound());
    }
    
    private Point2D getxyValueFromChart(LineChart<Number, Number> chart, double X, double Y)
    {
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0,true);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0,true);
        
        double XZeroInScene = chart.localToScene(0,0,true).getX();
        double YZeroInScene = chart.localToScene(0,0,true).getY();
        
        double xOffset = X - yAxisInScene.getX() - yAxis.getWidth() + XZeroInScene;
        double yOffset = Y - xAxisInScene.getY() /*- xAxis.getHeight()*/ + YZeroInScene;
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();
//        System.out.println("xAxisScale="+ xAxisScale + ", " + yAxisScale );

    	double xValue = xAxis.getLowerBound() + xOffset / xAxisScale;
    	double yValue = yAxis.getLowerBound() + yOffset / yAxisScale;
    	return new Point2D(xValue,yValue);
    }
    //DOBRZE DZIALA:
    private void setxField(LineChart<Number, Number> chart, double X)
    {
        Point2D p = getxyValueFromChart(chart,X,0);
        
        xField.setText(String.valueOf(p.getX()));
    }
    //DOBRZE DZIALA:
    private void setyField(LineChart<Number, Number> chart, double Y)
    {
        Point2D p = getxyValueFromChart(chart,0,Y);
        
        yField.setText(String.valueOf(p.getY()));
        
        updateExternalThreshold();
    }
    
    private void updateExternalThreshold()
    {
    	spectrumProcessor.setThreshold(Double.parseDouble(yField.getText()));
    }
    private Point2D getChartValueFromxy(LineChart<Number, Number> chart, double xValue, double yValue)//, boolean bFromZOOMING)//, double xAxisScale, double yAxisScale)
    {
    	
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0,true);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0,true);//BEZ ZNACZENIA 
        
     //   Point2D yAxisInSceneTRUE = yAxis.localToScene(0, 0,true);
     //   System.out.println("yAxisInScene = " + yAxisInScene + "yAxisInSceneTRUE = "+ yAxisInSceneTRUE);
        //ZAWSZE DAJE Y=35 BEZ ZNACZENIA CZY TRUE CZY NIE.
        
        Point2D chartInScene = chart.localToScene(0, 0, true);
        
        double xLowerBound = xAxis.getLowerBound();
//        double xUpperBound = xAxis.getUpperBound();
        double yLowerBound = yAxis.getLowerBound();
//        double yUpperBound = yAxis.getUpperBound();
        
        
        //JAK ZOOMUJE TO WIDZI TUTAJ CIAGLE STARA SKALE, wiec aby uaktualnic linie to musze zaczekac az ponizsze wartosci sa uaktualnione poprzez even handler dla scaleProperty :
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();

        double xOffset = (xValue- xLowerBound)*xAxisScale;
        double x = yAxisInScene.getX() + yAxis.getWidth()/**/ + xOffset - chartInScene.getX();///(xUpperBound-xLowerBound) ;
        
      //  double yOffset = xAxisInScene.getY() - xAxis.getHeight()+ yValue/(yUpperBound-yLowerBound) ;
        
        double yOffset = (yValue-yLowerBound)*yAxisScale;
        double y = xAxisInScene.getY() /*+ xAxis.getHeight()*/ + yOffset - chartInScene.getY();///(xUpperBound-xLowerBound) ;

       
    	//double x = xOffset;
    	//double y = yOffset;//yAxis.getLowerBound() + yOffset / yAxisScale;
        System.out.println("xValue="+xValue+" xLowerBound=" + xLowerBound+" xUpperBound=" + xAxis.getUpperBound() + " xOffset=" + xOffset + " xAxisScale=" + xAxisScale);
    	System.out.println("x = " + String.valueOf(x));
    	return new Point2D(x,y);
    	
   // 	return new Point2D(100,100);
    }
    
    static private void setNewCenterOnChart()
    {
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

    	double lowerBound = xAxis.getLowerBound();
    	double upperBound = xAxis.getUpperBound();
    	double range = upperBound- lowerBound;
    	double newLowerBound = offSetXOnChart(offSetX) - range*0.5;
    	double newUpperBound = offSetXOnChart(offSetX) + range*0.5;
    	
    	if(newLowerBound < X_MIN)
    		newLowerBound =X_MIN;
    	if(newUpperBound > X_MAX)
    		newUpperBound=X_MAX;
    	
    	xAxis.setLowerBound(newLowerBound);
    	xAxis.setUpperBound(newUpperBound);
    	
    	System.out.println("newLowerBound="+newLowerBound+"; newUpperBound="+newUpperBound);
    }
    
    static private void setZoomOnChart()
    {
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        // zoomlvl = zoomLvl.getValue();
    	
    	double lowerBound = xAxis.getLowerBound();
    	double upperBound = xAxis.getUpperBound();
    	double oldRange = upperBound- lowerBound;
    	double newRange = (X_MAX-X_MIN)/zoomlvl;  
    	//
    	
    	double newLowerBound = /*offSetX*/ offSetXOnChart(offSetX) - newRange*0.5;
    	double newUpperBound = /*offSetX*/ offSetXOnChart(offSetX) + newRange*0.5;
    	
    	if(newLowerBound < X_MIN)
    		newLowerBound =X_MIN;
    	if(newUpperBound > X_MAX)
    		newUpperBound=X_MAX;
    	
    	xAxis.setLowerBound(newLowerBound);
    	xAxis.setUpperBound(newUpperBound);
    }   
    static private double offSetXOnScroll( double offSetX)
    {
    	double rangeScroll = Hscroll.getMax() - Hscroll.getMin();
    	
    	//final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
    	double rangeImage = width;//xAxis.getUpperBound() - xAxis.getLowerBound();
    	
    	System.out.println("rrrrrrrrr>> rangeScroll="+ rangeScroll+ " rangeImage="+rangeImage+" offSetX="+offSetX +" Hscroll.getMin()="+ Hscroll.getMin());
    	System.out.println("wynik="+offSetX * rangeScroll/rangeImage+"wynik2="+ Hscroll.getMin() + offSetX *rangeScroll /rangeImage);
    	return Hscroll.getMin() + offSetX *rangeScroll /rangeImage;
    }
    static private double offSetXOnChart( double offSetX)
    {
    	double rangeImage = width;//Hscroll.getMax() - Hscroll.getMin();
    	
    	//final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
    	//double rangeChart = //xAxis.getUpperBound() - xAxis.getLowerBound();
    	double rangeScroll = Hscroll.getMax() - Hscroll.getMin();
    	
    	System.out.println("rrrrrrrrr offSetX="+offSetX+" rangeScroll="+ rangeScroll+ " rangeImage="+rangeImage);
    	System.out.println("wynik="+offSetX * rangeScroll/rangeImage);
    	return Hscroll.getMin() + offSetX * rangeScroll/rangeImage;
    }
    static private double xOnWaterfall(double HscrollValue)
    {
    	double rangeScroll = Hscroll.getMax() - Hscroll.getMin();

    	double rangeImage = width;
    	
    	return (HscrollValue-Hscroll.getMin()) * rangeImage/rangeScroll;
    }
 private void setUpVertLine(final Line line, LineChart<Number, Number> chart) {
    	
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        
        chart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	
            	if(event.getClickCount() == 1)
            		return;
            	
            	System.out.println("double click: " + event.getX()+","+event.getY() );
            	
            	if(event.getX() < 43)
            		return;//kliknieto na os Y
            	
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                          
                set_lineVert(line,mouseAnchor.get().getX());
                
                setxField(chart, event.getX());

            }
        });
        
        line.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                
                set_lineVert(line,x);
                
                setxField(chart,x);

            }
        });
        
        line.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
            	line.setCursor(Cursor.E_RESIZE);
            }
        });
    }
 
 private void setUpHorLine(final Line lineHor, LineChart<Number, Number> chart) {
 	
     final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
     
     chart.getYAxis().setOnMouseClicked(new EventHandler<MouseEvent>() {
         @Override
         public void handle(MouseEvent event) {
         	
         	if(event.getClickCount() == 1)
         		return;
         	
//       //  	if(event.getX() > 43)
//       //  		return;//klinieto poza os Y
            final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
            Point2D yAxisInScene = yAxis.localToScene(0, 0, true);//&&&
            final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
            Point2D xAxisInScene = xAxis.localToScene(0, 0, true);
            
            Point2D chartInScene = chart.localToScene(0, 0, true);
            
            ///double yOffset = xAxisInScene.getY()-yAxis.getHeight();
            double yOffset = xAxisInScene.getY()-yAxis.getHeight() - chartInScene.getY();
//      	
//         	
//         	System.out.println("double click on Y axis: " + event.getX() + "," + event.getY() );
//         	
//             mouseAnchor.set(new Point2D(event.getX()/*+xOffset*/, event.getY()));//+yOffset));
//             
//             
//             set_lineHor(lineHor, mouseAnchor.get().getY());
//             
//             setyField(chart,mouseAnchor.get().getY()+ xAxis.getHeight() );
         	//TUTAJ ROBILEM:::::::
            double x = event.getX();
            double y = event.getY();
            //dragged ma 0 w ponkcie wyzej niz kliked ktory jest na ostatniej linii poziomej wykresu
            //bo kliked jest liczony dla osi, a dragged dla StackPane
            //czyli trzeba przerobic punkt kliked na dragged:
            
            y = y + yOffset;
            System.out.println("/////////clicked x,y="+ x+" , "+y+"; yOffset="+yOffset);
            set_lineHor(lineHor, y);//+xAxis.getHeight());

     //       final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

            setyField(chart,y);///+xAxis.getHeight());
         }
     });
     
     lineHor.setOnMouseDragged(new EventHandler<MouseEvent>() {
         @Override
         public void handle(MouseEvent event) {
             double x = event.getX();
             double y = event.getY();//ZERO JEST W ROGU StackPane, liczone im dalej w dol tym wieksza wartosc
             
             System.out.println("/////////dragged x,y="+ x+" , "+y);
             //dobrze dziala:
             set_lineHor(lineHor, y);//w stosunku do zera w rogu StackPane do ktorego jest dodana linia

             final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
             //dobrze dziala:
             setyField(chart,y/*+xAxis.getHeight()*/);
         }
     });
     
     lineHor.setOnMouseEntered(new EventHandler<MouseEvent>() {
         @Override
         public void handle(MouseEvent event) {
         	lineHor.setCursor(Cursor.N_RESIZE);
         }
     });
 }
 
 private void set_lineHor(final Line lineHor, double y)
 {
     lineHor.setStartX(0);
     lineHor.setEndX(chart.getWidth());
    // lineHor.setStartY(y);
    // lineHor.setEndY(y);
     
     lineHor.setStartY(y);//+lineHor.localToScene(0,0,true).getY()); 	
     lineHor.setEndY(y);//+lineHor.localToScene(0,0,true).getY());
 }
 
 private void set_lineVert(final Line lineVert, double x)
 {
	 final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
	 Point2D xAxisInScene = xAxis.localToScene(0, 0,true);
	 final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
	 Point2D yAxisInScene = yAxis.localToScene(0, 0,true);
	 double chartHeight = chart.getHeight();
	 
     lineVert.setStartX(x);
     lineVert.setEndX(x);
     lineVert.setStartY(0);//.getLayoutY());// yAxisInScene.getY());
    // lineVert.setEndY(/*yAxis.getLayoutY()+*/yAxis.getHeight()+yAxis.getLayoutY());//lineVert.getStartY()+10);
     lineVert.setEndY(xAxisInScene.getY() );
     
     updateMarkedFreq();
 }
 private void update_lineVert(Line lineVert)
 {
		double x = Double.parseDouble( xField.getText() );
		System.out.println("xField changed " + x);
		
        Point2D p = getChartValueFromxy(chart,x,0);
        
		set_lineVert(lineVert, p.getX());
		
 }
 private void update_lineHor(Line lineHor)
 {
		double y = Double.parseDouble( yField.getText() );
		System.out.println("yField changed " + y);
		
		final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		
        Point2D p = getChartValueFromxy(chart,0,y);
        
		set_lineHor(lineHor, p.getY() );////- xAxis.getHeight());
 }
 private void setxyFields(TextField xField, TextField yField, LineChart<Number, Number> chart, final Line lineVert, final Line lineHor) {
	 	
//     final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>(); 
	 
	 xField.setOnKeyPressed(event->{
			KeyCode keyCode = event.getCode();
			if(keyCode==KeyCode.ENTER) {
				if (!xField.getText().matches("-?\\d+(\\.\\d+)?")) {
                	//gdy nie jest liczba to cofnij zmiany:
                	xField.setText(xFieldPrzed);
                }
				else
				{
					xFieldPrzed = xField.getText();
				}
				System.out.println("Klinal ENTER");
				if(!xField.getText().isEmpty())
					update_lineVert(lineVert);
			}
			
//			String character = event.getText();
//			String textPrzed = xField.getText();//tekst przed wprowadzeniem znaku
//			if(character.matches("[0-9\\.-]+"))//"-?\\d+(\\.\\d+)?"))
//			{//wszystkie cyfry i kropka:
//				
//				if( (textPrzed.matches(".") && character.matches("[.]+") ) ||
//				//gdy juz w tekscie byla kropka i chcemy wstawic nastepna kropke:
//					(textPrzed.isEmpty() && character.matches("[.]+")) ||
//					//gdy chcemy wstawic kropke na poczatku:
//						(!textPrzed.isEmpty() && character.matches("[-]+") ) )
//						{//jezeli chcemy wstawic minus ale juz sa jakies cyfry:
//							xField.setText(textPrzed);
//					
//						}
//				
//				//System.out.println("---- " + event.getText());
//			}
//	 		else
//	 		{//gdy inne znaki niz cyfry, minus i kropka:
//	 			xField.setText(textPrzed);
//	 			//System.out.println("++++ " + xField.getText());
//	 			event.consume();
//	 		}	
		});
	 
	 xField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
	        if (!newValue) { // when focus lost
                if (!xField.getText().matches("-?\\d+(\\.\\d+)?")) {
                	//gdy nie jest liczba to cofnij zmiany:
                	xField.setText(xFieldPrzed);
                }
                if(!xField.getText().isEmpty())
                	update_lineVert(lineVert);
	        }
	        else
	        {
	        	xFieldPrzed = xField.getText();
	        	System.out.println("Focus in x = "+xFieldPrzed);
	        }
	        });
	 
	 yField.setOnKeyPressed(event->{
			KeyCode keyCode = event.getCode();
			if(keyCode==KeyCode.ENTER) {
				
				if (!yField.getText().matches("-?\\d+(\\.\\d+)?")) {
                	//gdy nie jest liczba to cofnij zmiany:
                	yField.setText(yFieldPrzed);
                }
				else
				{
					yFieldPrzed = yField.getText();
				}
				System.out.println("Klinal ENTER");
				if(!yField.getText().isEmpty())
				{
					update_lineHor(lineHor);
					
					updateExternalThreshold();//spectrumProcessor.setThreshold(Double.parseDouble(yField.getText()));
				}
			}
		});
	 
	 yField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
	        if (!newValue) { // when focus lost
             if (!yField.getText().matches("-?\\d+(\\.\\d+)?")) {
             	//gdy nie jest liczba to cofnij zmiany:
             	yField.setText(yFieldPrzed);
             }
             if(!yField.getText().isEmpty())
             { 
            	 update_lineHor(lineHor);
             
             	updateExternalThreshold();//spectrumProcessor.setThreshold(Double.parseDouble(yField.getText()));
             }
	        }
	        else
	        {//gdy wchodzimy w pole tekstowe:
	        	yFieldPrzed = yField.getText();
	        	System.out.println("Focus in y = "+yFieldPrzed);
	        	
	        }
	        });
	 
	 
	 
/*		
	 //REAKCJA NA ZMIANA ROZMIARU WYKRESU:
	 //JEST WYKOMENTOWANE BO UAKTUALNIENIE LINII ROBIE POPRZEZ UCHWYT DO ZDARZENIA WYWOLYWANEGO PRZY ZMIANIE SKALI WYKRESU scaleProperty:
     BlockingQueue<Point2D> dimensionChangeQueue = new ArrayBlockingQueue<>(1);
     ChangeListener<Number> dimensionChangeListener = (obs, oldValue, newValue) -> {
              
	         dimensionChangeQueue.clear();
	         dimensionChangeQueue.add(new Point2D(0,0) );
     };
     chart.widthProperty().addListener(dimensionChangeListener);
     chart.heightProperty().addListener(dimensionChangeListener);

     Thread processDimensionChangeThread = new Thread(() -> {
         try {
             while (true) {
                 System.out.println("Waiting for change in size");
                 Point2D size = dimensionChangeQueue.take();
                 System.out.printf("Detected change in size to [%.1f, %.1f]: processing%n", size.getX(), size.getY());

                 Thread.sleep(50);

                Platform.runLater(() -> {
                	
                	if(!xField.getText().isEmpty())
                	{
                		update_lineVert(lineVert);
                	}
	 		        //------------
                	if(!yField.getText().isEmpty())
	                {
                		update_lineHor(lineHor);
                	}
	                 //----------
                });
                 System.out.println("Done processing");
             }
         } catch (InterruptedException letThreadExit) { }
     });
     processDimensionChangeThread.setDaemon(true);
     processDimensionChangeThread.start();
     */
	 
 }
 
//	public static void main(String[] args) {
//		launch(args);
//	}
	
	
	//+++++++++++++++++++++++++++++++++++++++++++++

	@Override
	public void onImageProcessed(WritableImage waterfallImage, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		
		
		
		imageView.setImage(waterfallImage);
		
//		height = (int) waterfallImage.getHeight();
//		width = (int) waterfallImage.getWidth();
//		Vscroll.setMax(height);
//        Vscroll.setMaxHeight(imageView.getFitHeight());
//        Vscroll.setMinHeight(imageView.getFitHeight());
//		Hscroll.setMax(width);
//        Hscroll.setMaxWidth(imageView.getFitWidth());
//        Hscroll.setMinWidth(imageView.getFitWidth());
		//imageView.setFitWidth(lineChart.getWidth());
		//imageView = new ImageView(waterfallImage);	
		//System.out.println("new waterfallImage");
		
	}
	
    static double initx;
    static double inity;
    static int height=150;
    static int width=NUM_DATA_POINTS;
    public static String path;
    static Scene initialScene,View;
    static double offSetX,offSetY,zoomlvl;
    static double offSetXOnChart;
    static Slider Hscroll = new Slider();
    static Slider Vscroll = new Slider();
    static Slider zoomLvl = new Slider();
    static Label value = new Label("1.0");
    
    public VBox initView(){
    	
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);


        ImageView image = imageView;// new ImageView(source);

        image.setPreserveRatio(false);
         
        HBox zoom = new HBox(10);
        zoom.setAlignment(Pos.CENTER);

//        Slider zoomLvl = new Slider();
        zoomLvl.setMax(100);
        zoomLvl.setMin(1);
        zoomLvl.setMaxWidth(200);
        zoomLvl.setMinWidth(200);
        Label hint = new Label("Zoom Level");
//        Label value = new Label("1.0");

        offSetX = width/2;
        offSetY = height/2;
        
        value.setPrefWidth(100);

        zoom.getChildren().addAll(hint,zoomLvl,value);

//        Slider Hscroll = new Slider();
        Hscroll.setMin(X_MIN);
        Hscroll.setMax(X_MAX);//width);
//        Hscroll.setMaxWidth(image.getFitWidth());
//        Hscroll.setMinWidth(image.getFitWidth());
//        System.out.println("image.getFitWidth()="+image.getFitWidth());
//        Hscroll.setTranslateY(-20);
        Hscroll.setMajorTickUnit((X_MAX-X_MIN)/10);
        Hscroll.setMinorTickCount(4);
        Hscroll.setShowTickLabels(true);
        Hscroll.setShowTickMarks(true);
        
        
        
//        Slider Vscroll = new Slider();
        Vscroll.setMin(1);
        Vscroll.setMax(height);
        Vscroll.setMaxHeight(height);//image.getFitHeight());
        Vscroll.setMinHeight(height);//image.getFitHeight());
        Vscroll.setOrientation(Orientation.VERTICAL);
//        Vscroll.setTranslateX(-20);


        BorderPane imageView = new BorderPane();

        Hscroll.valueProperty().addListener(e->{
        
        	setNewCenterOnWaterfall();
            setNewCenterOnChart();

        });
        
        Vscroll.valueProperty().addListener(e->{
        	nrTimeStamp = (int) Vscroll.getMax() - (int) Vscroll.getValue()+1;//(double)((int)(Vscroll.getValue()*10))/10;
        	Platform.runLater( new Runnable () {
				@Override
				public void run() {
					updateChart();
					updateMarkedFreq();
				}
        	});
        	
			if(Vscroll.getValue()!=spinner.getValue())
				spinner.getValueFactory().setValue((int)Vscroll.getValue());
        	//spinner.value
//            offSetY = height-Vscroll.getValue();
//            zoomlvl = zoomLvl.getValue();
//            double newValue = (double)((int)(zoomlvl*10))/10;
//            value.setText(newValue+"");
//            if(offSetY<(height/newValue)/2) {
//                offSetY = (height/newValue)/2;
//            }
//            if(offSetY>height-((height/newValue)/2)) {
//                offSetY = height-((height/newValue)/2);
//            }
//            image.setViewport(new Rectangle2D(offSetX-((width/newValue)/2), offSetY-((height/newValue)/2), width/newValue, height/newValue));
        });
        ChangeListener<Number> heightListener = (obs, oldValue, newValue) -> {

            Vscroll.setMaxHeight(image.getFitHeight());
            Vscroll.setMinHeight(image.getFitHeight());
        	
        };
        image.fitHeightProperty().addListener(heightListener);
        
        imageView.setCenter(image);
        imageView.setBottom(Hscroll);
        imageView.setLeft(Vscroll);
        BorderPane.setAlignment(Hscroll, Pos.CENTER);
        BorderPane.setAlignment(Vscroll, Pos.CENTER_LEFT);
        
        zoomLvl.valueProperty().addListener(e->{
            zoomlvl = zoomLvl.getValue();
            double newValue = zoomlvl;//(double)((int)(zoomlvl*10))/10;
            value.setText(newValue+"");
            if(offSetX<(width/newValue)/2) {
                offSetX = (width/newValue)/2;
            }
            if(offSetX>width-((width/newValue)/2)) {
                offSetX = width-((width/newValue)/2);
            }
            if(offSetY<(height/newValue)/2) {
                offSetY = (height/newValue)/2;
            }
            if(offSetY>height-((height/newValue)/2)) {
                offSetY = height-((height/newValue)/2);
            }
            Hscroll.setValue(offSetXOnScroll(offSetX));
            /*Vscroll.setValue(height-offSetY);*/
            image.setViewport(new Rectangle2D(offSetX-((width/newValue)/2), offSetY-((height/ /*newValue*/1.0)/2), width/newValue, height/1.0/*newValue*/));

            System.out.println("height="+height);
            setZoomOnChart();
        });
        imageView.setCursor(Cursor.OPEN_HAND);
        image.setOnMousePressed(e->{
            initx = e.getSceneX();
            inity = e.getSceneY();
            imageView.setCursor(Cursor.CLOSED_HAND);
        });
        image.setOnMouseReleased(e->{
            imageView.setCursor(Cursor.OPEN_HAND);
        });
        image.setOnMouseDragged(e->{
        	double speed = 100;
            Hscroll.setValue(Hscroll.getValue()+(speed)*(initx - e.getSceneX()));
//            Vscroll.setValue(Vscroll.getValue()-(inity - e.getSceneY()));
            initx = e.getSceneX();
            inity = e.getSceneY();
        });
        root.getChildren().addAll(/*title,*/imageView,zoom);

        //View = new Scene(root,(image.getFitWidth())+70,(image.getFitHeight())+150);
        
        return root;
    }
    
    void doZoomWaterfall()
    {
    	double x;
    	final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
    	x = xAxis.getLowerBound() + (xAxis.getUpperBound() - xAxis.getLowerBound() )/2; 
    			
    	double zoom = (X_MAX-X_MIN)/(xAxis.getUpperBound() - xAxis.getLowerBound() );
    	
    	zoomLvl.setValue(zoom);
    	
      	System.out.println("x="+x+"; zoom=" + zoom);
      	
    	Hscroll.setValue(x);//offSetXOnWaterfall(x));

    }
    
    static private void setNewCenterOnWaterfall()
    {
        offSetX = xOnWaterfall(Hscroll.getValue());
        
        zoomlvl = zoomLvl.getValue();
        double newValue = zoomlvl;//(double)((int)(zoomlvl*10))/10;
        value.setText(newValue+"");
        if(offSetX<(width/newValue)/2) {
            offSetX = (width/newValue)/2;
        }
        if(offSetX>width-((width/newValue)/2)) {
            offSetX = width-((width/newValue)/2);
        }

        imageView.setViewport(new Rectangle2D(offSetX-((width/newValue)/2), offSetY-((height/1.0 /*newValue*/)/2), width/newValue, height/1.0 /* newValue*/));

    }
    
	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub

	}
	
	@Override
	synchronized public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp, double freqStart,
			double freqStep) {
		   
		System.out.println("onDataReceived queueData.size()="+queueData.size());
		ReceivedDataWithTimeStamp receivedDataWithTimeStamp = new ReceivedDataWithTimeStamp (receivedData, timeStamp,freqStart,freqStep);
			
		if(queueData.size() > 149)
		{
			queueData.remove();
		}
		//tresholdField
		queueData.put(receivedDataWithTimeStamp);
		System.out.println("timeStamp="+receivedDataWithTimeStamp.timeStamp);
		
		//&&:
		updateChart();
		updateMarkedFreq();
	}

	
	class ReceivedDataWithTimeStamp{
		double[] receivedData;
		double timeStamp;
		double freqStart;
		double freqStep;
		
		ReceivedDataWithTimeStamp(double[] receivedData, double timeStamp,double freqStart, double freqStep)
		{
			this.receivedData = receivedData;
			this.timeStamp = timeStamp;
			this.freqStart = freqStart;
			this.freqStep = freqStep;
		}
		
		ObservableList<XYChart.Data<Number, Number>> getXYChartData(){
			   ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
			   
			   double x;
				for (int i =0; i<receivedData.length;i++)
				{
					x= freqStart + i*freqStep;
					data.add( new XYChart.Data<>( x, receivedData[i]) );
				}
				return data;
		}

	}
	
	ObservableList<XYChart.Data<Number, Number>> getElementFromQueue(int iElementFrom1, PriorityBlockingQueue<ReceivedDataWithTimeStamp> queue)
	{
		ObservableList<XYChart.Data<Number, Number>> returnValue=null;
		
		final int queueSize = (queue.size()==0)?1:queue.size();

	    PriorityBlockingQueue<ReceivedDataWithTimeStamp> queueTemp = new PriorityBlockingQueue<>(queueSize,comparator_queueData);
	    
	    queue.drainTo(queueTemp,iElementFrom1-1);
	    
	    if(!queue.isEmpty())
	    {
	    	System.out.println("queue.peek().timeStamp="+queue.peek().timeStamp);
	    	returnValue= queue.peek().getXYChartData();
	    }
	    else
	    {
	    	System.out.println("za malo elementow");
	    }
	    queue.addAll(queueTemp);
		
		//gdyby nie bylo bylo tylu elementow zwroc domyslny:
	    if(returnValue == null)
	    {
			ObservableList<XYChart.Data<Number, Number>> dummy = FXCollections.observableArrayList();
			   
			for (int k =0; k<NUM_DATA_POINTS;k++)
			{
				dummy.add( new XYChart.Data<>( k, 0) );
			}
			System.out.println("RETURN DUMMY");
			returnValue = dummy;
	    }
		return returnValue;
	}
	

}

