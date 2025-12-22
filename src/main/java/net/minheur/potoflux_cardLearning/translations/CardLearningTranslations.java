package net.minheur.potoflux_cardLearning.translations;

import net.minheur.potoflux.translations.AbstractTranslationsRegistry;
import net.minheur.potoflux_cardLearning.CardLearningMod;

public class CardLearningTranslations extends AbstractTranslationsRegistry {
    public CardLearningTranslations() {
        super(CardLearningMod.MOD_ID);
    }

    @Override
    protected void makeTranslation() {
        cardLearningTab("name")
                .en("Card Learning");
        cardLearningTab("title")
                .en("Your tab title");
    }

    // tabs helper
    private TranslationBuilder cardLearningTab(String... children) {
        return addTab("cardLearn", children);
    }
}
