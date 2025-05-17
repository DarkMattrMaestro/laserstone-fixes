package com.darkmattrmaestro.laserstone_fixes;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntArray;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.SaveLocation;
import finalforeach.cosmicreach.ui.debug.DebugInfo;
import finalforeach.cosmicreach.ui.debug.DebugIntItem;
import finalforeach.cosmicreach.ui.debug.DebugLongItem;
import finalforeach.cosmicreach.ui.debug.DebugVec3Item;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;


public class LaserstoneFixes {
    public static final Logger LOGGER = LoggerFactory.getLogger("LaserstoneFixes Mod");
}
