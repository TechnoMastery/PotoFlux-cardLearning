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
                .en("Card learning")
                .fr("Cartes mémoires");
        addCardTab("add_card")
                .en("Add card")
                .fr("Ajouter");
        addCardTab("available_lists")
                .en("Available lists")
                .fr("Listes disponible");
        addCardTab("cancel_all")
                .en("Cancel all")
                .fr("Tout annuler");
        addCardTab("card_number")
                .en("Number of cards: ")
                .fr("Nombre de cartes : ");
        addCardTab("choose_list")
                .en("Choose list")
                .fr("Choisissez un liste");
        addCardTab("delete_error")
                .en("Error while deleting file: ")
                .fr("Erreur lors de la suppression du fichier : ");
        addCardTab("details")
                .en("Details of ")
                .fr("Détails de ");
        addCardTab("empty_list_valid")
                .en("Empty list but enabled button !")
                .fr("Liste vide mais bouton activé !");
        addCardTab("error", "loading_list")
                .en("Error while loading list.")
                .fr("Erreur lors du chargement de la liste.");
        addCardTab("error", "reading_file")
                .en("Error while reading file")
                .fr("Erreur lors de la lecture du fichier");
        addCardTab("error", "saving")
                .en("Error while saving")
                .fr("Erreur lors de la sauvegarde");
        addCardTab("export")
                .en("Export list")
                .fr("Exporter la liste");
        addCardTab("export", "error")
                .en("Error while exporting: ")
                .fr("Erreur lors de l'export : ");
        addCardTab("export", "done")
                .en("List exported successfully in: ")
                .fr("Liste exportée avec succès dans : ");
        addCardTab("export", "done", "name")
                .en("Exported successfully")
                .fr("Export réussi");
        addCardTab("face", "front")
                .en("Front: ")
                .fr("Avant : ");
        addCardTab("face", "back")
                .en("Back: ")
                .fr("Arrière : ");
        addCardTab("list", "invalid")
                .en("Invalid or empty list")
                .fr("Liste invalide ou vide");
        addCardTab("list", "invalid", "name")
                .en("This list's name is invalid.")
                .fr("Le nom de la liste est vide.");
        addCardTab("list", "column")
                .en("List: ")
                .fr("Liste : ");
        addCardTab("list", "loaded")
                .en("List loaded: ")
                .fr("Liste chargée : ");
        addCardTab("list", "name")
                .en("List name: ")
                .fr("Nom de la liste : ");
        addCardTab("list", "delete", "confirm")
                .en("List delection check")
                .fr("Validation de la suppression");
        addCardTab("list", "delete", "confirm", "dialog")
                .en("Do you really want to delete the list ")
                .fr("Voulez vraiment supprimer la liste ");
        addCardTab("list", "delete", "done", "start")
                .en("The list ")
                .fr("La liste ");
        addCardTab("list", "delete", "done", "end")
                .en(" has been deleted.")
                .fr(" à été supprimée");
        addCardTab("list", "saved")
                .en("List saved successfully")
                .fr("Liste sauvegardée avec succès");
        addCardTab("list", "no_found")
                .en("No lists found.")
                .fr("Aucunes liste trouvée.");
        addCardTab("new")
                .en("New card")
                .fr("Nouvelle carte");
        addCardTab("new", "empty")
                .en("Both input should be filled correctly.")
                .fr("Les deux entrés doivent être complétées correctement.");
        addCardTab("no_card")
                .en("No cards added")
                .fr("Aucunes cartes ajoutée");
        addCardTab("no_selected")
                .en("No card selected.")
                .fr("Aucunes cartes sélectionnée");
        addCardTab("override")
                .en("Do you want to override the cards ?")
                .fr("Voulez vraiment écraser les cartes ?");
        addCardTab("createError")
                .en("ERROR while creating tab ")
                .fr("ERREUR lors de la créatio de l'onglet ");
    }

    // tabs helper
    private TranslationBuilder addCardTab(String... children) {
        return addTab("card", children);
    }
}
