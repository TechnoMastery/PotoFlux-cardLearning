package net.minheur.potoflux_cardLearning.utility;

import net.minheur.potoflux.utils.logger.ILogCategory;

public enum CardLogCategories implements ILogCategory {
    CARDS("cardLearning");

    private final String code;

    CardLogCategories(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
