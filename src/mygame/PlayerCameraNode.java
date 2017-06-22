/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl;
import java.util.Random;

public class PlayerCameraNode extends Node {

    private final BetterCharacterControl physicsCharacter;
    private final AnimControl animationControl;
    private final AnimChannel animationChannel;
    private Vector3f walkDirection = new Vector3f(0, 0, 0);
    private Vector3f viewDirection = new Vector3f(0, 0, 0);
    private Vector3f jumpDirection = new Vector3f(0, 0, 0);
    private float airTime;
    private Node oto;
    private String currentDir = "right";
    private String setJump = "jump";
    private int speed = 3000;
    private float currentZ = 0;
    private float max_distance = 2;


    public PlayerCameraNode(String name, AssetManager assetManager, BulletAppState bulletAppState, Camera cam) {
        super(name);

        oto = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
        oto.setLocalTranslation(0, 5, 0);
        scale(0.25f);
        setLocalTranslation(0, 10, 0);
        attachChild(oto);

        physicsCharacter = new BetterCharacterControl(1, 2.5f, 16f);
        addControl(physicsCharacter);

        bulletAppState.getPhysicsSpace().add(physicsCharacter);

        animationControl = oto.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();

        CameraNode camNode = new CameraNode("CamNode", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setLocalTranslation(new Vector3f(-50, 8, 0));
        camNode.lookAt(this.getLocalTranslation(), Vector3f.UNIT_Y);

        this.attachChild(camNode);
    }

    public Vector3f getWalkDirection() {
        return walkDirection;
    }

    public void setWalkDirection(Vector3f walkDirection) {
        this.walkDirection = walkDirection;
    }

    public Vector3f getViewDirection() {
        return viewDirection;
    }

    public void setViewDirection(Vector3f viewDirection) {
        this.viewDirection = viewDirection;
    }

    public Vector3f getJumpDirection() {
        return jumpDirection;
    }

    public void setjumpDirection(Vector3f jumpDirection) {
        this.jumpDirection = jumpDirection;
    }

    void upDateAnimationPlayer() {

        if (walkDirection.length() == 0) {
            if (!"stand".equals(animationChannel.getAnimationName())) {
                animationChannel.setAnim("stand", 1f);
            }
        } else {
            if (airTime > .3f) {
                if (!"stand".equals(animationChannel.getAnimationName())) {
                    animationChannel.setAnim("stand");
                }
            } else if (!"Walk".equals(animationChannel.getAnimationName())) {
                animationChannel.setAnim("Walk", 0.005f);
            }
        }
    }

    void upDateKeys(float tpf, boolean left, boolean right, boolean jump) {
        
        Vector3f camDir = getWorldRotation().mult(Vector3f.UNIT_Z);
        Vector3f ashuar = getWorldRotation().mult(Vector3f.UNIT_X);

        viewDirection.set(camDir);
        walkDirection.set(0, 0, 0);
        jumpDirection.set(0, 0, 0);

        if (left && currentZ > -max_distance) {
            walkDirection.addLocal(camDir.mult(-speed*tpf));
            currentZ -= tpf;
            if (!currentDir.equals("left")) {
                oto.rotate(0, FastMath.PI, 0);
                currentDir = "left";
            }
        } else if (right && currentZ < max_distance) {
            walkDirection.addLocal(camDir.mult(speed*tpf));
            currentZ += tpf;
            if (!currentDir.equals("right")) {
                oto.rotate(0, FastMath.PI, 0);
                currentDir = "right";
            }
        }
        if (jump) {
            physicsCharacter.jump();
        }

        physicsCharacter.setWalkDirection(walkDirection);
        physicsCharacter.setViewDirection(viewDirection);

        upDateAnimationPlayer();
    }
}
