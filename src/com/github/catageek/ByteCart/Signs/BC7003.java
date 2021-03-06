package com.github.catageek.ByteCart.Signs;

import org.bukkit.block.BlockFace;

import com.github.catageek.ByteCart.ByteCart;
import com.github.catageek.ByteCart.HAL.AbstractIC;
import com.github.catageek.ByteCart.HAL.PinRegistry;
import com.github.catageek.ByteCart.IO.InputFactory;
import com.github.catageek.ByteCart.IO.InputPin;
import com.github.catageek.ByteCart.IO.OutputPin;
import com.github.catageek.ByteCart.IO.OutputPinFactory;
import com.github.catageek.ByteCart.Storage.ExpirableMap;
import com.github.catageek.ByteCart.ThreadManagement.Expirable;
import com.github.catageek.ByteCartAPI.HAL.RegistryOutput;
import com.github.catageek.ByteCartAPI.Util.MathUtil;

/**
 * A cart counter
 */
final class BC7003 extends AbstractIC implements Triggable, Powerable {

	final static private ExpirableMap<org.bukkit.Location, Integer> wavecount = new ExpirableMap<org.bukkit.Location, Integer>(400, false, "BC7003");

	BC7003(org.bukkit.block.Block block) {
		super(block);
	}

	BC7003(org.bukkit.block.Block block, RegistryOutput io) {
		this(block);
		// forcing output[0] to be the one in parameter
		this.addOutputRegistry(io);
	}


	/* (non-Javadoc)
	 * @see com.github.catageek.ByteCart.Signs.Triggable#trigger()
	 */
	@Override
	public void trigger() {

		// adding lever as output 0 (if not forced in constructor)
		this.AddOutputIO();

		// We treat the counter
		try {


			if (!this.decrementWaveCount()) {

				(new RemoveCount(ByteCart.myPlugin.Lockduration + 6, true, "Removecount")).reset(getLocation(), this.getOutput(0));
			}
		}
		catch (Exception e) {
			if(ByteCart.debug)
				ByteCart.log.info("ByteCart : "+ e.toString());

			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.github.catageek.ByteCart.Signs.Powerable#power()
	 */
	@Override
	public void power() {
		// check if we are really powered
		if (! this.getBlock().getRelative(MathUtil.clockwise(getCardinal())).isBlockPowered() && ! this.getBlock().getRelative(MathUtil.anticlockwise(getCardinal())).isBlockPowered()) {
			return;
		}

		// add input command = redstone

		InputPin[] wire = new InputPin[2];

		// Right
		wire[0] = InputFactory.getInput(this.getBlock().getRelative(BlockFace.UP).getRelative(MathUtil.clockwise(getCardinal())));
		// left
		wire[1] = InputFactory.getInput(this.getBlock().getRelative(BlockFace.UP).getRelative(MathUtil.anticlockwise(getCardinal())));

		// InputRegistry[0] = detector
		this.addInputRegistry(new PinRegistry<InputPin>(wire));

		// Adding lever as output 0
		this.AddOutputIO();

		// if detector is on, the signal is red (= on)
		if (this.getInput(0).getAmount() != 0) {

			// setting red signal
			this.getOutput(0).setAmount(1);

			this.incrementWaveCount();
			(new RemoveCount(400, true, "Removecount")).reset(getLocation(), this.getOutput(0));
			wavecount.reset(getLocation(), this.getOutput(0));
		}

	}

	/**
	 * increment the counter
	 *
	 */
	final private void incrementWaveCount() {
		synchronized(wavecount) {
			if (!wavecount.contains(this.getLocation())) {
				wavecount.put(getLocation(), 1);
//				if(ByteCart.debug)
//					ByteCart.log.info("ByteCart." + getName() + ": count = " + wavecount.getValue(getBlock()) + " init");
			}
			else {
//				if(ByteCart.debug)
//					ByteCart.log.info("ByteCart." + getName() + ": ++count = " + wavecount.getValue(getBlock()) + " before");
				wavecount.put(getLocation(), wavecount.get(getLocation()) + 1);
//				if(ByteCart.debug)
//					ByteCart.log.info("ByteCart." + getName() + ": ++count = " + wavecount.getValue(getBlock()) + " after");
			}
		}


	}

	/**
	 * decrement the counter
	 *
	 * @return true if the counter is strictly positive
	 */
	final private boolean decrementWaveCount() {
		synchronized(wavecount) {
			if (wavecount.contains(getLocation()) && wavecount.get(getLocation()) > 1)
				wavecount.put(getLocation(), wavecount.get(getLocation()) - 1);

			else {
				wavecount.remove(getLocation());
//				if(ByteCart.debug)
//					ByteCart.log.info("ByteCart." + getName() + ": --count = 0");
				return false;
			}
		
//		if(ByteCart.debug)
//			ByteCart.log.info("ByteCart." + getName() + ": --count = " + wavecount.getValue(getBlock()));

		return true;
		}
	}

	/**
	 * Add the lever behind the sign to give the red light signal
	 *
	 */
	private final void AddOutputIO() {
		// Declare red light signal = lever

		OutputPin[] lever = new OutputPin[1];

		// Right
		lever[0] = OutputPinFactory.getOutput(this.getBlock().getRelative(getCardinal(), 2));

		// OutputRegistry = red light signal
		this.addOutputRegistry(new PinRegistry<OutputPin>(lever));
	}


	/**
	 * Runnable to remove the counter after a timeout
	 */
	final private class RemoveCount extends Expirable<org.bukkit.Location> {

		public RemoveCount(long duration, boolean isSync, String name) {
			super(duration, isSync, name);
		}

		@Override
		public void expire(Object... objects) {
				((RegistryOutput) objects[0]).setAmount(0);
		}
	}

	/* (non-Javadoc)
	 * @see com.github.catageek.ByteCart.HAL.AbstractIC#getName()
	 */
	@Override
	public final String getName() {
		return "BC7003";
	}

	/* (non-Javadoc)
	 * @see com.github.catageek.ByteCart.HAL.AbstractIC#getFriendlyName()
	 */
	@Override
	public final String getFriendlyName() {
		return "Cart counter";
	}

	/* (non-Javadoc)
	 * @see com.github.catageek.ByteCart.Signs.Triggable#isTrain()
	 */
	@Override
	public boolean isTrain() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.github.catageek.ByteCart.Signs.Triggable#wasTrain(org.bukkit.Location)
	 */
	@Override
	public boolean wasTrain(org.bukkit.Location loc) {
		return false;
	}

	@Override
	public boolean isLeverReversed() {
		return false;
	}
}


