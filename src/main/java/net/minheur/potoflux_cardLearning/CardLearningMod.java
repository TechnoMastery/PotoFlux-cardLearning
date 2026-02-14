package net.minheur.potoflux_cardLearning;

import net.minheur.potoflux.PotoFlux;
import net.minheur.potoflux.loader.PotoFluxLoadingContext;
import net.minheur.potoflux.loader.mod.Mod;
import net.minheur.potoflux.loader.mod.ModEventBus;
import net.minheur.potoflux.loader.mod.events.RegisterLangEvent;
import net.minheur.potoflux.logger.LogCategories;
import net.minheur.potoflux.logger.PtfLogger;
import net.minheur.potoflux_cardLearning.tabs.Tabs;
import net.minheur.potoflux_cardLearning.translations.CardLearningTranslations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Mod(modId = CardLearningMod.MOD_ID, version = "2.5", compatibleVersionUrl = "https://technomastery.github.io/PotoFluxAppData/ptfVersion/mods/cardLearning.json")
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

    public static String getVersion() {
        try {
            Properties props = new Properties();
            props.load(CardLearningMod.class.getResourceAsStream("/modVersion.properties"));

            return props.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
            PtfLogger.error("Could not get version for mod " + MOD_ID, LogCategories.MOD_LOADER);
            return null;
        }
    }
}
