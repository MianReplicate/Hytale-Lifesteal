package hytale.mian.lifesteal.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import hytale.mian.lifesteal.storage.LSComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GetHpCommand extends CommandBase {
    OptionalArg<List<PlayerRef>> players;

    public GetHpCommand(){
        super("gethp", "Set the amount of stolen health someone has!");
        this.setPermissionGroups(GameMode.Adventure.toString(), GameMode.Creative.toString());

        players = this.withListOptionalArg("players", "The players you want to include", ArgTypes.PLAYER_REF).setPermission("command.lifesteal.get_hp.has_op");
    }

    private void runWithPlayers(CommandContext commandContext, List<PlayerRef> players){
        players.forEach(playerRef -> {
            Ref<EntityStore> playerEntity = playerRef.getReference();
            playerEntity.getStore().getExternalData().getWorld().execute(() -> {
                if(playerEntity != null){
                    float amount = playerEntity.getStore().ensureAndGetComponent(playerEntity, LSComponent.getComponentType()).getHealthDifference();
                    commandContext.sendMessage(Message.translation("command.lifesteal.get_hp_success").param("player", playerRef.getUsername()).param("amount", amount));
                    return;
                }
                commandContext.sendMessage(Message.translation("command.lifesteal.get_hp_failure").param("player", playerRef.getUsername()));
            });
        });
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        boolean isPlayer = commandContext.isPlayer();

         AtomicReference<List<PlayerRef>> players = new AtomicReference<>(this.players.get(commandContext));

        if (players.get() == null && !isPlayer)
            return;
        else if (players.get() == null && isPlayer){
            commandContext.senderAsPlayerRef().getStore().getExternalData().getWorld().execute(() -> {
                players.set(new ArrayList<>());
                players.get().add(commandContext.senderAsPlayerRef().getStore().getComponent(commandContext.senderAsPlayerRef(), PlayerRef.getComponentType()));
                this.runWithPlayers(commandContext, players.get());
            });
        } else {
            this.runWithPlayers(commandContext, players.get());
        }
    }
}
