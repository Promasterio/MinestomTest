package me.promasterio.com.events;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;

import static me.promasterio.com.commands.GiveCommand.MAX_STACK_SIZE;

public class InventoryEvents {

    public static final int MAX_CLIENT_STACK = 99;

    public static class InventoryStackHandler {

        public static void hook(GlobalEventHandler node) {
            node.addListener(InventoryPreClickEvent.class, event -> {

                int slot = event.getSlot();
                if (slot < 0 || slot > 45 || slot == 36) return;

                if (!(event.getInventory() instanceof PlayerInventory inv)) return;

                Click click = event.getClick();

                ItemStack cursor = inv.getCursorItem();
                ItemStack slotItem = event.getClickedItem();

                int cursorMaxStack = cursor.getTag(MAX_STACK_SIZE);
                int slotMaxStack = slotItem.getTag(MAX_STACK_SIZE);

                int cursorAmount = cursor.amount();
                int slotAmount = slotItem.amount();

                boolean cursorEmpty = cursor.isAir();
                boolean slotEmpty = slotItem.isAir();
                if (cursorEmpty && slotEmpty) return;

                boolean sameItem = areSameItems(cursor, slotItem);

                if (slotEmpty) {
                    if (click instanceof Click.Left || click instanceof Click.LeftShift) {
                        event.setCancelled(true);
                        inv.setItemStack(slot, cursor);
                        inv.setCursorItem(ItemStack.AIR);
                    } else if (click instanceof Click.Right || click instanceof Click.RightShift) {
                        event.setCancelled(true);
                        inv.setItemStack(slot, cursor.withAmount(1));
                        inv.setCursorItem(cursor.withAmount(cursorAmount - 1));
                    } else if (click instanceof Click.Double) {
                        event.setCancelled(true);
                        int totalNeeded = cursorMaxStack - cursorAmount;
                        if (totalNeeded <= 0) return;
                        for (int index = 0; index < inv.getSize(); index++) {
                            ItemStack currentSlot = inv.getItemStack(index);
                            if (!areSameItems(currentSlot, cursor)) continue;
                            int currentSlotAmount = currentSlot.amount();
                            if (currentSlotAmount <= 0) continue;
                            int toMove = Math.min(totalNeeded, currentSlotAmount);
                            int remaining = currentSlotAmount - toMove;
                            inv.setItemStack(index, remaining > 0 ? currentSlot.withAmount(remaining) : ItemStack.AIR);
                            cursorAmount += toMove;
                            totalNeeded -= toMove;
                            if (totalNeeded <= 0) break;
                        }
                        inv.setCursorItem(cursor.withAmount(cursorAmount));
                    }
                } else if (sameItem) {
                    if (click instanceof Click.Left) {
                        event.setCancelled(true);
                        if (slotAmount >= slotMaxStack) return;
                        int space = slotMaxStack - slotAmount;
                        int toMove = Math.min(cursorAmount, space);
                        inv.setItemStack(slot, slotItem.withAmount(slotAmount + toMove));
                        inv.setCursorItem(cursor.withAmount(cursorAmount - toMove));
                    } else if (click instanceof Click.Right) {
                        event.setCancelled(true);
                        if (slotAmount >= slotMaxStack) return;
                        inv.setItemStack(slot, slotItem.withAmount(slotAmount + 1));
                        inv.setCursorItem(cursor.withAmount(cursorAmount - 1));
                    }
                } else if (click instanceof Click.LeftShift || click instanceof Click.RightShift) {
                    event.setCancelled(true);
                    if (cursor.isAir()) {
                        int index, limit;
                        if (slot < 9) {
                            index = 9;
                            limit = 35;
                        }
                        else {
                            index = 0;
                            limit = 9;
                        }
                        for (; index < limit; index++) {
                            var currentSlot = inv.getItemStack(index);
                            if (currentSlot.isAir()) {
                                inv.setItemStack(slot, ItemStack.AIR);
                                inv.setItemStack(index, slotItem);
                                return;
                            }
                            else if (areSameItems(currentSlot, slotItem)) {
                                int currentSlotAmount = currentSlot.amount();
                                int space = slotMaxStack - currentSlotAmount;
                                int toMove = Math.max(space, slotAmount);
                                inv.setItemStack(index, slotItem.withAmount(slotAmount + toMove));
                                slotAmount -= toMove;
                                if (slotAmount <= 0) {
                                    inv.setItemStack(slot, ItemStack.AIR);
                                    return;
                                }
                            }
                        }
                        inv.setItemStack(slot, slotItem.withAmount(slotAmount));
                    }
                }
            });
        }

        private static boolean areSameItems(ItemStack a, ItemStack b) {
            return a.withAmount(1).equals(b.withAmount(1));
        }
    }
}
