package com.imac.FlashMe;

import java.nio.Buffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.imac.VuforiaApp.SampleApplicationSession;
import com.imac.VuforiaApp.utils.CubeShaders;
import com.imac.VuforiaApp.utils.SampleUtils;
import com.imac.VuforiaApp.utils.Texture;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerResult;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;


public class GameRenderer implements GLSurfaceView.Renderer {

	GameActivity mActivity;
	SampleApplicationSession vuforiaAppSession;
	
	private Vector<Texture> mTextures;

    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;
	
	public boolean mIsActive = false;
    
    public GameRenderer(GameActivity activity, SampleApplicationSession session) {
            mActivity = activity;
            vuforiaAppSession = session;
        }
    
	@Override
	public void onDrawFrame(GL10 arg0) {
		if (!mIsActive)
            return;
        renderFrame();
	}
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		vuforiaAppSession.onSurfaceChanged(width, height);
	}
	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		initRendering();
		vuforiaAppSession.onSurfaceCreated();
	}
    
	void renderFrame() {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		State state = Renderer.getInstance().begin();
		Renderer.getInstance().drawVideoBackground();
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON) {
        	GLES20.glFrontFace(GLES20.GL_CW);
        }
        else {
        	GLES20.glFrontFace(GLES20.GL_CCW);
        }
        
        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            // Get the trackable:
            TrackableResult trackableResult = state.getTrackableResult(tIdx);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(
                trackableResult.getPose()).getData();
            
            // Choose the texture based on the target name:
            int textureIndex = 0;
            
            // Check the type of the trackable:
            assert (trackableResult.getType() == MarkerTracker.getClassType());
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();
            
            textureIndex = marker.getMarkerId();
            
            assert (textureIndex < mTextures.size());
            Texture thisTexture = mTextures.get(textureIndex);
            
            // Select which model to draw:
            Buffer vertices = null;
            Buffer normals = null;
            Buffer indices = null;
            Buffer texCoords = null;
            int numIndices = 0;
            
            if (marker.getMarkerId() == 0)
            {
            	System.out.println("il y a un marqueur !");
            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        Renderer.getInstance().end();
	}
	
    void initRendering() {
    	GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
//    	for (Texture t : mTextures)
//        {
//            GLES20.glGenTextures(1, t.mTextureID, 0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
//                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
//                GLES20.GL_UNSIGNED_BYTE, t.mData);
//        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
        CubeShaders.CUBE_MESH_VERTEX_SHADER,
        CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");
    }

}