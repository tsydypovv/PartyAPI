package me.m1ran.partyplugin;

import me.m1ran.worldchoiceplugin.WorldChoiceAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PartyTabCompleter implements TabCompleter {


    private final PartyManager partyManager;

    public PartyTabCompleter(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            //Первое слово команды (подсказки с субкомандами)
            return Arrays.asList("invite", "kick", "leave", "create", "accept", "list", "remove").stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            //Подсказки по никам игроков
            String sub = args[0].toLowerCase();
            String input = args[1].toLowerCase();

            if (sub.equals("kick")) {
                Party party = partyManager.getParty(player);
                if (party == null) return Collections.emptyList();

                return party.getMembers().stream()
                        .filter(uuid -> !uuid.equals(player.getUniqueId())) // не подсказывать самого себя
                        .map(Bukkit::getOfflinePlayer)
                        .map(OfflinePlayer::getName)
                        .filter(Objects::nonNull) // вдруг имя не прогрузилось
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            }

            if (sub.equals("invite")) {

                // Получаем API из главного плагина (например, через синглтон или инъекцию)
                WorldChoiceAPI api = PartyPlugin.getInstance().getWorldChoiceAPI();

                // Получаем мир игрока через API (замени метод на свой)
                String playerWorldId = api.getPlayerWorld(player);

                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !p.equals(player)) // не показываем себя
                        .filter(p -> {
                            String otherWorldId = api.getPlayerWorld(p);
                            return playerWorldId != null && playerWorldId.equals(otherWorldId);
                        })
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
        }


        return Collections.emptyList();
    }
}
