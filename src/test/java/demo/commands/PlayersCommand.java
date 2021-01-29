package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.lock.Acquirable;

import java.util.Collection;
import java.util.stream.Collectors;

public class PlayersCommand extends Command {

    public PlayersCommand() {
        super("players");
        setDefaultExecutor(this::usage);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        final Collection<Acquirable<Player>> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
        final int playerCount = players.size();
        sender.sendMessage("Total players: " + playerCount);
        final int limit = 15;
        if (playerCount <= limit) {
            for (final Acquirable<Player> player : players) {
                sender.sendMessage(player.unwrap().getUsername());
            }
        } else {
            for (final Acquirable<Player> player : players.stream().limit(limit).collect(Collectors.toList())) {
                sender.sendMessage(player.unwrap().getUsername());
            }
            sender.sendMessage("...");
        }
    }

}
