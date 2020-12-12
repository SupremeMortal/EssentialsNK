package cn.yescallop.essentialsnk.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import cn.yescallop.essentialsnk.EssentialsAPI;
import cn.yescallop.essentialsnk.Language;
import cn.yescallop.essentialsnk.command.CommandBase;

public class BreakCommand extends CommandBase {

    public BreakCommand(EssentialsAPI api) {
        super("break", api);
        commandParameters.clear();
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.testPermission(sender) || !this.testInGame(sender)) {
            return false;
        }

        Player player = (Player) sender;
        Block block = player.getTargetBlock(120, new Integer[]{Block.AIR});
        if (block == null) {
            player.sendMessage(TextFormat.RED + Language.translate("commands.break.unreachable"));
            return false;
        }

        if (block.getId() == Block.BEDROCK && !player.hasPermission("essentialsnk.break.bedrock")) {
            player.sendMessage(TextFormat.RED + Language.translate("commands.break.bedrock"));
            return false;
        }

        player.getLevel().setBlock(block, new BlockAir(), true, true);
        return true;
    }
}
