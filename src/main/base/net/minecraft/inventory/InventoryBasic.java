package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryBasic implements IInventory/* WDL >>> */, wdl.ducks.INetworkNameable /* <<< WDL */ {
	private String inventoryTitle;
	private int slotsCount;
	private ItemStack[] inventoryContents;
	private List<IInventoryChangedListener> listeners;
	private boolean hasCustomName;
	/* WDL >>> */
	@Nullable
	private String networkCustomName;

	@Nullable
	@Override
	public String getCustomDisplayName() {
		return networkCustomName;
	}
	/* <<< WDL */

	public InventoryBasic(String p_i1561_1_, boolean p_i1561_2_, int p_i1561_3_) {
		this.inventoryTitle = p_i1561_1_;
		this.hasCustomName = p_i1561_2_;
		this.slotsCount = p_i1561_3_;
		this.inventoryContents = new ItemStack[p_i1561_3_];
	}

	public InventoryBasic(ITextComponent p_i45902_1_, int p_i45902_2_) {
		this(p_i45902_1_.getString(), true, p_i45902_2_);
		/* WDL >>> */
		if (p_i45902_1_ instanceof TextComponentString) {
			this.networkCustomName = p_i45902_1_.getString();
		}
		/* <<< WDL */
	}

	/**
	 * Add a listener that will be notified when any item in this inventory is modified.
	 */
	public void addListener(IInventoryChangedListener listener) {
		if (this.listeners == null) {
			this.listeners = Lists.<IInventoryChangedListener>newArrayList();
		}

		this.listeners.add(listener);
	}

	/**
	 * removes the specified IInvBasic from receiving further change notices
	 */
	public void removeListener(IInventoryChangedListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Nullable
	public ItemStack getStackInSlot(int index) {
		return index >= 0 && index < this.inventoryContents.length ? this.inventoryContents[index] : null;
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 */
	@Nullable
	public ItemStack decrStackSize(int index, int count) {
		ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);

		if (itemstack != null) {
			this.markDirty();
		}

		return itemstack;
	}

	@Nullable
	public ItemStack addItem(ItemStack stack) {
		ItemStack itemstack = stack.copy();

		for (int i = 0; i < this.slotsCount; ++i) {
			ItemStack itemstack1 = this.getStackInSlot(i);

			if (itemstack1 == null) {
				this.setInventorySlotContents(i, itemstack);
				this.markDirty();
				return null;
			}

			if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
				int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
				int k = Math.min(itemstack.count, j - itemstack1.count);

				if (k > 0) {
					itemstack1.count += k;
					itemstack.count -= k;

					if (itemstack.count <= 0) {
						this.markDirty();
						return null;
					}
				}
			}
		}

		if (itemstack.count != stack.count) {
			this.markDirty();
		}

		return itemstack;
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Nullable
	public ItemStack removeStackFromSlot(int index) {
		if (this.inventoryContents[index] != null) {
			ItemStack itemstack = this.inventoryContents[index];
			this.inventoryContents[index] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
		this.inventoryContents[index] = stack;

		if (stack != null && stack.count > this.getInventoryStackLimit()) {
			stack.count = this.getInventoryStackLimit();
		}

		this.markDirty();
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	public int getSizeInventory() {
		return this.slotsCount;
	}

	/**
	 * Gets the name of this thing. This method has slightly different behavior depending on the interface (for <a
	 * href="https://github.com/ModCoderPack/MCPBot-Issues/issues/14">technical reasons</a> the same method is used for
	 * both IWorldNameable and ICommandSender):
	 *  
	 * <dl>
	 * <dt>{@link net.minecraft.util.INameable#getName() INameable.getName()}</dt>
	 * <dd>Returns the name of this inventory. If this {@linkplain net.minecraft.inventory#hasCustomName() has a custom
	 * name} then this <em>should</em> be a direct string; otherwise it <em>should</em> be a valid translation string.</dd>
	 * <dd>However, note that <strong>the translation string may be invalid</strong>, as is the case for {@link
	 * net.minecraft.tileentity.TileEntityBanner TileEntityBanner} (always returns nonexistent translation code
	 * <code>banner</code> without a custom name), {@link net.minecraft.block.BlockAnvil.Anvil BlockAnvil$Anvil} (always
	 * returns <code>anvil</code>), {@link net.minecraft.block.BlockWorkbench.InterfaceCraftingTable
	 * BlockWorkbench$InterfaceCraftingTable} (always returns <code>crafting_table</code>), {@link
	 * net.minecraft.inventory.InventoryCraftResult InventoryCraftResult} (always returns <code>Result</code>) and the
	 * {@link net.minecraft.entity.item.EntityMinecart EntityMinecart} family (uses the entity definition). This is not an
	 * exaustive list.</dd>
	 * <dd>In general, this method should be safe to use on tile entities that implement IInventory.</dd>
	 * <dt>{@link net.minecraft.command.ICommandSender#getName() ICommandSender.getName()} and {@link
	 * net.minecraft.entity.Entity#getName() Entity.getName()}</dt>
	 * <dd>Returns a valid, displayable name (which may be localized). For most entities, this is the translated version of
	 * its translation string (obtained via {@link net.minecraft.entity.EntityList#getEntityString
	 * EntityList.getEntityString}).</dd>
	 * <dd>If this entity has a custom name set, this will return that name.</dd>
	 * <dd>For some entities, this will attempt to translate a nonexistent translation string; see <a
	 * href="https://bugs.mojang.com/browse/MC-68446">MC-68446</a>. For {@linkplain
	 * net.minecraft.entity.player.EntityPlayer#getName() players} this returns the player's name. For {@linkplain
	 * net.minecraft.entity.passive.EntityOcelot ocelots} this may return the translation of <code>entity.Cat.name</code>
	 * if it is tamed. For {@linkplain net.minecraft.entity.item.EntityItem#getName() item entities}, this will attempt to
	 * return the name of the item in that item entity. In all cases other than players, the custom name will overrule
	 * this.</dd>
	 * <dd>For non-entity command senders, this will return some arbitrary name, such as "Rcon" or "Server".</dd>
	 * </dl>
	 */
	public String getName() {
		return this.inventoryTitle;
	}

	public boolean hasCustomName() {
		return this.hasCustomName;
	}

	public void setCustomName(String p_110133_1_) {
		this.hasCustomName = true;
		this.inventoryTitle = p_110133_1_;
	}

	public ITextComponent getDisplayName() {
		return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
	 * hasn't changed and skip it.
	 */
	public void markDirty() {
		if (this.listeners != null) {
			for (int i = 0; i < this.listeners.size(); ++i) {
				((IInventoryChangedListener)this.listeners.get(i)).onInventoryChanged(this);
			}
		}
	}

	/**
	 * Don't rename this method to canInteractWith due to conflicts with Container
	 */
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	public void openInventory(EntityPlayer player) {
	}

	public void closeInventory(EntityPlayer player) {
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For guis
	 * use Slot.isItemValid
	 */
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	public int getField(int p_174887_1_) {
		return 0;
	}

	public void setField(int p_174885_1_, int p_174885_2_) {
	}

	public int getFieldCount() {
		return 0;
	}

	public void clear() {
		for (int i = 0; i < this.inventoryContents.length; ++i) {
			this.inventoryContents[i] = null;
		}
	}
}
