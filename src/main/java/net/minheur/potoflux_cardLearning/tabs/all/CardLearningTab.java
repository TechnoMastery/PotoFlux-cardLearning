package net.minheur.potoflux_cardLearning.tabs.all;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.minheur.potoflux.PotoFlux;
import net.minheur.potoflux.utils.logger.LogCategories;
import net.minheur.potoflux_cardLearning.utility.Card;
import net.minheur.potoflux_cardLearning.utility.CardJsonManager;
import net.minheur.potoflux_cardLearning.utility.CardList;
import net.minheur.potoflux.screen.tabs.BaseTab;
import net.minheur.potoflux.translations.Translations;
import net.minheur.potoflux.utils.logger.PtfLogger;
import net.minheur.potoflux_cardLearning.CardLearningMod;
import net.minheur.potoflux_cardLearning.utility.CardLogCategories;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static net.minheur.potoflux.Functions.removeProhibitedChar;

public class CardLearningTab extends BaseTab {
    public static final Path cardsDir = Paths.get(CardLearningMod.getModDir().toString(), "cards");

    // all vars shared between methods
    private final JPanel listPanel = new JPanel();
    private final JComboBox<String> exportComboBox = new JComboBox<>();
    private final JComboBox<String> mainComboBox = new JComboBox<>();
    private final JComboBox<String> modifyComboBox = new JComboBox<>();
    private final List<JComboBox<String>> allComboBox = new ArrayList<>();

    @Override
    protected void setPanel() {
        PANEL.setLayout(new BorderLayout());

        allComboBox.add(exportComboBox);
        allComboBox.add(mainComboBox);
        allComboBox.add(modifyComboBox);

        checkAndCreateDir();

        // create sub-tabs
        JTabbedPane subTabs = new JTabbedPane();
        subTabs.addTab(Translations.get("common:main"), createMainPanel());
        subTabs.addTab(Translations.get("common:list"), createListPanel());
        subTabs.addTab(Translations.get("common:load"), createLoadPanel());
        subTabs.addTab(Translations.get("common:create"), createCreatePanel());
        subTabs.addTab(Translations.get("common:export"), createExportPanel());

        // add all to the main one
        PANEL.add(subTabs, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top - list selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel listLabel = new JLabel(Translations.get("card_learning:tabs.card.list.column"));
        JButton startButton = new JButton(Translations.get("common:start"));

        refreshComboBox();

        topPanel.add(listLabel);
        topPanel.add(mainComboBox);
        topPanel.add(startButton);

        panel.add(topPanel, BorderLayout.NORTH);

        // center - cards
        JPanel cardPanel = new JPanel(new BorderLayout());
        JLabel cardLabel = new JLabel("", SwingConstants.CENTER);
        cardLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        cardPanel.add(cardLabel, BorderLayout.CENTER);
        panel.add(cardPanel, BorderLayout.CENTER);

        // bottom - buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton backButton = new JButton(Translations.get("common:back"));
        JButton flipButton = new JButton(Translations.get("common:flip"));
        JButton nextButton = new JButton(Translations.get("common:next"));
        backButton.setEnabled(false);
        flipButton.setEnabled(false);
        nextButton.setEnabled(false);
        bottomPanel.add(backButton);
        bottomPanel.add(flipButton);
        bottomPanel.add(nextButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // quiz data
        final CardList[] currentList = new CardList[1];
        final int[] index = {0};

        startButton.addActionListener(e -> {
            String selected = (String) mainComboBox.getSelectedItem();
            if (selected == null || selected.equals(Translations.get("common:select_list"))) {
                PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "main");
                return;
            }

            Path filePath = cardsDir.resolve(selected + ".json");
            if (!Files.exists(filePath)) {
                PtfLogger.error("File not found: " + selected, CardLogCategories.CARDS, "main");
                JOptionPane.showMessageDialog(panel, Translations.get("file:error.not_found.linked") + selected, Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String content = Files.readString(filePath);
                currentList[0] = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), true);
                if (currentList[0] == null || currentList[0].cards == null || currentList[0].cards.isEmpty()) {
                    PtfLogger.error("List '" + selected + "' is invalid or empty !", CardLogCategories.CARDS, "main");
                    JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.list.invalid"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                PtfLogger.error("Can't read file for list '" + selected + "' !", CardLogCategories.CARDS, "main");
                JOptionPane.showMessageDialog(panel, Translations.get("common:read_error") + ex.getMessage(), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            // reset index
            index[0] = 0;
            cardLabel.setText(currentList[0].cards.get(index[0]).main);

            // default states for buttons
            backButton.setEnabled(false);
            flipButton.setEnabled(true);
            nextButton.setEnabled(currentList[0].cards.size() > 1);
        });

        flipButton.addActionListener(e -> {
            if (currentList[0] == null || currentList[0].cards.isEmpty()) {
                PtfLogger.warning("Empty list, but activated 'flip' button !", CardLogCategories.CARDS, "main");
                return;
            }

            // get current
            Card card = currentList[0].cards.get(index[0]);

            // flip
            if (cardLabel.getText().equals(card.main)) {
                cardLabel.setText(card.secondary);
            } else {
                cardLabel.setText(card.main);
            }
        });

        nextButton.addActionListener(e -> {
            if (currentList[0] == null || currentList[0].cards.isEmpty()) {
                PtfLogger.error("Empty list, but activated 'next' button !", CardLogCategories.CARDS, "main");
                return;
            }

            // get size
            int size = currentList[0].cards.size();

            // list size check
            if (size == 1) {
                PtfLogger.warning("List is 1 long but 'back' button enabled !", CardLogCategories.CARDS, "main");
                backButton.setEnabled(false);
                return;
            }

            // check if last card is live: button wrongly on
            if (index[0] == size - 1) {
                nextButton.setEnabled(false);
                backButton.setEnabled(true);
                PtfLogger.warning("'next' button on, but last card is live !", CardLogCategories.CARDS, "main");
                return;
            }

            // increase index by one : now prepare for next cycle
            index[0]++;

            // force back button on & update text
            backButton.setEnabled(true);
            cardLabel.setText(currentList[0].cards.get(index[0]).main);

            // disable itself if last card
            if (index[0] == size - 1) nextButton.setEnabled(false);
        });

        backButton.addActionListener(e -> {
            if (currentList[0] == null || currentList[0].cards.isEmpty()) {
                PtfLogger.error("Empty list, but activated 'back' button !", CardLogCategories.CARDS, "main");
                return;
            }

            // get size
            int size = currentList[0].cards.size();

            // list size check
            if (size == 1) {
                PtfLogger.warning("List is 1 long but 'back' button enabled !", CardLogCategories.CARDS, "main");
                backButton.setEnabled(false);
                return;
            }

            // check for first card
            if (index[0] <= 0) {
                backButton.setEnabled(false);
                nextButton.setEnabled(true);
                PtfLogger.warning("'back' button on, but first card is live !", CardLogCategories.CARDS, "main");
                return;
            }

            // decrease index : now preparing next cycle
            index[0]--;

            // force next button on & update text
            nextButton.setEnabled(true);
            cardLabel.setText(currentList[0].cards.get(index[0]).main);

            // disable itself if first card is live & enable next button
            if (index[0] == 0) backButton.setEnabled(false);
        });

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // title
        JLabel title = new JLabel(Translations.get("card_learning:tabs.card.available_lists"), SwingConstants.CENTER);
        title.setFont(new Font("Segeo UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        // content
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        loadListPanel();

        // scrollable
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void loadListPanel() {
        listPanel.removeAll();
        File[] jsonFiles = cardsDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));

        if (jsonFiles == null || jsonFiles.length == 0) {
            listPanel.add(new JLabel(Translations.get("card_learning:tabs.card.list.no_found"), SwingConstants.CENTER));
            PtfLogger.info("No lists found !", CardLogCategories.CARDS, "list");
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
                JPanel row = new JPanel(new BorderLayout());
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));

                // left - text
                JLabel label = new JLabel(list.name + " (" + list.cards.size() + " " + Translations.get("common:cards") + ")");
                label.setFont(new Font("Segeo UI", Font.PLAIN, 14));
                row.add(label, BorderLayout.WEST);

                // right - buttons
                JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                JButton deleteButton = new JButton(Translations.get("common:delete"));
                JButton infoButton = new JButton(Translations.get("common:info"));

                deleteButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(listPanel,
                            Translations.get("card_learning:tabs.card.list.delete.confirm.dialog") + list.name + " ?",
                            Translations.get("card_learning:tabs.card.list.delete.confirm"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            Files.deleteIfExists(file.toPath());
                            JOptionPane.showMessageDialog(listPanel,
                                    Translations.get("card_learning:tabs.card.list.delete.done.start") + list.name + Translations.get("card_learning:tabs.card.list.delete.done.end"),
                                    Translations.get("common:delete.success"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            PtfLogger.info("List " + list.name + " has successfully been deleted !", CardLogCategories.CARDS, "list");
                            loadListPanel();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(listPanel,
                                    Translations.get("card_learning:tabs.card.delete_error") + ex.getMessage(),
                                    Translations.get("common:error"),
                                    JOptionPane.ERROR_MESSAGE);
                            PtfLogger.error("Failed to delete list " + list.name, CardLogCategories.CARDS, "list");
                        }
                    }
                });

                infoButton.addActionListener(e -> {
                    // window for display
                    JDialog infoDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(PANEL), Translations.get("card_learning:tabs.card.details") + list.name, true);
                    infoDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    infoDialog.setLocationRelativeTo(PANEL);
                    infoDialog.setLayout(new BorderLayout());

                    // title
                    JLabel title = new JLabel(list.name + " (" + list.cards.size() + " " + Translations.get("common:cards") + ")", SwingConstants.CENTER);
                    title.setFont(new Font("Segeo UI", Font.BOLD, 16));
                    infoDialog.add(title, BorderLayout.NORTH);

                    // card
                    JScrollPane scrollPane = createCardPanelAsScroll(list);
                    infoDialog.add(scrollPane, BorderLayout.CENTER);

                    // close button
                    JButton closeButton = new JButton(Translations.get("common:close"));
                    closeButton.addActionListener(ev -> infoDialog.dispose());

                    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    bottomPanel.add(closeButton);
                    infoDialog.add(bottomPanel, BorderLayout.SOUTH);

                    infoDialog.pack();
                    infoDialog.setLocationRelativeTo(PANEL);
                    infoDialog.setVisible(true);
                });

                buttons.add(deleteButton);
                buttons.add(infoButton);

                row.add(buttons, BorderLayout.EAST);

                listPanel.add(row);
            } catch (Exception e) {
                e.printStackTrace();
                PtfLogger.error("Failed to read list file !", CardLogCategories.CARDS, "list");
            }
        }

        listPanel.revalidate();
        listPanel.repaint();

        refreshComboBox();
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // title
        JLabel title = new JLabel(Translations.get("card_learning:tabs.card.export"), SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        // up - select list 6 button
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        // list
        refreshComboBox(); // adding .json files
        // export button
        JButton exportButton = new JButton(Translations.get("common:export"));
        exportButton.setEnabled(false);

        exportButton.addActionListener(e -> {
            String selected = (String) exportComboBox.getSelectedItem();
            if (selected == null || selected.equals(Translations.get("common:select_list"))) {
                PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "export");
                return;
            }
            PtfLogger.info("User wants to export: " + selected, CardLogCategories.CARDS, "export");

            Path sourcePath = cardsDir.resolve(selected + ".json");
            if (!Files.exists(sourcePath)) {
                PtfLogger.error("Can't find file '" + selected + "' !", CardLogCategories.CARDS, "export");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("file:error.not_found.linked") + sourcePath,
                        Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(Translations.get("card_learning:tabs.card.export") + selected);
            chooser.setSelectedFile(new File(selected + ".json"));

            // ONLY json
            FileNameExtensionFilter filter = new FileNameExtensionFilter(Translations.get("file:json"), "json");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);

            int userSelection = chooser.showSaveDialog(PANEL);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                PtfLogger.info("User canceled export: " + selected, CardLogCategories.CARDS, "export");
                return;
            }

            File destinationFile = chooser.getSelectedFile();
            if (!destinationFile.getName().toLowerCase().endsWith(".json")) {
                destinationFile = new File(destinationFile.getAbsolutePath() + ".json"); // force file to be json
                PtfLogger.warning("User wanted to export as other than JSON ! Forced...", CardLogCategories.CARDS, "export");
            }

            // check if existing
            if (destinationFile.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(PANEL,
                        Translations.get("card_learning:tabs.card.replace.content"),
                        Translations.get("card_learning:tabs.card.replace.name"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (overwrite != JOptionPane.YES_OPTION) {
                    PtfLogger.info("User canceled export: " + selected + " because it would override", CardLogCategories.CARDS, "export");
                    return;
                }
            }

            try {
                Files.copy(sourcePath, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                PtfLogger.info("List '"  + selected + "' exported successfully in: " + destinationFile, CardLogCategories.CARDS, "export");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("card_learning:tabs.card.export.done") + "\n" + destinationFile.getAbsolutePath(),
                        Translations.get("card_learning:tabs.card.export.done.name"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                PtfLogger.error("Failed to copy list '" + selected + "' in: " + destinationFile, CardLogCategories.CARDS, "export");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("card_learning.tabs.card.export.error") + ex.getMessage(),
                        Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        topPanel.add(exportComboBox, BorderLayout.CENTER);
        topPanel.add(exportButton, BorderLayout.EAST);

        // center content
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        // display cards
        JPanel cardsPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        cardsPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(cardsPanel, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        // behaviour
        exportComboBox.addActionListener(e -> {
            String selected = (String) exportComboBox.getSelectedItem();

            // null check
            if (selected == null || selected.equals(Translations.get("common:select_list"))) {
                scrollPane.setViewportView(null);
                exportButton.setEnabled(false);
                PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS, "export");
                return;
            }

            // existing file check
            Path filePath = cardsDir.resolve(selected + ".json");
            if (!Files.exists(filePath)) {
                scrollPane.setViewportView(new JLabel(Translations.get("file:error.not_found"), SwingConstants.CENTER));
                exportButton.setEnabled(false);
                PtfLogger.error("File not found: " + selected, CardLogCategories.CARDS, "export");
                return;
            }

            try {
                String content = Files.readString(filePath);
                CardList list = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

                // null check
                if (list == null || list.cards == null) {
                    scrollPane.setViewportView(new JLabel(Translations.get("potoflux:tabs.card.error.loading_list"), SwingConstants.CENTER));
                    exportButton.setEnabled(false);
                    PtfLogger.error("List can't be null: " + selected, CardLogCategories.CARDS, "export");
                    return;
                }

                JScrollPane cardsScroll = createCardPanelAsScroll(list);
                scrollPane.setViewportView(cardsScroll.getViewport().getView());
                exportButton.setEnabled(true); // export button is now available
            } catch (Exception ex) {
                ex.printStackTrace();
                PtfLogger.error("Error while exporting: " + selected, CardLogCategories.CARDS, "export");
                scrollPane.setViewportView(new JLabel(Translations.get("card_learning:tabs.card.error.reading_file"), SwingConstants.CENTER));
                exportButton.setEnabled(false);
            }
        });

        return panel;
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
                PtfLogger.info("Added " + name + " to " + box.getName(), CardLogCategories.CARDS, "export");
            }
        } else PtfLogger.error("Can't get the list of lists !", CardLogCategories.CARDS, "export");
    }

    private JPanel createLoadPanel() {
        CardList[] list = new CardList[1];
        JScrollPane[] loadedListCards = new JScrollPane[1];

        JPanel panel = new JPanel(new BorderLayout());

        JButton loadButton = new JButton(Translations.get("file:json.import"));
        JButton validateButton = new JButton(Translations.get("common:validate"));
        validateButton.setEnabled(false);

        loadButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            PtfLogger.info("User wants to import a list !", CardLogCategories.CARDS, "load");

            FileNameExtensionFilter filter = new FileNameExtensionFilter(Translations.get("file:json"), "json");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);

            int result = chooser.showOpenDialog(panel);
            if (result != JFileChooser.APPROVE_OPTION) {
                PtfLogger.info("User canceled import.", CardLogCategories.CARDS, "load");
                return;
            }

            // get file
            File selectedFile = chooser.getSelectedFile();
            // turn to path
            Path selectedPath = selectedFile.toPath();

            // show check : file loaded
            PtfLogger.info("User chose file " + selectedFile.getName(), CardLogCategories.CARDS, "load");
            JOptionPane.showMessageDialog(
                    panel, Translations.get("card_learning:tabs.card.file_loaded") + selectedFile.getName() +
                            "\n" + Translations.get("common:path") + selectedPath);

            try {
                // read content
                String content = Files.readString(selectedPath);

                // parse to JSON object
                list[0] = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

                // check is everything right
                if (list[0] == null || list[0].cards == null || getCheckedListName(list[0].name) == null) {
                    showCardError();
                    return;
                } else {
                    for (Card card : list[0].cards) if (card.main == null || card.secondary == null) {
                        showCardError();
                        return;
                    }
                    list[0].name = removeProhibitedChar(list[0].name);
                    PtfLogger.info("Successfully loaded list: " + list[0].name, CardLogCategories.CARDS, "load");
                    JOptionPane.showMessageDialog(PANEL, Translations.get("card_learning:tabs.card.list.loaded") + list[0].name +
                            "\n" + Translations.get("card_learning:tabs.card.card_number") + list[0].cards.size());
                    validateButton.setEnabled(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                PtfLogger.error("Error while reading file " + selectedPath, CardLogCategories.CARDS, "load");
                JOptionPane.showMessageDialog(PANEL, Translations.get("file:error.json.loading"),
                        Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            loadedListCards[0] = createCardPanelAsScroll(list[0]);
            panel.add(loadedListCards[0]);
            panel.revalidate();
            panel.repaint();
        });

        validateButton.addActionListener(e -> {
            if (list[0] == null) {
                PtfLogger.error("No loaded list, but enabled 'Validate' button !", CardLogCategories.CARDS, "load");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("card_learning:tabs.card.empty_list_valid"),
                        Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                list[0] = null;
                validateButton.setEnabled(false);
                removeLoadedCards(loadedListCards[0], panel);
                loadedListCards[0] = null;
            }

            String fileName = list[0].name.replaceAll(" ", "_");
            Path outputFile = cardsDir.resolve(fileName + ".json");

            // cancel if already existing
            if (Files.exists(outputFile)) {
                PtfLogger.error("There is already a list named '" + list[0].name + "' in directory !", CardLogCategories.CARDS, "load");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("file:error.exist.desc") +
                                "\n" + Translations.get("common:add_cancel"),
                        Translations.get("file:error.exist"),
                        JOptionPane.ERROR_MESSAGE);
                list[0] = null;
                validateButton.setEnabled(false);
                removeLoadedCards(loadedListCards[0], panel);
                loadedListCards[0] = null;
                return;
            }

            try {
                Gson gson = new Gson();
                Files.writeString(outputFile, gson.toJson(list[0]));

                PtfLogger.info("File " + list[0].name + " has been saved !", CardLogCategories.CARDS, "load");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("file:saved"),
                        Translations.get("common:success"),
                        JOptionPane.INFORMATION_MESSAGE);

                validateButton.setEnabled(false);
                list[0] = null;
                removeLoadedCards(loadedListCards[0], panel);
                loadedListCards[0] = null;

                loadListPanel(); // reload
            } catch (IOException ex) {
                ex.printStackTrace();
                PtfLogger.error("Failed to write into file: " + list[0].name, CardLogCategories.CARDS, "load");
                JOptionPane.showMessageDialog(PANEL,
                        Translations.get("file:error.saving") + ex.getMessage(),
                        Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        // adding buttons to the panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(loadButton);
        buttonPanel.add(validateButton);

        panel.add(buttonPanel, BorderLayout.NORTH);
        return panel;
    }

    private void removeLoadedCards(JScrollPane scrollPane, JPanel panel) {
        if (scrollPane != null) {
            panel.remove(scrollPane);
            panel.revalidate();
            panel.repaint();
            PtfLogger.info("Removed pane " + scrollPane.getName() + " from " + panel.getName(), CardLogCategories.CARDS, "load");
        } else PtfLogger.warning("Can't remove a null panel !", CardLogCategories.CARDS, "load");
    }

    private JPanel createCardPanel(CardList list) {
        JPanel allCards = new JPanel();
        allCards.setLayout(new GridLayout(0, 1, 10, 10));
        allCards.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Card card : list.cards) {
            JPanel cardPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            cardPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));

            JLabel left = new JLabel(card.main, SwingConstants.CENTER);
            JLabel right = new JLabel(card.secondary, SwingConstants.CENTER);

            left.setFont(new Font("Segoe UI", Font.BOLD, 14));
            right.setFont(new Font("Segoe UI", Font.BOLD, 14));

            cardPanel.add(left);
            cardPanel.add(right);
            allCards.add(cardPanel);
        }

        return allCards;
    }

    private JScrollPane createCardPanelAsScroll(CardList list) {
        JPanel p = createCardPanel(list);
        JScrollPane scrollPane = new JScrollPane(p);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
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
        JOptionPane.showMessageDialog(PANEL, Translations.get("file:json.error.invalid"),
                Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
    }

    private void checkAndCreateDir() {
        try {
            Files.createDirectories(cardsDir);
            PtfLogger.info("Successfully created cardsDir", CardLogCategories.CARDS);
        } catch (IOException ignored) {
            PtfLogger.warning("Failed to create cardsDir...", CardLogCategories.CARDS);
        }
    }

    @Override
    protected boolean invokeLater() {
        return true;
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
}
