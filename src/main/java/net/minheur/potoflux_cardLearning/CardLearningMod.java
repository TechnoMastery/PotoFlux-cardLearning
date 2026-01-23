package net.minheur.potoflux_cardLearning;

import net.minheur.potoflux.PotoFlux;
import net.minheur.potoflux.loader.PotoFluxLoadingContext;
import net.minheur.potoflux.loader.mod.Mod;
import net.minheur.potoflux.loader.mod.ModEventBus;
import net.minheur.potoflux.loader.mod.events.RegisterLangEvent;
import net.minheur.potoflux_cardLearning.tabs.Tabs;
import net.minheur.potoflux_cardLearning.translations.CardLearningTranslations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(modId = CardLearningMod.MOD_ID, version = "2.0", compatibleVersions = {"6.0"})
public class CardLearningMod {
    public static final String MOD_ID = "card_learning";

    public CardLearningMod() {
        ModEventBus modEventBus = PotoFluxLoadingContext.get().getModEventBus();

        modEventBus.addListener(Tabs::register);
        modEventBus.addListener(this::onRegisterLang);
    }

    private void onRegisterLang(RegisterLangEvent event) {
        event.registerLang(new CardLearningTranslations());
    }

    public static Path getModDir() {
        Path dir = PotoFlux.getModDataDir().resolve(MOD_ID);
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        return dir;
    }
}
