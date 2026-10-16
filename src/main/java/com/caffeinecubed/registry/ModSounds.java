package com.caffeinecubed.registry;

import com.caffeinecubed.CaffeineCubed;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, CaffeineCubed.MODID);

    // The Coffee Machine brewing: 20 seconds, as long as a cup takes (sounds/block/coffee_machine_brewing.ogg)
    public static final DeferredHolder<SoundEvent, SoundEvent> COFFEE_MACHINE_BREWING = SOUND_EVENTS.register("block.coffee_machine.brewing",
            SoundEvent::createVariableRangeEvent);

    private ModSounds() {
    }
}
