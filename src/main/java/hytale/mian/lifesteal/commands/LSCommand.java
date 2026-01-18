package hytale.mian.lifesteal.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class LSCommand extends AbstractCommandCollection {
    public LSCommand() {
        super("ls", "command.lifesteal.desc");
        this.setPermissionGroups(GameMode.Adventure.toString(), GameMode.Creative.toString());
        this.addAliases(new String[]{"lifesteal"});
        this.addSubCommand(new SetHpCommand());
        this.addSubCommand(new GetHpCommand());
    }
}
