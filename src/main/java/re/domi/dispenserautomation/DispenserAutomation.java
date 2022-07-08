package re.domi.dispenserautomation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class DispenserAutomation implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ServerTickEvents.START_WORLD_TICK.register(w -> ((DispenserTicker)w).dispAuto_tick());
    }
}
