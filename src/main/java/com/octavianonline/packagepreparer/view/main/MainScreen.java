package com.octavianonline.packagepreparer.view.main;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import com.octavianonline.packagepreparer.model.CriticalFile;
import com.octavianonline.packagepreparer.model.ManifestModel;
import com.octavianonline.packagepreparer.service.ManifestService;
import com.octavianonline.packagepreparer.utils.Packages;
import com.octavianonline.packagepreparer.utils.ZipDirectory;
import com.octavianonline.packagepreparer.view.common.BaseView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class MainScreen extends BaseView implements IMainView {
    private final static String OS = System.getProperty("os.name").toLowerCase();
    private final IMainPresenter presenter;
    @FXML
    public Button buttonOpen;
    @FXML
    public Button buttonChoiceFont;
    @FXML
    public Label labelFontInfo;
    public StringProperty currentFontProperty = new SimpleStringProperty();
    @FXML
    public TreeView criticalTreeView;
    @FXML
    public TreeView configTreeView;
    private static final String CRITICAL_LIST = "critical.list";
    private static final String CONFIGURATION_LIST = "config.list";
    @FXML
    public TextField openPackage;
    @FXML
    public TextField packageNameText;
    @FXML
    public TextField versionText;
    @FXML
    public TextField descriptionText;
    @FXML
    public ToggleGroup typeGroup;
    @FXML
    public TextField manufacturerText;
    @FXML
    public TextField fileNameText;
    @FXML
    public TextField pathText;
    @FXML
    public TabPane mainTab;

    @FXML
    public TextField dependencyNameText;
    @FXML
    public ComboBox<String> signCombo;
    @FXML
    public TextField dependencyVersion;
    @FXML
    public ListView<String> dependencyListView;
    @FXML
    public ListView<String> configListView;
    @FXML
    public ProgressBar progressId;
    @FXML
    public ProgressIndicator progressIndId;
    @FXML
    public ListView<String> criticalListView;
    @FXML
    public Label buildMessage;
    @FXML
    public Button buttonBuild;
    @FXML
    public TextArea jsonText;

    private ZipDirectory zipFiles;
    @Autowired
    private ManifestService manifestService;

    @FXML
    public Tab criticFile;
    @FXML
    public Tab configFiles;
    @FXML
    public Tab dependFiles;
    @FXML
    public Tab duildFiles;

    @FXML
    TextField criticFilter;

    @FXML
    TextField configFilter;

    @FXML
    Button addDependency;

    @FXML
    TitledPane forBuild;

    @FXML
    ListView tips;
    Set<String> depend = new HashSet<>();

    @FXML
    ListView manuList;
    Set<String> manu = new HashSet<>();

    @FXML
    Button breakButton;


    @FXML
    Button buttonOpenManifest;

    @FXML
    private CheckBox restorePackage;

    private EventBus eventBus;

    @FXML
    private RadioButton checkContent;
    @FXML
    private RadioButton checkOs;
    @FXML
    private RadioButton checkEGM;
    @FXML
    private TextField restorePath;

    @Autowired
    Packages packages;

    static enum FileType {
        CRITICAL, CONFIG
    }

    public void breakZipping(ActionEvent actionEvent) {
        if (zipFiles != null) {
            zipFiles.setBreakZip(true);
            breakButton.setDisable(true);
        }
    }


    private final static String ZIP = ".zip";

    private CheckBoxTreeItem<CriticalFile> rootCriticalFiles;
    private CheckBoxTreeItem<CriticalFile> rootConfigFiles;

    private Map<String, Boolean> validationMap = new HashMap() {{
        put("packageName", false);
        put("version", false);
//        put("description", false);
        put("manufacturer", false);
        put("path", false);
        put("opened", false);
    }};


    public MainScreen(EventBus eventBus, IMainPresenter presenter) {
        super(eventBus, presenter);
        this.eventBus = eventBus;
        this.presenter = presenter;
    }

    @Override //afterActivated
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        ObservableList<String> signList = FXCollections.observableArrayList(">", ">=", "<", "<=", "==");
        signCombo.setItems((ObservableList<String>) signList);
        signCombo.getSelectionModel().select(0);

        TreeCellFactory cellFactory = new TreeCellFactory();
        criticalTreeView.setCellFactory(cellFactory);
        configTreeView.setCellFactory(cellFactory);

        criticalTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        configTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        initKeyListeners();


        dependencyListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initializeValidation();
        progressId.setProgress(0.0);


        try {
            packages.checkPackages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        manifestModels = packages.createManifestModelList();
        readDependencyFile();
        readManuFile();

    }

    private void readDependencyFile() {
        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }

        try (Stream<String> stream = Files.lines(Paths.get(packages.getPackagesDirectory() + pathDelimiter + "dependencies.list"))) {
            stream.forEach(s -> {
                depend.add(s);
            });
        } catch (IOException e) {
            System.out.println("Dependencies list was not found");
        }
        tips.getItems().addAll(depend.stream().sorted().collect(Collectors.toList()));

    }

    private void readManuFile() {
        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }
        try (Stream<String> stream = Files.lines(Paths.get(packages.getPackagesDirectory() + pathDelimiter + "manufacturer.list"))) {
            stream.forEach(s -> {
                manu.add(s);
            });
        } catch (IOException e) {
            System.out.println("Manufacturer list was not found");
        }


        manifestModels.forEach(manifestModel -> {
            manu.add(manifestModel.getManufacturer());
        });

        manuList.getItems().addAll(manu.stream().sorted().collect(Collectors.toList()));
    }


    public void refreshCritical(ActionEvent actionEvent) {
        filterTree(criticFilter.getText(), FileType.CRITICAL);

    }

    /**
     * dependency list listener for delete
     */
    void initKeyListeners() {

        tips.setOnMouseClicked(event -> {

            Object selected = tips.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String signValue = selected.toString();
                String dependName = signValue.replaceAll("[|-].*", "");
                String dependVersion = signValue.replaceAll(".*(\\||\\-){1}", "");

                dependencyNameText.setText(dependName);
                dependencyVersion.setText(dependVersion);

                String sign = signValue.replaceAll(dependName, "").replaceAll(dependVersion, "").replaceAll("\\|", "");

                signCombo.getItems().forEach(s -> {
                    if (s.equals(sign)) {
                        signCombo.getSelectionModel().select(s);
                    }
                });
            }
        });

        manuList.setOnMouseClicked(event -> {

            Object selected = manuList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String signValue = selected.toString();
                manufacturerText.setText(signValue);

            }
        });


        dependencyListView.setOnKeyPressed((new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {

                if (ke.getCode().getName().toLowerCase().equals("delete")) {
                    ObservableList removeList = dependencyListView.getSelectionModel().getSelectedItems();
                    dependencyListView.getItems().removeAll(removeList);
                }

            }
        }));

        configFilter.setOnKeyPressed((new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {

                if (ke.getCode().getName().toLowerCase().equals("enter")) {
                    filterTree(configFilter.getText(), FileType.CONFIG);
                }

            }
        }));

        criticFilter.setOnKeyPressed((new EventHandler<KeyEvent>() {
            public void handle(KeyEvent ke) {

                if (ke.getCode().getName().toLowerCase().equals("enter")) {
                    filterTree(criticFilter.getText(), FileType.CRITICAL);
                }

            }
        }));

        jsonText.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue,
                                Object newValue) {
                jsonText.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                //use Double.MIN_VALUE to scroll to the top
            }
        });

    }

    public void refreshConfig(ActionEvent actionEvent) {
        filterTree(configFilter.getText(), FileType.CONFIG);
    }

    private void clearData() {
        packageNameText.clear();
        versionText.clear();
        descriptionText.clear();
        manufacturerText.clear();
        fileNameText.clear();
        pathText.clear();

        validationMap.put("packageName", false);
        validationMap.put("version", false);
        validationMap.put("manufacturer", false);
        validationMap.put("path", false);

        buildMessage.setText("Please press 'Build package' to build");
        dependencyListView.getItems().removeAll(dependencyListView.getItems());
        configListView.getItems().removeAll(configListView.getItems());
        criticalListView.getItems().removeAll(criticalListView.getItems());
        dependencyNameText.clear();
        dependencyVersion.clear();
        jsonText.clear();
    }

    List<ManifestModel> manifestModels;

    /**
     * For fill list of package dependencies after open a package
     */
    public void refreshDepList() {
        manifestModels = packages.createManifestModelList();

        /**
         *
         */
        manifestModels.forEach(manifestModel -> {
            manifestModel.getDependencies().forEach(s -> {
                if (!tips.getItems().contains(s)) {
                    depend.add(s);
                }
            });

            String packageName = manifestModel.getPackageName() + "-" + manifestModel.getVersion();
            if (!tips.getItems().contains(packageName)) {
                depend.add(packageName);
            }

            manu.add(manifestModel.getManufacturer());

        });

        List<String> sortedDependencies = depend.stream().sorted().collect(Collectors.toList());
        tips.getItems().clear();
        tips.getItems().addAll(sortedDependencies);


        manuList.getItems().clear();
        manuList.getItems().addAll(manu.stream().sorted().collect(Collectors.toList()));

    }

    public void addToDependencyList(ActionEvent actionEvent) {
        Set<String> dependencyList = new HashSet<>();
        dependencyList.addAll(dependencyListView.getItems());
        String value = dependencyNameText.getText() + "|" + signCombo.getSelectionModel().getSelectedItem() + "|" + dependencyVersion.getText();
        dependencyList.add(value);
        dependencyListView.getItems().removeAll(dependencyListView.getItems());
        dependencyList.forEach(s -> {
            dependencyListView.getItems().add(s);
        });
        dependencyNameText.setText("");
        dependencyVersion.setText("");
    }

    public void removeDependency(ActionEvent actionEvent) {
        ObservableList removeList = dependencyListView.getSelectionModel().getSelectedItems();
        dependencyListView.getItems().removeAll(removeList);
    }


    class TreeCellFactory implements Callback<TreeView<CriticalFile>, CheckBoxTreeCell<CriticalFile>> {

        @Override
        public CheckBoxTreeCell<CriticalFile> call(TreeView<CriticalFile> param) {
            CheckBoxTreeCell<CriticalFile> checkBox = new CheckBoxTreeCell();
            return checkBox;
        }
    }


    CustomEventHandler customEventHandler;
    CustomEventHandler customEventHandler2;

    /**
     * Creating tree after filter
     *
     * @param filter
     * @param fileType
     */
    private void filterTree(String filter, FileType fileType) {
        if (fileType.equals(FileType.CRITICAL)) {
            rootCriticalFiles = getNodesForDirectory(rootDirectory, filter, true);
            rootCriticalFiles.setExpanded(true);
            criticalTreeView.setRoot(rootCriticalFiles);
            criticalListView.getItems().removeAll(criticalListView.getItems());
            List<String> sortedCritical = criticalFilesLists.stream().map(criticalFile -> criticalFile.getPath()).sorted((o1, o2) -> o1.compareTo(o2)).collect(Collectors.toList());
            criticalListView.getItems().addAll(sortedCritical);
            customEventHandler = new CustomEventHandler(criticalFilesLists, criticalListView, "critic");
            rootCriticalFiles.removeEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler);
            rootCriticalFiles.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler);
            setTreeChecked(rootCriticalFiles);
        } else if (fileType.equals(FileType.CONFIG)) {
            rootConfigFiles = getNodesForDirectory(rootDirectory, filter, true);
            rootConfigFiles.setExpanded(true);
            configTreeView.setRoot(rootConfigFiles);
            configListView.getItems().removeAll(configListView.getItems());
            List<String> sortedConfig = configFilesLists.stream().map(configFile -> configFile.getPath()).sorted((o1, o2) -> o1.compareTo(o2)).collect(Collectors.toList());
            configListView.getItems().addAll(sortedConfig);
            customEventHandler2 = new CustomEventHandler(configFilesLists, configListView, "config");
            rootConfigFiles.removeEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler2);
            rootConfigFiles.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler2);
            setTreeCheckedConfig(rootConfigFiles);
        }
    }

    /**
     * For check old value in CheckBoxes after refresh filter in critical tab
     *
     * @param checkBoxTreeItem
     */
    private void setTreeChecked(CheckBoxTreeItem checkBoxTreeItem) {
        checkBoxTreeItem.getChildren().forEach(o -> {

            if (((CheckBoxTreeItem) o).getValue() instanceof CriticalFile) {
                CriticalFile criticalFile = (CriticalFile) ((CheckBoxTreeItem) o).getValue();
                if (criticalFilesLists.contains(criticalFile)) {
                    ((CheckBoxTreeItem) o).setSelected(true);
                }
            } else {
                setTreeChecked((CheckBoxTreeItem) o);
            }

        });
    }

    /**
     * For check old value in CheckBoxes after refresh filter in config tab
     *
     * @param checkBoxTreeItem
     */
    private void setTreeCheckedConfig(CheckBoxTreeItem checkBoxTreeItem) {
        checkBoxTreeItem.getChildren().forEach(o -> {

            if (((CheckBoxTreeItem) o).getValue() instanceof CriticalFile) {
                CriticalFile criticalFile = (CriticalFile) ((CheckBoxTreeItem) o).getValue();
                if (configFilesLists.contains(criticalFile)) {
                    ((CheckBoxTreeItem) o).setSelected(true);
                }
            } else {
                setTreeCheckedConfig((CheckBoxTreeItem) o);
            }

        });
    }


    File rootDirectory;

    public void openAction(ActionEvent actionEvent) {
        presenter.openAction();
    }

    @Override
    public void showProjectFile(File workDirectory) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(workDirectory);
        rootDirectory = directoryChooser.showDialog(rootView.getScene().getWindow());
        if (rootDirectory != null) {
            presenter.setWorkingDirectory(rootDirectory.getPath());

//            presenter.saveLastWorkingDirectory(openPackage.getText());
            jsonText.clear();

            packageNameText.setDisable(false);
            packageNameText.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CCCCCC;");

            versionText.setDisable(false);
            versionText.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CCCCCC;");

            descriptionText.setDisable(false);
            descriptionText.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CCCCCC;");

            manufacturerText.setDisable(false);
            manufacturerText.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CCCCCC;");

            pathText.setDisable(false);
            pathText.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CCCCCC;");

            criticFilter.setText("");
            configFilter.setText("");
            progressId.progressProperty().unbind();
            progressId.setProgress(0.0);
            criticalList = new ArrayList<>();
            configList = new ArrayList<>();
            clearData();
            packageNameText.setText(rootDirectory.getName());
            String directory = rootDirectory.getPath();
            openPackage.setText(directory);
            openPackage.setTooltip(new Tooltip(directory));
            if (criticalFilesLists != null) {
                criticalFilesLists.clear();
            } else {
                criticalFilesLists = new HashSet<>();
            }

            if (configFilesLists != null) {
                configFilesLists.clear();
            } else {
                configFilesLists = new HashSet<>();
            }

            rootCriticalFiles = getNodesForDirectory(rootDirectory, null, false);
            rootConfigFiles = getNodesForDirectory(rootDirectory, null, false);

            rootCriticalFiles.setExpanded(true);
            rootConfigFiles.setExpanded(true);
            criticalTreeView.setRoot(rootCriticalFiles);
            configTreeView.setRoot(rootConfigFiles);

            customEventHandler = new CustomEventHandler(criticalFilesLists, criticalListView, "critic");
            customEventHandler2 = new CustomEventHandler(configFilesLists, configListView, "config");

            rootCriticalFiles.removeEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler);
            rootConfigFiles.removeEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler2);

            rootCriticalFiles.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler);
            rootConfigFiles.addEventHandler(CheckBoxTreeItem.checkBoxSelectionChangedEvent(), customEventHandler2);
            validationMap.put("opened", true);
            refreshDepList();

            restorePackage.setDisable(false);
            restorePackage.setSelected(false);
            buttonOpenManifest.setDisable(true);

        }
    }


    private Set<CriticalFile> criticalFilesLists;
    private Set<CriticalFile> configFilesLists;


    /**
     * For create a file tree
     *
     * @param directory
     * @return
     */
    public CheckBoxTreeItem<CriticalFile> getNodesForDirectory(File directory, String filter, boolean filtered) {

        List<CheckBoxTreeItem<CriticalFile>> nodes = new ArrayList<>();
        List<CheckBoxTreeItem<CriticalFile>> leaves = new ArrayList<>();

        CheckBoxTreeItem<CriticalFile> root = new CheckBoxTreeItem(directory.getName());
        for (File filePath : directory.listFiles()) {
            if (filePath.isDirectory()) {
//                getNodesForDirectory(filePath, filter);
                if (checkForFiles(filePath, filter, false)) {
                    CheckBoxTreeItem<CriticalFile> node = getNodesForDirectory(filePath, filter, filtered);
                    if (filtered) {
                        node.setExpanded(true);
                    }
                    nodes.add(node);
                }
            } else {
                Path rootFolder = rootDirectory.toPath();
                Path currentAllPath = filePath.toPath();
                Path relativePath = rootFolder.relativize(currentAllPath);
                String fileName = filePath.getName();

                if (filter != null && filter.length() > 0 && minifilter(fileName, filter) || filter == null || filter.length() == 0) {
                    leaves.add(new CheckBoxTreeItem(new CriticalFile(relativePath.toString(), fileName, false)));
                }
            }
        }
        leaves.sort((o1, o2) -> (o1.getValue().getName().compareTo(o2.getValue().getName())));
        root.getChildren().addAll(sort(nodes));
        root.getChildren().addAll(leaves);
        return root;
    }

    /**
     * For checking if there is any file in the directory
     *
     * @param rootPath
     * @param filter
     * @param boole
     * @return
     */
    boolean checkForFiles(File rootPath, String filter, boolean boole) {
        boolean b = boole;
        for (File filePath : rootPath.listFiles()) {
            if (!filePath.isDirectory()) {
                String fileName = filePath.getName();
                if (filter != null && filter.length() > 0 && minifilter(fileName, filter) || filter == null || filter.length() == 0) {
                    b = true;
                    return b;
                }
            } else {
                b = checkForFiles(filePath, filter, b);
            }

        }

        return b;
    }

    boolean minifilter(String fileName, String filter) {
        String newFilter = filter.replaceAll("\\?", ".");
        newFilter = newFilter.replaceAll("\\*", ".*");
        return fileName.toLowerCase().matches(newFilter.toLowerCase());
    }

    /**
     * For sorting tree folders
     *
     * @param unsorted
     * @return
     */
    public List<CheckBoxTreeItem<CriticalFile>> sort(List<CheckBoxTreeItem<CriticalFile>> unsorted) {
        boolean isSorted = false;
        CheckBoxTreeItem<CriticalFile> buf;
        while (!isSorted) {
            isSorted = true;
            for (int i = 0; i < unsorted.size() - 1; i++) {
                CheckBoxTreeItem<CriticalFile> oldVal = unsorted.get(i);
                CheckBoxTreeItem<CriticalFile> newVal = unsorted.get(i + 1);
                Object previousFolder = oldVal.getValue();
                Object nextFolder = newVal.getValue();
                if (previousFolder.toString().compareTo(nextFolder.toString()) > 0) {
                    isSorted = false;
                    buf = unsorted.get(i);
                    unsorted.set(i, unsorted.get(i + 1));
                    unsorted.set(i + 1, buf);
                }
            }
        }
        return unsorted;
    }


    /**
     * For handling flag selection events in files trees
     */
    class CustomEventHandler implements EventHandler<CheckBoxTreeItem.TreeModificationEvent<CriticalFile>> {
        private Set<CriticalFile> fileList;
        ListView<String> listView;
        String type;

        public CustomEventHandler(Set<CriticalFile> fileList, ListView<String> listView, String type) {
            super();
            this.fileList = fileList;
            this.listView = listView;
            this.type = type;
        }

        /**
         * for selecting checkboxes in trees
         *
         * @param event
         */
        @Override
        public void handle(CheckBoxTreeItem.TreeModificationEvent<CriticalFile> event) {
            CheckBoxTreeItem<CriticalFile> item = event.getTreeItem();
            if (item.getValue() instanceof CriticalFile && (item.isSelected() || item.isIndeterminate())) {

                if (criticalFilesLists.contains((CriticalFile) item.getValue()) && type.equals("config") || configFilesLists.contains((CriticalFile) item.getValue()) && type.equals("critic")) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info");
                    alert.setHeaderText("This file has already been added to another list");
                    Optional<ButtonType> option = alert.showAndWait();
                    if (option.get() == ButtonType.OK) {
                        Timeline timeline = new Timeline(new KeyFrame(
                                Duration.millis(100),
                                ae -> ((CheckBoxTreeItem) item).setSelected(false)));
                        timeline.play();
                    }

                } else {
                    item.getValue().setCheck(true);
                    fileList.add(item.getValue());
                    listView.getItems().removeAll();
                }
            } else if (item.getValue() instanceof CriticalFile) {
                item.getValue().setCheck(false);
                fileList.remove(item.getValue());
                System.out.println("Some items are unchecked");
                listView.getItems().remove(item.getValue().getPath());
            }
            listView.getItems().removeAll(listView.getItems());

            List<String> sortelList = fileList.stream().map(criticalFile -> criticalFile.getPath()).sorted().collect(Collectors.toList());
            listView.getItems().addAll(sortelList);
        }
    }





    List<String> criticalList;

    /**
     * form critical files list and create file "critical.list"
     *
     * @throws IOException
     */
    public void createCritical() throws IOException {
        criticalList = new ArrayList<>();
        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }
        if (criticalFilesLists != null) {


            criticalFilesLists.forEach(criticalFile -> {
                if (criticalFile.isCheck()) {
                    criticalList.add(criticalFile.getPath().replace("\\", "/"));
                }


            });
        }
        Path file;
        String path;
        path = rootDirectory.getPath() + pathDelimiter + CRITICAL_LIST;
        file = Paths.get(path);
        Files.write(file, criticalList, StandardCharsets.UTF_8);
    }

    List<String> configList;

    /**
     * fill manifest file's list
     */
    public void createConfig() throws IOException {
        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }

        configList = new ArrayList<>();
        if (configFilesLists != null) {
            configFilesLists.forEach(configFile -> {
                if (configFile.isCheck()) {
                    String path = (configFile.getPath());
                    configList.add(path.replace("\\", "/"));
                }
            });
        }
        Path file;
        String path;
        path = rootDirectory.getPath() + pathDelimiter + CONFIGURATION_LIST;
        file = Paths.get(path);
        Files.write(file, configList, StandardCharsets.UTF_8);
//        manifestService.setConfiguration(configList);
    }

    /**
     * create zip - archive
     *
     * @param actionEvent
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void build(ActionEvent actionEvent) throws IOException, NoSuchAlgorithmException {
        String messsage = "";
        jsonText.clear();
        createCritical();
        createConfig();
        jsonText.appendText(CONFIGURATION_LIST + " was created \n");
        jsonText.appendText(CRITICAL_LIST + " was created \n\n");
        if (criticalList == null || criticalList.isEmpty()) {
            messsage += "Critical file list is empty. Proceed?";
        }

        if (configList == null || configList.isEmpty()) {
            messsage += "\nConfiguration file list is empty. Proceed?";
        }

        if (messsage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Warning");
            alert.setHeaderText(messsage);
            Optional<ButtonType> option = alert.showAndWait();
            if (option.get() == ButtonType.OK) {
                jsonText.appendText("---------------------- Zip - packaging start----------------------\n");
                breakButton.setDisable(false);
                createZipPackage();
            }
        } else {
            jsonText.appendText("---------------------- Zip - packaging start----------------------\n\n");
            breakButton.setDisable(false);
            createZipPackage();
        }
    }

    private void createZipPackage() throws FileNotFoundException {
        buildMessage.setText("Please wait");
        String path;
        path = rootDirectory.getPath();
        String version = versionText.getText();
        if (version.length() > 0) {
            version = "-" + version;
        }
        String zipName = packageNameText.getText() + version + ZIP;

        progressIndId.setVisible(true);

        zipFiles = new ZipDirectory(packages.getPackagesDirectory(), path, zipName, progressId, jsonText, pathText);
        progressId.progressProperty().unbind();
        progressId.progressProperty().bind(zipFiles.progressProperty());
        EndZippingEvent event = new EndZippingEvent(zipFiles);
        zipFiles.removeEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event);
        zipFiles.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event);
        buttonBuild.setDisable(true);

        mainTab.getTabs().forEach(tab -> {
            if (!tab.getId().equals("duildFiles")) {
                tab.setDisable(true);
            }
        });
        new Thread(zipFiles).start();
    }

    class EndZippingEvent implements EventHandler<WorkerStateEvent> {

        ZipDirectory copyTask;

        //        public EndZippingEvent(ZipFiles copyTask) {
        public EndZippingEvent(ZipDirectory copyTask) {
            this.copyTask = copyTask;
        }

        @Override
        public void handle(WorkerStateEvent event) {
            try {
                createManifest();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            progressIndId.setVisible(false);
            mainTab.getTabs().forEach(tab -> tab.setDisable(false));
            buttonBuild.setDisable(false);
            buildMessage.setText("The package was successfully created.");
            jsonText.appendText("---------------------- Zip - packaging end----------------------\n\n");
            jsonText.appendText("\n" + "Manifest:" + "\n" + manifestService.getGson());
            breakButton.setDisable(true);
        }
    }

    private void createManifest() throws IOException, NoSuchAlgorithmException {
        String packagePath = rootDirectory.getPath();
        manifestService.checksum(
                packages.getPackagesDirectory(),
                packageNameText,
                versionText,
                descriptionText,
                typeGroup,
                manufacturerText,
                pathText,
                dependencyListView);
    }

    private boolean allowTabs = true;

    /**
     * Checking if other tabs can be opened
     */
    private void checkValidation() {
        allowTabs = true;

        validationMap.forEach((s, aBoolean) -> {
            if (!aBoolean) {
                allowTabs = aBoolean;
            }
        });

        if (allowTabs) {
            criticFile.setDisable(false);
            configFiles.setDisable(false);
            dependFiles.setDisable(false);
            duildFiles.setDisable(false);
        } else {
            criticFile.setDisable(true);
            configFiles.setDisable(true);
            dependFiles.setDisable(true);
            duildFiles.setDisable(true);
        }
    }

    /**
     * For fields validation
     */
    public void initializeValidation() {
        packageNameText.textProperty().addListener((observable, oldValue, newValue) -> {
            String packageText1 = packageNameText.getText();
            if (packageText1.length() > 0) {
                if (packageText1.length() > 255) {
                    packageNameText.setText(packageNameText.getText(0, 255));
                }

                if (!Character.isLetter(packageNameText.getText().charAt(0))) {
                    packageNameText.setText("");
                }
                packageNameText.setText(packageNameText.getText().replaceAll("[^A-Za-zА0-9_]", ""));

                if (!packageNameText.getText().matches("^[a-zA-Z]{1,1}[a-zA-Z0-9-_]{1,255}$|^[a-zA-Z]{1,1}")) {
                    validationMap.put("packageName", false);
                } else {
                    validationMap.put("packageName", true);
                }
            } else {
                validationMap.put("packageName", false);
            }

            String version = versionText.getText();
            if (version.length() > 0) {
                version = "-" + version;
            }
            fileNameText.setText(packageNameText.getText() + version + ZIP);
            checkValidation();
        });


        versionText.textProperty().addListener((observable, oldValue, newValue) -> {
            String versionText1 = versionText.getText();
            if (versionText1.length() > 0) {
                if (versionText1.length() > 255) {
                    versionText.setText(versionText.getText(0, 255));
                }
                if (!newValue.matches("[0-9]{1,4}|[0-9]{1,4}\\.|[0-9]{1,4}\\.[0-9]{1,4}|[0-9]{1,4}\\.[0-9]{1,4}\\.|[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}|[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}\\.|[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")) {
//                    if(!newValue.matches("\\d{1,4}|(\\d{1,4}\\.){1,3}|(\\d{1,4}\\.\\d{1,4}){1,3}")){
                    versionText.setText(oldValue);
                }

                if (!versionText.getText().matches(".*[0-9]{1,4}$")) {
                    validationMap.put("version", false);
                } else {
                    validationMap.put("version", true);
                }

            } else {
                validationMap.put("version", false);
            }
            String version = versionText.getText();
            if (version.length() > 0) {
                version = "-" + version;
            }
            fileNameText.setText(packageNameText.getText() + version + ZIP);
            checkValidation();
        });

        pathText.textProperty().addListener((observable, oldValue, newValue) -> {

            if (pathText.getText().length() > 0) {
                if (pathText.getText().length() > 255) {
                    pathText.setText(pathText.getText(0, 255));
                }

                if (!pathText.getText().substring(0, 1).equals("/")) {
                    pathText.setText("/" + pathText.getText());
                }
                if (pathText.getText().length() == 2) {
                    if (!Character.isLetter(pathText.getText().charAt(1))) {
                        pathText.setText("/");
                    }
                }

                pathText.setText(pathText.getText().replaceAll("[^A-Za-zА0-9_/]", ""));

                String path = pathText.getText();

                if (!pathText.getText().matches("^[a-zA-Z/]{1,1}[a-zA-Z0-9-_/-]{1,255}$|/")) {
                    validationMap.put("path", false);
                } else {
                    validationMap.put("path", true);
                }
                pathText.setText(path.replaceAll("//", "/"));

            } else {
                validationMap.put("path", false);
            }
            checkValidation();
        });

        manufacturerText.textProperty().addListener((observable, oldValue, newValue) -> {
            manuList.getItems().clear();

            if (manufacturerText.getText().length() > 0) {
                if (manufacturerText.getText().length() > 255) {
                    manufacturerText.setText(manufacturerText.getText(0, 255));
                }

                validationMap.put("manufacturer", true);
            } else {
                validationMap.put("manufacturer", false);
            }

            List<String> sortedManu = manu.stream().sorted().collect(Collectors.toList());
            sortedManu.forEach(s -> {
                String manUText = manufacturerText.getText();
                if (s.toLowerCase().contains(manufacturerText.getText().toLowerCase())) {
                    manuList.getItems().add(s);
                }
            });

            checkValidation();
        });


        dependencyVersion.textProperty().addListener((observable, oldValue, newValue) -> {
            String versionText1 = dependencyVersion.getText();

            if (versionText1.length() > 0) {

                if (!newValue.matches("[0-9]{1,4}|[0-9]{1,4}\\.|[0-9]{1,4}\\.[0-9]{1,4}|[0-9]{1,4}\\.[0-9]{1,4}\\.|[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}|[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}\\.|[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}\\.[0-9]{1,4}")) {
                    dependencyVersion.setText(oldValue);
                } else {
                    if (dependencyNameText.getText().length() > 0) {
                        addDependency.setDisable(false);
                    }
                }


            } else {

                addDependency.setDisable(true);
            }
        });

        dependencyNameText.textProperty().addListener((observable, oldValue, newValue) -> {
            String dependencyVersionText = dependencyNameText.getText();
            tips.getItems().clear();
            if (dependencyVersionText.length() > 0) {
                if (dependencyVersionText.length() > 255) {
                    dependencyNameText.setText(dependencyNameText.getText(0, 255));
                    if (dependencyVersion.getText().length() > 0) {
                        addDependency.setDisable(false);
                    }
                }
                dependencyNameText.setText(dependencyNameText.getText().replaceAll("[^A-Za-zА0-9_.]", ""));
                depend.stream().sorted().collect(Collectors.toList()).forEach(s -> {
                    if (s.toLowerCase().contains(dependencyNameText.getText().toLowerCase())) {
                        tips.getItems().add(s);
                    }
                });

            } else {
                tips.getItems().addAll(depend.stream().sorted().collect(Collectors.toList()));
                addDependency.setDisable(true);
            }
        });


        addDependency.textProperty().addListener((observable, oldValue, newValue) -> {

        });


    }


    /**
     * checkbox for loading previous manifest settings
     *
     * @param actionEvent
     */
    public void activateRestore(ActionEvent actionEvent) {
        if (restorePackage.isSelected()) {
            buttonOpenManifest.setDisable(false);
        } else {
            buttonOpenManifest.setDisable(true);
        }

    }

    public void openManifest(ActionEvent actionEvent) {
        presenter.openManifest();

    }


    /**
     * Open manifest file for restore
     * @param workDirectory
     */
    @Override
    public void showManifestFile(File workDirectory) {
        FileChooser directoryChooser = new FileChooser();
        directoryChooser.setInitialDirectory(workDirectory);
        directoryChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Manifest", "*.json"));
        File file = directoryChooser.showOpenDialog(rootView.getScene().getWindow());

        if (file != null) {
            restorePath.setText(file.getPath());
            Gson gson = new Gson();
            String str = "";
            try {
                str = FileUtils.readFileToString(file);
            } catch (IOException e) {
                System.err.println("bad json format");
            }
            ManifestModel model = gson.fromJson(str, ManifestModel.class);
            fillPackageSelection(model);
            selectCriticalFiles();
            restoreDependencies(model);
        }

    }

    /**
     * fill previous parameters on the package selection page
     *
     * @param model
     */
    private void fillPackageSelection(ManifestModel model) {
        String parameter = model.getPackageName();
        if (parameter != null && parameter.length() > 0) {
            packageNameText.setText(parameter);
        }

        parameter = model.getVersion();
        if (parameter != null && parameter.length() > 0) {
            versionText.setText(parameter);
        }

        parameter = model.getDescription();
        if (parameter != null && parameter.length() > 0) {
            descriptionText.setText(parameter);
        }

        parameter = model.getType();
        if (parameter != null && parameter.length() > 0) {
            switch (parameter) {
                case "Content":
                    checkContent.setSelected(true);
                    break;
                case "Os":
                    checkOs.setSelected(true);
                    break;
                case "EGM":
                    checkEGM.setSelected(true);
                    break;
            }
        }

        parameter = model.getPath();
        if (parameter != null && parameter.length() > 0) {
            pathText.setText(parameter);
        }

        parameter = model.getManufacturer();
        if (parameter != null && parameter.length() > 0) {
            manufacturerText.setText(parameter);
        }

    }


    private List<String> restoredCriticalList = new ArrayList<>();
    private List<String> restoredConfigList = new ArrayList<>();

    private void selectCriticalFiles() {
        restoredCriticalList.clear();
        restoredConfigList.clear();

        String pathDelimiter;
        if (OS.contains("win")) {
            pathDelimiter = "\\";
        } else {
            pathDelimiter = "/";
        }

        try (Stream<String> stream = Files.lines(Paths.get(rootDirectory.getPath() + pathDelimiter + "critical.list"))) {
            stream.forEach(s -> {
                restoredCriticalList.add(s);
            });
        } catch (IOException e) {
            System.out.println("critical list was not found");
        }

        try (Stream<String> stream = Files.lines(Paths.get(rootDirectory.getPath() + pathDelimiter + "config.list"))) {
            stream.forEach(s -> {
                restoredConfigList.add(s);
            });
        } catch (IOException e) {
            System.out.println("config list was not found");
        }

        criticFilter.setText("");
        filterTree("", FileType.CRITICAL);
        restoredCriticalList.forEach(s -> {
            restoreTreeChecked(rootCriticalFiles, s);
        });

        configFilter.setText("");
        filterTree("", FileType.CONFIG);
        restoredConfigList.forEach(s -> {
            restoreTreeChecked(rootConfigFiles, s);
        });

        System.out.println();

    }


    private void restoreTreeChecked(CheckBoxTreeItem checkBoxTreeItem, String path) {
        checkBoxTreeItem.getChildren().forEach(o -> {

            if (((CheckBoxTreeItem) o).getValue() instanceof CriticalFile) {
                CriticalFile criticalFile = (CriticalFile) ((CheckBoxTreeItem) o).getValue();
                if (criticalFile.getPath().equals(path) || criticalFile.getPath().replaceAll("\\\\","/").equals(path) ) {
                    ((CheckBoxTreeItem) o).setSelected(true);
                }
            } else {
                restoreTreeChecked((CheckBoxTreeItem) o, path);
            }

        });
    }

    private void restoreDependencies(ManifestModel model){
        dependencyListView.getItems().removeAll(dependencyListView.getItems());
        dependencyNameText.clear();
        dependencyVersion.clear();
        dependencyListView.getItems().addAll(model.getDependencies());
    }

}
