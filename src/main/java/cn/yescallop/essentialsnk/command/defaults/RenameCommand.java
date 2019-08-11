package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

import java.util.StringJoiner;

public class RenameCommand extends CommandBase {

    public RenameCommand(EssentialsAPI api) {
        super("rename", api);
        this.setAliases(new String[]{"renameme"});


        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("name", CommandParamType.STRING, false)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testIngame(sender)) {
            return false;
        }

        Player player = (Player) sender;

        Item item = player.getInventory().getItemInHand();

        if (item != null && !item.isNull()){

            StringJoiner newName = new StringJoiner(" ");

            for (String arg : args){
                newName.add(arg);
            }

            if (newName.length() < 50){
                item.setCustomName(newName.toString());
                player.getInventory().setItemInHand(item);
            } else {
                player.sendMessage(Language.translate("commands.rename.length"));
                return false;
            }
        }
        return true;
    }
}
