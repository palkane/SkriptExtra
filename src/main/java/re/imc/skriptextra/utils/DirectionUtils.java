package re.imc.skriptextra.utils;

import org.bukkit.block.BlockFace;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DirectionUtils {
    private static final List<BlockFace> DUE_FACES = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    public static BlockFace getRandomDueFace() {
        return DUE_FACES.get(ThreadLocalRandom.current().nextInt(DUE_FACES.size()));
    }

    public static int faceToDegree(BlockFace face) {
        return DUE_FACES.indexOf(face) * 90;
    }



}
