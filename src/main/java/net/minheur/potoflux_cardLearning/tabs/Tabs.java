package net.minheur.potoflux_cardLearning.tabs;

import net.minheur.potoflux.loader.mod.events.RegisterTabsEvent;
import net.minheur.potoflux.registry.RegistryList;
import net.minheur.potoflux.screen.tabs.Tab;
import net.minheur.potoflux.translations.Translations;
import net.minheur.potoflux.utils.SmartSupplier;
import net.minheur.potoflux.utils.ressourcelocation.ResourceLocation;
import net.minheur.potoflux_cardLearning.CardLearningMod;
import net.minheur.potoflux_cardLearning.tabs.all.CardLearningTab;

public class Tabs {
    private static final RegistryList<Tab> LIST = new RegistryList<>();

    public static final SmartSupplier<Tab> CARD_LEARNING = LIST.add(() -> new Tab(new ResourceLocation(CardLearningMod.MOD_ID, "card_learning"), CardLearningTab.class));

    public static void register(RegisterTabsEvent event) {
        LIST.register(event.reg);
    }
}
