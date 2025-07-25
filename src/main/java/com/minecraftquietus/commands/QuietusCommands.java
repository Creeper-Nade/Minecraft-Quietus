package com.minecraftquietus.commands;

import com.minecraftquietus.server.commands.SkillCommands;

import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class QuietusCommands {
    
    public static void registerCommands(RegisterCommandsEvent event) {
        SkillCommands.register(event.getDispatcher());
    }
}
