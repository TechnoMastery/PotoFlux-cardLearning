package your.packagename;

import net.minheur.potoflux.PotoFlux;
import net.minheur.potoflux.loader.PotoFluxLoadingContext;
import net.minheur.potoflux.loader.mod.Mod;
import net.minheur.potoflux.loader.mod.ModEventBus;
import net.minheur.potoflux.loader.mod.events.RegisterLangEvent;
import your.packagename.tabs.Tabs;
import your.packagename.translations.CardLearningTranslations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(modId = CardLearningMod.MOD_ID)
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
