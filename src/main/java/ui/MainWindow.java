package ui;

import model.Location;
import model.Place;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.util.List;


public class MainWindow extends JFrame implements MainView {

    private MainPresenter presenter;

    private final JTextField searchField = new JTextField();
    private final JButton searchButton = new JButton("Найти");
    private final DefaultListModel<Location> locationsModel = new DefaultListModel<>();
    private final JList<Location> locationsList = new JList<>(locationsModel);
    private final JTextArea weatherArea = new JTextArea(4, 20);
    private final DefaultListModel<Place> placesModel = new DefaultListModel<>();
    private final JList<Place> placesList = new JList<>(placesModel);
    private final JTextArea placeDescriptionArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" ");

    public MainWindow() {
        setTitle("Travio_R7");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        buildLayout();
        wireEvents();
    }

    public void setPresenter(MainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showLocations(List<Location> locations) {
        locationsModel.clear();
        locations.forEach(locationsModel::addElement);
    }

    @Override
    public void showWeather(String text) {
        weatherArea.setText(text);
    }

    @Override
    public void showPlaces(List<Place> places) {
        placesModel.clear();
        places.forEach(placesModel::addElement);
    }

    @Override
    public void showDescription(String text) {
        placeDescriptionArea.setText(text);
        placeDescriptionArea.setCaretPosition(0);
    }

    @Override
    public void showStatus(String text) {
        statusLabel.setText(text);
    }

    @Override
    public void setSearchEnabled(boolean enabled) {
        searchButton.setEnabled(enabled);
        searchField.setEnabled(enabled);
    }

    @Override
    public void clearResults() {
        locationsModel.clear();
        placesModel.clear();
        weatherArea.setText("");
        placeDescriptionArea.setText("");
    }

    private void buildLayout() {
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        top.add(new JLabel("Город или место:"), BorderLayout.WEST);
        top.add(searchField, BorderLayout.CENTER);
        top.add(searchButton, BorderLayout.EAST);

        locationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        locationsList.setVisibleRowCount(4);
        locationsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Location loc = (Location) value;
                setText(loc.name() + ", " + loc.country());
                return this;
            }
        });
        JPanel locationsPanel = new JPanel(new BorderLayout());
        locationsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 8, 0, 8),
                BorderFactory.createTitledBorder("Найденные локации")));
        locationsPanel.add(new JScrollPane(locationsList), BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.add(top, BorderLayout.NORTH);
        header.add(locationsPanel, BorderLayout.CENTER);

        weatherArea.setEditable(false);
        weatherArea.setOpaque(false);
        JPanel weatherPanel = new JPanel(new BorderLayout());
        weatherPanel.setBorder(BorderFactory.createTitledBorder("Погода"));
        weatherPanel.add(weatherArea, BorderLayout.CENTER);

        placesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Place place = (Place) value;
                setText(place.name());
                return this;
            }
        });
        JPanel placesPanel = new JPanel(new BorderLayout());
        placesPanel.setBorder(BorderFactory.createTitledBorder("Интересные места рядом"));
        placesPanel.add(new JScrollPane(placesList), BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout());
        left.add(weatherPanel, BorderLayout.NORTH);
        left.add(placesPanel, BorderLayout.CENTER);

        placeDescriptionArea.setEditable(false);
        placeDescriptionArea.setLineWrap(true);
        placeDescriptionArea.setWrapStyleWord(true);
        placeDescriptionArea.setMargin(new Insets(6, 8, 6, 8));
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder("Описание"));
        descriptionPanel.add(new JScrollPane(placeDescriptionArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, descriptionPanel);
        split.setResizeWeight(0.4);
        split.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        searchButton.addActionListener(e -> presenter.onSearch(searchField.getText()));
        searchField.addActionListener(e -> presenter.onSearch(searchField.getText()));

        locationsList.addListSelectionListener(e -> {
            Location selected = locationsList.getSelectedValue();
            if (!e.getValueIsAdjusting() && selected != null) {
                presenter.onLocationSelected(selected);
                locationsList.clearSelection();
            }
        });
        placesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                presenter.onPlaceSelected(placesList.getSelectedValue());
            }
        });
    }
}
