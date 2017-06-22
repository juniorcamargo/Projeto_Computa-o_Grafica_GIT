package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main
        extends SimpleApplication
        implements ActionListener, PhysicsCollisionListener {

    public static void main(String[] args) {
        Main app = new Main();
        app.showSettings = true;
        app.setDisplayFps(false);
        app.setDisplayStatView(false);
        app.start();
    }
    
    public void Restart() {
        life = 1;
        left = right = jump=  false;
        for (Map.Entry<Geometry, String> aux : cubos.entrySet()) {
            Spatial s = aux.getKey();
            rootNode.detachChild(s);
            bulletAppState.getPhysicsSpace().remove(s);
        }
        cubos = new HashMap<>();
        this.restart();
    }
    
    public void Quit() {
        this.stop();
    }
    
    private void showMenu(){
        Object opcoes[] = {"Easy","Normal","Hard"};
        int op = JOptionPane.showOptionDialog(null,"Choose your difficult:","MENU",1,3, null, opcoes, null);
        System.out.println(op);
        switch(op){
            case 0:
                setLevel(level.EASY);
                break;
            case 1:
                setLevel(level.NORMAL);
                break;
            case 2:
                setLevel(level.HARD);
                break;
            default:
                showMenu();
        }
    }

    private void showGameOverMenu(){
        Object opcoes[] = {"Restart","Exit"};
        int op = JOptionPane.showOptionDialog(null,"Escolha uma dificuldade:","MENU",1,3, null, opcoes, null);
        switch(op){
            case 0:
                Restart();
                break;
            case 1:
                Quit();
                break;
            default:
                showGameOverMenu();
        }
    }

    private BulletAppState bulletAppState;
    private PlayerCameraNode player;
    private boolean left = false, right = false, jump = false;
    private Random r = null;
    private Map<Geometry, String> cubos = new HashMap<>();
    private int countBox = 0;
    private float countTpf = 0;
    private int cube_distance = 15;
    private int life = 1;

    private enum level {
        EASY(70), NORMAL(80), HARD(90);

        private int value;

        level(int value) {
            this.value = value;
        }
    }
    
    private level currentLevel;

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        createLigth();
        createFloor();
        createPlayer();
        initKeys();

        bulletAppState.setDebugEnabled(false);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        
        showMenu();
    }

    @Override
    public void simpleUpdate(float tpf) {
        player.upDateKeys(tpf, left, right, jump);
        for (Map.Entry<Geometry, String> aux : cubos.entrySet()) {
            String side = aux.getValue();
            float x = aux.getKey().getControl(RigidBodyControl.class).getPhysicsLocation().getX();
            float y = aux.getKey().getControl(RigidBodyControl.class).getPhysicsLocation().getY();
            float z = aux.getKey().getControl(RigidBodyControl.class).getPhysicsLocation().getZ();
            
            if(side == "right") {
                z = (float) (z - tpf*3);
            }
            else {
                z = (float) (z + tpf*3);
            }
            
            aux.getKey().getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(x, y, z));
            
            
        }
        if(countTpf < 10 - (getLevel()/10)) {
            countTpf += tpf;
        }
        else {
            countTpf = 0;
            r = new Random();
            if (r.nextInt(100) < getLevel()) {
                r = new Random();
                geraCubo(r.nextInt(100) >= 50 ? "left" : "right");
            }
        }
    }

    private void geraCubo(String startSide) {
        Geometry cube = createCubo(startSide);
        RigidBodyControl boxPhysicsNode = new RigidBodyControl(0);
        cube.addControl(boxPhysicsNode);
        bulletAppState.getPhysicsSpace().add(boxPhysicsNode);
        
        cubos.put(cube, startSide);
        rootNode.attachChild(cube);
        
    }
    
    private Geometry createCubo(String startSide) {
        countBox++;
        /* A colored lit cube. Needs light source! */
        Box boxMesh = new Box(0.5f, 0.5f, 0.5f);
        Geometry boxGeo = new Geometry("Box"+countBox, boxMesh);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        boxMat.setBoolean("UseMaterialColors", true);
        boxMat.setColor("Ambient", ColorRGBA.Red);
        boxMat.setColor("Diffuse", ColorRGBA.Red);
        boxGeo.setMaterial(boxMat);
        
        int Y = r.nextInt(8)-4;
        int Z = startSide == "right" ? cube_distance: -cube_distance;
        
        boxGeo.setLocalTranslation(0, Y, Z);
        
        return boxGeo;
    }
    
    private int getLevel() {
        return currentLevel.value;
    }
    
    private void setLevel(level l) {
        currentLevel = l;
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private void createLigth() {
        DirectionalLight l1 = new DirectionalLight();
        l1.setDirection(new Vector3f(1, -0.7f, 0));
        rootNode.addLight(l1);

        DirectionalLight l2 = new DirectionalLight();
        l2.setDirection(new Vector3f(-1, 0, 0));
        rootNode.addLight(l2);

        DirectionalLight l3 = new DirectionalLight();
        l3.setDirection(new Vector3f(0, 0, -1.0f));
        rootNode.addLight(l3);

        DirectionalLight l4 = new DirectionalLight();
        l4.setDirection(new Vector3f(0, 0, 1.0f));
        rootNode.addLight(l4);

        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);

        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);
    }

    private void createPlayer() {
        player = new PlayerCameraNode("player", assetManager, bulletAppState, cam);
        rootNode.attachChild(player);
        flyCam.setEnabled(false);
    }

    private void initKeys() {
        inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

        inputManager.addListener(this, "CharLeft", "CharRight");
        inputManager.addListener(this, "Jump");

    }

    @Override
    public void onAction(String binding, boolean value, float tpf) {
        switch (binding) {
            case "CharLeft":
                if (value) {
                    left = true;
                } else {
                    left = false;
                }
                break;
            case "CharRight":
                if (value) {
                    right = true;
                } else {
                    right = false;
                }
                break;
            case "Jump":
                if (value) {
                    jump = true;
                } else {
                    jump = false;
                }
                break;
        }
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if(event.getNodeA().getName().equals("player") || event.getNodeB().getName().equals("player")){
        
            if(event.getNodeA().getName().contains("Box")){
                Spatial s = event.getNodeA();
                rootNode.detachChild(s);
                bulletAppState.getPhysicsSpace().remove(s);
                life--;
                if(life <= 0)
                    showGameOverMenu();
            }
            else
            if(event.getNodeB().getName().contains("Box")){
                Spatial s = event.getNodeB();
                rootNode.detachChild(s);
                bulletAppState.getPhysicsSpace().remove(s);
                life--;
                if(life <= 0)
                    showGameOverMenu();

            }
        }
    }

    private void createFloor() {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Textures/texture1.jpg"));
        Box floorBox = new Box(140, 0.25f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(material);
        floorGeometry.setLocalTranslation(0, -5, 0);
        floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
        bulletAppState.getPhysicsSpace().add(floorGeometry);
    }
}
