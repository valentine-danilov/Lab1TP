package gui;

import shape.base.CloseShape;
import shape.line.Line;
import shape.line.Ray;
import shape.line.Segment;
import shape.polygon.*;
import shape.polygon.Polygon;
import shape.rectangle.Rectangle;
import shape.base.Shape;
import shape.rectangle.Circle;
import shape.rectangle.Ellipse;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

enum DrawAction {
    MOVE, RECTANGLE, ELLIPSE, REGULAR_POLYGON, SEGMENT, RAY, LINE,
    POLYGON, UPDATE_POLYGON, PARALLELOGRAM, RHOMBUS
}

public class App extends JFrame {
    private JToggleButton rectangleButton, ellipseButton, regularPolygonButton, segmentButton,
            lineButton, rayButton, polygonButton, parallelogramButton, rhombusButton;
    private JToggleButton moveShapesButton;
    private JPanel rootPanel;
    private JPanel drawPanel;
    private JSlider redSlider, greenSlider, blueSlider;
    private JButton frameColorButton, fillColorButton;
    private JIconComboBox widthComboBox;
    private JCheckBox transparencyCheckBox;
    private RegularPolygonDialog sideNumDialog;
    private ArrayList<Shape> shapes = new ArrayList<>();
    private boolean isDragged = false;
    private boolean isFrameColorChanging = true;
    private DrawAction drawAction = DrawAction.MOVE;
    private int frameWidth = 1;
    private Color frameColor = new Color(0, 0, 0);
    private Color fillColor = new Color(255, 255, 255);

    public App() {
        super("Just Draw It");
        sideNumDialog = new RegularPolygonDialog(this);
        setContentPane(rootPanel);
        setUpGUI();
        setSize(1400, 700);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private class ColorListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource().getClass().equals(JSlider.class)) {
                if (isFrameColorChanging) {
                    frameColor = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                    frameColorButton.setBackground(frameColor);
                } else {
                    fillColor = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
                    fillColorButton.setBackground(fillColor);

                }
            }
        }
    }

    private void setUpGUI() {
        rhombusButton.addActionListener(e -> drawAction = DrawAction.RHOMBUS);
        parallelogramButton.addActionListener(e -> drawAction = DrawAction.PARALLELOGRAM);
        polygonButton.addActionListener(e -> drawAction = DrawAction.POLYGON);
        lineButton.addActionListener(e -> drawAction = DrawAction.LINE);
        rayButton.addActionListener(e -> drawAction = DrawAction.RAY);
        segmentButton.addActionListener(e -> drawAction = DrawAction.SEGMENT);
        regularPolygonButton.addActionListener(e -> {
            drawAction = DrawAction.REGULAR_POLYGON;
            sideNumDialog.showDialog();
        });
        rectangleButton.addActionListener(e -> drawAction = DrawAction.RECTANGLE);
        ellipseButton.addActionListener(e -> drawAction = DrawAction.ELLIPSE);
        moveShapesButton.addActionListener(e -> drawAction = DrawAction.MOVE);
        redSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED), "Red"));
        greenSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GREEN), "Green"));
        blueSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLUE), "Blue"));
        ColorListener listener = new ColorListener();
        redSlider.addChangeListener(listener);
        greenSlider.addChangeListener(listener);
        blueSlider.addChangeListener(listener);

        drawPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (drawAction == DrawAction.MOVE)
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                else
                    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    switch (drawAction) {
                        case MOVE:
                            ListIterator<Shape> li = shapes.listIterator(shapes.size());
                            while (li.hasPrevious()) {
                                int prevIndex = li.previousIndex();
                                if (li.previous().contains(e.getPoint())) {
                                    isDragged = true;
                                    shapes.add(shapes.remove(prevIndex));
                                    break;
                                }
                            }
                            break;

                        case RECTANGLE:
                            shapes.add(new Rectangle(e.getPoint(), e.getPoint(), frameWidth, frameColor, fillColor));
                            break;
                        case ELLIPSE:
                            shapes.add(new Ellipse(e.getPoint(), e.getPoint(), frameWidth, frameColor, fillColor));
                            break;
                        case REGULAR_POLYGON:
                            shapes.add(new RegularPolygon(e.getPoint(), e.getPoint(), sideNumDialog.getSideNum(),
                                    frameWidth, frameColor, fillColor));
                            break;
                        case SEGMENT:
                            shapes.add(new Segment(e.getPoint(), e.getPoint(), frameWidth, frameColor));
                            break;
                        case RAY:
                            shapes.add(new Ray(e.getPoint(), e.getPoint(), frameWidth, frameColor));
                            break;
                        case LINE:
                            shapes.add(new Line(e.getPoint(), e.getPoint(), frameWidth, frameColor));
                            break;
                        case POLYGON:
                            List<Point> points = new ArrayList<>();
                            points.add(e.getPoint());
                            points.add(e.getPoint());
                            shapes.add(new Polygon(e.getPoint(), points, frameWidth, frameColor, fillColor));
                            drawAction = DrawAction.UPDATE_POLYGON;
                            break;
                        case UPDATE_POLYGON:
                            Shape currentShape = shapes.get(shapes.size() - 1);
                            ((Polygon) currentShape).addPoint(e.getPoint());
                            break;
                        case PARALLELOGRAM:
                            shapes.add(new Parallelogram(e.getPoint(), e.getPoint(), frameWidth, frameColor, fillColor));
                            break;
                        case RHOMBUS:
                            shapes.add(new Rhombus(e.getPoint(), e.getPoint(), frameWidth, frameColor, fillColor));
                            break;
                    }
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (drawAction == DrawAction.UPDATE_POLYGON) {
                        drawAction = DrawAction.POLYGON;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawAction == DrawAction.MOVE)
                    isDragged = false;
            }

        });

        drawPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && shapes.size() > 0) {
                    Shape currentShape = shapes.get(shapes.size() - 1);
                    switch (drawAction) {
                        case MOVE:
                            if (isDragged)
                                currentShape.move(e.getPoint());
                            break;
                        case RECTANGLE:
                            if (e.isShiftDown()) {
                                if (currentShape.getClass() != RegularPolygon.class) {
                                    RegularPolygon square = new RegularPolygon(currentShape.getLocation(),
                                            e.getPoint(), 4, currentShape.getFrameWidth(),
                                            currentShape.getFrameColor(), ((CloseShape) currentShape).getFillColor());
                                    square.setRotating(false);
                                    shapes.set(shapes.size() - 1, square);
                                } else
                                    ((RegularPolygon) currentShape).setPointOnCircle(e.getPoint());
                            } else {
                                if (currentShape.getClass() != Rectangle.class) {
                                    Rectangle rectangle = new Rectangle(currentShape.getLocation(), e.getPoint(),
                                            currentShape.getFrameWidth(), currentShape.getFrameColor(),
                                            ((CloseShape) currentShape).getFillColor());
                                    shapes.set(shapes.size() - 1, rectangle);
                                } else
                                    ((Rectangle) currentShape).setCornerPoint(e.getPoint());
                            }
                            break;
                        case ELLIPSE:
                            Ellipse ellipse = (Ellipse) currentShape;
                            ellipse.setCornerPoint(e.getPoint());
                            if (e.isShiftDown() && currentShape.getClass() != Circle.class) {
                                ellipse = new Circle(ellipse.getLocation(), ellipse.getCornerPoint(),
                                        ellipse.getFrameWidth(), ellipse.getFrameColor(), ellipse.getFillColor());
                                shapes.set(shapes.size() - 1, ellipse);
                            } else if (!e.isShiftDown() && currentShape.getClass() != Ellipse.class)
                                ellipse = new Ellipse(ellipse.getLocation(), ellipse.getCornerPoint(),
                                        ellipse.getFrameWidth(), ellipse.getFrameColor(), ellipse.getFillColor());
                            shapes.set(shapes.size() - 1, ellipse);
                            break;
                        case REGULAR_POLYGON:
                            RegularPolygon polygon = (RegularPolygon) currentShape;
                            polygon.setPointOnCircle(e.getPoint());
                            if (e.isShiftDown() && polygon.isRotating())
                                polygon.setRotating(false);
                            else if (!e.isShiftDown() && !polygon.isRotating())
                                polygon.setRotating(true);
                            break;
                        case SEGMENT:
                        case RAY:
                        case LINE:
                            Segment segment = (Segment) currentShape;
                            if (e.isShiftDown())
                                segment.setEndPoint(e.getPoint(), true);
                            else
                                segment.setEndPoint(e.getPoint());
                            break;
                        case UPDATE_POLYGON:
                            ((Polygon) currentShape).setLastPoint(e.getPoint());
                            break;
                        case PARALLELOGRAM:
                        case RHOMBUS:
                            ((Parallelogram) currentShape).setCornerPoint(e.getPoint());
                            break;
                    }
                    repaint();
                }
            }
        });
        widthComboBox.addItem(new ImageIcon(getClass().getResource("/resources/line_width_1.png")));
        widthComboBox.addItem(new ImageIcon(getClass().getResource("/resources/line_width_2.png")));
        widthComboBox.addItem(new ImageIcon(getClass().getResource("/resources/line_width_3.png")));
        widthComboBox.addItem(new ImageIcon(getClass().getResource("/resources/line_width_4.png")));
        widthComboBox.addActionListener(e -> frameWidth = (int) Math.pow(2, widthComboBox.getSelectedIndex()));

        ActionListener buttonColorListener = e -> {
            JButton source = (JButton) e.getSource();
            if ((!isFrameColorChanging && source == frameColorButton) ||
                    (isFrameColorChanging && source == fillColorButton)) {
                isFrameColorChanging = !isFrameColorChanging;
                setSlidersColor(source.getBackground());
                changeButtonsSize(frameColorButton, fillColorButton);
                validate();
            }
        };
        frameColorButton.addActionListener(buttonColorListener);
        fillColorButton.addActionListener(buttonColorListener);
        transparencyCheckBox.addItemListener(e -> {
            boolean isChecked = e.getStateChange() == ItemEvent.SELECTED;
            fillColor = new Color(fillColor.getRGB() & 0x00ffffff, isChecked);
            fillColorButton.setEnabled(!isChecked);
        });
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(App::new);
    }

    private void changeButtonsSize(JButton b1, JButton b2) {
        Dimension newSize = b2.getSize();
        b2.setMinimumSize(b1.getSize());
        b2.setPreferredSize(b1.getSize());
        b2.setMaximumSize(b1.getSize());
        b2.setSize(b1.getSize());
        b1.setMinimumSize(newSize);
        b1.setPreferredSize(newSize);
        b1.setMaximumSize(newSize);
        b1.setSize(newSize);
    }

    private void setSlidersColor(Color newColor) {
        redSlider.setValue(newColor.getRed());
        greenSlider.setValue(newColor.getGreen());
        blueSlider.setValue(newColor.getBlue());
    }

    private void createUIComponents() {
        drawPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (Shape s : shapes)
                    s.draw(g2d);
            }
        };
    }

}


