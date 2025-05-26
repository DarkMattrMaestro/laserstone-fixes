package com.darkmattrmaestro.laserstone_fixes.configs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.darkmattrmaestro.laserstone_fixes.LaserstoneFixesConfig;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.KeybindsMenu;
import finalforeach.cosmicreach.gamestates.MainMenu;
import finalforeach.cosmicreach.gamestates.PauseMenu;
import finalforeach.cosmicreach.lang.Lang;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.settings.*;
import finalforeach.cosmicreach.settings.types.IntSetting;
import finalforeach.cosmicreach.ui.actions.AlignXAction;
import finalforeach.cosmicreach.ui.actions.AlignYAction;
import finalforeach.cosmicreach.ui.widgets.CRButton;
import finalforeach.cosmicreach.ui.widgets.CRSlider;
import org.quiltmc.config.api.values.TrackedValue;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LaserstoneFixesOptionMenu extends GameState {
    private final String collisionBlockOrderingSliderStr = "Laser Collision Check Order: "; // TODO: Use Lang.get()
    private final NumberFormat intFormat = new DecimalFormat("#");
    private final NumberFormat percentFormat = Lang.getPercentFormatter();
    private GameState previousState;

    private CRSlider createSettingsCRSlider(final INumberSetting setting, final String prefix, float min, float max, float stepSize, final NumberFormat valueTextFormat) {
        CRSlider slider = new CRSlider((String)null, min, max, stepSize, false) {
            protected void onChangeEvent(ChangeListener.ChangeEvent event) {
                float currentValue = this.getValue();
                setting.setValue(currentValue);
                String formattedValue;
                if (valueTextFormat == null) {
                    if (setting instanceof IntSetting) {
                        formattedValue = "" + (int)currentValue;
                    } else {
                        formattedValue = "" + currentValue;
                    }
                } else {
                    formattedValue = valueTextFormat.format((double)currentValue);
                }

                this.setText(prefix + formattedValue);
            }
        };
        slider.setWidth(250.0F);
        slider.setValue(setting.getValueAsFloat());
        return slider;
    }

    private CRSlider createSettingsCREnumSlider(final TrackedValue<Integer> setting, List<String> settingEnum, final String prefix) {
        CRSlider slider = new CRSlider((String)null, 0, settingEnum.size()-1, 1, false) {
            protected void onChangeEvent(ChangeListener.ChangeEvent event) {
                float currentValue = this.getValue();
                setting.setValue((int)currentValue);
                String formattedValue = settingEnum.get((int)currentValue);

                this.setText(prefix + formattedValue);
            }
        };
        slider.setWidth(500.0F);
        slider.setValue(setting.value());
        return slider;
    }

    public LaserstoneFixesOptionMenu(GameState previousState) {
        this.previousState = previousState;
    }

    public void create() {
        super.create();

        CRSlider CollisionBlockOrderingSlider = this.createSettingsCREnumSlider(LaserstoneFixesConfig.INSTANCE.collisionOrderMethod, LaserstoneFixesSettings.COLLISION_ORDER_METHOD_ENUM, this.collisionBlockOrderingSliderStr);

        CRButton doneButton = new CRButton(Lang.get("doneButton")) {
            public void onClick() {
                super.onClick();
//                this.returnToPrevious();
            }
        };

        Table table = new Table();
        table.setFillParent(true);
        this.stage.addActor(table);
        table.add().height(50.0F).expand();
        table.row();
        table.add().expand();
        table.add(CollisionBlockOrderingSlider).width(500.0F).height(50.0F).top().padTop(4.0F).padBottom(4.0F);
        table.row();
        table.add().expand();
        table.add(doneButton).width(250.0F).height(50.0F).top().padRight(12.0F).padTop(4.0F).padBottom(4.0F);
        table.add().expand();
        table.row();
        table.add().expand();
    }

    private void returnToPrevious() {
        if (this.previousState instanceof MainMenu) {
            switchToGameState(new MainMenu());
//        } else if (this.previousState instanceof PauseMenu) {
//            switchToGameState(new PauseMenu(((PauseMenu)this.previousState).cursorCaught));
        } else {
            switchToGameState(this.previousState);
        }

    }

    public void onSwitchTo() {
        super.onSwitchTo();
        Gdx.input.setInputProcessor(this.stage);
    }

    public void switchAwayTo(GameState gameState) {
        super.switchAwayTo(gameState);
        Gdx.input.setInputProcessor((InputProcessor)null);
    }

    public void render() {
        super.render();
        this.stage.act();
        if (Gdx.input.isKeyJustPressed(111)) {
            this.returnToPrevious();
        }

        ScreenUtils.clear(0.145F, 0.078F, 0.153F, 1.0F, true);
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        Gdx.gl.glCullFace(1028);
        this.stage.draw();
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glDepthFunc(519);
        this.drawUIElements();
    }
}
