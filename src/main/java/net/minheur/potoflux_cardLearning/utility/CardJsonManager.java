package net.minheur.potoflux_cardLearning.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minheur.potoflux.logger.PtfLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardJsonManager {
    public static CardList fromJson(JsonObject json, boolean randomized) {
        // checks
        if (json == null) {
            PtfLogger.error("Trying to extract a null list !", CardLogCategories.CARDS);
            return null;
        }
        String name = json.get("name").toString();
        if (name == null) {
            PtfLogger.error("Trying to extract a list with no name !", CardLogCategories.CARDS);
            return null;
        }

        PtfLogger.info("List " + name + " seems correct. Loading...", CardLogCategories.CARDS);

        // main principal def
        JsonArray cardsJsonArray = json.getAsJsonArray("cards");
        List<Card> cardsArray = new ArrayList<>();

        for (int i = 0; i < cardsJsonArray.size(); i++) {
            // def of the target card
            JsonObject j = cardsJsonArray.get(i).getAsJsonObject();
            Card c = new Card();

            // randomize the main & secondary, if asked
            boolean reverse = Math.random() < 0.5 || randomized;
            c.main = j.get(reverse ? "secondary" : "main").getAsString();
            c.secondary = j.get(reverse ? "main" : "secondary").getAsString();

            // null check
            if (c.main == null || c.secondary == null) {
                PtfLogger.error("A card of list " + name + " is wrong !", CardLogCategories.CARDS);
                return null;
            }

            // add to list
            cardsArray.add(c);
        }

        // create the list
        CardList list = new CardList();

        if (randomized) {
            Collections.shuffle(cardsArray);
            PtfLogger.info("List "+ name + " has been randomized !", CardLogCategories.CARDS);
        }

        list.name = name;
        list.cards = cardsArray;

        PtfLogger.info("List " + name + " has successfully been loaded !");

        return list;
    }
}
