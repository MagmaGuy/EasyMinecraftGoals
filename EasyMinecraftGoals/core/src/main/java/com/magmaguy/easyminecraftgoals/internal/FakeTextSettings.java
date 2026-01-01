package com.magmaguy.easyminecraftgoals.internal;

import org.bukkit.Color;
import org.bukkit.entity.Display;

/**
 * Configuration settings for FakeText.
 * These settings are applied to TextDisplay entities for Java Edition players.
 * ArmorStand fallback (for Bedrock players) ignores most of these settings.
 */
public class FakeTextSettings {
    private String text = "";
    private Color backgroundColor = null; // null = transparent
    private int backgroundArgb = 0x40000000; // Default semi-transparent black
    private boolean hasBackgroundColor = false;
    private byte textOpacity = (byte) -1; // -1 = fully opaque
    private Display.Billboard billboard = Display.Billboard.CENTER;
    private FakeText.TextAlignment alignment = FakeText.TextAlignment.CENTER;
    private boolean shadow = false;
    private boolean seeThrough = false;
    private int lineWidth = 200;
    private float viewRange = 1.0f;
    private float scale = 1.0f;

    public FakeTextSettings() {
    }

    // Copy constructor
    public FakeTextSettings(FakeTextSettings other) {
        this.text = other.text;
        this.backgroundColor = other.backgroundColor;
        this.backgroundArgb = other.backgroundArgb;
        this.hasBackgroundColor = other.hasBackgroundColor;
        this.textOpacity = other.textOpacity;
        this.billboard = other.billboard;
        this.alignment = other.alignment;
        this.shadow = other.shadow;
        this.seeThrough = other.seeThrough;
        this.lineWidth = other.lineWidth;
        this.viewRange = other.viewRange;
        this.scale = other.scale;
    }

    public String getText() {
        return text;
    }

    public FakeTextSettings setText(String text) {
        this.text = text;
        return this;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public FakeTextSettings setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.hasBackgroundColor = true;
        return this;
    }

    public int getBackgroundArgb() {
        return backgroundArgb;
    }

    public FakeTextSettings setBackgroundArgb(int backgroundArgb) {
        this.backgroundArgb = backgroundArgb;
        this.hasBackgroundColor = true;
        return this;
    }

    public boolean hasBackgroundColor() {
        return hasBackgroundColor;
    }

    public byte getTextOpacity() {
        return textOpacity;
    }

    public FakeTextSettings setTextOpacity(byte textOpacity) {
        this.textOpacity = textOpacity;
        return this;
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    public FakeTextSettings setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    public FakeText.TextAlignment getAlignment() {
        return alignment;
    }

    public FakeTextSettings setAlignment(FakeText.TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public FakeTextSettings setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public boolean isSeeThrough() {
        return seeThrough;
    }

    public FakeTextSettings setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        return this;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public FakeTextSettings setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public float getViewRange() {
        return viewRange;
    }

    public FakeTextSettings setViewRange(float viewRange) {
        this.viewRange = viewRange;
        return this;
    }

    public float getScale() {
        return scale;
    }

    public FakeTextSettings setScale(float scale) {
        this.scale = scale;
        return this;
    }
}
