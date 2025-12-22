package net.minheur.potoflux_cardLearning.tabs.all;

import com.google.gson.JsonParser;
import net.minheur.potoflux_cardLearning.utility.Card;
import net.minheur.potoflux_cardLearning.utility.CardJsonManager;
import net.minheur.potoflux_cardLearning.utility.CardList;
import net.minheur.potoflux.screen.tabs.BaseTab;
import net.minheur.potoflux.translations.Translations;
import net.minheur.potoflux.utils.logger.PtfLogger;
import net.minheur.potoflux_cardLearning.CardLearningMod;
import net.minheur.potoflux_cardLearning.utility.CardLogCategories;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                PtfLogger.warning("Can't select 'choose list' option !", CardLogCategories.CARDS);
                return;
            }

            Path filePath = cardsDir.resolve(selected + ".json");
            if (!Files.exists(filePath)) {
                PtfLogger.error("File not found: " + selected, CardLogCategories.CARDS);
                JOptionPane.showMessageDialog(panel, Translations.get("file:error.not_found.linked") + selected, Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String content = Files.readString(filePath);
                currentList[0] = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), true);
                if (currentList[0] == null || currentList[0].cards == null || currentList[0].cards.isEmpty()) {
                    PtfLogger.error("List '" + selected + "' is invalid or empty !", CardLogCategories.CARDS);
                    JOptionPane.showMessageDialog(panel, Translations.get("card_learning:tabs.card.list.invalid"), Translations.get("common:error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                PtfLogger.error("Can't read file for list '" + selected + "' !", CardLogCategories.CARDS);
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
                PtfLogger.warning("Empty list, but activated 'flip' button !", CardLogCategories.CARDS);
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
                PtfLogger.error("Empty list, but activated 'next' button !", CardLogCategories.CARDS);
                return;
            }

            // get size
            int size = currentList[0].cards.size();

            // list size check
            if (size == 1) {
                PtfLogger.warning("List is 1 long but 'back' button enabled !");
                backButton.setEnabled(false);
                return;
            }

            // check if last card is live: button wrongly on
            if (index[0] == size - 1) {
                nextButton.setEnabled(false);
                backButton.setEnabled(true);
                PtfLogger.warning("'next' button on, but last card is live !");
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
                PtfLogger.error("Empty list, but activated 'back' button !", CardLogCategories.CARDS);
                return;
            }

            // get size
            int size = currentList[0].cards.size();

            // list size check
            if (size == 1) {
                PtfLogger.warning("List is 1 long but 'back' button enabled !");
                backButton.setEnabled(false);
                return;
            }

            // check for first card
            if (index[0] <= 0) {
                backButton.setEnabled(false);
                nextButton.setEnabled(true);
                PtfLogger.warning("'back' button on, but first card is live !");
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
            PtfLogger.info("No lists found !", CardLogCategories.CARDS);
        } else {
            for (File f : jsonFiles) try {
                // read content
                String content = Files.readString(f.toPath());
                CardList list = CardJsonManager.fromJson(JsonParser.parseString(content).getAsJsonObject(), false);

                if (list == null || list.cards == null) {
                    PtfLogger.error("There is an empty list in directory !", CardLogCategories.CARDS);
                    continue;
                }

                // TODO
            }
        }
    }

    @Override
    protected String getTitle() {
        return Translations.get("yourmodid:tabs.yourTab.title");
    }
}
