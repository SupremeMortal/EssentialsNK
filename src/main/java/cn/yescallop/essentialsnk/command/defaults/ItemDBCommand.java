package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class ItemDBCommand extends CommandBase {

    public ItemDBCommand(EssentialsAPI api) {
        super("itemdb", api);
        this.setAliases(new String[]{"itemno", "durability", "dura"});

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("target", true, new String[]{"name", "id"})
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        if (!this.testIngame(sender)) {
            return false;
        }
        if (args.length > 1) {
            this.sendUsage(sender);
            return false;
        }
        Item item = ((Player) sender).getInventory().getItemInHand();
        String message = api.isRepairable(item) ? Language.translate("commands.itemdb.damage", String.valueOf(item.getDamage())) : Language.translate("commands.itemdb.meta", String.valueOf(item.getDamage()));
        if (args.length == 1) {
            switch (args[0]) {
                case "name":
                    message = Language.translate("commands.itemdb.name", item.getName());
                    break;
                case "id":
                    message = Language.translate("commands.itemdb.id", String.valueOf(item.getId()));
                    break;
            }
        }
        sender.sendMessage(message);
        return true;
    }
}
