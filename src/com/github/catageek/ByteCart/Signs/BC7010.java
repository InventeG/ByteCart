package com.github.catageek.ByteCart.Signs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.catageek.ByteCart.ByteCart;
import com.github.catageek.ByteCart.Routing.Address;
import com.github.catageek.ByteCart.Routing.AddressFactory;
import com.github.catageek.ByteCart.Routing.AddressRouted;
import com.github.catageek.ByteCart.Util.Book;


public class BC7010 extends AbstractTriggeredSign implements Triggable, Clickable {

	protected boolean PlayerAllowed = true;
	protected boolean StorageCartAllowed = false;

	// Constructor : !! vehicle can be null !!

	public BC7010(org.bukkit.block.Block block,
			org.bukkit.entity.Vehicle vehicle) {
		super(block, vehicle);
	}

	public BC7010(Block block, Player player) {
		super(block, null);
		this.setInventory(player.getInventory());
	}

	@Override
	public final void trigger() {

		if (! this.isHolderAllowed())
			return;

		// if this is a cart in a train
		if (this.wasTrain(this.getLocation())) {
			ByteCart.myPlugin.getIsTrainManager().getMap().reset(getLocation());
			return;
		}

		Address address = getAddressToWrite();

		if (address == null)
			return;

		this.setAddress(address);

		// if this is the first car of a train
		// we save the state during 2 s
		if (address.isTrain()) {
			this.setWasTrain(this.getLocation(), true);
		}


	}

	protected Address getAddressToWrite() {
		Address Address = AddressFactory.getAddress(this.getBlock(), 3);
		return Address;
	}

	protected AddressRouted getTargetAddress() {
		return AddressFactory.getAddress(this.getInventory());
	}

	public final boolean setAddress(Address SignAddress){
		Player player = null;
		
		if (this.getInventory().getHolder() instanceof Player)
			player = (Player) this.getInventory().getHolder();

		if (ByteCart.usebooks) {
			getOrCreateTicket(player);
		}

		AddressRouted IPaddress = getTargetAddress();

		if (!IPaddress.setAddress(SignAddress)) {

			if (this.getInventory().getHolder() instanceof Player) {
				((Player) this.getInventory().getHolder()).sendMessage(ChatColor.GREEN+"[Bytecart] " + ChatColor.RED + ByteCart.myPlugin.getConfig().getString("Error.SetAddress") );
			}
			return false;
		}
		if (this.getInventory().getHolder() instanceof Player) {
			((Player) this.getInventory().getHolder()).sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.YELLOW + ByteCart.myPlugin.getConfig().getString("Info.SetAddress") + " (" + ChatColor.RED + IPaddress + ")");
			if (this.getVehicle() == null)
				((Player) this.getInventory().getHolder()).sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.YELLOW + ByteCart.myPlugin.getConfig().getString("Info.SetAddress2") );
		} else
			IPaddress.initializeTTL();
		return true;
	}

	@SuppressWarnings("deprecation")
	private void getOrCreateTicket(Player player) {
		int slot;

		if (player == null  || this.forceTicketReuse()) {
			// if storage cart or we must reuse a existing ticket
			// check if a ticket exists and return
			// otherwise continue
			slot = Book.getTicketslot(this.getInventory());
			if (slot != -1)
				return;
		}

		// get a slot containing an emtpy book (or nothing)
		slot = Book.getEmptyOrBookAndQuillSlot(this.getInventory());

		if (slot == -1) {
			String msg = "Error: No space in inventory.";
			player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.RED + msg);
			return;
		}

		ItemStack stack;
		
		if (this.getInventory().getItem(slot) == null
				&& ByteCart.myPlugin.getConfig().getBoolean("mustProvideBooks")
				&& ByteCart.myPlugin.getConfig().getBoolean("usebooks")) {
			String msg = "No empty book in your inventory, using legacy address storage.";
			player.sendMessage(ChatColor.DARK_GREEN+"[Bytecart] " + ChatColor.RED + msg);
			return;
		}

		/*
		 * Here we create a ticket in slot, replacing empty book if needed
		 */
		BookMeta book;

		book = (BookMeta) Bukkit.getServer().getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
		book.setAuthor(ByteCart.myPlugin.getConfig().getString("author"));
		book.setTitle(ByteCart.myPlugin.getConfig().getString("title"));
		stack = new ItemStack(Material.WRITTEN_BOOK);
		stack.setItemMeta(book);

		this.getInventory().setItem(slot, stack);
		if (player != null)
			player.updateInventory();
	}

	protected final boolean isHolderAllowed() {
		InventoryHolder holder = this.getInventory().getHolder();
		if (holder instanceof Player)
			return PlayerAllowed;
		if (holder instanceof StorageMinecart) {
			return StorageCartAllowed;
		}
		return false;
	}

	@Override
	public final void click() {
		this.trigger();

	}

	@Override
	public String getName() {
		return "BC7010";
	}

	@Override
	public String getFriendlyName() {
		return "Goto";
	}
	
	protected boolean forceTicketReuse() {
		return false;
	}
}
