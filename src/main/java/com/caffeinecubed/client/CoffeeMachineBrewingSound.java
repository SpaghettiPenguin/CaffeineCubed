package com.caffeinecubed.client;

import com.caffeinecubed.block.CoffeeMachineBlock;
import com.caffeinecubed.block.entity.CoffeeMachineBlockEntity;
import com.caffeinecubed.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

/**
 * The Coffee Machine's brewing sound, played from the machine for as long as it brews. It is as long as a cup takes,
 * so it loops once a cup while the machine brews cup after cup, and stops as soon as the machine does.
 */
public class CoffeeMachineBrewingSound extends AbstractTickableSoundInstance {
    // The sound playing at each machine, so that a machine never plays two at once
    private static final Map<BlockPos, CoffeeMachineBrewingSound> PLAYING = new HashMap<>();

    private final CoffeeMachineBlockEntity machine;

    private CoffeeMachineBrewingSound(CoffeeMachineBlockEntity machine) {
        super(ModSounds.COFFEE_MACHINE_BREWING.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.machine = machine;
        BlockPos pos = machine.getBlockPos();
        x = pos.getX() + 0.5;
        y = pos.getY() + 0.5;
        z = pos.getZ() + 0.5;
        looping = true;
        delay = 0;
    }

    /**
     * Runs every tick for each machine the client knows of: starts the sound when the machine starts brewing.
     */
    public static void update(CoffeeMachineBlockEntity machine) {
        BlockPos pos = machine.getBlockPos();
        CoffeeMachineBrewingSound sound = PLAYING.get(pos);
        if (!isBrewing(machine)) {
            // The sound stops itself
            PLAYING.remove(pos);
            return;
        }
        SoundManager sounds = Minecraft.getInstance().getSoundManager();
        if (sound == null || sound.machine != machine || !sounds.isActive(sound)) {
            sound = new CoffeeMachineBrewingSound(machine);
            PLAYING.put(pos.immutable(), sound);
            sounds.play(sound);
        }
    }

    private static boolean isBrewing(CoffeeMachineBlockEntity machine) {
        return !machine.isRemoved() && machine.getBlockState().getValue(CoffeeMachineBlock.LIT);
    }

    @Override
    public void tick() {
        if (!isBrewing(machine)) {
            stop();
        }
    }
}
