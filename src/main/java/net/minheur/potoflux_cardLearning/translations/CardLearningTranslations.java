package net.minheur.potoflux_cardLearning.translations;

import net.minheur.potoflux.translations.AbstractTranslationsRegistry;
import net.minheur.potoflux_cardLearning.CardLearningMod;

public class CardLearningTranslations extends AbstractTranslationsRegistry {
    public CardLearningTranslations() {
        super(CardLearningMod.MOD_ID);
    }

    @Override
    protected void makeTranslation() {
        addCardTab("name")
                .en("Card learning");
        addCardTab("add_card")
                .en("Add card");
        addCardTab("available_lists")
                .en("Available lists");
        addCardTab("cancel_all")
                .en("Cancel all");
        addCardTab("card_number")
                .en("Number of cards: ");
        addCardTab("choose_list")
                .en("Choose list");
        addCardTab("delete_error")
                .en("Error while deleting file: ");
        addCardTab("details")
                .en("Details of ");
        addCardTab("empty_list_valid")
                .en("Empty list but enabled button !");
        addCardTab("error", "loading_list")
                .en("Error while loading list.");
        addCardTab("error", "reading_file")
                .en("Error while reading file");
        addCardTab("error", "saving")
                .en("Error while saving");
        addCardTab("export")
                .en("Export list");
        addCardTab("export", "error")
                .en("Error while exporting: ");
        addCardTab("export", "done")
                .en("List exported successfully in: ");
        addCardTab("export", "done", "name")
                .en("Exported successfully");
        addCardTab("face", "front")
                .en("Front: ");
        addCardTab("face", "back")
                .en("Back: ");
        addCardTab("list", "invalid")
                .en("Invalid or empty list");
        addCardTab("list", "invalid", "name")
                .en("This list's name is invalid.");
        addCardTab("list", "column")
                .en("List: ");
        addCardTab("list", "loaded")
                .en("List loaded: ");
        addCardTab("list", "name")
                .en("List name: ");
        addCardTab("list", "delete", "confirm")
                .en("List delection check");
        addCardTab("list", "delete", "confirm", "dialog")
                .en("Do you really want to delete the list ");
        addCardTab("list", "delete", "done", "start")
                .en("The list ");
        addCardTab("list", "delete", "done", "end")
                .en(" has been deleted.");
        addCardTab("list", "saved")
                .en("List saved successfully");
        addCardTab("list", "no_found")
                .en("No lists found.");
        addCardTab("new")
                .en("New card");
        addCardTab("new", "empty")
                .en("Both input should be filled correctly.");
        addCardTab("no_card")
                .en("No cards added");
        addCardTab("no_selected")
                .en("No card selected.");
        addCardTab("override")
                .en("Do you want to override the cards ?");
        addCardTab("createError")
                .en("ERROR while creating tab ");
    }

    // tabs helper
    private TranslationBuilder addCardTab(String... children) {
        return addTab("card", children);
    }
}
