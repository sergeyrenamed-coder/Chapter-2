package com.rnmd.cursedquest;

import net.minecraftforge.fml.common.Mod;

@Mod(CursedQuest.MOD_ID)
public final class CursedQuest {
    public static final String MOD_ID = "cursedquest";
    public static final String DISPLAY_NAME = "Cursed Quest: Part II";

    public CursedQuest() {
        ModNetworking.register();
    }
}
