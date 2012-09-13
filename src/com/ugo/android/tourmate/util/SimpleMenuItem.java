package com.ugo.android.tourmate.util;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * A <em>really</em> dumb implementation of the {@link MenuItem} interface, that's only useful for
 * our old-actionbar purposes. See <code>com.android.internal.view.menu.MenuItemImpl</code> in
 * AOSP for a more complete implementation.
 */
public class SimpleMenuItem implements MenuItem {

    private SimpleMenu menu;

    private final int id;
    private final int order;
    private CharSequence title;
    private CharSequence titleCondensed;
    private Drawable iconDrawable;
    private int iconResId = 0;
    private boolean enabled = true;

    public SimpleMenuItem(SimpleMenu menu, int id, int order, CharSequence title) {
        this.menu = menu;
        this.id = id;
        this.order = order;
        this.title = title;
    }

    public View getActionView() {
        return null;
    }

    public char getAlphabeticShortcut() {
        return 0;
    }

    public int getGroupId() {
        return 0;
    }

    public Drawable getIcon() {
        if (iconDrawable != null) {
            return iconDrawable;
        }

        if (iconResId != 0) {
            return menu.getResources().getDrawable(iconResId);
        }

        return null;
    }

    public Intent getIntent() {
        return null;
    }

    public int getItemId() {
        return id;
    }

    public ContextMenu.ContextMenuInfo getMenuInfo() {
        return null;
    }

   public char getNumericShortcut() {
    return 0;
}

    public int getOrder() {
        return order;
    }

    public SubMenu getSubMenu() {
        return null;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getTitleCondensed() {
        return titleCondensed != null ? titleCondensed : title;
    }

    // No-op operations. We use no-ops to allow inflation from menu XML.

    public boolean hasSubMenu() {
        return false;
    }

    public boolean isCheckable() {
        return false;
    }

    public boolean isChecked() {
        return false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isVisible() {
        return true;
    }

    public MenuItem setActionView(int i) {
        // Noop
        return this;
    }

    public MenuItem setActionView(View view) {
        // Noop
        return this;
    }

    public MenuItem setAlphabeticShortcut(char c) {
        // Noop
        return this;
    }

    public MenuItem setCheckable(boolean b) {
        // Noop
        return this;
    }

    public MenuItem setChecked(boolean b) {
        // Noop
        return this;
    }

    public MenuItem setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public MenuItem setIcon(Drawable icon) {
	        iconResId = 0;
	        iconDrawable = icon;
	        return this;
	    }

    public MenuItem setIcon(int iconResId) {
        this.iconDrawable = null;
        this.iconResId = iconResId;
        return this;
    }

    public MenuItem setIntent(Intent intent) {
        // Noop
        return this;
    }

    public MenuItem setNumericShortcut(char c) {
        // Noop
        return this;
    }

    public MenuItem setOnMenuItemClickListener(
            OnMenuItemClickListener onMenuItemClickListener) {
        // Noop
        return this;
    }

    public MenuItem setShortcut(char c, char c1) {
        // Noop
        return this;
    }

    public void setShowAsAction(int i) {
        // Noop
    }

    public MenuItem setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    public MenuItem setTitle(int titleRes) {
        return setTitle(menu.getContext().getString(titleRes));
    }

    public MenuItem setTitleCondensed(CharSequence title) {
        this.titleCondensed = title;
        return this;
    }

    public MenuItem setVisible(boolean b) {
        // Noop
        return this;
    }

}

