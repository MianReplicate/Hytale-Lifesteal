package hytale.mian.lifesteal.commands;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import hytale.mian.lifesteal.Lifesteal;
import hytale.mian.lifesteal.storage.LSComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SetHpCommand extends CommandBase {
    RequiredArg<Float> amount;
    OptionalArg<List<PlayerRef>> players;

    public SetHpCommand(){
        super("sethp", "command.lifesteal.set_hp.desc");
        this.setPermissionGroup(null);

        amount = this.withRequiredArg("amount", "command.lifesteal.amount", ArgTypes.FLOAT).addValidator(Validators.range(Lifesteal.config.get().getMinAmount(), Lifesteal.config.get().getCapAmount()));
        players = this.withListOptionalArg("players", "command.lifesteal.players", ArgTypes.PLAYER_REF);
    }

    private void runWithPlayers(CommandContext commandContext, List<PlayerRef> players, float amount){
        players.forEach(playerRef -> {
            Ref<EntityStore> playerEntity = playerRef.getReference();
            if(playerEntity != null){
                playerEntity.getStore().getExternalData().getWorld().execute(() -> {
                    playerEntity.getStore().ensureAndGetComponent(playerEntity, LSComponent.getComponentType()).setHealthDifference(amount);
                    commandContext.sendMessage(Message.translation("command.lifesteal.set_hp_success").param("player", playerRef.getUsername()).param("amount", amount));
                });
            } else {
                commandContext.sendMessage(Message.translation("command.lifesteal.set_hp_failure").param("player", playerRef.getUsername()));
            }
        });
    }

    @Override
    protected void executeSync(@Nonnull CommandContext commandContext) {
        boolean isPlayer = commandContext.isPlayer();

        float amount = this.amount.get(commandContext);
        AtomicReference<List<PlayerRef>> players = new AtomicReference<>(this.players.get(commandContext));

        if (players.get() == null && !isPlayer)
            return;
        else if (players.get() == null && isPlayer){
            commandContext.senderAsPlayerRef().getStore().getExternalData().getWorld().execute(() -> {
                players.set(new ArrayList<>());
                players.get().add(commandContext.senderAsPlayerRef().getStore().getComponent(commandContext.senderAsPlayerRef(), PlayerRef.getComponentType()));
                this.runWithPlayers(commandContext, players.get(), amount);
            });
        } else {
            this.runWithPlayers(commandContext, players.get(), amount);
        }
    }
}
