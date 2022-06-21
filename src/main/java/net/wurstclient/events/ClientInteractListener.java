package net.wurstclient.events;

import java.util.ArrayList;

import net.wurstclient.event.Listener;
import net.wurstclient.event.Event;

public interface ClientInteractListener extends Listener {
	public void onInteract(ClientInteractEvent event);
	public void onInteractAt(ClientInteractAtEvent event);

	public static class ClientInteractEvent extends Event<ClientInteractListener> {
		net.minecraft.entity.player.PlayerEntity player;
		net.minecraft.entity.Entity entity;
		net.minecraft.entity.passive.VillagerEntity vlrEty;
		net.minecraft.util.Hand hand;

		boolean plrRes;
		net.minecraft.util.ActionResult actRes;
		
		public ClientInteractEvent(net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand, boolean plrRes) {
			this.player = player;
			this.hand = hand;
			this.plrRes = plrRes;
		}

		public ClientInteractEvent(net.minecraft.entity.Entity entity, net.minecraft.util.Hand hand, net.minecraft.util.ActionResult actRes) {
			this.entity = entity;
			this.hand = hand;
			this.actRes = actRes;
		}

		public ClientInteractEvent(net.minecraft.entity.passive.VillagerEntity vlrEty, net.minecraft.util.Hand hand, net.minecraft.util.ActionResult actRes) {
			this.vlrEty = vlrEty;
			this.hand = hand;
			this.actRes = actRes;
		}

		public net.minecraft.entity.player.PlayerEntity getPlrEty() {
			return this.player;
		}

		public net.minecraft.entity.Entity getEty() {
			return this.entity;
		}

		public net.minecraft.entity.passive.VillagerEntity getVlrEty() {
			return this.vlrEty;
		}

		public net.minecraft.util.Hand getHand() {
			return this.hand;
		}

		public boolean getPlrRes() {
			return this.plrRes;
		}

		public net.minecraft.util.ActionResult getActRes() {
			return this.actRes;
		}
		
		@Override
		public void fire(ArrayList<ClientInteractListener> listeners) {
			for(ClientInteractListener listener : listeners)
				listener.onInteract(this);
		}
		
		@Override
		public Class<ClientInteractListener> getListenerType() {
			return ClientInteractListener.class;
		}
	}

	public static class ClientInteractAtEvent extends Event<ClientInteractListener> {
		net.minecraft.entity.player.PlayerEntity player;
		net.minecraft.util.math.Vec3d hitPos;
		net.minecraft.util.Hand hand;

		net.minecraft.util.ActionResult actRes;
		
		public ClientInteractAtEvent(net.minecraft.entity.player.PlayerEntity player,
										net.minecraft.util.math.Vec3d hitPos,
										net.minecraft.util.Hand hand,
										net.minecraft.util.ActionResult actRes) {
			this.player = player;
			this.hitPos = hitPos;
			this.hand = hand;
			this.actRes = actRes;
		}

		public net.minecraft.entity.player.PlayerEntity getPlrEty() {
			return this.player;
		}

		public net.minecraft.util.Hand getHand() {
			return this.hand;
		}

		public net.minecraft.util.math.Vec3d getHitPos() {
			return this.hitPos;
		}

		public net.minecraft.util.ActionResult getActRes() {
			return this.actRes;
		}
		
		@Override
		public void fire(ArrayList<ClientInteractListener> listeners) {
			for(ClientInteractListener listener : listeners)
				listener.onInteractAt(this);
		}
		
		@Override
		public Class<ClientInteractListener> getListenerType() {
			return ClientInteractListener.class;
		}
	}
}