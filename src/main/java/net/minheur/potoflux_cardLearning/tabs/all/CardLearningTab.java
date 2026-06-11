package net.minheur.potoflux_cardLearning.tabs.all;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import net.minheur.potoflux.PotoFlux;
import net.minheur.potoflux.logger.PtfLogger;
import net.minheur.potoflux.ui.UiUtils;
import net.minheur.potoflux_cardLearning.utility.Card;
import net.minheur.potoflux_cardLearning.utility.CardJsonManager;
import net.minheur.potoflux_cardLearning.utility.CardList;
import net.minheur.potoflux.screen.tabs.BaseTab;
import net.minheur.potoflux.translations.Translations;
import net.minheur.potoflux_cardLearning.CardLearningMod;
import net.minheur.potoflux_cardLearning.utility.CardLogCategories;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static net.minheur.potoflux.Functions.removeProhibitedChar;

public class CardLearningTab extends BaseTab<BorderPane> {
    public static final Path cardsDir = Paths.get(CardLearningMod.getModDir().toString(), "cards");

    private TabPane subTabs;

    // main
    private CardList currentList;
    private int index;
    private Label cardLabel;
    private Button backButton;
    private Button flipButton;
    private Button nextButton;

    // load
    private CardList loadedCards;
    private ScrollPane loadedCardsDisplay;
    private Button validateLoadButton;

    // all vars shared between methods
    private final ListView<GridPane> listPanel = new ListView<>();
    private final ComboBox<String> exportComboBox = new ComboBox<>();
    private final ComboBox<String> mainComboBox = new ComboBox<>();
    private final JComboBox<String> modifyComboBox = new JComboBox<>();
    private final List<JComboBox<String>> allComboBox = new ArrayList<>();

    @Override
    protected void instantiate() {
        PANEL = new BorderPane();

        subTabs = new TabPane();
        PANEL.setCenter(subTabs);
    }

    @Override
    protected void setPanel() {

        allComboBox.add(exportComboBox);
        allComboBox.add(mainComboBox);
        allComboBox.add(modifyComboBox);

        // exportComboBox.setName("Export box"); // todo
        // mainComboBox.setName("Main box"); // todo
        modifyComboBox.setName("Modify box"); // todo

        // TODO: mk selection models

        checkAndCreateDir();

        // create sub-tabs
        subTabs.getTabs().add(new Tab(Translations.get("common:main"), createMainPanel()));
        subTabs.getTabs().add(new Tab(Translations.get("common:list"), createListPanel()));
        subTabs.getTabs().add(new Tab(Translations.get("common:load"), createLoadPanel()));
        subTabs.getTabs().add(new Tab(Translations.get("common:create"), createCreatePanel()));
        subTabs.getTabs().add(new Tab(Translations.get("common:export"), createExportPanel()));

    }

    private BorderPane createMainPanel() {
        BorderPane pane = new BorderPane();

        // === TOP - list selection ===
        HBox topPanel = new HBox();
        Label listLabel = new Label(Translations.get("card_learning:tabs.card.list.column"));
        Button startButton = new Button(Translations.get("common:start"));

        refreshComboBox();

        topPanel.getChildren().addAll(
                listLabel,
                mainComboBox,
                startButton
        );

        pane.setTop(topPanel);

        // === CENTER - play ===
        cardLabel = new Label("");
        cardLabel.setFont(new Font("Segoe UI", 24));
        pane.setCenter(cardLabel);

        // bottom - buttons
        HBox bottomPanel = new HBox();
        backButton = new Button(Translations.get("common:back"));
        flipButton = new Button(Translations.get("common:flip"));
        nextButton = new Button(Translations.get("common:next"));
        backButton.setDisable(true);
        flipButton.setDisable(true);
        nextButton.setDisable(true);
        bottomPanel.getChildren().addAll(
                backButton, flipButton, nextButton
        );
        pane.setBottom(bottomPanel);

        // quiz data
        currentList = new CardList();
        index = 0;

        startButton.setOnAction(e -> startQuiz());
        flipButton.setOnAction(e -> flipCard());
        nextButton.setOnAction(e -> nextCard());
        backButton.setOnAction(e -> previousCard());

        return pane;
    }

    private void previousCard() {
        if (currentList == null || currentList.cards.isEmpty()) {
            PtfLogger.error("Empty list, but activated 'back' button !", CardLogCategories.CARDS, "main");
            return;
        }

        // get size
        int size = currentList.cards.size();

        // list size check
        if (size == 1) {
            PtfLogger.warning("List is 1 long but 'back' button enabled !", CardLogCategories.CARDS, "main");
            backButton.setDisable(true);
            return;
        }

        // check for first card
        if (index <= 0) {
            backButton.setDisable(true);
            nextButton.setDisable(false);
            PtfLogger.warning("'back' button on, but first card is live !", CardLogCategories.CARDS, "main");
            return;
        }

        // decrease index : now preparing next cycle
        index--;

        // force next button on & update text
        nextButton.setDisable(false);
        cardLabel.setText(currentList.cards.get(index).main);

        // disable itself if first card is live & enable next button
        if (index == 0) backButton.setDisable(true);
    }
    private void nextCard() {
        if (currentList == null || currentList.cards.isEmpty()) {
            PtfLogger.error("Empty list, but activated 'next' button !", CardLogCategories.CARDS, "main");
            return;
        }

        // get size
        int size = currentList.cards.size();

        // list size check
        if (size == 1) {
            PtfLogger.warning("List is 1 long but 'back' button enabled !", CardLogCategories.CARDS, "main");
            backButton.setDisable(true);
            return;
        }

        // check if last card is live: button wrongly on
        if (index == size - 1) {
            nextButton.setDisable(true);
            backButton.setDisable(false);
            PtfLogger.warning("'next' button on, but last card is live !", CardLogCategories.CARDS, "main");
            return;
        }

        // increase index by one : now prepare for next cycle
        index++;

        // force back button on & update text
        backButton.setDisable(false);
        cardLabel.setText(currentList.cards.get(index).main);

        // disable itself if last card
        if (index == size - 1) nextButton.setDisable(true);
    }
    private void flipCard() {
        if (currentList == null || currentList.cards.isEmpty()) {
            PtfLogger.warning("Empty list, but activated 'flip' button !", CardLogCategories.CARDS, "main");
            return;
        }

        // get current
        Card card = currentList.cards.get(index);

        // flip
        if (cardLabel.getText().equals(card.main)) {
            cardLabel.setText(card.secondary);
        } else {
            cardLabel.setText(card.main);
        }
    }
    private void startQuiz() {
        String selected = mainComboBox.getSelectionModel().getSelectedItem();
        if (selected == null || selected.equals(Translations.get("common:select_list"))) {
            PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "main");
            return;
        }

        Path filePath = cardsDir.resolve(selected + ".json");
        if (!Files.exists(filePath)) {
            PtfLogger.error("File not found: " + selected, CardLogCategories.CARDS, "main");
            UiUtils.showErrorPane(Translations.get("file:error.not_found.linked") + selected);
            return;
        }

        try {
            String content = Files.readString(filePath);
            currentList = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), true);
            if (currentList == null || currentList.cards == null || currentList.cards.isEmpty()) {
                PtfLogger.error("List '" + selected + "' is invalid or empty !", CardLogCategories.CARDS, "main");
                UiUtils.showErrorPane(Translations.get("card_learning:tabs.card.list.invalid"));
                return;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            PtfLogger.error("Can't read file for list '" + selected + "' !", CardLogCategories.CARDS, "main");
            UiUtils.showErrorPane(Translations.get("common:read_error") + ex.getMessage());
            return;
        }

        // reset index
        index = 0;
        cardLabel.setText(currentList.cards.get(index).main);

        // default states for buttons
        backButton.setDisable(true);
        flipButton.setDisable(false);
        nextButton.setDisable(!(currentList.cards.size() > 1));
    }

    private BorderPane createListPanel() {
        BorderPane panel = new BorderPane();

        // title
        Label title = new Label(Translations.get("card_learning:tabs.card.available_lists"));
        title.setFont(new Font("Segeo UI", 16));
        panel.setTop(title);

        loadListPanel();

        // scrollable
        ScrollPane scrollPane = new ScrollPane(listPanel);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        panel.setCenter(scrollPane);
        return panel;
    }

    private void loadListPanel() {
        listPanel.getItems().clear();

        File[] jsonFiles = cardsDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            PtfLogger.info("No lists found !", CardLogCategories.CARDS, "list");

            GridPane fallbackGrid = new GridPane();
            fallbackGrid.add(new Label(Translations.get("card_learning:tabs.card.list.no_found")), 0, 0);
            listPanel.getItems().add(fallbackGrid);

        } else {
            for (File file : jsonFiles) try {
                // read content
                String content = Files.readString(file.toPath());
                CardList list = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

                if (list == null || list.cards == null) {
                    PtfLogger.error("There is an empty list in directory !", CardLogCategories.CARDS, "list");
                    continue;
                }

                // create line for list
                GridPane row = new GridPane();

                // left - text
                Label label = new Label(list.name + " (" + list.cards.size() + " " + Translations.get("common:cards") + ")");
                label.setFont(new Font("Segeo UI", 14));
                row.add(label, 0, 0);

                // right - buttons
                Button deleteButton = new Button(Translations.get("common:delete"));
                Button infoButton = new Button(Translations.get("common:info"));

                deleteButton.setOnAction(e -> deleteList(file, list));
                infoButton.setOnAction(e -> displayListDetails(list));

                row.add(deleteButton, 2, 0);
                row.add(infoButton, 1, 0);

                listPanel.getItems().add(row);
            } catch (Exception e) {
                e.printStackTrace();
                PtfLogger.error("Failed to read list file !", CardLogCategories.CARDS, "list");
            }
        }

        refreshComboBox();
    }

    private void displayListDetails(CardList list) {
        // window for display
        Dialog<Void> infoDialog = new Dialog<>();
        infoDialog.setTitle(Translations.get("card_learning:tabs.card.details") + list.name);

        infoDialog.getDialogPane().getButtonTypes().add(UiUtils.closeButton.get());

        // pane
        BorderPane dialogPane = new BorderPane();

        // title
        Label title = new Label(list.name + " (" + list.cards.size() + " " + Translations.get("common:cards") + ")");
        title.setFont(new Font("Segeo UI", 16));
        dialogPane.setTop(title);

        // card
        ScrollPane scrollPane = createCardPanelAsScroll(list);
        dialogPane.setCenter(scrollPane);

        infoDialog.getDialogPane().setContent(dialogPane);

        infoDialog.show();
    }
    private void deleteList(File filePlacement, CardList content) {
        boolean confirmed = UiUtils.showConfirmationDialog(
                new Label(
                        Translations.get("card_learning:tabs.card.list.delete.confirm.dialog") + content.name + " ?"
                ), Translations.get("card_learning:tabs.card.list.delete.confirm")
        );

        if (!confirmed) return;

        try {
            Files.deleteIfExists(filePlacement.toPath());

            PtfLogger.info("List " + content.name + " has successfully been deleted !", CardLogCategories.CARDS, "list");
            UiUtils.showMessagePane(Translations.get("card_learning:tabs.card.list.delete.done.start") + content.name + Translations.get("card_learning:tabs.card.list.delete.done.end"));

            loadListPanel();

        } catch (IOException ex) {
            ex.printStackTrace();
            PtfLogger.error("Failed to delete list " + content.name, CardLogCategories.CARDS, "list");

            UiUtils.showErrorPane(Translations.get("card_learning:tabs.card.delete_error") + ex.getMessage());
        }
    }

    private BorderPane createExportPanel() {
        BorderPane panel = new BorderPane();

        // UP - title + select list + button
        HBox topPanel = new HBox(5);

        // title
        Label title = new Label(Translations.get("card_learning:tabs.card.export"));
        title.setFont(new Font("Segoe UI", 16));

        refreshComboBox(); // adding .json files -- random, why here ?

        // export button
        Button exportButton = new Button(Translations.get("common:export"));
        exportButton.setDisable(true);

        exportButton.setOnAction(e -> exportList());

        topPanel.getChildren().addAll(
                title,
                exportComboBox,
                exportButton
        );

        // display cards
        ScrollPane cardScroll = new ScrollPane();
        cardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        cardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // behaviour
        exportComboBox.setOnAction(e -> reloadExportDisplayedList(cardScroll, exportButton));

        return panel;
    }

    private void reloadExportDisplayedList(ScrollPane cardScroll, Button exportButton) {
        String selected = exportComboBox.getSelectionModel().getSelectedItem();

        // null check
        if (selected == null || selected.equals(Translations.get("common:select_list"))) {
            cardScroll.setContent(null);
            exportButton.setDisable(true);
            PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "export");
            return;
        }

        // existing file check
        Path filePath = cardsDir.resolve(selected + ".json");
        if (!Files.exists(filePath)) {
            cardScroll.setContent(new Label(Translations.get("file:error.not_found")));
            exportButton.setDisable(true);
            PtfLogger.error("File not found: " + selected, CardLogCategories.CARDS, "export");
            return;
        }

        try {
            String content = Files.readString(filePath);
            CardList list = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

            // null check
            if (list == null || list.cards == null) {
                cardScroll.setContent(new Label(Translations.get("potoflux:tabs.card.error.loading_list")));
                exportButton.setDisable(true);
                PtfLogger.error("List can't be null: " + selected, CardLogCategories.CARDS, "export");
                return;
            }

            ListView<GridPane> items = createCardPanel(list);
            cardScroll.setContent(items);
            exportButton.setDisable(false); // export button is now available
        } catch (Exception ex) {
            ex.printStackTrace();
            PtfLogger.error("Error while exporting: " + selected, CardLogCategories.CARDS, "export");
            cardScroll.setContent(new Label(Translations.get("card_learning:tabs.card.error.reading_file")));
            exportButton.setDisable(true);
        }
    }
    private void exportList() {
        String selected = exportComboBox.getSelectionModel().getSelectedItem();

        if (selected == null || selected.equals(Translations.get("common:select_list"))) {
            PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "export");
            return;
        }
        PtfLogger.info("User wants to export: " + selected, CardLogCategories.CARDS, "export");

        Path sourcePath = cardsDir.resolve(selected + ".json");
        if (!Files.exists(sourcePath)) {
            PtfLogger.error("Can't find file '" + selected + "' !", CardLogCategories.CARDS, "export");
            UiUtils.showErrorPane(Translations.get("file:error.not_found.linked") + sourcePath);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle(Translations.get("card_learning:tabs.card.export") + selected);
        chooser.setTitle(selected + ".json");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "JSON files (*.json)",
                        "*.json"
                )
        );

        File destinationFile = chooser.showSaveDialog(PotoFlux.app.getStage());

        if (destinationFile == null) {
            PtfLogger.info("User canceled export: " + selected, CardLogCategories.CARDS, "export");
            return;
        }

        if (!destinationFile.getName().toLowerCase().endsWith(".json")) {
            destinationFile = new File(destinationFile.getAbsolutePath() + ".json"); // force file to be json
            PtfLogger.warning("User wanted to export as other than JSON ! Forced...", CardLogCategories.CARDS, "export");
        }

        // check if existing
        if (destinationFile.exists()) {
            boolean overwrite = UiUtils.showConfirmationDialog(
                    new Label(Translations.get("card_learning:tabs.card.replace.content")),
                    Translations.get("card_learning:tabs.card.replace.name")
            );
            if (!overwrite) {
                PtfLogger.info("User canceled export: " + selected + " because it would override", CardLogCategories.CARDS, "export");
                return;
            }
        }

        try {
            Files.copy(sourcePath, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            PtfLogger.info("List '"  + selected + "' exported successfully in: " + destinationFile, CardLogCategories.CARDS, "export");
            UiUtils.showMessagePane(Translations.get("card_learning:tabs.card.export.done") + "\n" + destinationFile.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
            PtfLogger.error("Failed to copy list '" + selected + "' in: " + destinationFile, CardLogCategories.CARDS, "export");
            UiUtils.showErrorPane(Translations.get("card_learning.tabs.card.export.error") + ex.getMessage());
        }
    }

    private void refreshComboBox() {
        for (JComboBox<String> c : allComboBox) refreshComboBox(c);
        PtfLogger.info("Refreshed all combo boxes !", CardLogCategories.CARDS);
    }

    private void refreshComboBox(JComboBox<String> box) {
        box.removeAllItems();
        box.addItem(Translations.get("common:select_list"));
        File[] jsonFiles = cardsDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        // null check
        if (jsonFiles != null) {
            // add
            for (File file : jsonFiles) {
                String name = file.getName().replace(".json", "");
                box.addItem(name);
                PtfLogger.info("Added " + name + " to " + box.getName(), CardLogCategories.CARDS, "reloadBox");
            }
        } else PtfLogger.error("Can't get the list of lists !", CardLogCategories.CARDS, "reloadBox");
    }

    private BorderPane createLoadPanel() {
        loadedCardsDisplay = new ScrollPane();
        loadedCardsDisplay.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        loadedCardsDisplay.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        BorderPane panel = new BorderPane();
        panel.setCenter(loadedCardsDisplay);

        HBox buttons = new HBox();
        Button loadButton = new Button(Translations.get("file:json.import"));
        validateLoadButton = new Button(Translations.get("common:validate"));
        validateLoadButton.setDisable(true);

        buttons.getChildren().addAll(
                loadButton, validateLoadButton
        );

        loadButton.setOnAction(e -> loadList());
        validateLoadButton.setOnAction(e -> validateLoad());

        panel.setTop(buttons);
        return panel;
    }

    private void validateLoad() {
        if (loadedCards == null) {
            PtfLogger.error("No loaded list, but enabled 'Validate' button !", CardLogCategories.CARDS, "load");
            UiUtils.showErrorPane(Translations.get("card_learning:tabs.card.empty_list_valid"));
            validateLoadButton.setDisable(true);
            removeLoadedCards();
        }

        String fileName = loadedCards.name.replace(" ", "_");
        Path outputFile = cardsDir.resolve(fileName + ".json");

        // cancel if already existing
        if (Files.exists(outputFile)) {
            PtfLogger.error("There is already a list named '" + loadedCards.name + "' in directory !", CardLogCategories.CARDS, "load");
            UiUtils.showErrorPane(Translations.get("file:error.exist.desc") + "\n" + Translations.get("common:add_cancel"));
            loadedCards = null;
            validateLoadButton.setDisable(true);
            removeLoadedCards();
            return;
        }

        try {
            Gson gson = new Gson();
            Files.writeString(outputFile, gson.toJson(loadedCards));

            PtfLogger.info("File " + loadedCards.name + " has been saved !", CardLogCategories.CARDS, "load");
            UiUtils.showMessagePane(Translations.get("file:saved"));

            validateLoadButton.setDisable(true);
            loadedCards = null;
            removeLoadedCards();

            loadListPanel(); // reload
        } catch (IOException ex) {
            ex.printStackTrace();
            PtfLogger.error("Failed to write into file: " + loadedCards.name, CardLogCategories.CARDS, "load");
            UiUtils.showErrorPane(Translations.get("file:error.saving") + ex.getMessage());
        }
    }
    private void loadList() {
        PtfLogger.info("User wants to import a list !", CardLogCategories.CARDS, "load");

        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        Translations.get("file:json"),
                        "*.json"
                )
        );

        File selectedFile = chooser.showOpenDialog(PotoFlux.app.getStage());
        if (selectedFile == null) {
            PtfLogger.info("User canceled import.", CardLogCategories.CARDS, "load");
            return;
        }

        // turn to path
        Path selectedPath = selectedFile.toPath();

        // show check : file loaded
        PtfLogger.info("User chose file " + selectedFile.getName(), CardLogCategories.CARDS, "load");
        UiUtils.showMessagePane(
                Translations.get("card_learning:tabs.card.file_loaded") + selectedFile.getName() + "\n" + Translations.get("common:path") + selectedPath
        );

        try {
            // read content
            String content = Files.readString(selectedPath);

            // parse to JSON object
            loadedCards = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

            // check is everything right
            if (loadedCards == null || loadedCards.cards == null || getCheckedListName(loadedCards.name) == null) {
                showCardError();
                return;
            } else {
                for (Card card : loadedCards.cards) if (card.main == null || card.secondary == null) {
                    showCardError();
                    return;
                }
                loadedCards.name = removeProhibitedChar(loadedCards.name);
                PtfLogger.info("Successfully loaded list: " + loadedCards.name, CardLogCategories.CARDS, "load");
                UiUtils.showMessagePane(
                        Translations.get("card_learning:tabs.card.list.loaded") + loadedCards.name + "\n" + Translations.get("card_learning:tabs.card.card_number") + loadedCards.cards.size()
                );
                validateLoadButton.setDisable(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            PtfLogger.error("Error while reading file " + selectedPath, CardLogCategories.CARDS, "load");
            UiUtils.showErrorPane(Translations.get("file:error.json.loading"));
            return;
        }

        loadedCardsDisplay.setContent(createCardPanel(loadedCards));
    }

    private void removeLoadedCards() {
        if (loadedCardsDisplay != null)
            loadedCardsDisplay.setContent(null);
        else PtfLogger.warning("Can't clear a null panel !", CardLogCategories.CARDS, "load");
    }

    private ListView<GridPane> createCardPanel(CardList list) {
        ListView<GridPane> allCards = new ListView<>();
        Region spacer = new Region();
        spacer.setMinHeight(10);
        spacer.setPrefHeight(10);

        for (Card card : list.cards) {
            GridPane grid = new GridPane();

            Label left = new Label(card.main);
            Label right = new Label(card.secondary);

            left.setFont(new Font("Segoe UI", 14));
            right.setFont(new Font("Segoe UI", 14));

            grid.add(left, 0, 0);
            grid.add(spacer, 1, 0);
            grid.add(right, 2, 0);

            allCards.getItems().add(grid);
        }

        return allCards;
    }

    private ScrollPane createCardPanelAsScroll(CardList list) {
        Node p = createCardPanel(list);

        ScrollPane scrollPane = new ScrollPane(p);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        return scrollPane;
    }

    private String getCheckedListName(String s) {
        if (s == null) return null;
        if (s.isEmpty() || s.trim().isEmpty()) return null;

        String newString = removeProhibitedChar(s);

        if (newString.isEmpty() || newString.trim().isEmpty()) return null;
        return newString;
    }

    private void showCardError() {
        PtfLogger.error("Invalid JSON file !", CardLogCategories.CARDS, "load");
        UiUtils.showErrorPane(Translations.get("file:json.error.invalid"));
    }

    private void checkAndCreateDir() {
        try {
            Files.createDirectories(cardsDir);
            PtfLogger.info("Successfully created cardsDir", CardLogCategories.CARDS);
        } catch (IOException ignored) {
            PtfLogger.warning("Failed to create cardsDir...", CardLogCategories.CARDS);
        }
    }

    private JPanel createCreatePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // temp data
        final List<Card> tempCards = new ArrayList<>();
        final JTextField nameField = new JTextField(20);

        // up - name + button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton addCardButton = new JButton(Translations.get("card_learning:tabs.card.add_card"));
        JButton saveButton = new JButton(Translations.get("common:validate"));
        JButton cancelButton = new JButton(Translations.get("common:cancel"));
        JButton modifyButton = new JButton(Translations.get("common:modify"));
        saveButton.setEnabled(false);

        topPanel.add(new JLabel(Translations.get("card_learning:tabs.card.list.name")));
        topPanel.add(nameField);
        topPanel.add(addCardButton);
        topPanel.add(saveButton);
        topPanel.add(cancelButton);
        topPanel.add(modifyButton);

        // center - added cards
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // display features
        Runnable[] refreshCards = new Runnable[1];

        refreshCards[0] = () -> {
            PtfLogger.info("Refreshing cards...", CardLogCategories.CARDS, "create");
            cardsPanel.removeAll();

            if (tempCards.isEmpty()) cardsPanel.add(new JLabel(Translations.get("card_learning:tabs.card.no_card"), SwingConstants.CENTER));
            else for (Card card : tempCards) {
                JPanel row = new JPanel(new GridLayout(1, 2, 5, 5));
                row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));

                JPanel content = new JPanel(new GridLayout(1, 2, 5, 5));
                JLabel left = new JLabel(card.main, SwingConstants.CENTER);
                JLabel right = new JLabel(card.secondary, SwingConstants.CENTER);

                left.setFont(new Font("Segoe UI", Font.BOLD, 14));
                right.setFont(new Font("Segoe UI", Font.BOLD, 14));

                content.add(left);
                content.add(right);

                // delete button
                JButton deleteButton = new JButton("✕");
                deleteButton.addActionListener(e -> {
                    tempCards.remove(card);
                    refreshCards[0].run();
                });

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                buttonPanel.add(deleteButton);

                row.add(content, BorderLayout.CENTER);
                row.add(buttonPanel, BorderLayout.EAST);

                cardsPanel.add(row);
            }

            cardsPanel.revalidate();
            cardsPanel.repaint();
            saveButton.setEnabled(!tempCards.isEmpty() && getCheckedListName(nameField.getText()) != null);
        };

        modifyButton.addActionListener(e -> {
            if (!tempCards.isEmpty()) {
                int reset = JOptionPane.showConfirmDialog(
                        panel, Translations.get("card_learning:tabs.card.override"),
                        Translations.get("common:override_check"),
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                );
                if (reset != JOptionPane.YES_OPTION) {
                    PtfLogger.info("Reset all cards", CardLogCategories.CARDS, "create");
                    return;
                }
            }

            refreshComboBox();
            tempCards.clear();

            // def
            JPanel comboPanel = new JPanel(new BorderLayout(50, 400));
            JLabel label = new JLabel(Translations.get("card_learning:tabs.card.choose_list"));
            comboPanel.add(label, BorderLayout.WEST);
            comboPanel.add(modifyComboBox, BorderLayout.EAST);

            JOptionPane.showMessageDialog(
                    panel, comboPanel,
                    Translations.get("card_learning:tabs.card.choose_list"),
                    JOptionPane.QUESTION_MESSAGE
            );

            String selected = (String) modifyComboBox.getSelectedItem();
            if (selected == null || selected.equals(Translations.get("common:select_list"))) {
                PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "create");
                JOptionPane.showMessageDialog(
                        panel, Translations.get("card_learning:tabs.card.no_selected"),
                        Translations.get("common:error"),
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Path filePath = cardsDir.resolve(selected + ".json");
            if (!Files.exists(filePath)) {
                PtfLogger.error("Can't modify unexisting list !", CardLogCategories.CARDS, "create");
                JOptionPane.showMessageDialog(panel, Translations.get("file:error.not_found.linked") + selected, Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String content = Files.readString(filePath);
                CardList cardList = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

                for (Card c : cardList.cards) {
                    if (c == null || c.main == null || c.secondary == null) {
                        PtfLogger.error("Trying to modify an invalid list !", CardLogCategories.CARDS, "create");
                        JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.list.invalid"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    tempCards.add(c);
                }

                if (tempCards.isEmpty() || cardList.name == null) {
                    PtfLogger.error("Trying to modify an invalid list !", CardLogCategories.CARDS, "create");
                    JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.list.invalid"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                nameField.setText(cardList.name.replaceAll("\"", ""));
            } catch (IOException ex) {
                ex.printStackTrace();
                PtfLogger.error("failed to read file", CardLogCategories.CARDS, "create");
                JOptionPane.showMessageDialog(panel, Translations.get("common:read_error") + ex.getMessage(), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            refreshCards[0].run();

        });

        // button "add card"
        addCardButton.addActionListener(e -> {
            JTextField mainField = new JTextField();
            JTextField secondaryField = new JTextField();

            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            inputPanel.add(new JLabel(Translations.get("card_learning:tabs.card.face.front")));
            inputPanel.add(mainField);
            inputPanel.add(new JLabel(Translations.get("card_learning:tabs.card.face.back")));
            inputPanel.add(secondaryField);

            int result = JOptionPane.showConfirmDialog(
                    panel, inputPanel, Translations.get("card_learning:tabs.card.new"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String main = removeProhibitedChar(mainField.getText());
                String secondary = removeProhibitedChar(secondaryField.getText());

                if (main.isEmpty() || secondary.isEmpty()) {
                    PtfLogger.warning("User wanted to add empty card !", CardLogCategories.CARDS, "create");
                    JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.new.empty"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Card c = new Card();
                c.main = main;
                c.secondary = secondary;

                tempCards.add(c);
                refreshCards[0].run();
            }
        });

        // button validate (save)
        saveButton.addActionListener(e -> {
            String listName = getCheckedListName(nameField.getText());
            if (listName == null) {
                PtfLogger.warning("User wants to export a list, but has no name !", CardLogCategories.CARDS, "create");
                JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.list.invalid.name"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tempCards.isEmpty()) {
                PtfLogger.warning("User wants to expoty a list, but has no cards !", "create");
                JOptionPane.showMessageDialog(panel, Translations.get("common:nothing_save"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fileName = listName.replaceAll(" ", "_") + ".json";
            Path outputFile = cardsDir.resolve(fileName);
            if (Files.exists(outputFile)) {
                PtfLogger.warning("A file named " + fileName + " already exists ! Asking override...", CardLogCategories.CARDS, "create");
                int overwrite = JOptionPane.showConfirmDialog(panel,
                        Translations.get("card_learning:tabs.card.replace.content"),
                        Translations.get("file:error.exist"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite == JOptionPane.YES_OPTION) PtfLogger.info("User accepted override for " + fileName, CardLogCategories.CARDS, "create");
                else {
                    PtfLogger.info("User refused override for " + fileName, CardLogCategories.CARDS, "create");
                    return;
                }
            }

            CardList list = new CardList();
            list.name = listName;
            list.cards = new ArrayList<>(tempCards);

            try {
                Gson gson = new Gson();
                Files.writeString(outputFile, gson.toJson(list));
                PtfLogger.info("List " + listName + " has been saved !", CardLogCategories.CARDS, "create");
                JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.list.saved"), Translations.get("common:saveSuccess"), JOptionPane.INFORMATION_MESSAGE);

                tempCards.clear();
                nameField.setText("");
                refreshCards[0].run();
                loadListPanel(); // refresh global list
            } catch (IOException ex) {
                ex.printStackTrace();
                PtfLogger.error("Error while saving list !", CardLogCategories.CARDS, "create");
                JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.error.saving") + ex.getMessage(), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        // button cancel
        cancelButton.addActionListener(e -> {
            if (tempCards.isEmpty() && nameField.getText().isEmpty()) {
                PtfLogger.warning("Can't cancel empty list !", CardLogCategories.CARDS, "create");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                    Translations.get("card_learning:tabs.card.cancel_all"),
                    Translations.get("common:confirm"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                PtfLogger.info("Canceled list.", CardLogCategories.CARDS, "create");
                tempCards.clear();
                nameField.setText("");
                refreshCards[0].run();
            }
        });

        // auto run validate button check
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshCards[0].run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshCards[0].run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshCards[0].run(); }
        });

        refreshCards[0].run();
        return panel;
    }

    @Override
    protected boolean doPreset() {
        return false;
    }

    @Override
    protected String getTitle() {
        return Translations.get("card_learning:tabs.card.name");
    }
    @Override
    public String getName() {
        return getTitle();
    }
}
