package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.lock.Acquirable;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((source, args) -> source.sendMessage("Usage: /tp x y z"));

        Argument posArg = ArgumentType.RelativeVec3("pos");
        Argument playerArg = ArgumentType.Word("player");

        addSyntax(this::onPlayerTeleport, playerArg);
        addSyntax(this::onPositionTeleport, posArg);
    }

    private void onPlayerTeleport(CommandSender sender, Arguments args) {
        final String playerName = args.getWord("player");
        Acquirable<Player> acquirableTarget = MinecraftServer.getConnectionManager().getPlayer(playerName);
        if (acquirableTarget != null && sender.isPlayer()) {
            Player player = sender.asPlayer();
            player.teleport(acquirableTarget.unsafeUnwrap().getPosition());
        }
    }

    private void onPositionTeleport(CommandSender sender, Arguments args) {
        final Player player = sender.asPlayer();

        final RelativeVec relativeVec = args.getRelativeVector("pos");
        final Position position = relativeVec.fromRelativePosition(player).toPosition();

        player.teleport(position);
        player.sendMessage("You have been teleported to " + position);
    }

}
