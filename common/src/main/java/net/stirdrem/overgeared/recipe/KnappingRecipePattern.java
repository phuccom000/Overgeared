package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class KnappingRecipePattern {
    private final boolean[][] pattern;
    private final boolean mirrored;

    public KnappingRecipePattern(boolean[][] pattern, boolean mirrored) {
        if (pattern.length != 3 || pattern[0].length != 3) {
            throw new IllegalArgumentException("Knapping pattern must be 3x3");
        }
        this.pattern = pattern;
        this.mirrored = mirrored;
    }

    public boolean matches(boolean[][] inputGrid) {
        if (inputGrid.length != 3 || inputGrid[0].length != 3) {
            return false;
        }

        // Try all offsets
        for (int offsetY = -2; offsetY <= 2; offsetY++) {
            for (int offsetX = -2; offsetX <= 2; offsetX++) {
                if (matchesPattern(inputGrid, offsetX, offsetY, false)) {
                    return true;
                }
                if (mirrored && matchesPattern(inputGrid, offsetX, offsetY, true)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesPattern(boolean[][] inputGrid, int offsetX, int offsetY, boolean mirror) {
        // Check Pattern
        for (int py = 0; py < 3; py++) {
            for (int px = 0; px < 3; px++) {
                int patternX = mirror ? (2 - px) : px;
                int inputX = px + offsetX;
                int inputY = py + offsetY;

                if (inputX < 0 || inputX >= 3 || inputY < 0 || inputY >= 3) {
                    if (pattern[py][patternX]) {
                        return false;
                    }
                    continue;
                }

                if (pattern[py][patternX] != inputGrid[inputY][inputX]) {
                    return false;
                }
            }
        }

        // Checked extra chipped areas
        for (int iy = 0; iy < 3; iy++) {
            for (int ix = 0; ix < 3; ix++) {
                int patternX = ix - offsetX;
                int patternY = iy - offsetY;

                if (mirror) {
                    patternX = 2 - patternX;
                }

                boolean inPattern = patternY >= 0 && patternY < 3 && patternX >= 0 && patternX < 3;

                if (!inPattern && inputGrid[iy][ix]) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean[][] getPattern() {
        return pattern;
    }

    public boolean isMirrored() {
        return mirrored;
    }

    private static final Codec<boolean[][]> PATTERN_ARRAY_CODEC = Codec.STRING.listOf().xmap(
            list -> {
                boolean[][] pattern = new boolean[3][3];
                for (int y = 0; y < Math.min(list.size(), 3); y++) {
                    String row = list.get(y);
                    for (int x = 0; x < Math.min(row.length(), 3); x++) {
                        pattern[y][x] = row.charAt(x) == 'x' || row.charAt(x) == 'X';
                    }
                }
                return pattern;
            },
            pattern -> {
                List<String> out = new ArrayList<>();
                for (int y = 0; y < 3; y++) {
                    StringBuilder sb = new StringBuilder();
                    for (int x = 0; x < 3; x++) {
                        sb.append(pattern[y][x] ? 'x' : ' ');
                    }
                    out.add(sb.toString());
                }
                return out;
            }
    );

    public static final MapCodec<KnappingRecipePattern> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PATTERN_ARRAY_CODEC.fieldOf("pattern").forGetter(p -> p.pattern),
            Codec.BOOL.optionalFieldOf("mirrored", false).forGetter(p -> p.mirrored)
    ).apply(instance, KnappingRecipePattern::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, boolean[][]> PATTERN_ARRAY_STREAM_CODEC = StreamCodec.of(
            KnappingRecipePattern::toNetwork, KnappingRecipePattern::fromNetwork
    );

    public static boolean[][] fromNetwork(RegistryFriendlyByteBuf buffer) {
        boolean[][] pattern = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pattern[i][j] = buffer.readBoolean();
            }
        }
        return pattern;
    }

    public static void toNetwork(RegistryFriendlyByteBuf buffer, boolean[][] pattern) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buffer.writeBoolean(pattern[i][j]);
            }
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, KnappingRecipePattern> STREAM_CODEC = StreamCodec.composite(
            PATTERN_ARRAY_STREAM_CODEC, p -> p.pattern,
            ByteBufCodecs.BOOL, p -> p.mirrored,
            KnappingRecipePattern::new
    );
}
