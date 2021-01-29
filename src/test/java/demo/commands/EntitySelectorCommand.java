package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.lock.Acquirable;

import java.util.List;

public class EntitySelectorCommand extends Command {

    public EntitySelectorCommand() {
        super("ent");

        setDefaultExecutor((sender, args) -> System.out.println("DEFAULT"));

        ArgumentEntity argumentEntity = ArgumentType.Entities("entities").onlyPlayers(true);

        setArgumentCallback((sender, exception) -> exception.printStackTrace(), argumentEntity);

        addSyntax(this::executor, argumentEntity);

    }

    private void executor(CommandSender commandSender, Arguments arguments) {
        Instance instance = commandSender.asPlayer().getInstance();
        List<Acquirable<? extends Entity>> entities = arguments.getEntities("entities").find(instance, null);
        Acquirable<Entity> entity = (Acquirable<Entity>) entities.get(0);
        System.out.println("test " + ((Player) entity.unwrap()).getUsername());
    }
}
