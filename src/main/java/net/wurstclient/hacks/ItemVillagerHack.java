package net.wurstclient.hacks;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.wurstclient.WurstClient;

// basic source https://github.com/crazysnailboy/VillagerInventory
@net.wurstclient.SearchTags({"villager", "inventory", "villager inventory"})
public final class ItemVillagerHack extends net.wurstclient.hack.Hack implements net.wurstclient.events.UpdateListener,
	net.wurstclient.events.ClientInteractListener,
	net.wurstclient.events.PacketInputListener {

	//private final java.util.ArrayList<net.minecraft.entity.passive.VillagerEntity> villagers = new java.util.ArrayList<>();

	public ItemVillagerHack() {
		super("VillagerInventory");
		setCategory(net.wurstclient.Category.ITEMS);
	}
	
	@Override
	public void onEnable() {
		EVENTS.add(net.wurstclient.events.UpdateListener.class, this);
		EVENTS.add(net.wurstclient.events.ClientInteractListener.class, this);
		EVENTS.add(net.wurstclient.events.PacketInputListener.class, this);
	}
	
	@Override
	public void onDisable() {
		EVENTS.remove(net.wurstclient.events.UpdateListener.class, this);
		EVENTS.remove(net.wurstclient.events.ClientInteractListener.class, this);
		EVENTS.remove(net.wurstclient.events.PacketInputListener.class, this);
	}
	
	@Override
	public void onUpdate() {
/*		villagers.clear();

		java.util.stream.Stream<net.minecraft.entity.passive.VillagerEntity> stream =
		java.util.stream.StreamSupport.stream(WurstClient.MC.world.getEntities().spliterator(), false)
				.filter(e -> e instanceof net.minecraft.entity.passive.VillagerEntity).map(e -> (net.minecraft.entity.passive.VillagerEntity)e)
				.filter(e -> !e.removed && e.getHealth() > 0);

		villagers.addAll(stream.collect(java.util.stream.Collectors.toList()));
*/
		//src\main\java\net\wurstclient\mixin\ClientPlayerEntityMixin.java
/*		WurstClient.MC.player.isTouchingWater();

		net.minecraft.entity.Entity entity;
		net.minecraft.util.Hand hand;	
		WurstClient.MC.player.interact(entity, hand);
		net.minecraft.entity.player.PlayerEntity player;
		WurstClient.MC.player.interact(player, hand);

		net.minecraft.util.math.Vec3d vec3D;
		WurstClient.MC.player.interactAt(player, vec3D, hand);
*/

		//src\main\java\net\wurstclient\mixin\ClientPlayerInteractionManagerMixin.java
/*		WurstClient.MC.interactionManager.interactEntity(player, entity, hand);
		net.minecraft.util.hit.EntityHitResult eHitRet;
		WurstClient.MC.interactionManager.interactEntityAtLocation(player, entity, eHitRet, hand);
*/

		//net.fabricmc.fabric.api.event.player.UseEntityCallback;
/*		UseEntityCallback.EVENT.invoker().interact(player, world, hand, entity, hitResult);
		UseEntityCallback.EVENT.register(listener);
*/
	}

	@Override
	public void onInteract(ClientInteractEvent event){
		boolean isValid = true;
/*		for(net.minecraft.entity.passive.VillagerEntity villager : villagers) {
			net.minecraft.inventory.BasicInventory bi = villager.getInventory();
			//villager.onInteractionWith(interaction, entity);
			//villager.onSellingItem(itemStack);
			//villager.talkWithVillager(villagerEntity, time);
			//villager.trade(tradeOffer);
			//villager.getEquipmentForSlot(equipmentSlot, equipmentLevel)
			//villager.canEquipmentSlotContain(slot, item)
			isValid = true;
		}
*/
		net.minecraft.entity.player.PlayerEntity player = event.getPlrEty();
		net.minecraft.entity.Entity entity = event.getEty();
		net.minecraft.entity.passive.VillagerEntity vlrEty = event.getVlrEty();
		net.minecraft.util.Hand hand = event.getHand();
		boolean plrRes = event.getPlrRes();
		net.minecraft.util.ActionResult actRes = event.getActRes();
		//entity.getType().getTranslationKey()

		if(entity == null)
			entity = vlrEty;

		if(entity != null) {
			net.minecraft.entity.passive.VillagerEntity villager = ((net.minecraft.entity.passive.VillagerEntity)entity);
			/*net.minecraft.inventory.BasicInventory bi = villager.getInventory();
			bi = vlrEty.getInventory();
			if(bi != null) {
				int invSize = bi.getInvSize();
				boolean isEmpty = bi.isInvEmpty();
				bi.toString();
			}*/
			net.minecraft.inventory.SimpleInventory si = villager.getInventory();
			si = vlrEty.getInventory();
			if(si != null) {
				int invSize = si.size();
				boolean isEmpty = si.isEmpty();
				si.toString();
			}

			//villager = (net.minecraft.entity.passive.VillagerEntity) WurstClient.MC.world.getEntityById(villager.getEntityId());
			villager = (net.minecraft.entity.passive.VillagerEntity) WurstClient.MC.world.getEntityById(villager.getId());

			java.util.List<net.minecraft.entity.data.DataTracker.Entry<?>> entrys = villager.getDataTracker().getAllEntries();
			/*for(net.minecraft.entity.data.DataTracker.Entry<?> e : entrys) {
				boolean isDirt = e.isDirty();
			}*/
			villager.getVillagerData().getProfession();
			net.minecraft.server.MinecraftServer mcSvr = villager.getServer();
			if(mcSvr == null) {
				mcSvr = WurstClient.MC.getServer();
				if(mcSvr == null)
					mcSvr = WurstClient.MC.player.getServer();
				villager.getServer();
			}

			if(mcSvr != null) {
				//mcSvr.ask(messageProvider);
				//mcSvr.getCommandFunctionManager().getTags();
				//mcSvr.getCommandManager().execute(commandSource, command);
				net.minecraft.entity.Entity ety = mcSvr.getCommandSource().getEntity();
				mcSvr.getCommandSource().getWorld();
//				mcSvr.getDataCommandStorage().get(id);
				mcSvr.getDataCommandStorage().getIds();
				//mcSvr.getDataManager().getAllResources(id);
				java.io.File file;
//				mcSvr.getFile(string);
				//mcSvr.getGameProfileRepo();
				file = mcSvr.getRunDirectory();
				//mcSvr.getTagManager().entityTypes();
				//mcSvr.getTagManager().items();
				//mcSvr.main(args);
			}

			try {
//				entity.getBrightnessAtEyes();
/* can not cast
				net.minecraft.entity.passive.AbstractDonkeyEntity asctDkyEty = ((net.minecraft.entity.passive.AbstractDonkeyEntity)entity);
				net.minecraft.inventory.Inventory inv;
				//asctDkyEty.onInvChange(inv);
				asctDkyEty.openInventory(WurstClient.MC.player);
				net.minecraft.entity.passive.HorseBaseEntity hrsBsEty = ((net.minecraft.entity.passive.HorseBaseEntity)entity);
				//hrsBsEty.onInvChange(inv);
				hrsBsEty.openInventory(WurstClient.MC.player);
				net.minecraft.entity.passive.HorseEntity hrsEty = ((net.minecraft.entity.passive.HorseEntity)entity);
				//hrsEty.onInvChange(inv);
				hrsEty.openInventory(player);
				net.minecraft.entity.passive.DonkeyEntity dkyEty = ((net.minecraft.entity.passive.DonkeyEntity)entity);
				//dkyEty.onInvChange(inv);
				dkyEty.openInventory(WurstClient.MC.player);
*/			} catch (Exception e) {
				new net.wurstclient.command.CmdSyntaxError("Invalid" + e.getMessage());
			}


			net.minecraft.entity.player.PlayerInventory plrInventory = new net.minecraft.entity.player.PlayerInventory(WurstClient.MC.player);
//			WurstClient.MC.player.inventory.clone(plrInventory);
/* crash, can not cast			net.minecraft.entity.player.PlayerInventory etyInventory = ((net.minecraft.entity.player.PlayerEntity)entity).inventory;
			if(etyInventory != null) {
				etyInventory.clone(plrInventory);
			}*/

/*			net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
			villager.writeCustomDataToTag(compound);
			//.saveSelfToTag(tag)
			//.saveToTag(tag)
			if(compound != null) {
				int professionId = compound.getInt("Profession");
				int careerId = compound.getInt("Career");
			}

			//villager.fromTag(tag);
			villager.toTag(compound);
			net.minecraft.entity.passive.VillagerEntity ety = new net.minecraft.entity.passive.VillagerEntity(net.minecraft.entity.EntityType.VILLAGER, WurstClient.MC.world.getWorld());
			ety.fromTag(compound);
*/
			isValid = true;
		}
		
		isValid = true;
	}

	@Override
	public void onInteractAt(ClientInteractAtEvent event){
		boolean isValid = true;

		net.minecraft.entity.player.PlayerEntity player = event.getPlrEty();
		net.minecraft.util.Hand hand = event.getHand();
		net.minecraft.util.math.Vec3d hitPos = event.getHitPos();
		net.minecraft.util.ActionResult actRes = event.getActRes();
		
		isValid = true;
	}

	@Override
	public void onReceivedPacket(PacketInputEvent event){
		if((event.getPacket() instanceof net.minecraft.network.packet.s2c.query.QueryPongS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.InventoryS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.SetTradeOffersS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket))
			return;
		else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket))
			return;
		/*else if((event.getPacket() instanceof net.minecraft.network.packet.s2c.play.TagQueryResponseS2CPacket))
			return;*/

		//net.minecraft.network.packet.s2c.play.TagQueryResponseS2CPacket
	}

/*	net.minecraft.util.ActionResult interact(net.minecraft.entity.player.PlayerEntity player,
                                         net.minecraft.world.World world,
                                         net.minecraft.util.Hand hand,
                                         net.minecraft.entity.Entity entity,
                                         net.minecraft.util.hit.EntityHitResult hitResult) {
											
											return net.minecraft.util.ActionResult.FAIL;
										 }
*/

}