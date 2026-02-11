package com.amethyst.client;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class CopyMessageCommand extends CommandBase {
    
    @Override
    public String getCommandName() {
        return "copymsg";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/copymsg <text>";
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            String text = String.join(" ", args);
            ChatCopyButton.copyToClipboard(text);
        }
    }
    
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}