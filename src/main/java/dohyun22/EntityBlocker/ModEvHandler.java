package dohyun22.EntityBlocker;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class ModEvHandler {
	@SubscribeEvent
	public void onEntitySpawningEvent(EntityJoinWorldEvent ev) {
		if (ev.entity != null && !ev.entity.worldObj.isRemote && EntityBlocker.entities.contains(EntityList.getEntityString(ev.entity))) {
			ev.setCanceled(true);
		}
	}
}
