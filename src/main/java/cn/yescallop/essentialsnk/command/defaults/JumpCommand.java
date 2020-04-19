package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class JumpCommand extends CommandBase {

    public JumpCommand(EssentialsAPI api) {
        super("jump", api);
        this.setAliases(new String[]{"j", "jumpto"});
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        Player player = (Player) sender;
        Block block = player.getTargetBlock(120, EssentialsAPI.NON_SOLID_BLOCKS);
        if (block == null) {
            sender.sendMessage(TextFormat.RED + Language.translate("commands.jump.unreachable"));
            return false;
        }

        player.teleport(essentialsAPI.getStandablePositionAt(block));
        return true;
    }
}