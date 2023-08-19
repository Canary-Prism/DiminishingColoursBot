package diminishingcoloursbot;

import java.awt.Color;
import java.util.Arrays;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.permission.PermissionState;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionChoice;
import org.javacord.api.interaction.SlashCommandOptionType;

public class Bot {
    private BotRoleColorHistory history;
    private BotRoleAllocator allocator;

    private DiscordApi api;
    private volatile long time;

    private Main main;
    
    public Bot(String token, Main main) {
        this(token, main, new BotRoleColorHistory());
    }
    public Bot(String token, Main main, BotRoleColorHistory history) {
        this.main = main;
        this.history = history;
        allocator = new BotRoleAllocator(history);
        api = new DiscordApiBuilder().setToken(token).addIntents(Intent.GUILD_MEMBERS).login().join();
    }

    public void createPointlessRoles(int amount, long server_id) {
        var server = api.getServerById(server_id).get();
        for (int i = 0; i < amount; i++) {
            server.createRoleBuilder().setName("test" + i).create().join();
        }
    }


    public void start() {
        api.addSlashCommandCreateListener((event) -> {
            time = System.nanoTime();
            var interaction = event.getSlashCommandInteraction();
            if (interaction.getCommandName().equals("ping")) { // ping command
                interaction.createImmediateResponder().setContent("Pong!").respond().join();
                System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (slash command)");
                return;
            } else if (interaction.getCommandName().equals("rolecolor")) { //rolecolor command
                if (interaction.getOptionByIndex(0).get().getName().equals("get")) {
                    User target = interaction.getOptionByIndex(0).get().getOptionByIndex(1).isPresent()?
                    interaction.getOptionByIndex(0).get().getOptionByIndex(1).get().getUserValue().orElse(interaction.getUser()) : interaction.getUser();
                    for (var role : target.getRoles(interaction.getServer().get())) {
                        if (role.getName().startsWith("​")) {
                            Color color = role.getColor().get();
                            interaction.createImmediateResponder().setContent(
                                switch (interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getLongValue().get().intValue()) {
                                    case 0 -> 
                                        String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                                    case 1 -> 
                                        String.valueOf(color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
                                    default ->
                                        "Invalid Arguments";
                                }
                            ).setFlags(MessageFlag.EPHEMERAL).respond().join();
                            System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (slash command)");
                            return;
                        }
                    };
                }
                if (interaction.getOptionByIndex(0).get().getName().equals("set")) {
                    Color temp_color;
                    User target = switch (interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getName()) {
                        case "hex" -> 
                            interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(1).isPresent()?
                            interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(1).get().getUserValue().orElse(interaction.getUser()) : interaction.getUser();
                        case "rgb" ->
                            interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(3).isPresent()?
                            interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(3).get().getUserValue().orElse(interaction.getUser()) : interaction.getUser();
                        default ->
                            throw new RuntimeException();
                    };
                    boolean imposed = (target != interaction.getUser());

                    if (imposed && !interaction.getServer().get().getPermissions(interaction.getUser()).getState(PermissionType.MANAGE_ROLES).equals(PermissionState.ALLOWED)) {
                        interaction.createImmediateResponder().setContent("Error: You don't have the permission to change other users' role colours").setFlags(MessageFlag.EPHEMERAL).respond().join();
                        System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (slash command)");
                        return;
                    }

                    try {
                        switch (interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getName()) {
                            case "hex" -> {
                                String value = interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(0).get().getStringValue().get();
                                value = value.trim();
                                value = value.replaceAll("#", "");
                                if (value.length() != 6)
                                    throw new NumberFormatException("Invalid Arguments");
                                temp_color = Color.decode("#" + value);
                            }

                            case "rgb" -> {
                                    temp_color = new Color(
                                        interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(0).get().getLongValue().get().intValue(),
                                        interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(1).get().getLongValue().get().intValue(),
                                        interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionByIndex(2).get().getLongValue().get().intValue()
                                    );
                                }

                            default ->
                                throw new RuntimeException("Invalid Arguments");
                        }
                    } catch (NumberFormatException e) {
                        interaction.createImmediateResponder().setContent("Error: Not a hexadecimal color").setFlags(MessageFlag.EPHEMERAL).respond().join();
                        return;
                    } catch (IllegalArgumentException e) {
                        interaction.createImmediateResponder().setContent("Error: RGB color values have to be between 0 and 255 (inclusive)").setFlags(MessageFlag.EPHEMERAL).respond().join();
                        return;
                    }


                    history.add(interaction.getServer().get().getId(), target.getId(), temp_color, imposed);
                    var responder = interaction.respondLater(true).join();
                    responder.setContent("Allocating Roles...").setFlags(MessageFlag.EPHEMERAL).update().join();
                    main.save(history);
                    System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (slash command)");
                    time = System.nanoTime();
                    allocator.update(interaction.getServer().get());
                    responder.setContent("Done!").update().join();
                    System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (role allocation)");
                    return;
                } else if (interaction.getOptionByIndex(0).get().getName().equals("restore")) {
                    Color old_color = history.getCurrent(interaction.getServer().get().getId(), interaction.getUser().getId());
                    Color new_color = history.rewindLatest(interaction.getServer().get().getId(), interaction.getUser().getId());

                    String response;

                    var responder = interaction.respondLater(true).join();
                        
                    if (new_color != null) {
                        response = "Changed rolecolor from " 
                        + String.format("#%02x%02x%02x", old_color.getRed(), old_color.getGreen(), old_color.getBlue())
                        + " back to "
                        + String.format("#%02x%02x%02x", new_color.getRed(), new_color.getGreen(), new_color.getBlue());

                        responder.setContent(response).setFlags(MessageFlag.EPHEMERAL).update().join();
                    } else {
                        responder.setContent("No older colour to revert to").setFlags(MessageFlag.EPHEMERAL).update().join();
                        return;
                    }

                    
                    responder.setContent(response + "\nAllocating Roles...").setFlags(MessageFlag.EPHEMERAL).update().join();
                    main.save(history);
                    System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (slash command)");
                    time = System.nanoTime();
                    allocator.update(interaction.getServer().get());
                    responder.setContent(response + "\nDone!").update().join();
                    System.out.printf("%21s\n", System.nanoTime() - time + " nanoseconds (role allocation)");

                    return;
                }

            }
        });


        api.addRoleCreateListener((e) -> {
            if (!e.getRole().getName().startsWith("​")) {
                allocator.update(e.getServer());
            }
        });

        api.addServerMemberLeaveListener((e) -> {
            if (e.getUser().isYourself()) {
                return;
            }
            history.delete(e.getServer().getId(), e.getUser().getId());
            allocator.update(e.getServer());
            main.save(history);
        });

        api.addServerLeaveListener((e) -> {
            history.delete(e.getServer().getId());
            main.save(history);
        });
    }

    protected void reloadAllSlashCommands() {
        removeAllSlashCommands();
        createPingCommand();
        createColorCommand();
    }

    protected void removeAllSlashCommands() {
        api.getGlobalSlashCommands().join().forEach(command -> command.delete().join());

        api.getServers().forEach(server -> api.getServerSlashCommands(server).join().forEach(command -> command.delete()));
    }

    protected SlashCommand createPingCommand() {
        return SlashCommand.with("ping", "Pings the bot").createGlobal(api).join();
    }

    protected SlashCommand createColorCommand() {
        return SlashCommand.with("rolecolor", "Change or get your custom role colour",
            Arrays.asList(
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, "set", "set a new role colour", Arrays.asList(
                    SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "HEX", "Set a Hexadecimal colour value", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.STRING, "value", "the Hexadecimal colour value", true),
                        SlashCommandOption.create(SlashCommandOptionType.USER, "target", "the user to change the role colour of (only for mods)", false)
                    )),
                    SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "RGB", "Set each Red Green Blue decimal values", Arrays.asList(
                        SlashCommandOption.create(SlashCommandOptionType.LONG, "Red", "value for the red channel", true),
                        SlashCommandOption.create(SlashCommandOptionType.LONG, "Green", "value for the green channel", true),
                        SlashCommandOption.create(SlashCommandOptionType.LONG, "Blue", "value for the blue channel", true),
                        SlashCommandOption.create(SlashCommandOptionType.USER, "target", "the user to change the role colour of (only for mods)", false)
                    ))
                )),
                
                SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, "get", "get your current role colour", Arrays.asList(
                    SlashCommandOption.createWithChoices(SlashCommandOptionType.LONG, "format", "format of color to get", true, Arrays.asList(
                        SlashCommandOptionChoice.create("HEX", 0),
                        SlashCommandOptionChoice.create("RGB", 1)
                    )),
                    SlashCommandOption.create(SlashCommandOptionType.USER, "target", "the user to change the role colour of", false)
                )),

                SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, "restore", "restore the last colour")
            )
        ).setEnabledInDms(false).createGlobal(api).join();
    }

}
