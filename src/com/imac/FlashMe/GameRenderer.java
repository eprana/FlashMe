package com.imac.FlashMe;

import java.nio.Buffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.imac.RenderingObjects.LogoObject;
import com.imac.RenderingObjects.PictoObject;
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
import android.opengl.Matrix;
import android.util.Log;


public class GameRenderer implements GLSurfaceView.Renderer {

	SampleApplicationSession vuforiaAppSession;
	GameActivity mActivity;
	
	public boolean mIsActive = false;
	
	private Vector<Texture> mTextures;

	// OpenGL ES 2.0 specific
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;
	
	// Constants:
    static private float logoScale = 25.0f;
    static private float logoRotate = 25.0f;
	
	private LogoObject logoObject = new LogoObject();
    private PictoObject pictoObject = new PictoObject();
    
    public GameRenderer(GameActivity activity, SampleApplicationSession session) {
            mActivity = activity;
            vuforiaAppSession = session;
        }
    
	// Called when the surface is created or recreated.
	@Override
	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
	
		// Call function to initialize rendering:
		initRendering();
		
		// Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
		vuforiaAppSession.onSurfaceCreated();
	}
	
	// Called when the surface changed size.
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		// Call Vuforia function to handle render surface size changes:
		vuforiaAppSession.onSurfaceChanged(width, height);
	}

	// Called to draw the current frame.
	@Override
	public void onDrawFrame(GL10 arg0) {
		if (!mIsActive)
            return;
		// Call our function to render content
        renderFrame();
	}
	
	void initRendering() {
	
		// Define clear color
    	GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);
		
		// Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
        CubeShaders.CUBE_MESH_VERTEX_SHADER,
        CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");
    }
    
	void renderFrame() {
		
		logoRotate += 2;
	
	    // Clear color and depth buffer
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		// Get the state from Vuforia and mark the beginning of a rendering section
		State state = Renderer.getInstance().begin();

		// Explicitly render the Video Background
		Renderer.getInstance().drawVideoBackground();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // We must detect if background reflection is active and adjust the culling direction.
        // If the reflection is active, this means the post matrix has been reflected as well,
        // therefore standard counter clockwise face culling will result in "inside out" models.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
		
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON) {
        	GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        }
        else {
        	GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
        }
        
        // Loop on found trackables
        for (int tracakbleId = 0; tracakbleId < state.getNumTrackableResults(); tracakbleId++) {
        	
            // Get the trackable
            TrackableResult trackableResult = state.getTrackableResult(tracakbleId);
            float[] modelViewMatrix = Tool.convertPose2GLMatrix(trackableResult.getPose()).getData();

			// Choose the texture based on the target index:
            int textureIndex = 0;
			
            // Check its type
            assert (trackableResult.getType() == MarkerTracker.getClassType());
            MarkerResult markerResult = (MarkerResult) (trackableResult);
            Marker marker = (Marker) markerResult.getTrackable();
			
        	textureIndex = marker.getMarkerId() + 6;
			assert (textureIndex < mTextures.size());
		            
            int markerId = marker.getMarkerId();
            String userId = marker.getName();
            
    		
            
            switch(markerId) {
            case 511:
            	// Poison
            	mActivity.updateCurrentUserPoints(-10);
            	textureIndex = 5;
            	break;
            case 510:
            	// Points
            	mActivity.updateCurrentUserPoints(10);
            	textureIndex = 4;
            	break;
            case 509 :
            	// Munitions
            	mActivity.updateMunitions(50);
            	textureIndex = 3;
            	break;
            case 508:
            	// Scourge
            	mActivity.updateGun(2);
            	textureIndex = 2;
            	break;
            case 507:
            	// Chain-Saw
            	mActivity.updateGun(1);
            	textureIndex = 1;
            	break;
            case 506:
            	// Gun
            	mActivity.updateGun(0);
            	textureIndex = 0;
            	break;
            default:
            	mActivity.updateGauge(markerId, userId);
            	break;
            }

            Log.d("########################", "Marker " + textureIndex + " detected from ");
            Texture thisTexture = mTextures.get(textureIndex);
            
			// Select which model to draw:
            Buffer vertices = null;
            Buffer normals = null;
            Buffer indices = null;
            Buffer texCoords = null;
            int numIndices = 0;
            int numVertices = 0;
            
            if(markerId >= 506) {
                vertices = pictoObject.getVertices();
                normals = pictoObject.getNormals();
                texCoords = pictoObject.getTexCoords();
                numIndices = pictoObject.getNumObjectIndex();
                numVertices = pictoObject.getNumObjectVertex();
            }else {
            	vertices = logoObject.getVertices();
                normals = logoObject.getNormals();
                texCoords = logoObject.getTexCoords();
                numIndices = logoObject.getNumObjectIndex();
                numVertices = logoObject.getNumObjectVertex();
            }
			
			float[] modelViewProjection = new float[16];
            
            Matrix.translateM(modelViewMatrix, 0, 0.f,
                0.f, 0.f);
            Matrix.scaleM(modelViewMatrix, 0, logoScale, logoScale, logoScale);
            Matrix.rotateM(modelViewMatrix, 0, logoRotate, 0.f, 1.0f, 0.f);
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
				
			GLES20.glUseProgram(shaderProgramID);
            
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, vertices);
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normals);
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoords);
            
            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);
			
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                thisTexture.mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
//            GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
//                GLES20.GL_UNSIGNED_SHORT, indices);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numVertices);
            
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
			
			SampleUtils.checkGLError("FrameMarkers render frame");		
       		
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        Renderer.getInstance().end();
	}
	
	public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
    }
}