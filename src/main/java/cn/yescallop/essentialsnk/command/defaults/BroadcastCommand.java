package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.command.CommandBase;

public class BroadcastCommand extends CommandBase {

    public BroadcastCommand(EssentialsAPI api) {
        super("broadcast", api);
        this.setAliases(new String[]{"bcast"});

        // command parameters
        commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[] {
                new CommandParameter("message", CommandParamType.MESSAGE, true)
        });
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender)) {
            return false;
        }
        if (args.length == 0) {
            this.sendUsage(sender);
            return false;
        }
        api.getServer().broadcastMessage(String.join(" ", args));
        return true;
    }
}
