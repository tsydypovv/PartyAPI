package me.m1ran.partyplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду!");
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage("Использование: /party <create|invite|join|leave|list|remove|kick>");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        PartyManager manager = PartyManager.getInstance();

        switch (subcommand) {
            case "create":
                if (args.length != 1) {
                    sender.sendMessage("Использование: /party <create|invite|join|leave|list|remove|kick>");
                    return true;
                }

                manager.createParty(player);
                return true;
            case "invite":
                if (args.length != 2) {
                    sender.sendMessage("Использование: /party invite <nickname>");
                    return true;
                }

                manager.invitePlayer(player, args[1]);
                return true;
            case "accept":
                if (args.length != 2) {
                    player.sendMessage("Использование: /party accept <ник_лидера>");
                    return true;
                }
                manager.acceptParty(player, args[1]);
                return true;
            case "leave":
                if (args.length != 1) {
                    sender.sendMessage("Использование: /party <create|invite|join|leave|list|remove|kick>");
                    return true;
                }
                manager.leaveParty(player);
                return true;
            case "list":
                if (args.length != 1) {
                    sender.sendMessage("Использование: /party <create|invite|join|leave|list|remove|kick>");
                    return true;
                }
                manager.listParty(player);
                return true;
            case "remove":
                if (args.length != 1) {
                    sender.sendMessage("Использование: /party <create|invite|join|leave|list|remove|kick>");
                    return true;
                }
                manager.removeParty(player);
                return true;
            case "kick":
                if (args.length != 2) {
                    sender.sendMessage("Использование: /party kick <nickname>");
                    return true;
                }
                manager.kickPlayer(player, args[1]);
                return true;
        }

        return false;
    }
}
