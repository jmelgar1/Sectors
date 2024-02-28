package me.jm3l.sectors.utilities.nms;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.block.data.BlockData;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * NMS Registry
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NmsRegistry {

    private static Class<?> blockClass;
    private static Method getStateMethod;
    private static Method getIdMethod;

    static {
        try {
            findBlockClass();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // Handle the error or rethrow as a RuntimeException
            // For example: throw new RuntimeException("Failed to find the Block class", e);
        }
    }

    private static void findBlockClass() throws ClassNotFoundException {
        if (ServerVersion.isGreaterThan(1, 17, 1)) {
            blockClass = Class.forName("net.minecraft.world.level.block.Block");
        } else {
            blockClass = Class.forName(
                "net.minecraft.server." + ServerVersion.getNmsVersion() + ".Block"
            );
        }
    }

    /**
     * @return the block id from the BLOCK_STATE_REGISTRY
     */
    @SneakyThrows
    public static int getBlockId(BlockData blockData) {
        try {
            if (getStateMethod == null) {
                getStateMethod = blockData.getClass().getMethod("getState");
            }

            Object state = getStateMethod.invoke(blockData);

            if (getIdMethod == null) {
                getIdMethod = Arrays.stream(blockClass.getMethods())
                    .filter(m -> m.getReturnType() == int.class)
                    .filter(m -> m.getParameterCount() == 1)
                    .filter(m -> m.getParameterTypes()[0].isAssignableFrom(state.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("Method not found for: " + state.getClass()));
            }
            return (int) getIdMethod.invoke(null, state);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get block ID", e);
        }
    }

}
