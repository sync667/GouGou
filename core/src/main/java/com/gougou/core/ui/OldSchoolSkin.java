package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Unified old-school MMORPG skin.
 *
 * UI drawables: Kenney Pixel Theme (CC0) — https://github.com/czyzby/gdx-skins
 * Fonts: MedievalSharp (SIL OFL) & Metamorphous (SIL OFL) via gdx-freetype
 */
public class OldSchoolSkin extends Skin {

    // ── Palette ───────────────────────────────────────────────────────────
    public static final Color BG          = new Color(0.04f, 0.02f, 0.02f, 1.00f);
    public static final Color PANEL       = new Color(0.11f, 0.07f, 0.04f, 0.96f);
    public static final Color PANEL_DARK  = new Color(0.07f, 0.04f, 0.02f, 0.97f);
    public static final Color GOLD        = new Color(0.78f, 0.62f, 0.20f, 1.00f);
    public static final Color GOLD_DARK   = new Color(0.45f, 0.32f, 0.08f, 1.00f);
    public static final Color PARCHMENT   = new Color(0.91f, 0.88f, 0.72f, 1.00f);
    public static final Color GOLD_TEXT   = new Color(0.95f, 0.80f, 0.28f, 1.00f);
    public static final Color GOLD_BRIGHT = new Color(1.00f, 0.93f, 0.45f, 1.00f);
    public static final Color BTN_UP      = new Color(0.36f, 0.27f, 0.11f, 1.00f);
    public static final Color BTN_OVER    = new Color(0.55f, 0.42f, 0.14f, 1.00f);
    public static final Color BTN_DOWN    = new Color(0.20f, 0.14f, 0.05f, 1.00f);
    public static final Color FIELD_BG    = new Color(0.13f, 0.09f, 0.05f, 0.93f);
    public static final Color DISABLED    = new Color(0.25f, 0.20f, 0.12f, 0.60f);
    public static final Color HP_COLOR    = new Color(0.80f, 0.12f, 0.12f, 1.00f);
    public static final Color MP_COLOR    = new Color(0.12f, 0.28f, 0.80f, 1.00f);
    public static final Color XP_COLOR    = new Color(0.55f, 0.35f, 0.06f, 1.00f);

    public OldSchoolSkin() {
        // Load kenney-pixel atlas (CC0 pixel-art UI drawables)
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/kenney-skin.atlas"));
        addRegions(atlas);

        // 1×1 white pixel for solid fills
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        add("white-pixel", new Texture(pix));
        pix.dispose();

        buildFonts();
        buildDrawables();
        buildStyles();
    }

    // ── Font generation ───────────────────────────────────────────────────

    private void buildFonts() {
        // Title: MedievalSharp — dramatic gothic style
        FreeTypeFontGenerator medieval =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/MedievalSharp.ttf"));

        add("title-font",    generateFont(medieval, 52, true));
        add("subtitle-font", generateFont(medieval, 28, false));
        medieval.dispose();

        // Body / UI: Metamorphous — elegant old-school serif
        FreeTypeFontGenerator meta =
                new FreeTypeFontGenerator(Gdx.files.internal("fonts/Metamorphous-Regular.ttf"));

        add("default-font", generateFont(meta, 22, false));
        add("small-font",   generateFont(meta, 17, false));
        add("hud-font",     generateFont(meta, 19, false));
        meta.dispose();
    }

    private BitmapFont generateFont(FreeTypeFontGenerator gen, int size, boolean shadow) {
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.color = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        if (shadow) {
            p.shadowOffsetX = 2;
            p.shadowOffsetY = 2;
            p.shadowColor = new Color(0f, 0f, 0f, 0.55f);
        }
        return gen.generateFont(p);
    }

    // ── Tinted drawables ──────────────────────────────────────────────────

    private void buildDrawables() {
        // Panels
        add("panel-bg",      newDrawable("outline-press", PANEL));
        add("panel-dark",    newDrawable("outline-press", PANEL_DARK));
        add("panel-gold",    newDrawable("outline",       GOLD_DARK));

        // Buttons
        add("btn-up",        newDrawable("base",       BTN_UP));
        add("btn-over",      newDrawable("base",       BTN_OVER));
        add("btn-down",      newDrawable("base-press", BTN_DOWN));
        add("btn-disabled",  newDrawable("base",       DISABLED));

        // Text fields
        add("field-normal",  newDrawable("field", FIELD_BG));
        add("field-focused", newDrawable("field", new Color(0.20f, 0.14f, 0.07f, 0.96f)));

        // Select box
        add("sel-up",        newDrawable("select",       BTN_UP));
        add("sel-over",      newDrawable("select",       BTN_OVER));
        add("sel-down",      newDrawable("select-press", BTN_DOWN));
        add("sel-list-bg",   newDrawable("select-list",  PANEL_DARK));

        // Checkboxes
        add("cb-off",        newDrawable("check-off", BTN_UP));
        add("cb-on",         newDrawable("check-on",  GOLD));
        add("cb-over",       newDrawable("check-off", BTN_OVER));

        // Sliders
        add("slider-track",  newDrawable("slider",   GOLD_DARK));
        add("knob-up",       newDrawable("knob",     GOLD));
        add("knob-over",     newDrawable("knob",     GOLD_BRIGHT));

        // Misc
        add("selection-bg",  newDrawable("dot",    new Color(0.45f, 0.30f, 0.06f, 0.65f)));
        add("cursor-draw",   newDrawable("cursor", GOLD));
        add("scroll-h",      newDrawable("scroll",   GOLD));
        add("scroll-v-draw", newDrawable("scroll-v", GOLD));
        add("scroll-h-bg",   newDrawable("scroll",   GOLD_DARK));
        add("scroll-v-bg",   newDrawable("scroll-v", GOLD_DARK));
    }

    // ── Widget styles ─────────────────────────────────────────────────────

    private void buildStyles() {
        BitmapFont defFont  = get("default-font", BitmapFont.class);
        BitmapFont smlFont  = get("small-font",   BitmapFont.class);
        BitmapFont titFont  = get("title-font",   BitmapFont.class);
        BitmapFont subFont  = get("subtitle-font",BitmapFont.class);

        // ── Labels ────────────────────────────────────────────────────────
        add("default",  new Label.LabelStyle(defFont, PARCHMENT));
        add("title",    new Label.LabelStyle(titFont, GOLD_TEXT));
        add("subtitle", new Label.LabelStyle(subFont, GOLD_TEXT));
        add("small",    new Label.LabelStyle(smlFont, PARCHMENT));
        add("gold",     new Label.LabelStyle(defFont, GOLD_TEXT));

        // ── TextButton ────────────────────────────────────────────────────
        TextButton.TextButtonStyle btn = new TextButton.TextButtonStyle();
        btn.font           = defFont;
        btn.fontColor      = PARCHMENT;
        btn.overFontColor  = GOLD_BRIGHT;
        btn.downFontColor  = GOLD_DARK;
        btn.up             = getDrawable("btn-up");
        btn.over           = getDrawable("btn-over");
        btn.down           = getDrawable("btn-down");
        btn.disabled       = getDrawable("btn-disabled");
        add("default", btn);

        // ── TextField ─────────────────────────────────────────────────────
        TextField.TextFieldStyle tf = new TextField.TextFieldStyle();
        tf.font              = defFont;
        tf.fontColor         = PARCHMENT;
        tf.background        = getDrawable("field-normal");
        tf.focusedBackground = getDrawable("field-focused");
        tf.cursor            = getDrawable("cursor-draw");
        tf.selection         = getDrawable("selection-bg");
        add("default", tf);

        // ── SelectBox ─────────────────────────────────────────────────────
        List.ListStyle ls = new List.ListStyle();
        ls.font                = defFont;
        ls.fontColorSelected   = GOLD_BRIGHT;
        ls.fontColorUnselected = PARCHMENT;
        ls.selection           = getDrawable("selection-bg");

        ScrollPane.ScrollPaneStyle selScroll = new ScrollPane.ScrollPaneStyle();
        selScroll.background = getDrawable("sel-list-bg");

        SelectBox.SelectBoxStyle sb = new SelectBox.SelectBoxStyle();
        sb.font          = defFont;
        sb.fontColor     = PARCHMENT;
        sb.overFontColor = GOLD_BRIGHT;
        sb.background     = getDrawable("sel-up");
        sb.backgroundOver = getDrawable("sel-over");
        sb.backgroundOpen = getDrawable("sel-down");
        sb.listStyle      = ls;
        sb.scrollStyle    = selScroll;
        add("default", sb);

        // ── CheckBox ──────────────────────────────────────────────────────
        CheckBox.CheckBoxStyle cb = new CheckBox.CheckBoxStyle();
        cb.font          = defFont;
        cb.fontColor     = PARCHMENT;
        cb.overFontColor = GOLD_BRIGHT;
        cb.checkboxOn    = getDrawable("cb-on");
        cb.checkboxOff   = getDrawable("cb-off");
        cb.checkboxOver  = getDrawable("cb-over");
        add("default", cb);

        // ── Slider ────────────────────────────────────────────────────────
        Slider.SliderStyle sl = new Slider.SliderStyle();
        sl.background = getDrawable("slider-track");
        sl.knob       = getDrawable("knob-up");
        sl.knobOver   = getDrawable("knob-over");
        add("default-horizontal", sl);

        // ── ScrollPane ────────────────────────────────────────────────────
        ScrollPane.ScrollPaneStyle sp = new ScrollPane.ScrollPaneStyle();
        sp.background  = getDrawable("panel-dark");
        sp.hScrollKnob = getDrawable("scroll-h");
        sp.vScrollKnob = getDrawable("scroll-v-draw");
        sp.hScroll     = getDrawable("scroll-h-bg");
        sp.vScroll     = getDrawable("scroll-v-bg");
        add("default", sp);

        // ── Window ────────────────────────────────────────────────────────
        Window.WindowStyle win = new Window.WindowStyle();
        win.titleFont      = subFont;
        win.titleFontColor = GOLD_TEXT;
        win.background     = getDrawable("panel-bg");
        add("default", win);
    }

    /** Utility: gold horizontal separator drawable (1px tall). */
    public Drawable goldSeparator() {
        return newDrawable("dot", GOLD);
    }

    /** Utility: panel background drawable. */
    public Drawable panelDrawable() {
        return getDrawable("panel-bg");
    }
}
