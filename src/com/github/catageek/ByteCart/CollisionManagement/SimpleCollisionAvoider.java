package com.github.catageek.ByteCart.CollisionManagement;

import org.bukkit.Location;

import com.github.catageek.ByteCart.HAL.RegistryOutput;
import com.github.catageek.ByteCart.Signs.Triggable;
import com.github.catageek.ByteCart.Storage.ExpirableMap;

public class SimpleCollisionAvoider extends AbstractCollisionAvoider implements CollisionAvoider {

	private static final ExpirableMap<Location, Boolean> recentlyUsedMap = new ExpirableMap<Location, Boolean>(20, false, "recentlyUsed9000");
	private static final ExpirableMap<Location, Boolean> hasTrainMap = new ExpirableMap<Location, Boolean>(14, false, "hastrain");
	
	private RegistryOutput Lever1 = null, Lever2 = null;
	
	private Side state;

	public enum Side {
		RIGHT (3),
		LEFT (0);

		private int Value;

		Side(int b) {
			Value = b;
		}

		public int Value() {
			return Value;
		}
		
		public Side opposite() {
			if (this.equals(LEFT))
				return RIGHT;
			return LEFT;
		}
	}

	public SimpleCollisionAvoider(Triggable ic, org.bukkit.Location loc) {
		super(loc);
/*		if(ByteCart.debug)
			ByteCart.log.info("ByteCart: new SimpleCollisionAvoider() at " + loc);
*/		Lever1 = ic.getOutput(0);
		state = (Lever1.getAmount() == 0 ? Side.LEFT : Side.RIGHT);
		//Initialize();
	}

	public Side WishToGo(Side s, boolean isTrain) {
/*
		if(ByteCart.debug)
			ByteCart.log.info("ByteCart : WishToGo to side " + s + " and isTrain is " + isTrain);
		if(ByteCart.debug)
			ByteCart.log.info("ByteCart : state is " + state + " and Lever2 is " + Lever2);
		if(ByteCart.debug)
			ByteCart.log.info("ByteCart : recentlyUsed is " + recentlyUsed + " and hasTrain is " + hasTrain);
		if(ByteCart.debug)
			ByteCart.log.info("ByteCart : Lever1 is " + Lever1);
*/
		if ( s != state && (Lever2 == null || ( !this.getRecentlyUsed()) && !this.getHasTrain())) {
			Set(s);
		}
		this.setRecentlyUsed(true);
		return state;

	}


	@Override
	public void Add(Triggable t) {
		Lever2 = t.getOutput(0);
		Lever2.setAmount(state.opposite().Value());
	}

	private void Set(Side s) {
		this.Lever1.setAmount(s.Value());
		if (this.Lever2 != null)
			this.Lever2.setAmount(s.opposite().Value());
		state = s;
	}
	
/*	private void Initialize() {
		//Set(Side.RIGHT);
	}
*/
	@Override
	public int getSecondpos() {
		throw new UnsupportedOperationException();
	}

	protected ExpirableMap<Location, Boolean> getRecentlyUsedMap() {
		return recentlyUsedMap;
	}

	protected ExpirableMap<Location, Boolean> getHasTrainMap() {
		return hasTrainMap;
	}



}
