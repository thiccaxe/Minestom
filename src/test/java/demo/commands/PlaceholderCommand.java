package demo.commands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.extras.placeholder.Placeholders;
import net.minestom.server.extras.placeholder.component.PlaceholderComponent;

public class PlaceholderCommand extends Command {
    public PlaceholderCommand() {
        super("placeholder", "ph");

        var test = ArgumentType.Boolean("idk");

        addSyntax((sender, context) -> {
            if (context.get(test)) {
                sender.sendMessage(Placeholders.setPlaceholder(PlaceholderComponent.builder(Key.key("minestom:server.port"))
                        .resultError(result -> Component.text("???", NamedTextColor.RED, TextDecoration.BOLD))
                        .resultSuccess(result -> result.getValues().get(0).style(Style.style(NamedTextColor.YELLOW)))
                        .resultUnknown(result -> Component.text(25565, NamedTextColor.YELLOW))
                        .build()));
            } else {
                sender.sendMessage(Placeholders.setPlaceholder(PlaceholderComponent.builder(Key.key("minestom:server.uptime"))
                        .resultError(result -> Component.text("Error", NamedTextColor.RED, TextDecoration.ITALIC))
                        .resultSuccess(result -> result.getValues().get(0).style(Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)))
                        .build()));
            }
        }, test);
    }

}
