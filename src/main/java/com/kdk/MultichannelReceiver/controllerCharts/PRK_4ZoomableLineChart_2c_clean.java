package com.kdk.MultichannelReceiver.controllerCharts;

import com.kdk.MultichannelReceiver.model.*;
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
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Klasa obslugujaca interface wykresow umieszczonych w srodkowej czesci aplikacji
 * wraz z wszystkimi kontrolkami obslugujacymi wykresy.
 *
 * @author Damian Garsta / damiangarsta@wp.pl
 * @version 1.0
 */
public class PRK_4ZoomableLineChart_2c_clean implements SpectrumWaterfallListener, ReceiverDataConverterListener {

    public VBox chartsVbox;
    public static final int NUM_DATA_POINTS = 250;
    public static final int X_MIN = 30000000;
    public static final int X_MAX = 30000000 + 10500 * NUM_DATA_POINTS;
    public static final int Y_MIN = -150;
    public static final int Y_MAX = 50;

    Task<Void> task;
    static LineChart<Number, Number> chart;
    Series<Number, Number> series;
    boolean bStop = false;
    static int nrTimeStamp = -1;

    private TextField xField;
    private TextField yField;// to samo co tresholdField

    private String xFieldPrzed;
    private String yFieldPrzed;

    private TextField xMarkedField;

    double xMarked;

    Spinner<Integer> spinner;

    StackPane waterfallContainer;
    private static ImageView imageView;
    SpectrumWaterfall spectrumWaterfall = new SpectrumWaterfall(NUM_DATA_POINTS);
    SpectrumDataProcessor spectrumProcessor;

    /**
     * Metoda implementujaca interface Comparator dla kolejki serii danych wyswietlanych na wykresie 1D.
     * param o1 element do porownania 1
     * param o2 element do porownania 2
     * zwraca wynik porownania uzyty do ustawiania elementow w kolejce wyswietlania
     */

    static Comparator<ReceivedDataWithTimeStamp> comparator_queueData = (o1, o2) -> {
        double ts1 = o1.timeStamp;
        double ts2 = o2.timeStamp;

        return Double.compare(ts1, ts2);
    };

    private static PriorityBlockingQueue<ReceivedDataWithTimeStamp> queueData = new PriorityBlockingQueue<>(150,
            comparator_queueData);
    ReceiverDataConverter dataConverter;// new ReceiverDataConverter();

    /**
     * Konstruktor
     * @param dataConverter obiekt klasy do ktorej dopina sie nasluchiwacza otrzymywania danych
     * @param vboxCharts obiekt na scenie do ktorego dodaje sie wykresy i wszystkie kontrolki obslugujace wykresy
     * @param spectrumProcessor
     */

    public PRK_4ZoomableLineChart_2c_clean(ReceiverDataConverter dataConverter, VBox vboxCharts,
                                           SpectrumDataProcessor spectrumProcessor) {
        this.dataConverter = dataConverter;
        this.spectrumProcessor = spectrumProcessor;
        // vboxCharts;
        vboxCharts.getChildren().remove(0, vboxCharts.getChildren().size());
        start(vboxCharts);
    }

    /**
     * Metoda podpinajaca zdarzenia obslugujace graficzny interface wykresow.
     *
     * @param vboxCharts obiekt na scenie do ktorego dodaje sie wykresy i wszystkie kontrolki obslugujace wykresy
     */
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

        final HBox controls = new HBox(10);
        controls.setPadding(new Insets(0, 0, 0, 50));
        controls.setAlignment(Pos.CENTER_RIGHT);

        /* final Spinner<Integer> */
        spinner = new Spinner<Integer>(1, 150, 1);
        spinner.setPrefWidth(100);
        spinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (Vscroll.getValue() != spinner.getValue())
                Vscroll.setValue(spinner.getValue());
        });

        HBox controlsHBox = new HBox(10);
        controlsHBox.setPadding(new Insets(0, 0, 10, 0));// bottom padding
        controlsHBox.getChildren().addAll(spinner, controls);

        final Button startButton = new Button("Start/Stop");

        final Button zoomButton = new Button("Zoom");
        final Button zoomInButton = new Button("+");
        final Button zoomOutButton = new Button("-");
        final Button resetButton = new Button("Reset");
        /* final TextField */
        xField = new TextField("");
        /* final TextField */
        yField = new TextField("");// tresholdField;//

        xMarkedField = new TextField("");
        xMarkedField.setDisable(true);

        setxyFields(xField, yField, chart, lineVert, lineHor);

        startButton.setOnAction((ActionEvent event) -> {

            startChangingChart(bStop);
            bStop = !bStop;

            startButton.setText(bStop ? "Stop" : "Start");
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
                double range = upperBound - lowerBound;
                xAxis.setLowerBound(lowerBound + range * 0.25);
                xAxis.setUpperBound(upperBound - range * 0.25);

                doZoomWaterfall();
            }
        });

        zoomOutButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

                double lowerBound = xAxis.getLowerBound();
                double upperBound = xAxis.getUpperBound();
                double range = upperBound - lowerBound;
                double newLowerBound = lowerBound - range * 0.25;
                double newUpperBound = upperBound + range * 0.25;

                if (newLowerBound < X_MIN)
                    newLowerBound = X_MIN;
                if (newUpperBound > X_MAX)
                    newUpperBound = X_MAX;

                xAxis.setLowerBound(newLowerBound);
                xAxis.setUpperBound(newUpperBound);

                doZoomWaterfall();
            }
        });

        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                xAxis.setLowerBound(X_MIN);
                xAxis.setUpperBound(X_MAX);// NUM_DATA_POINTS);
                final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
                yAxis.setLowerBound(Y_MIN);
                yAxis.setUpperBound(Y_MAX);

                zoomRect.setWidth(0);
                zoomRect.setHeight(0);

                doZoomWaterfall();
            }
        });

        final BooleanBinding disableControls = zoomRect.widthProperty().lessThan(5)
                .or(zoomRect.heightProperty().lessThan(5));
        zoomButton.disableProperty().bind(disableControls);

        controls.getChildren().addAll(startButton, zoomButton, zoomInButton, zoomOutButton, resetButton,
                new Label("Hz ="), xField, new Label("Threshold ="), yField, new Label("Amplitude ="), xMarkedField);

        // ++++++++++++++
        spectrumWaterfall.addListener(this);

        imageView = new WrappedImageView(chart.getXAxis());// new ImageView();

        VBox temp = initView();

        vboxCharts.getChildren().addAll(chartContainer, controlsHBox/* , waterfallContainer */, temp);

        dataConverter.addListener(spectrumWaterfall);

    }

    /**
     * Metoda wykorzystana tylko do testowania tej klasy. Generuje ona dane do wywietlania.
     *
     * @param bStop informuje czy rozpoczac dostarczanie danych czy tez zatrzymac
     */
    private void startChangingChart(boolean bStop) {

        Random generator = new Random();
        long t1 = System.currentTimeMillis();

        if (bStop) {
            task.cancel();
            System.out.println("STOP");
            return;
        }

        task = new Task<Void>() {
            long ii = 0;
            boolean done = true;
            // series.getData().addAll(data);

            @Override
            public Void call() /* throws IOException */ {
                try {
                    while (true) {
                        if (isCancelled()) {
                            return null;
                        }
                        // TWORZONA JEST LISTA ZA KAZDYM RAZEM OD NOWA W PETLI WHILE()
                        // WIEC NIE MA PROBLEMU Z TYM ZE W RUNLATER() JEST POBIERANA TA LISTA

                        // List<XYChart.Data<Integer,Double>> data = new
                        // ArrayList<XYChart.Data<Integer,Double> >();
                        ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();

                        for (int i = 0; i < NUM_DATA_POINTS; i++) {
                            data.add(new XYChart.Data<>(i, generator.nextDouble() * 900));
                        }

                        if (!done) {
                            ii++;
                        } else {
                            done = false;

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    long t2 = System.currentTimeMillis();

                                    chart.setTitle(
                                            String.format("czas dzialania %.3f [sec] - ilosc probek utraconych: %d",
                                                    (float) (t2 - t1) / 1000, ii));
                                    // W SOBOTE ZAUWAZYLEM ZE JEZELI JEST POZA WATKIEM TA FUNKCJA
                                    // TO WYSKAKUJE BLAD PRZY ODSWIEZANIU WATERFALL:
                                    // BO CHYBA CHCE ODSWIEZYC ELEMENT JAVAFX WIEC MUSI BYC W runLater:
                                    dataConverter.convertData();

                                    done = true;

                                }
                            });// runLater()
                        } // else
                        Thread.sleep(100);
                    } // while
                } catch (/* IOException | */ InterruptedException ex) {

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

    /**
     * Uaktualnia dane na wykresie 1D.
     */
    private void updateChart() {

        series.getData().remove(0, series.getData().size());// NUM_DATA_POINTS);

        int nrTimeStampTEMP;
        if (nrTimeStamp == -1) {// gdy jeszcze ani razu nie przemiescilismy kursorem pionowym Vscroll
            nrTimeStampTEMP = queueData.size();
        } else {
            nrTimeStampTEMP = nrTimeStamp;
        }

        series.getData().addAll(getElementFromQueue(nrTimeStampTEMP, queueData));

    }

    /**
     * Uaktulnia pole tekstowe wyswietlajace amplitude (decybele) na podstawie wartosci z
     * pola tekstowego oznaczajacego wartosc na osi x (czestotliwosc).
     */
    private void updateMarkedFreq()// ObservableList<XYChart.Data<Number, Number>> data)
    {
        // numbers including scientific notation:
        if (xField.getText().matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
            double x = Double.parseDouble(xField.getText());

            ObservableList<XYChart.Data<Number, Number>> data = chart.getData().get(0).getData();

            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getXValue().doubleValue() >= x) {
                    xMarkedField.setText(data.get(i).getYValue().toString());
                    return;
                }
            }
        }

    }

    /**
     * Metoda okreslajaca podstawowy wyglad wyresu na poczatku jego tworzenia.
     *
     * @return zwraca obiekt utworzonego wykresu
     */
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
        xAxis.setTickUnit((X_MAX - X_MIN) / 10);

        yAxis.setTickLabelsVisible(true);
        yAxis.setTickMarkVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickUnit((Y_MAX - Y_MIN) / 5);

        chart.setLegendVisible(false);

        return chart;
    }

    /**
     * Metoda okreslajaca podstawowy wyglad osi X wyresu na poczatku jego tworzenia.
     *
     * @return zwraca obiekt osi wykresu
     */
    private NumberAxis createXAxis() {
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(X_MIN);
        xAxis.setUpperBound(X_MAX);// NUM_DATA_POINTS);
        return xAxis;
    }

    /**
     * Metoda okreslajaca podstawowy wyglad osi Y wyresu na poczatku jego tworzenia.
     *
     * @return zwraca obiekt osi wykresu
     */
    private NumberAxis createYAxis() {
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(Y_MIN);
        yAxis.setUpperBound(Y_MAX);
        return yAxis;
    }

    /**
     * Metoda tworzy poczatkowe dane dla wykresu 1D.
     *
     * @return zwraca liste z seria danych
     */
    private ObservableList<Series<Number, Number>> generateChartData() {
        series = new Series<>();
        series.setName("Data");
        final Random rng = new Random();
        for (int i = 0; i < NUM_DATA_POINTS; i++) {
            Data<Number, Number> dataPoint = new Data<Number, Number>(i, rng.nextInt(1000));
            series.getData().add(dataPoint);
        }
        return FXCollections.observableArrayList(Collections.singleton(series));
    }

    /**
     * Metoda podlaczajaca obsluge zdarzen potrzebna dla zoomowania na wykresie 1D.
     *
     * @param rect        prostokat oznaczajacy zakres zoomowania
     * @param zoomingNode obszar na ktorym nalezy zoomowac, w tym wypadku obszar wykresu 1D
     * @param lineVert    obiekt linii czestotliwosci (pionowej)
     * @param lineHor     obiekt linii progu detekcji (poziomowej)
     */
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

                if (event.getClickCount() == 1)
                    return;

                doZoom(rect, chart);
            }
        });

        // CZEKANIE NA UAKTUALNIENIE SKALI PO ZOOMIE, aby odswiezyc linie, nie od razu
        // po zoomie sa prawidlowe wartosci z funkcji getScale():
        ChangeListener<Number> scaleListener = (obs, oldValue, newValue) -> {
            if (!xField.getText().isEmpty())
                update_lineVert(lineVert);

            if (!yField.getText().isEmpty())
                update_lineHor(lineHor);

            double lowerBoundX = ((NumberAxis) chart.getXAxis()).getLowerBound();
            double upperBoundX = ((NumberAxis) chart.getXAxis()).getUpperBound();
            double tickUnitX = (upperBoundX - lowerBoundX) / 10;
            ((NumberAxis) chart.getXAxis()).setTickUnit(tickUnitX);

            double lowerBoundY = ((NumberAxis) chart.getYAxis()).getLowerBound();
            double upperBoundY = ((NumberAxis) chart.getYAxis()).getUpperBound();
            double tickUnitY = (upperBoundY - lowerBoundY) / 5;
            ((NumberAxis) chart.getYAxis()).setTickUnit(tickUnitY);

        };
        ((NumberAxis) chart.getXAxis()).scaleProperty().addListener(scaleListener);
        ((NumberAxis) chart.getYAxis()).scaleProperty().addListener(scaleListener);
        ((NumberAxis) chart.getXAxis()).lowerBoundProperty().addListener(scaleListener);
        ((NumberAxis) chart.getYAxis()).lowerBoundProperty().addListener(scaleListener);

        ChangeListener<Number> widthListener = (obs, oldValue, newValue) -> {
            Vscroll.setMaxHeight(imageView.getFitHeight());
            Vscroll.setMinHeight(imageView.getFitHeight());
        };
        ((NumberAxis) chart.getXAxis()).widthProperty().addListener(widthListener);
        ((NumberAxis) chart.getYAxis()).heightProperty().addListener(widthListener);
    }

    /**
     * Metoda zoomojaca wykresy 1D oraz Waterfall.
     *
     * @param zoomRect prostokat oznaczajacy zakres zoomowania
     * @param chart    obszar na ktorym nalezy zoomowac, w tym wypadku obszar wykresu 1D
     */
    private void doZoom(Rectangle zoomRect, LineChart<Number, Number> chart) {

        zoomRect.setX(zoomRect.localToScene(zoomRect.getX(), 0, true).getX());
        zoomRect.setY(zoomRect.localToScene(0, zoomRect.getY(), true).getY());

        Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());

        Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(),
                zoomRect.getY() + zoomRect.getHeight());
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene/* Parent */(0, 0, true);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene/* Parent */(0, 0, true);
        double xOffset = zoomTopLeft.getX() - yAxisInScene.getX() - yAxis.getWidth();
        double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();// - xAxis.getHeight();
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();

        xAxis.setLowerBound(xAxis.getLowerBound() + xOffset / xAxisScale);
        xAxis.setUpperBound(xAxis.getLowerBound() + zoomRect.getWidth() / xAxisScale);
        yAxis.setLowerBound(yAxis.getLowerBound() + yOffset / yAxisScale);
        yAxis.setUpperBound(yAxis.getLowerBound() - zoomRect.getHeight() / yAxisScale);

        zoomRect.setWidth(0);
        zoomRect.setHeight(0);

        chart.setCursor(Cursor.DEFAULT);

        doZoomWaterfall();

    }

    /**
     * Metoda przeksztalcajaca wspolrzedne na scenie na wspolrzedne logiczne na wykresie.
     *
     * @param chart obiekt wykresu
     * @param X     wartosc odcietej na scenie
     * @param Y     wartosc rzednej na scenie
     * @return punkt okreslajacy wartosc logiczna na wykresie
     */
    private Point2D getxyValueFromChart(LineChart<Number, Number> chart, double X, double Y) {
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0, true);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0, true);

        double XZeroInScene = chart.localToScene(0, 0, true).getX();
        double YZeroInScene = chart.localToScene(0, 0, true).getY();

        double xOffset = X - yAxisInScene.getX() - yAxis.getWidth() + XZeroInScene;
        double yOffset = Y - xAxisInScene.getY() /*- xAxis.getHeight()*/ + YZeroInScene;
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();

        double xValue = xAxis.getLowerBound() + xOffset / xAxisScale;
        double yValue = yAxis.getLowerBound() + yOffset / yAxisScale;
        return new Point2D(xValue, yValue);
    }

    /**
     * Metoda uaktualniajaca wartosc pola tekstowego okreslajacego zaznaczona czestotliwosc.
     *
     * @param chart obiekt wykresu
     * @param X     wartosc odcietej na scenie
     */
    private void setxField(LineChart<Number, Number> chart, double X) {
        Point2D p = getxyValueFromChart(chart, X, 0);

        xField.setText(String.valueOf(p.getX()));
    }

    /**
     * Metoda uaktualniajaca wartosc pola tekstowego okreslajacego prog detekcji (wartosc na osi Y).
     *
     * @param chart obiekt wykresu
     * @param Y     wartosc rzednej na scenie
     */
    private void setyField(LineChart<Number, Number> chart, double Y) {
        Point2D p = getxyValueFromChart(chart, 0, Y);

        yField.setText(String.valueOf(p.getY()));

        updateExternalThreshold();
    }

    /**
     * Metoda informujaca o tym ze prog detekcji sie zmienil.
     */
    private void updateExternalThreshold() {
        spectrumProcessor.setThreshold(Double.parseDouble(yField.getText()));
    }

    /**
     * Metoda przeksztalcajaca wspolrzedne logiczne na wykresie 1D na wspolrzedne na scenie.
     *
     * @param chart  obiekt wykresu
     * @param xValue wartosc logiczna odcietej na wykresie 1D
     * @param yValue wartosc logiczna rzednej na wykresie 1D
     * @return punkt okreslajacy wartosc wspolrzednych na scenie
     */
    private Point2D getChartValueFromxy(LineChart<Number, Number> chart, double xValue, double yValue) {

        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0, true);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0, true);// BEZ ZNACZENIA

        Point2D chartInScene = chart.localToScene(0, 0, true);

        double xLowerBound = xAxis.getLowerBound();

        double yLowerBound = yAxis.getLowerBound();

        // JAK ZOOMUJE TO WIDZI TUTAJ CIAGLE STARA SKALE, wiec aby uaktualnic linie to
        // musze zaczekac az ponizsze wartosci sa uaktualnione poprzez even handler dla
        // scaleProperty :
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();

        double xOffset = (xValue - xLowerBound) * xAxisScale;
        double x = yAxisInScene.getX() + yAxis.getWidth()/**/ + xOffset - chartInScene.getX();

        double yOffset = (yValue - yLowerBound) * yAxisScale;
        double y = xAxisInScene.getY() /* + xAxis.getHeight() */ + yOffset - chartInScene.getY();

        return new Point2D(x, y);
    }

    /**
     * Metoda okreslajaca nowy punk centralny na wykresie 1D na podstawie punktu centralnego na wykresie Waterfall.
     */
    static private void setNewCenterOnChart() {
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

        double lowerBound = xAxis.getLowerBound();
        double upperBound = xAxis.getUpperBound();
        double range = upperBound - lowerBound;
        double newLowerBound = offSetXOnChart(offSetX) - range * 0.5;
        double newUpperBound = offSetXOnChart(offSetX) + range * 0.5;

        if (newLowerBound < X_MIN)
            newLowerBound = X_MIN;
        if (newUpperBound > X_MAX)
            newUpperBound = X_MAX;

        xAxis.setLowerBound(newLowerBound);
        xAxis.setUpperBound(newUpperBound);

    }

    /**
     * Metoda okreslajaca nowy zoom na wykresie 1D na podstawie nowego zoomu ustawionego przez Slider oraz
     * srodka ustawionego na wykresie Waterfall.
     */
    static private void setZoomOnChart() {
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        // zoomlvl = zoomLvl.getValue();

        double lowerBound = xAxis.getLowerBound();
        double upperBound = xAxis.getUpperBound();
        double oldRange = upperBound - lowerBound;
        double newRange = (X_MAX - X_MIN) / zoomlvl;
        //

        double newLowerBound = /* offSetX */ offSetXOnChart(offSetX) - newRange * 0.5;
        double newUpperBound = /* offSetX */ offSetXOnChart(offSetX) + newRange * 0.5;

        if (newLowerBound < X_MIN)
            newLowerBound = X_MIN;
        if (newUpperBound > X_MAX)
            newUpperBound = X_MAX;

        xAxis.setLowerBound(newLowerBound);
        xAxis.setUpperBound(newUpperBound);
    }

    /**
     * Metoda przeksztalcajaca polozenie na wykresie Waterfall na polozenie na suwaku Hscroll.
     *
     * @param offSetX punk centralny na rysunku wykresu Waterfall
     * @return zwraca wartosc na suwaku
     */
    static private double offSetXOnScroll(double offSetX) {
        double rangeScroll = Hscroll.getMax() - Hscroll.getMin();

        double rangeImage = width;

        return Hscroll.getMin() + offSetX * rangeScroll / rangeImage;
    }

    /**
     * Metoda przeksztalcajaca polozenie na wykresie Waterfall na polozenie na wykresie 1D.
     *
     * @param offSetX punk centralny na rysunku wykresu Waterfall
     * @return zwraca wartosc na suwaku
     */
    static private double offSetXOnChart(double offSetX) {
        double rangeImage = width;

        double rangeScroll = Hscroll.getMax() - Hscroll.getMin();

        return Hscroll.getMin() + offSetX * rangeScroll / rangeImage;
    }

    /**
     * Metoda przeksztalcajaca polozenie na suwaku Hscroll na polozenie na wykresie Waterfall.
     *
     * @param HscrollValue wartosc na suwaku Hscroll dla wyboru czestotliwosci (os X)
     * @return zwraca wartosc na wykresie Waterfall
     */
    static private double xOnWaterfall(double HscrollValue) {
        double rangeScroll = Hscroll.getMax() - Hscroll.getMin();

        double rangeImage = width;

        return (HscrollValue - Hscroll.getMin()) * rangeImage / rangeScroll;
    }

    /**
     * Metoda podlaczajaca obsluge zdarzen potrzebna dla pokazywania linii pionowej na wykresie 1D
     * wskazujacej zaznaczona czestotliwosc (os X).
     *
     * @param line  obiekt linii
     * @param chart obiekt wykresu 1D
     */
    private void setUpVertLine(final Line line, LineChart<Number, Number> chart) {

        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();

        chart.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (event.getClickCount() == 1)
                    return;

                if (event.getX() < 43)
                    return;// kliknieto na os Y

                mouseAnchor.set(new Point2D(event.getX(), event.getY()));

                set_lineVert(line, mouseAnchor.get().getX());

                setxField(chart, event.getX());

            }
        });

        line.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();

                set_lineVert(line, x);

                setxField(chart, x);

            }
        });

        line.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                line.setCursor(Cursor.E_RESIZE);
            }
        });
    }

    /**
     * Metoda podlaczajaca obsluge zdarzen potrzebna dla pokazywania linii poziomej na wykresie 1D
     * wskazujacej prog detekcji (os Y).
     *
     * @param lineHor obiekt linii
     * @param chart   obiekt wykresu 1D
     */
    private void setUpHorLine(final Line lineHor, LineChart<Number, Number> chart) {

        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();

        chart.getYAxis().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                if (event.getClickCount() == 1)
                    return;

                final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
                Point2D yAxisInScene = yAxis.localToScene(0, 0, true);// &&&
                final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                Point2D xAxisInScene = xAxis.localToScene(0, 0, true);

                Point2D chartInScene = chart.localToScene(0, 0, true);

                double yOffset = xAxisInScene.getY() - yAxis.getHeight() - chartInScene.getY();

                double x = event.getX();
                double y = event.getY();
                // dragged ma 0 w ponkcie wyzej niz kliked ktory jest na ostatniej linii
                // poziomej wykresu
                // bo kliked jest liczony dla osi, a dragged dla StackPane
                // czyli trzeba przerobic punkt kliked na dragged:

                y = y + yOffset;
                set_lineHor(lineHor, y);

                setyField(chart, y);
            }
        });

        lineHor.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();// ZERO JEST W ROGU StackPane, liczone im dalej w dol tym wieksza wartosc

                set_lineHor(lineHor, y);// w stosunku do zera w rogu StackPane do ktorego jest dodana linia

                final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

                setyField(chart, y);
            }
        });

        lineHor.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lineHor.setCursor(Cursor.N_RESIZE);
            }
        });
    }

    /**
     * Metoda rysujaca linie pozioma na wykresie 1D
     * wskazujacej zaznaczona czestotliwosc (os X).
     *
     * @param lineHor obiekt linii
     * @param y       wartosc rzednej na scenie
     */
    private void set_lineHor(final Line lineHor, double y) {
        lineHor.setStartX(0);
        lineHor.setEndX(chart.getWidth());

        lineHor.setStartY(y);
        lineHor.setEndY(y);
    }

    /**
     * Metoda rysujaca linie pionowa na wykresie 1D
     * wskazujacej wybrany prog detekcji (os Y).
     *
     * @param lineVert obiekt linii
     * @param x        wartosc na osi odcietych na scenie
     */
    private void set_lineVert(final Line lineVert, double x) {
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0, true);
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0, true);
        double chartHeight = chart.getHeight();

        lineVert.setStartX(x);
        lineVert.setEndX(x);
        lineVert.setStartY(0);
        lineVert.setEndY(xAxisInScene.getY());

        updateMarkedFreq();
    }

    /**
     * Metoda uaktualniajaca linie pionowa na wykresie 1D
     * wskazujacej wybrany prog detekcji (os Y)
     * na podstawie pola tekstowego.
     *
     * @param lineVert obiekt linii
     */
    private void update_lineVert(Line lineVert) {
        double x = Double.parseDouble(xField.getText());

        Point2D p = getChartValueFromxy(chart, x, 0);

        set_lineVert(lineVert, p.getX());

    }

    /**
     * Metoda uaktualniajaca linie pozioma na wykresie 1D
     * wskazujacej wybrana czestotliwosc (os X)
     * na podstawie pola tekstowego.
     *
     * @param lineHor obiekt linii
     */
    private void update_lineHor(Line lineHor) {
        double y = Double.parseDouble(yField.getText());

        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();

        Point2D p = getChartValueFromxy(chart, 0, y);

        set_lineHor(lineHor, p.getY());
    }

    /**
     * Metoda podlaczajaca obsluge zdarzen dla pol tekstowych dla wartosci progu detekcji (na osi Y) oraz
     * czestotliwosci zaznaczonej (na osi X).
     *
     * @param xField   pole tekstowe dla wybranej czestotliwosci na osi X
     * @param yField   pole tekstowe dla wybranego progu detekcji na osi Y
     * @param chart    obiekt wykresu 1D
     * @param lineVert obiekt linii czestotliwosci (pionowej)
     * @param lineHor  obiekt linii progu detekcji (poziomej)
     */
    private void setxyFields(TextField xField, TextField yField, LineChart<Number, Number> chart, final Line lineVert,
                             final Line lineHor) {

        xField.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.ENTER) {
                // numbers including scientific notation:
                if (!xField.getText().matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
                    // gdy nie jest liczba to cofnij zmiany:
                    xField.setText(xFieldPrzed);
                } else {
                    xFieldPrzed = xField.getText();
                }

                if (!xField.getText().isEmpty())
                    update_lineVert(lineVert);
            }

        });

        xField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { // when focus lost
                if (!xField.getText().matches("-?\\d+(\\.\\d+)?")) {
                    // gdy nie jest liczba to cofnij zmiany:
                    xField.setText(xFieldPrzed);
                }
                if (!xField.getText().isEmpty())
                    update_lineVert(lineVert);
            } else {
                xFieldPrzed = xField.getText();

            }
        });

        yField.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.ENTER) {

                if (!yField.getText().matches("-?\\d+(\\.\\d+)?")) {
                    // gdy nie jest liczba to cofnij zmiany:
                    yField.setText(yFieldPrzed);
                } else {
                    yFieldPrzed = yField.getText();
                }

                if (!yField.getText().isEmpty()) {
                    update_lineHor(lineHor);

                    updateExternalThreshold();// spectrumProcessor.setThreshold(Double.parseDouble(yField.getText()));
                }
            }
        });

        yField.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) { // when focus lost
                if (!yField.getText().matches("-?\\d+(\\.\\d+)?")) {
                    // gdy nie jest liczba to cofnij zmiany:
                    yField.setText(yFieldPrzed);
                }
                if (!yField.getText().isEmpty()) {
                    update_lineHor(lineHor);

                    updateExternalThreshold();// spectrumProcessor.setThreshold(Double.parseDouble(yField.getText()));
                }
            } else {// gdy wchodzimy w pole tekstowe:
                yFieldPrzed = yField.getText();

            }
        });

    }

    // +++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Metoda interface SpectrumWaterfallListener wywolywana przy aktualizacji wykresu Waterfall
     * czestotliwosci zaznaczonej (na osi X).
     *
     * @param waterfallImage obrazek wykresu Waterfall ktory ma byc aktualnie pokazany
     * @param seqNumber      numer sekwencji (wartosc nie obslugiwana przez klase)
     * @param timeStamp      numer probki danych
     * @param freqStart      poczatkowy zakres czestotliwosci wyswietlanych
     * @param freqStep       dystans w Hz pomiedzy poszczegolnymi czestotliwosciami
     */
    @Override
    public void onImageProcessed(WritableImage waterfallImage, int seqNumber, double timeStamp, double freqStart,
                                 double freqStep) {

        imageView.setImage(waterfallImage);

    }

    static double initx;
    static double inity;
    static int height = 150;
    static int width = NUM_DATA_POINTS;
    public static String path;
    static Scene initialScene, View;
    static double offSetX, offSetY, zoomlvl;
    static double offSetXOnChart;
    static Slider Hscroll = new Slider();
    static Slider Vscroll = new Slider();
    static Slider zoomLvl = new Slider();
    static Label value = new Label("1.0");

    /**
     * Metoda podpinajaca metody obslugujace graficzny interface wykresu Waterfall
     * wraz z kontrolkami obslugujacymi ten wykres.
     */
    public VBox initView() {

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        ImageView image = imageView;// new ImageView(source);

        image.setPreserveRatio(false);

        HBox zoom = new HBox(10);
        zoom.setAlignment(Pos.CENTER);

        zoomLvl.setMax(100);
        zoomLvl.setMin(1);
        zoomLvl.setMaxWidth(200);
        zoomLvl.setMinWidth(200);
        Label hint = new Label("Zoom Level");

        offSetX = width / 2;
        offSetY = height / 2;

        value.setPrefWidth(100);

        zoom.getChildren().addAll(hint, zoomLvl, value);

        Hscroll.setMin(X_MIN);
        Hscroll.setMax(X_MAX);// width);

        Hscroll.setMajorTickUnit((X_MAX - X_MIN) / 10);
        Hscroll.setMinorTickCount(4);
        Hscroll.setShowTickLabels(true);
        Hscroll.setShowTickMarks(true);

        Vscroll.setMin(1);
        Vscroll.setMax(height);
        Vscroll.setMaxHeight(height);
        Vscroll.setMinHeight(height);
        Vscroll.setOrientation(Orientation.VERTICAL);

        BorderPane imageView = new BorderPane();

        Hscroll.valueProperty().addListener(e -> {

            setNewCenterOnWaterfall();
            setNewCenterOnChart();

        });

        Vscroll.valueProperty().addListener(e -> {
            nrTimeStamp = (int) Vscroll.getMax() - (int) Vscroll.getValue() + 1;// (double)((int)(Vscroll.getValue()*10))/10;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateChart();
                    updateMarkedFreq();
                }
            });

            if (Vscroll.getValue() != spinner.getValue())
                spinner.getValueFactory().setValue((int) Vscroll.getValue());
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

        zoomLvl.valueProperty().addListener(e -> {
            zoomlvl = zoomLvl.getValue();
            double newValue = zoomlvl;// (double)((int)(zoomlvl*10))/10;
            value.setText(newValue + "");
            if (offSetX < (width / newValue) / 2) {
                offSetX = (width / newValue) / 2;
            }
            if (offSetX > width - ((width / newValue) / 2)) {
                offSetX = width - ((width / newValue) / 2);
            }
            if (offSetY < (height / newValue) / 2) {
                offSetY = (height / newValue) / 2;
            }
            if (offSetY > height - ((height / newValue) / 2)) {
                offSetY = height - ((height / newValue) / 2);
            }
            Hscroll.setValue(offSetXOnScroll(offSetX));
            /* Vscroll.setValue(height-offSetY); */
            image.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
                    offSetY - ((height / /* newValue */1.0) / 2), width / newValue, height / 1.0/* newValue */));

            setZoomOnChart();
        });
        imageView.setCursor(Cursor.OPEN_HAND);
        image.setOnMousePressed(e -> {
            initx = e.getSceneX();
            inity = e.getSceneY();
            imageView.setCursor(Cursor.CLOSED_HAND);
        });
        image.setOnMouseReleased(e -> {
            imageView.setCursor(Cursor.OPEN_HAND);
        });
        image.setOnMouseDragged(e -> {
            double speed = 100;
            Hscroll.setValue(Hscroll.getValue() + (speed) * (initx - e.getSceneX()));
//            Vscroll.setValue(Vscroll.getValue()-(inity - e.getSceneY()));
            initx = e.getSceneX();
            inity = e.getSceneY();
        });
        root.getChildren().addAll(/* title, */imageView, zoom);

        return root;
    }

    /**
     * Metoda zoomujaca wykres Waterfall na podstawie zakresow danych na wykresie 1D
     * czyli ustawiane sa wartosci dla suwaka zoomu i suwaka czestotliwosci.
     */
    void doZoomWaterfall() {
        double x;
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        x = xAxis.getLowerBound() + (xAxis.getUpperBound() - xAxis.getLowerBound()) / 2;

        double zoom = (X_MAX - X_MIN) / (xAxis.getUpperBound() - xAxis.getLowerBound());

        zoomLvl.setValue(zoom);

        Hscroll.setValue(x);

    }

    /**
     * Metoda ustawiajaca nowe polozenie centralne na wykresie Waterfall
     * na podstawie wartosci na suwaku czestotliwosci (Hscroll) oraz suwaka zoomu (zoomLvl).
     */
    static private void setNewCenterOnWaterfall() {
        offSetX = xOnWaterfall(Hscroll.getValue());

        zoomlvl = zoomLvl.getValue();
        double newValue = zoomlvl;// (double)((int)(zoomlvl*10))/10;
        value.setText(newValue + "");
        if (offSetX < (width / newValue) / 2) {
            offSetX = (width / newValue) / 2;
        }
        if (offSetX > width - ((width / newValue) / 2)) {
            offSetX = width - ((width / newValue) / 2);
        }

        imageView.setViewport(new Rectangle2D(offSetX - ((width / newValue) / 2),
                offSetY - ((height / 1.0 /* newValue */) / 2), width / newValue, height / 1.0 /* newValue */));

    }

    /**
     * Metoda interface ReceiverDataConverterListener dla obslugi bledow.
     */
    @Override
    public void onError(String error) {
        // TODO Auto-generated method stub

    }

    /**
     * Metoda interface ReceiverDataConverterListener wywolywana w momencie otrzymania nowych danych.
     *
     * @param receivedData nowe dane w postaci tabeli
     * @param dataSize     rozmiar tabeli danych
     * @param seqNumber    numer sekwencji (wartosc nie obslugiwana przez klase)
     * @param timeStamp    numer probki danych
     * @param freqStart    poczatkowy zakres czestotliwosci wyswietlanych
     * @param freqStep     dystans w Hz pomiedzy poszczegolnymi czestotliwosciami
     */
    @Override
    synchronized public void onDataReceived(double[] receivedData, int dataSize, int seqNumber, double timeStamp,
                                            double freqStart, double freqStep) {

        ReceivedDataWithTimeStamp receivedDataWithTimeStamp = new ReceivedDataWithTimeStamp(receivedData, timeStamp,
                freqStart, freqStep);

        if (queueData.size() > 149) {
            queueData.remove();
        }

        queueData.put(receivedDataWithTimeStamp);

        updateChart();
        updateMarkedFreq();
    }

    /**
     * Klasa wewnetrzna obslugujaca dane ktore maja wyswietlic sie na wykresach
     * obiekty tej klasy sa gromadzone w kolejce do wyswietlenia.
     *
     * @author Damian Garsta / damiangarsta@wp.pl
     * @version 1.0
     */
    class ReceivedDataWithTimeStamp {
        double[] receivedData;
        double timeStamp;
        double freqStart;
        double freqStep;

        /**
         * Konstruktor
         *
         * @param receivedData nowe dane w postaci tabeli
         * @param timeStamp    numer probki danych
         * @param freqStart    poczatkowy zakres czestotliwosci wyswietlanych
         * @param freqStep     dystans w Hz pomiedzy poszczegolnymi czestotliwosciami
         */
        ReceivedDataWithTimeStamp(double[] receivedData, double timeStamp, double freqStart, double freqStep) {
            this.receivedData = receivedData;
            this.timeStamp = timeStamp;
            this.freqStart = freqStart;
            this.freqStep = freqStep;
        }

        /**
         * Metoda formatujaca dane na postac akceptowalna przez wykres.
         *
         * @return zwaraca liste tworzaca serie danych dla wykresu
         */
        ObservableList<XYChart.Data<Number, Number>> getXYChartData() {
            ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();

            double x;
            for (int i = 0; i < receivedData.length; i++) {
                x = freqStart + i * freqStep;
                data.add(new XYChart.Data<>(x, receivedData[i]));
            }
            return data;
        }

    }

    /**
     * Metoda pobiera wybrany element z kolejki zawieracacej 150 serii danych.
     *
     * @param iElementFrom1 numer serii danych ktora nalezy zwrocic liczana od 1
     * @param queue         kolejka z ktorej nalezy zwrocic serie danych
     * @return lista zwieracaja serie danych dla wykresu 1D
     */
    ObservableList<XYChart.Data<Number, Number>> getElementFromQueue(int iElementFrom1,
                                                                     PriorityBlockingQueue<ReceivedDataWithTimeStamp> queue) {
        ObservableList<XYChart.Data<Number, Number>> returnValue = null;

        final int queueSize = (queue.size() == 0) ? 1 : queue.size();

        PriorityBlockingQueue<ReceivedDataWithTimeStamp> queueTemp = new PriorityBlockingQueue<>(queueSize,
                comparator_queueData);

        queue.drainTo(queueTemp, iElementFrom1 - 1);

        if (!queue.isEmpty()) {

            returnValue = queue.peek().getXYChartData();
        } else {
            System.out.println("za malo elementow");
        }
        queue.addAll(queueTemp);

        // gdyby nie bylo bylo tylu elementow zwroc domyslny:
        if (returnValue == null) {
            ObservableList<XYChart.Data<Number, Number>> dummy = FXCollections.observableArrayList();

            for (int k = 0; k < NUM_DATA_POINTS; k++) {
                dummy.add(new XYChart.Data<>(k, 0));
            }

            returnValue = dummy;
        }
        return returnValue;
    }

}
