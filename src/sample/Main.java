package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.util.concurrent.TimeUnit;

abstract class Zombie implements Serializable {
    private static final long serialVersionUID = 40L;
    private int health;
    private int x_pos;
    private int y_pos;
    private double speed;
    private int damage;
    private transient ImageView[] images;
    private transient Timeline setUpTime;
    private int frame;
    private Level level;
    private Boolean isDead, isActive = false;

    public Boolean getIsDead() {
        return isDead;
    }

    public void setDead(Boolean dead) {
        isDead = dead;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Timeline getSetUpTime() {
        return setUpTime;
    }

    public void setSetUpTime(Timeline setUpTime) {
        this.setUpTime = setUpTime;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public ImageView[] getImages() {
        return images;
    }
    public void setImages(ImageView[] images) {
        this.images = images;
    }
    public int getHealth() {
        return health;
    }
    public void hit(int h){
        setHealth(getHealth()-h);
    }
    public void setHealth(int health) {
        this.health = health;
        if(health<=0){
            die();
        }
    }
    public int getX() {
        return x_pos;
    }
    public void setX(int x_pos) {
        this.x_pos = x_pos;
    }
    public int getY() {
        return y_pos;
    }
    public void setY(int y_pos) {
        this.y_pos = y_pos;
    }
    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    public int getDamage() {
        return damage;
    }
    public void setDamage(int damage) {
        this.damage = damage;
    }
    public void die(){
        level.levelLayout.getChildren().remove(getImages()[getFrame()]);
        level.removeZombie(this);
        isDead = true;
    }
    abstract public void setupTimeLine();
    abstract public void load() throws Exception;
}
class BasicZombie extends Zombie{
    public BasicZombie(Level l, int y) throws Exception{
        setHealth(200);
        setDead(false);
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/BasicZombie/";
        this.setImages(new ImageView[52]);
        for(int i = 1; i<53; i++){
            this.getImages()[i-1] = new ImageView(new Image(new FileInputStream(s + i + ".png"),114,200,false,false));
            this.getImages()[i-1].setLayoutY(l.getLayoutGUI().getY(y)-30);
            this.getImages()[i-1].setLayoutX(1500);
        }
        Random r = new Random();
        this.setX(1500 + r.nextInt(200));
        this.setY(y);
        this.setFrame(r.nextInt(52));
        setLevel(l);
        l.levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void load() throws Exception{
        this.setImages(new ImageView[52]);
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/BasicZombie/";
        for(int i = 1; i<53; i++){
            this.getImages()[i-1] = new ImageView(new Image(new FileInputStream(s + i + ".png"),114,200,false,false));
            this.getImages()[i-1].setLayoutY(getLevel().getLayoutGUI().getY(getY())-30);
            this.getImages()[i-1].setLayoutX(getX());
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(50),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        if(getLevel().isPaused())
            getSetUpTime().pause();
        else
            getSetUpTime().play();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(getActive()){
                Plant np = getLevel().getNearestPlant(BasicZombie.this);
                if(BasicZombie.this.getIsDead()){
                    getSetUpTime().stop();
                }
                else if(np!=null && getLevel().getLayoutGUI().getX(np.getX())+80 >= getX() && getLevel().getLayoutGUI().getX(np.getX()) <= getX()){
                    np.setHealth(np.getHealth()-1);
                    int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                    getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                    setFrame(getFrame() + 3);
                    if (getFrame() > 51) {
                        setFrame(0);
                    }
                    if (i >= 0) {
                        getLevel().levelLayout.getChildren().add(i, getImages()[getFrame()]);
                    }
                    getImages()[getFrame()].setLayoutX(getX());
                }
                else {
                    int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                    getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                    setFrame(getFrame() + 1);
                    if (getFrame() > 51) {
                        setFrame(0);
                    }
                    if (i >= 0) {
                        getLevel().levelLayout.getChildren().add(i, getImages()[getFrame()]);
                    }
                    setX(getX() - 1);
                    if(getX()<0){
                        getLevel().isLawnMowerPresent(BasicZombie.this);
                    }
                    if(getX()<-50){
                        try{
                            getSetUpTime().stop();
                            getLevel().loseGame();
                        }catch (Exception e){}
                    }
                    getImages()[getFrame()].setLayoutX(getX());
                }
            }
        }
    }
}
class ConeZombie extends Zombie{
    public ConeZombie(Level l, int y) throws Exception{
        setHealth(400);
        setDead(false);
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/ConeZombie/";
        this.setImages(new ImageView[52]);
        for(int i = 1; i<53; i++){
            this.getImages()[i-1] = new ImageView(new Image(new FileInputStream(s + i + ".png"),114,200,false,false));
            this.getImages()[i-1].setLayoutY(l.getLayoutGUI().getY(y)-30);
            this.getImages()[i-1].setLayoutX(1500);
        }
        Random r = new Random();
        this.setX(1500 + r.nextInt(200));
        this.setY(y);
        this.setFrame(r.nextInt(52));
        setLevel(l);
        l.levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void load() throws Exception{
        this.setImages(new ImageView[52]);
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/ConeZombie/";
        for(int i = 1; i<53; i++){
            this.getImages()[i-1] = new ImageView(new Image(new FileInputStream(s + i + ".png"),114,200,false,false));
            this.getImages()[i-1].setLayoutY(getLevel().getLayoutGUI().getY(getY())-30);
            this.getImages()[i-1].setLayoutX(getX());
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(50),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(getActive()){
                Plant np = getLevel().getNearestPlant(ConeZombie.this);
                if(ConeZombie.this.getIsDead()){
                    getSetUpTime().stop();
                }
                else if(np!=null && getLevel().getLayoutGUI().getX(np.getX())+80 >= getX() && getLevel().getLayoutGUI().getX(np.getX()) <= getX()){
                    np.setHealth(np.getHealth()-1);
                    int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                    getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                    setFrame(getFrame() + 3);
                    if (getFrame() > 51) {
                        setFrame(0);
                    }
                    if (i >= 0) {
                        getLevel().levelLayout.getChildren().add(i, getImages()[getFrame()]);
                    }
                    getImages()[getFrame()].setLayoutX(getX());
                }
                else {
                    int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                    getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                    setFrame(getFrame() + 1);
                    if (getFrame() > 51) {
                        setFrame(0);
                    }
                    if (i >= 0) {
                        getLevel().levelLayout.getChildren().add(i, getImages()[getFrame()]);
                    }
                    setX(getX() - 1);
                    if(getX()<0){
                        getLevel().isLawnMowerPresent(ConeZombie.this);
                    }
                    if(getX()<-50){
                        try{
                            getSetUpTime().stop();
                            getLevel().loseGame();
                        }catch (Exception e){}
                    }
                    getImages()[getFrame()].setLayoutX(getX());
                }
            }
        }
    }
}
class BucketZombie extends Zombie{
    public BucketZombie(Level l, int y) throws Exception{
        setHealth(600);
        setDead(false);
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/BucketZombie/";
        this.setImages(new ImageView[52]);
        for(int i = 1; i<53; i++){
            this.getImages()[i-1] = new ImageView(new Image(new FileInputStream(s + i + ".png"),114,200,false,false));
            this.getImages()[i-1].setLayoutY(l.getLayoutGUI().getY(y)-30);
            this.getImages()[i-1].setLayoutX(1500);
        }
        Random r = new Random();
        this.setX(1500 + r.nextInt(200));
        this.setY(y);
        this.setFrame(r.nextInt(52));
        setLevel(l);
        l.levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void load() throws Exception{
        this.setImages(new ImageView[52]);
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/BucketZombie/";
        for(int i = 1; i<53; i++){
            this.getImages()[i-1] = new ImageView(new Image(new FileInputStream(s + i + ".png"),114,200,false,false));
            this.getImages()[i-1].setLayoutY(getLevel().getLayoutGUI().getY(getY())-30);
            this.getImages()[i-1].setLayoutX(getX());
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(50),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();

    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(getActive()){
                Plant np = getLevel().getNearestPlant(BucketZombie.this);
                if(BucketZombie.this.getIsDead()){
                    getSetUpTime().stop();
                }
                else if(np!=null && getLevel().getLayoutGUI().getX(np.getX())+80 >= getX() && getLevel().getLayoutGUI().getX(np.getX()) <= getX()){
                    np.setHealth(np.getHealth()-1);
                    int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                    getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                    setFrame(getFrame() + 3);
                    if (getFrame() > 51) {
                        setFrame(0);
                    }
                    if (i >= 0) {
                        getLevel().levelLayout.getChildren().add(i, getImages()[getFrame()]);
                    }
                    getImages()[getFrame()].setLayoutX(getX());
                }
                else {
                    int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                    getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                    setFrame(getFrame() + 1);
                    if (getFrame() > 51) {
                        setFrame(0);
                    }
                    if (i >= 0) {
                        getLevel().levelLayout.getChildren().add(i, getImages()[getFrame()]);
                    }
                    setX(getX() - 1);
                    if(getX()<0){
                        getLevel().isLawnMowerPresent(BucketZombie.this);
                    }
                    if(getX()<-50){
                        try{
                            getSetUpTime().stop();
                            getLevel().loseGame();
                        }catch (Exception e){}
                    }
                    getImages()[getFrame()].setLayoutX(getX());
                }
            }
        }
    }
}

abstract class Plant implements Serializable {
    private static final long serialVersionUID = 40L;
    private int price;
    private int health = 200;
    private int x_pos;
    private int y_pos;
    private int frame;
    private Level level;
    private transient ImageView[] images;
    private transient Timeline setUpTime;
    private int countDown;

    public int getCountDown() {
        return countDown;
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public int getFrame() {
        return frame;
    }
    public void setFrame(int frame) {
        this.frame = frame;
    }


    public Timeline getSetUpTime() {
        return setUpTime;
    }

    public void setSetUpTime(Timeline setUpTime) {
        this.setUpTime = setUpTime;
    }

    public ImageView[] getImages() {
        return images;
    }
    public void setImages(ImageView[] images) {
        this.images = images;
    }

    public int getPrice() {
        return this.price;
    }
    public int getHealth() {
        return this.health;
    }
    public void setHealth(int Damage_threshold) {
        this.health = Damage_threshold;
        if(health<0){
            die();
        }
    }
    public void die(){
        setUpTime.stop();
        level.levelLayout.getChildren().remove(getImages()[getFrame()]);
        level.removePlant(this);
    }
    public boolean isPlantDead(){
        return ((this.health<=0)?(true):(false));
    }
    public int getX() {
        return this.x_pos;
    }
    public void setX(int x_pos) {
        this.x_pos = x_pos;
    }
    public void setY(int y_pos) {
        this.y_pos = y_pos;
    }
    public int getY() {
        return this.y_pos;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
    public abstract void load() throws Exception;
}
abstract class ShooterPlants extends Plant{
    public static final long serialVersionUID =41L;
    private int attackValue;

    public int getAttackValue() {
        return this.attackValue;
    }
}
class PeaShooter extends ShooterPlants{
    public static final long serialVersionUID =42L;
    public static final int PeaShooter_Price = 100;
    public static final int PeaShooter_health = 100;
    public static final int PeaShooter_Attack_Value = 25;

    public PeaShooter(int[] coOrdinates, Level p) throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/PeaShooter/";
        this.setImages(new ImageView[49]);
        for(int i = 0; i<49; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".gif"),150,150,false,false));
            this.getImages()[i].setLayoutX(p.getLayoutGUI().getX(coOrdinates[0]));
            this.getImages()[i].setLayoutY(p.getLayoutGUI().getY(coOrdinates[1]));
        }
        this.setX(coOrdinates[0]);
        this.setY(coOrdinates[1]);
        this.setFrame(0);
        setCountDown(49);
        setLevel(p);
        getLevel().addPlant(this);
        getLevel().levelLayout.getChildren().add(0,this.getImages()[0]);
        setupTimeLine();
    }
    public void load() throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/PeaShooter/";
        this.setImages(new ImageView[49]);
        for(int i = 0; i<49; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".gif"),150,150,false,false));
            this.getImages()[i].setLayoutX(getLevel().getLayoutGUI().getX(getX()));
            this.getImages()[i].setLayoutY(getLevel().getLayoutGUI().getY(getY()));
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public int shootpea(){
        return 0;
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(30),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
            getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
            setFrame(getFrame()+1);
            setCountDown(getCountDown()+1);
            if(getFrame()>48){
                setFrame(0);
            }
            if(getCountDown()%98==0 && getLevel().getNearestZombie(getY(), getLevel().getLayoutGUI().getX(getX()))!=null && getLevel().getNearestZombie(getY(), getLevel().getLayoutGUI().getX(getX())).getX()<1400){
                Pea pea = new Pea(getX(),getY(), getLevel());
            }
            if(i>=0)
                getLevel().levelLayout.getChildren().add(i,getImages()[getFrame()]);
        }
    }
}
//class Repeater extends ShooterPlants{
//    public static final long serialVersionUID =43L;
//    public final int Repeater_Price = 200;
//    public final int Repeater_health = 10;
//    public final int Repeater_Attack_Value = 20;
//
//    public int shootpea(){
//        return 0;
//    }
//}
abstract class IdlePlants extends Plant{
    public static final long serialVersionUID =44L;
}
class SunFlower extends IdlePlants{
    public static final long serialVersionUID =44L;
    public static final int SunFlower_Price = 50;
    public static final int SunFlower_Health =10;
    public final int Sun=25;
    public final int Time_until_next_sun = 10;

    public SunFlower(int[] coOrdinates, Level p) throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/SunFlower/";
        this.setImages(new ImageView[55]);
        for(int i = 0; i<55; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".gif"),150,150,false,false));
            this.getImages()[i].setLayoutX(p.getLayoutGUI().getX(coOrdinates[0]));
            this.getImages()[i].setLayoutY(p.getLayoutGUI().getY(coOrdinates[1]));
        }
        this.setX(coOrdinates[0]);
        this.setY(coOrdinates[1]);
        this.setFrame(0);

        setLevel(p);
        getLevel().addPlant(this);
        getLevel().levelLayout.getChildren().add(0,this.getImages()[0]);
        setupTimeLine();

    }
    public void load() throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/SunFlower/";
        this.setImages(new ImageView[55]);
        for(int i = 0; i<55; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".gif"),150,150,false,false));
            this.getImages()[i].setLayoutX(getLevel().getLayoutGUI().getX(getX()));
            this.getImages()[i].setLayoutY(getLevel().getLayoutGUI().getY(getY()));
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(30),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
            getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
            setFrame(getFrame()+1);
            setCountDown(getCountDown()+1);
            if(getFrame()>54){
                setFrame(0);
            }
            if(getCountDown()%900==0){
                Sun s = new Sun(getLevel(), SunFlower.this);
            }
            if(i>=0)
                getLevel().levelLayout.getChildren().add(i,getImages()[getFrame()]);
        }
    }
}
class WallNut extends IdlePlants{
    public static final long serialVersionUID =45L;
    public static final int WallNut_Price = 50;
    public final int WallNut_Health = 1000;
    public WallNut(int[] coOrdinates, Level p) throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/WallNut/";
        this.setImages(new ImageView[3]);
        this.setHealth(WallNut_Health);
        for(int i = 0; i<3; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".png"),150,150,false,false));
            this.getImages()[i].setLayoutX(p.getLayoutGUI().getX(coOrdinates[0])-20);
            this.getImages()[i].setLayoutY(p.getLayoutGUI().getY(coOrdinates[1]));
        }
        this.setX(coOrdinates[0]);
        this.setY(coOrdinates[1]);
        this.setFrame(0);
        setLevel(p);
        getLevel().addPlant(this);
        getLevel().levelLayout.getChildren().add(0,this.getImages()[0]);
        setupTimeLine();
    }
    public void load() throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/WallNut/";
        this.setImages(new ImageView[3]);
        for(int i = 0; i<3; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".png"),150,150,false,false));
            this.getImages()[i].setLayoutX(getLevel().getLayoutGUI().getX(getX()-20));
            this.getImages()[i].setLayoutY(getLevel().getLayoutGUI().getY(getY()));
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(30),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(getHealth()<=333 && getFrame()==1|| getHealth()<=667 && getFrame()==0){
                int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                setFrame(getFrame()+1);
                getLevel().levelLayout.getChildren().add(i,getImages()[getFrame()]);
            }
        }
    }
}
abstract class ExplosivePlants extends Plant{
    public static final long serialVersionUID =46L;
    private boolean Has_exploded=false;

    public void explode(){
        Has_exploded=true;
    }
    public boolean Has_exploded(){
        return this.Has_exploded;
    }
//    public int Countdown_to_Explosion(){
//
//    }
//    public void decrement_counter(){
//
//    }
}
class CherryBomb extends ExplosivePlants{
    public static final long serialVersionUID =47L;
    public static final int Cherry_Price=150;
    public final int Spawn_time=2;
    public CherryBomb(int[] coOrdinates, Level p) throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/CherryBomb/";
        this.setImages(new ImageView[31]);
        for(int i = 0; i<31; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".gif"),150,150,false,false));
            this.getImages()[i].setLayoutX(p.getLayoutGUI().getX(coOrdinates[0]));
            this.getImages()[i].setLayoutY(p.getLayoutGUI().getY(coOrdinates[1]) + 30);
        }

        this.setX(coOrdinates[0]);
        this.setY(coOrdinates[1]);
        this.setFrame(0);

        setLevel(p);
        getLevel().addPlant(this);
        getLevel().levelLayout.getChildren().add(0,this.getImages()[0]);
        setupTimeLine();

    }
    public void load() throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/CherryBomb/";
        this.setImages(new ImageView[31]);
        for(int i = 0; i<31; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".gif"),150,150,false,false));
            this.getImages()[i].setLayoutX(getLevel().getLayoutGUI().getX(getX()));
            this.getImages()[i].setLayoutY(getLevel().getLayoutGUI().getY(getY()));
        }
        getLevel().levelLayout.getChildren().add(this.getImages()[0]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(40),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
            getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
            setFrame(getFrame()+1);
            setCountDown(getCountDown()+1);
            if(i>=0)
                getLevel().levelLayout.getChildren().add(i,getImages()[getFrame()]);
            if(getFrame()==18){
                getLevel().blast(CherryBomb.this);
            }
            if(getFrame()==30){
                die();
            }
        }
    }
}
class PotatoMine extends ExplosivePlants{
    public static final long serialVersionUID =48L;
    public static final int Potato_Price=25;
    public static final int Potato_health=100;
    public final int Spawn_time=10;
    private boolean is_Active=false;
    private long startTime;
    public boolean isActive(){
        return is_Active;
    }
    public PotatoMine(int[] coOrdinates, Level p) throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/PotatoMine/";
        this.setImages(new ImageView[3]);
        setHealth(Potato_health);
        for(int i = 0; i<2; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".png"),150,150,false,false));
            this.getImages()[i].setLayoutX(p.getLayoutGUI().getX(coOrdinates[0])-10);
            this.getImages()[i].setLayoutY(p.getLayoutGUI().getY(coOrdinates[1]));
        }
        this.getImages()[2] = new ImageView(new Image(new FileInputStream(s + 2 + ".png"),250,250,false,false));
        this.getImages()[2].setLayoutX(p.getLayoutGUI().getX(coOrdinates[0])-70);
        this.getImages()[2].setLayoutY(p.getLayoutGUI().getY(coOrdinates[1])-50);
        this.setX(coOrdinates[0]);
        this.setY(coOrdinates[1]);
        this.setFrame(0);
        setLevel(p);
        getLevel().addPlant(this);
        getLevel().levelLayout.getChildren().add(0,this.getImages()[0]);
        setupTimeLine();

    }
    public void load() throws Exception{
        String s = "/Users/abhinav/IdeaProjects/PVZ2/src/sprites/PotatoMine/";
        this.setImages(new ImageView[3]);
        for(int i = 0; i<2; i++){
            this.getImages()[i] = new ImageView(new Image(new FileInputStream(s + i + ".png"),150,150,false,false));
            this.getImages()[i].setLayoutX(getLevel().getLayoutGUI().getX(getX()));
            this.getImages()[i].setLayoutY(getLevel().getLayoutGUI().getY(getY()));
        }
        this.getImages()[2] = new ImageView(new Image(new FileInputStream(s + 2 + ".png"),250,250,false,false));
        this.getImages()[2].setLayoutX(getLevel().getLayoutGUI().getX(getX())-70);
        this.getImages()[2].setLayoutY(getLevel().getLayoutGUI().getY(getY())-50);
        getLevel().levelLayout.getChildren().add(this.getImages()[getFrame()]);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(30),new TimeHandler());
        this.setSetUpTime(new Timeline(newKey));
        this.getSetUpTime().setCycleCount(Timeline.INDEFINITE);
        startTime = System.currentTimeMillis();
        this.getSetUpTime().play();
        if(getLevel().isPaused())
            getSetUpTime().pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(getCountDown()>=333 && getFrame()==0){
                int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                setFrame(getFrame()+1);
                getLevel().levelLayout.getChildren().add(i,getImages()[getFrame()]);
            }
            else if(getFrame()==1 && getLevel().getNearestZombie(getY(),getLevel().getLayoutGUI().getX(getX()))!=null && getLevel().getLayoutGUI().getX(getX())+80 >= getLevel().getNearestZombie(getY(),getLevel().getLayoutGUI().getX(getX())).getX() && getLevel().getLayoutGUI().getX(getX()) <= getLevel().getNearestZombie(getY(),getLevel().getLayoutGUI().getX(getX())).getX()){
                getLevel().blast(PotatoMine.this);
                int i = getLevel().levelLayout.getChildren().indexOf(getImages()[getFrame()]);
                getLevel().levelLayout.getChildren().remove(getImages()[getFrame()]);
                setFrame(getFrame()+1);
                getLevel().levelLayout.getChildren().add(i,getImages()[getFrame()]);
                setCountDown(0);
            }
            else if(getCountDown()>=100 && getFrame()==2){
                die();
            }
            setCountDown(getCountDown()+1);
        }
    }
}

class Layout implements Serializable {
    private static final long serialVersionUID = 40L;
    private int[] layoutX;
    private int[] layoutY;
    private transient ArrayList<Label>[] lawnLayout;
    private int[] coOrdinates;
    private Level level;
    public int[] getLayoutX() {
        return layoutX;
    }

    public int[] getLayoutY() {
        return layoutY;
    }

    public ArrayList<Label>[] getLawnLayout() {
        return lawnLayout;
    }

    public int[] getCoOrdinates() {
        return coOrdinates;
    }

    public Layout(int levelNo, Level level){
        layoutX = new int[]{50,200,350,500,650,800,950,1090,1250};
        layoutY = new int[]{75,205,335,475,610};
        coOrdinates = new int[]{-1,-1};
        lawnLayout = new ArrayList[5];
        for(int i = 0; i<5; i++){
            lawnLayout[i] = new ArrayList<Label>();
        }
        this.level = level;
        if(levelNo==1){
            for(int i = 0; i<9; i++) {
                int j = i;
                Label l = new Label();
                lawnLayout[2].add(l);
                l.setPrefSize(157, 130);
                l.setLayoutX(layoutX[i] - 13);
                l.setLayoutY(layoutY[2] + 30);
                l.setStyle("-fx-background-color: white;-fx-background-color: white;-fx-opacity: 0.0");
                l.setOnMouseDragEntered(e -> {
                    l.setOpacity(0.5);
                    coOrdinates[0] = j;
                    coOrdinates[1] = 2;
                });
                l.setOnMouseDragExited(e -> {
                    l.setOpacity(0.0);
                    coOrdinates[0] = -1;
                    coOrdinates[1] = -1;
                });
                level.levelLayout.getChildren().add(l);
            }
        }
        else if(levelNo==2 || levelNo==3){
            for(int k = 1; k<4; k++) {
                int m = k;
                for (int i = 0; i < 9; i++) {
                    int j = i;
                    Label l = new Label();
                    lawnLayout[k].add(l);
                    l.setPrefSize(157, 130);
                    l.setLayoutX(layoutX[i] - 13);
                    l.setLayoutY(layoutY[k] + 30);
                    l.setStyle("-fx-background-color: white;-fx-background-color: white;-fx-opacity: 0.0");
                    l.setOnMouseDragEntered(e -> {
                        l.setOpacity(0.5);
                        coOrdinates[0] = j;
                        coOrdinates[1] = m;
                    });
                    l.setOnMouseDragExited(e -> {
                        l.setOpacity(0.0);
                        coOrdinates[0] = -1;
                        coOrdinates[1] = -1;
                    });
                    level.levelLayout.getChildren().add(l);
                }
            }
        }
        else{
            for(int k = 0; k<5; k++) {
                int m = k;
                for (int i = 0; i < 9; i++) {
                    int j = i;
                    Label l = new Label();
                    lawnLayout[k].add(l);
                    l.setPrefSize(157, 130);
                    l.setLayoutX(layoutX[i] - 13);
                    l.setLayoutY(layoutY[k] + 30);
                    l.setStyle("-fx-background-color: white;-fx-background-color: white;-fx-opacity: 0.0");
                    l.setOnMouseDragEntered(e -> {
                        l.setOpacity(0.5);
                        coOrdinates[0] = j;
                        coOrdinates[1] = m;
                    });
                    l.setOnMouseDragExited(e -> {
                        l.setOpacity(0.0);
                        coOrdinates[0] = -1;
                        coOrdinates[1] = -1;
                    });
                    level.levelLayout.getChildren().add(l);
                }
            }
        }
    }
    public int getX(int i){
        return layoutX[i];
    }
    public int getY(int i){
        return layoutY[i];
    }
    public boolean canPlace(){
        return (coOrdinates[0]!=-1 && coOrdinates[1]!=-1 && level.getPlants()[coOrdinates[1]][coOrdinates[0]]==null);
    }
    public boolean canShovel(){
        return (coOrdinates[0]!=-1 && coOrdinates[1]!=-1 && level.getPlants()[coOrdinates[1]][coOrdinates[0]]!=null);
    }
}

class Level implements Serializable {
    private static final long serialVersionUID = 40L;
    private int level;
    private int sun = 50;
    private transient Scene scene;
    transient Pane levelLayout;
    private transient Button pauseMenu_button, backToGame_button, saveGame_button, restartLevel_button, mainMenu_button;
    private transient ArrayList<SeedPacket> seedPackets;
    private int[] rechargeTimers;
    private transient Label plantContainer, totalSunLabel;
    private transient ProgressBar progressBar;
    private transient Timeline setUpTime;
    private long lastAdded = 0;
    private long startTime = 0;
    private boolean isPaused = false;
    private ArrayList<Zombie>[] zombies;
    private int[] zombieLoc;
    private int zombieCounter = 0;
    private int[][] wave;
    private Plant[][] plants;
    private ArrayList<Pea> peas;
    private ArrayList<Sun> suns;
    private LawnMower[] lawnMowers;
    private Layout layoutGUI;
    private int wavesGenerated = 0;

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public int getLevelno() {
        return level;
    }

    public Pane getLevelLayout() {
        return levelLayout;
    }

    public void setLevelLayout(Pane levelLayout) {
        this.levelLayout = levelLayout;
    }

    public Plant[][] getPlants() {
        return plants;
    }

    public Layout getLayoutGUI() {
        return layoutGUI;
    }

    public void setLayoutGUI(Layout layoutGUI) {
        this.layoutGUI = layoutGUI;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public Level(int level) throws Exception {
        startTime = System.currentTimeMillis();
        this.level = level;
        zombies = new ArrayList[5];
        plants = new Plant[5][9];
        for(int i = 0; i<5; i++){
            zombies[i] = new ArrayList<Zombie>();
        }
        peas = new ArrayList<Pea>();
        suns = new ArrayList<Sun>();
        lawnMowers = new LawnMower[5];
        seedPackets = new ArrayList<SeedPacket>();
        rechargeTimers = new int[5];
        pauseMenu_button = new Button();
        pauseMenu_button.setPrefSize(166,52);
        pauseMenu_button.setLayoutX(1280);
        pauseMenu_button.setLayoutY(0);
        pauseMenu_button.getStyleClass().add("pauseMenu-button");
        ImageView inGameMenu = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/inGameMenu.png"),435,506,false,false));
        pauseMenu_button.setOnAction(e -> {
            levelLayout.getChildren().addAll(inGameMenu,backToGame_button,saveGame_button,restartLevel_button,mainMenu_button);
            pause();
        });
        plantContainer = new Label();
        plantContainer.setPrefSize(653, 111);
        plantContainer.getStyleClass().add("plant-container");
        totalSunLabel = new Label(sun + "");
        totalSunLabel.setPrefSize(70,20);
        totalSunLabel.setLayoutX(12);
        totalSunLabel.setLayoutY(75);
        totalSunLabel.setAlignment(Pos.CENTER);
        totalSunLabel.getStyleClass().add("sun-text");

        inGameMenu.setLayoutX(508);
        inGameMenu.setLayoutY(147);
        backToGame_button = new Button();
        backToGame_button.setPrefSize(357,84);
        backToGame_button.setLayoutX(548);
        backToGame_button.setLayoutY(556);
        backToGame_button.getStyleClass().add("backToGame-button");
        backToGame_button.setOnAction(e -> {
            levelLayout.getChildren().removeAll(inGameMenu,backToGame_button,saveGame_button,restartLevel_button,mainMenu_button);
            play();
        });
        saveGame_button = new Button();
        saveGame_button.setPrefSize(218,42);
        saveGame_button.setLayoutX(618);
        saveGame_button.setLayoutY(292);
        saveGame_button.getStyleClass().add("saveGame-button");
        saveGame_button.setOnAction(e -> {
            try{
                Main.serialize(Level.this);
            }catch (Exception e1){}
        });
        restartLevel_button = new Button();
        restartLevel_button.setPrefSize(219,40);
        restartLevel_button.setLayoutX(618);
        restartLevel_button.setLayoutY(351);
        restartLevel_button.getStyleClass().add("restartLevel-button");
        restartLevel_button.setOnAction(e -> {
            try{
                Main.setLevel(level, true);
            }catch (Exception e1){}
        });
        mainMenu_button = new Button();
        mainMenu_button.setPrefSize(219,41);
        mainMenu_button.setLayoutX(619);
        mainMenu_button.setLayoutY(411);
        mainMenu_button.getStyleClass().add("mainMenu-button");
        mainMenu_button.setOnAction(e -> {
            Main.getStage().setScene(Main.getMainMenuScene());
        });
        ImageView progress = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/progressBar.png"),178,40,false,false));
        progress.setLayoutX(1000);
        progress.setLayoutY(765);
        progressBar = new ProgressBar();
        progressBar.setProgress(zombieCounter);
        progressBar.setPrefSize(164, 20);
        progressBar.setLayoutX(1007);
        progressBar.setLayoutY(772);
        progressBar.setStyle("-fx-text-box-border: black;-fx-control-inner-background: black;-fx-accent: LawnGreen");
        levelLayout = new Pane();
        layoutGUI = new Layout(level, this);
        SeedPacket peaShooter_seedPacket = new SeedPacket("PeaShooter", this);
        levelLayout.getChildren().addAll(plantContainer, peaShooter_seedPacket, totalSunLabel,progress);
        seedPackets.add(peaShooter_seedPacket);
        Shovel sh = new Shovel(this);
        LawnMower l2 = new LawnMower(this,2);
        levelLayout.getChildren().addAll(pauseMenu_button, progressBar, sh);
        scene = new Scene(levelLayout, 1500, 800);
        Random r = new Random();
        if(level==1){
            zombieLoc = new int[6];
            for(int i = 0; i<6; i++){
                addZombie(2,"Basic");
            }
            scene.getStylesheets().addAll("/sample/level1Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==2){
            zombieLoc = new int[16];
            LawnMower l1 = new LawnMower(this,1);
            LawnMower l3 = new LawnMower(this,3);
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            int ii[] = new int[3];
            for(int i = 0; i<16; i++){
                int rr = r.nextInt(3)+1;
                if(i<10 || i>12){
                    zombieLoc[i] = rr;
                    ii[rr-1]++;
                }
                else {
                    zombieLoc[i] = i - 9;
                    ii[i-10]++;
                }
            }
            for(int i = 1; i<4; i++){
                System.out.println(ii[i-1]);
                for(int j = 0; j<ii[i-1]; j++)
                    addZombie(i, "Basic");
            }
            scene.getStylesheets().addAll("/sample/level2Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==3){
            zombieLoc = new int[20];
            LawnMower l1 = new LawnMower(this,1);
            LawnMower l3 = new LawnMower(this,3);
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            SeedPacket cherryBomb_seedPacket = new SeedPacket("CherryBomb", this);
            levelLayout.getChildren().addAll(cherryBomb_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            seedPackets.add(cherryBomb_seedPacket);
            int ii[] = new int[3];
            for(int i = 0; i<20; i++){
                int rr = r.nextInt(3)+1;
                if(i<12 || i>14){
                    zombieLoc[i] = rr;
                    ii[rr-1]++;
                }
                else {
                    zombieLoc[i] = i - 11;
                    ii[i-12]++;
                }
            }
            for(int i = 1; i<4; i++){
                System.out.println(ii[i-1]);
                for(int j = 0; j<ii[i-1]; j++) {
                    switch (r.nextInt(4)){
                        case 0 : addZombie(i, "Cone"); break;
                        default: addZombie(i, "Basic");
                    }

                }
            }
            scene.getStylesheets().addAll("/sample/level2Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==4){
            zombieLoc = new int[30];
            LawnMower l0 = new LawnMower(this,0);
            LawnMower l1 = new LawnMower(this,1);
            LawnMower l3 = new LawnMower(this,3);
            LawnMower l4 = new LawnMower(this,4);
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            SeedPacket cherryBomb_seedPacket = new SeedPacket("CherryBomb", this);
            levelLayout.getChildren().addAll(cherryBomb_seedPacket);
            SeedPacket wallNut_seedPacket = new SeedPacket("WallNut", this);
            levelLayout.getChildren().addAll(wallNut_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            seedPackets.add(cherryBomb_seedPacket);
            seedPackets.add(wallNut_seedPacket);
            int ii[] = new int[5];
            for(int i = 0; i<30; i++){
                int rr = r.nextInt(5);
                if(i<20 || i>24){
                    zombieLoc[i] = rr;
                    ii[rr]++;
                }
                else {
                    zombieLoc[i] = i - 20;
                    ii[i-20]++;
                }
            }
            for(int i = 0; i<5; i++){
                System.out.println(ii[i]);
                for(int j = 0; j<ii[i]; j++)
                    switch (r.nextInt(13)){
                        case 0 :
                        case 1 :
                        case 2 : addZombie(i, "Cone"); break;
                        case 3 : addZombie(i, "Bucket"); break;
                        default: addZombie(i, "Basic");
                    }
            }
            scene.getStylesheets().addAll("/sample/level4Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==5){
            wave = new int[2][5];
            zombieLoc = new int[50];
            LawnMower l0 = new LawnMower(this,0);
            LawnMower l1 = new LawnMower(this,1);
            LawnMower l3 = new LawnMower(this,3);
            LawnMower l4 = new LawnMower(this,4);
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            SeedPacket cherryBomb_seedPacket = new SeedPacket("CherryBomb", this);
            levelLayout.getChildren().addAll(cherryBomb_seedPacket);
            SeedPacket wallNut_seedPacket = new SeedPacket("WallNut", this);
            levelLayout.getChildren().addAll(wallNut_seedPacket);
            SeedPacket potatoMine_seedPacket = new SeedPacket("PotatoMine", this);
            levelLayout.getChildren().addAll(potatoMine_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            seedPackets.add(cherryBomb_seedPacket);
            seedPackets.add(wallNut_seedPacket);
            seedPackets.add(potatoMine_seedPacket);
            int ii[] = new int[5];
            for(int i = 0; i<50; i++){
                int rr = r.nextInt(5);
                if(i>=19 && i<24){
                    zombieLoc[i] = i - 19;
                    ii[i-19]++;
                    wave[0][i-19]++;
                }
                else if(i>=24 && i<30){
                    zombieLoc[i] = rr;
                    ii[rr]++;
                    wave[0][rr]++;
                }
                else if(i>=39 && i<44){
                    zombieLoc[i] = i - 39;
                    ii[i-39]++;
                    wave[1][i-39]++;
                }
                else if(i>=44 && i<50){
                    zombieLoc[i] = rr;
                    ii[rr]++;
                    wave[1][rr]++;
                }
                else {
                    zombieLoc[i] = rr;
                    ii[rr]++;
                }
            }
            for(int i = 0; i<5; i++){
                System.out.println(ii[i]);
                for(int j = 0; j<ii[i]; j++)
                    switch (r.nextInt(13)){
                        case 0 :
                        case 1 :
                        case 2 : addZombie(i, "Cone"); break;
                        case 3 : addZombie(i, "Bucket"); break;
                        default: addZombie(i, "Basic");
                    }
            }
            scene.getStylesheets().addAll("/sample/level4Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        setupTimeLine();
    }
    public void load() throws Exception{
        seedPackets = new ArrayList<SeedPacket>();
        pauseMenu_button = new Button();
        pauseMenu_button.setPrefSize(166,52);
        pauseMenu_button.setLayoutX(1280);
        pauseMenu_button.setLayoutY(0);
        pauseMenu_button.getStyleClass().add("pauseMenu-button");
        ImageView inGameMenu = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/inGameMenu.png"),435,506,false,false));
        pauseMenu_button.setOnAction(e -> {
            levelLayout.getChildren().addAll(inGameMenu,backToGame_button,saveGame_button,restartLevel_button,mainMenu_button);
            pause();
        });
        plantContainer = new Label();
        plantContainer.setPrefSize(653, 111);
        plantContainer.getStyleClass().add("plant-container");
        totalSunLabel = new Label(sun + "");
        totalSunLabel.setPrefSize(70,20);
        totalSunLabel.setLayoutX(12);
        totalSunLabel.setLayoutY(75);
        totalSunLabel.setAlignment(Pos.CENTER);
        totalSunLabel.getStyleClass().add("sun-text");

        inGameMenu.setLayoutX(508);
        inGameMenu.setLayoutY(147);
        backToGame_button = new Button();
        backToGame_button.setPrefSize(357,84);
        backToGame_button.setLayoutX(548);
        backToGame_button.setLayoutY(556);
        backToGame_button.getStyleClass().add("backToGame-button");
        backToGame_button.setOnAction(e -> {
            levelLayout.getChildren().removeAll(inGameMenu,backToGame_button,saveGame_button,restartLevel_button,mainMenu_button);
            play();
        });
        saveGame_button = new Button();
        saveGame_button.setPrefSize(218,42);
        saveGame_button.setLayoutX(618);
        saveGame_button.setLayoutY(292);
        saveGame_button.getStyleClass().add("saveGame-button");
        saveGame_button.setOnAction(e -> {
            try{
                Main.serialize(Level.this);
            }catch (Exception e1){}
        });
        restartLevel_button = new Button();
        restartLevel_button.setPrefSize(219,40);
        restartLevel_button.setLayoutX(618);
        restartLevel_button.setLayoutY(351);
        restartLevel_button.getStyleClass().add("restartLevel-button");
        restartLevel_button.setOnAction(e -> {
            try{
                Main.setLevel(level, true);
            }catch (Exception e1){}
        });
        mainMenu_button = new Button();
        mainMenu_button.setPrefSize(219,41);
        mainMenu_button.setLayoutX(619);
        mainMenu_button.setLayoutY(411);
        mainMenu_button.getStyleClass().add("mainMenu-button");
        mainMenu_button.setOnAction(e -> {
            Main.getStage().setScene(Main.getMainMenuScene());
        });
        ImageView progress = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/progressBar.png"),178,40,false,false));
        progress.setLayoutX(1000);
        progress.setLayoutY(765);
        progressBar = new ProgressBar();
        progressBar.setProgress(zombieCounter);
        progressBar.setPrefSize(164, 20);
        progressBar.setLayoutX(1007);
        progressBar.setLayoutY(772);
        progressBar.setStyle("-fx-text-box-border: black;-fx-control-inner-background: black;-fx-accent: LawnGreen");
        levelLayout = new Pane();
        layoutGUI = new Layout(level, this);
        setupTimeLine();
        SeedPacket peaShooter_seedPacket = new SeedPacket("PeaShooter", this);
        levelLayout.getChildren().addAll(plantContainer, peaShooter_seedPacket, totalSunLabel, progress);
        Shovel sh = new Shovel(this);
        seedPackets.add(peaShooter_seedPacket);
        levelLayout.getChildren().addAll(pauseMenu_button, progressBar, sh);
        scene = new Scene(levelLayout, 1500, 800);
        if(level==1){
            scene.getStylesheets().addAll("/sample/level1Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==2){
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            scene.getStylesheets().addAll("/sample/level2Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==3){
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            SeedPacket cherryBomb_seedPacket = new SeedPacket("CherryBomb", this);
            levelLayout.getChildren().addAll(cherryBomb_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            seedPackets.add(cherryBomb_seedPacket);
            scene.getStylesheets().addAll("/sample/level2Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==4){
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            SeedPacket cherryBomb_seedPacket = new SeedPacket("CherryBomb", this);
            levelLayout.getChildren().addAll(cherryBomb_seedPacket);
            SeedPacket wallNut_seedPacket = new SeedPacket("WallNut", this);
            levelLayout.getChildren().addAll(wallNut_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            seedPackets.add(cherryBomb_seedPacket);
            seedPackets.add(wallNut_seedPacket);
            scene.getStylesheets().addAll("/sample/level4Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        else if(level==5){
            SeedPacket sunFlower_seedPacket = new SeedPacket("SunFlower", this);
            levelLayout.getChildren().addAll(sunFlower_seedPacket);
            SeedPacket cherryBomb_seedPacket = new SeedPacket("CherryBomb", this);
            levelLayout.getChildren().addAll(cherryBomb_seedPacket);
            SeedPacket wallNut_seedPacket = new SeedPacket("WallNut", this);
            levelLayout.getChildren().addAll(wallNut_seedPacket);
            SeedPacket potatoMine_seedPacket = new SeedPacket("PotatoMine", this);
            levelLayout.getChildren().addAll(potatoMine_seedPacket);
            seedPackets.add(sunFlower_seedPacket);
            seedPackets.add(cherryBomb_seedPacket);
            seedPackets.add(wallNut_seedPacket);
            seedPackets.add(potatoMine_seedPacket);
            scene.getStylesheets().addAll("/sample/level4Style.css","/sample/plantContainerStyle.css","/sample/inGameMenuStyle.css");
        }
        loadPlants();
        loadZombies();
        loadPeas();
        loadSuns();
        loadLawnMowers();
        levelLayout.getChildren().addAll(inGameMenu,backToGame_button,saveGame_button,restartLevel_button,mainMenu_button);
        pause();
    }
    public void addPlant(Plant p){
        this.plants[p.getY()][p.getX()] = p;
        if(p.getClass().getSimpleName().equals("PeaShooter")){
            rechargeTimers[0] = 10000;
        }
        else if(p.getClass().getSimpleName().equals("SunFlower")){
            rechargeTimers[1] = 10000;
        }
        else if(p.getClass().getSimpleName().equals("CherryBomb")){
            rechargeTimers[2] = 35000;
        }
        else if(p.getClass().getSimpleName().equals("WallNut")){
            rechargeTimers[3] = 20000;
        }
        else if(p.getClass().getSimpleName().equals("PotatoMine")){
            rechargeTimers[4] = 20000;
        }
    }
    public void rechargePlants(){
        for(int i = 0; i<5; i++){
            if(rechargeTimers[i]!=0){
                rechargeTimers[i]--;
                seedPackets.get(i).setDisable(true);
            }
            else if(seedPackets.size()>i && seedPackets.get(i).isDisabled()){
                seedPackets.get(i).setDisable(false);
            }
        }
    }
    public void disableSeedPackets(Boolean b){
        for(int i = 0; i<seedPackets.size(); i++){
            seedPackets.get(i).setDisable(b);
        }
    }
    public void pausePlants(){
        for(int i = 0; i<5; i++){
            for(int j = 0; j<9; j++){
                if(plants[i][j]!=null)
                    plants[i][j].getSetUpTime().pause();
            }
        }
    }
    public void loadPlants() throws Exception{
        for(int i = 0; i<5; i++){
            for(int j = 0; j<9; j++){
                if(plants[i][j]!=null)
                    plants[i][j].load();
            }
        }
    }
    public void playPlants(){
        for(int i = 0; i<5; i++){
            for(int j = 0; j<9; j++){
                if(plants[i][j]!=null)
                    plants[i][j].getSetUpTime().play();
            }
        }
    }
    public void pauseZombies(){
        for(int i = 0; i<5; i++){
            for(int j = 0; j<zombies[i].size(); j++){
                zombies[i].get(j).getSetUpTime().pause();
            }
        }
    }
    public void loadZombies() throws Exception{
        for(int i = 0; i<5; i++){
            for(int j = 0; j<zombies[i].size(); j++){
                zombies[i].get(j).load();
            }
        }
    }
    public void playZombies(){
        for(int i = 0; i<5; i++){
            for(int j = 0; j<zombies[i].size(); j++){
                zombies[i].get(j).getSetUpTime().play();
            }
        }
    }
    public void pausePeas(){
        for(int i = 0; i<peas.size(); i++){
            peas.get(i).getSetUpTime().pause();
        }
    }
    public void loadPeas() throws Exception{
        for(int i = 0; i<peas.size(); i++){
            peas.get(i).load();
        }
    }
    public void playPeas(){
        for(int i = 0; i<peas.size(); i++){
            peas.get(i).getSetUpTime().play();
        }
    }
    public void addPea(Pea p){
        peas.add(p);
    }
    public void removePea(Pea p){
        peas.remove(p);
        levelLayout.getChildren().remove(p.getImage());
    }
    public void pauseSuns(){
        for(int i = 0; i<suns.size(); i++){
            suns.get(i).getSetUpTime().pause();
        }
    }
    public void loadSuns() throws Exception{
        for(int i = 0; i<suns.size(); i++){
            suns.get(i).load();
        }
    }
    public void playSuns(){
        for(int i = 0; i<suns.size(); i++){
            suns.get(i).getSetUpTime().play();
        }
    }
    public void addLawnMower(LawnMower l){
        lawnMowers[l.getY()] = l;
    }
    public void removeLawnMower(LawnMower l){
        lawnMowers[l.getY()] = null;
    }
    public void pauseLawnMowers(){
        for(int i = 0; i<5; i++){
            if(lawnMowers[i]!=null)
                lawnMowers[i].getSetUpTime().pause();
        }
    }
    public void loadLawnMowers() throws Exception{
        for(int i = 0; i<5; i++){
            if(lawnMowers[i]!=null)
                lawnMowers[i].load();
        }
    }
    public void playLawnMowers(){
        for(int i = 0; i<5; i++){
            if(lawnMowers[i]!=null)
                lawnMowers[i].getSetUpTime().play();
        }
    }
    public Boolean isLawnMowerPresent(Zombie z){
        if(lawnMowers[z.getY()]!=null){
            lawnMowers[z.getY()].activate();
            return true;
        }
        return false;
    }
    public void addSun(Sun s){
        suns.add(s);
    }
    public void removeSun(Sun s){
        suns.remove(s);
    }
    public void pause(){
        isPaused = true;
        pauseMenu_button.setDisable(true);
        disableSeedPackets(true);
        setUpTime.pause();
        pausePlants();
        pauseZombies();
        pauseSuns();
        pausePeas();
        pauseLawnMowers();
    }
    public void play(){
        isPaused = false;
        pauseMenu_button.setDisable(false);
        disableSeedPackets(false);
        setUpTime.play();
        playPlants();
        playZombies();
        playSuns();
        playPeas();
        playLawnMowers();
    }
    public void winGame() throws Exception{
        setUpTime.stop();
        System.out.println("Won");
        Main.getStage().setScene(Main.getChooseLevelScene());
        if(getLevelno()==5)
            return;
        Main.getActiveUser().getLevelsUnlocked()[getLevelno()] = true;
        Main.getLevels()[getLevelno()-1] = null;
        Main.lockLevels();
    }
    public void loseGame() throws Exception{
        setUpTime.stop();
        Main.setLevel(level, true);
    }
    public void generateSun(){
        long curTime = System.currentTimeMillis();
        if(curTime>=lastAdded + 15000){
            lastAdded = curTime;
            Sun s = new Sun(this, null);
        }
    }
    public void generateZombies(){
        long curTime = System.currentTimeMillis();
        if(level==1){
            if(zombieCounter<4 && curTime-startTime>10000) {
                if (!getNearestZombie(2, -1).getActive()) {
                    getNearestZombie(2, -1).setActive(true);
                    zombieCounter++;
                }
            }
            else if(curTime-startTime>10000){
                if(wavesGenerated==0) {
                    getNearestZombie(2, -1).setActive(true);
                    zombies[2].get(1).setActive(true);
                    zombies[2].get(2).setActive(true);
                    wavesGenerated++;
                }
                if (getNearestZombie(2, -1) == null) {
                    try {
                        winGame();
                    }catch (Exception e){}
                }
            }
        }
        else if(level==2){
            Random r = new Random();
            if(zombieCounter<4 && curTime-startTime>10000){
                if (!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive()) {
                    setZombieActive(1);
                }
            }
            else if(zombieCounter<10 && curTime-startTime>10000) {
                if (!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive()) {
                    setZombieActive(2);
                }
            }
            else if(curTime-startTime>10000){
                if(wavesGenerated==0) {
                    if (!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive())
                        generateWave(++wavesGenerated);
                }
                if (getNearestZombie(1, -1) == null && getNearestZombie(2, -1) == null && getNearestZombie(3, -1) == null) {
                    try {
                        winGame();
                    }catch (Exception e){}
                }
            }
        }
        else if(level==3){
            Random r = new Random();
            //System.out.println(zombieCounter);
            if(zombieCounter==0 && curTime-startTime>10000){
                getNearestZombie(zombieLoc[0], -1).setActive(true);
                zombieCounter++;
            }
            else if(zombieCounter<2 && curTime-startTime>10000){
                if (!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive()) {
                    setZombieActive(1);
                }
            }
            else if(zombieCounter<12 && curTime-startTime>10000) {
                if (!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive()) {
                    setZombieActive(2);
                }
            }
            else if(curTime-startTime>10000){
                if(wavesGenerated==0) {
                    if (!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive())
                        generateWave(++wavesGenerated);
                }
                if (getNearestZombie(1, -1) == null && getNearestZombie(2, -1) == null && getNearestZombie(3, -1) == null) {
                    try {
                        winGame();
                    }catch (Exception e){}
                }
            }
        }
        else if(level==4){
            Random r = new Random();
            if(zombieCounter<5 && curTime-startTime>10000){
                if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive()) {
                    setZombieActive(1);
                }
            }
            else if(zombieCounter<20 && curTime-startTime>10000) {
                if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive()) {
                    setZombieActive(3);
                }
            }
            else if(curTime-startTime>10000){
                if(wavesGenerated==0) {
                    if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive())
                        generateWave(++wavesGenerated);
                }
                if (getNearestZombie(0, -1) == null && getNearestZombie(1, -1) == null && getNearestZombie(2, -1) == null && getNearestZombie(3, -1) == null && getNearestZombie(4, -1) == null) {
                    try {
                        winGame();
                    }catch (Exception e){}
                }
            }
        }
        else if(level==5){
            Random r = new Random();
            if(zombieCounter<1 && curTime-startTime>10000){
                if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive()) {
                    setZombieActive(1);
                }
            }
            else if(zombieCounter<19 && curTime-startTime>10000) {
                if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive()) {
                    setZombieActive(3);
                }
            }
            else if(zombieCounter<30 && curTime-startTime>10000) {
                if(wavesGenerated==0) {
                    if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive())
                        generateWave(++wavesGenerated);
                }
            }
            else if(zombieCounter<39 && curTime-startTime>10000) {
                if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive()) {
                    setZombieActive(3);
                }
            }
            else if(curTime-startTime>10000){
                if(wavesGenerated==1) {
                    if (!getNearestZombie(0, -1).getActive() &&!getNearestZombie(1, -1).getActive() && !getNearestZombie(2, -1).getActive() && !getNearestZombie(3, -1).getActive() && !getNearestZombie(4, -1).getActive())
                        generateWave(++wavesGenerated);
                }
                if (getNearestZombie(0, -1) == null && getNearestZombie(1, -1) == null && getNearestZombie(2, -1) == null && getNearestZombie(3, -1) == null && getNearestZombie(4, -1) == null) {
                    try {
                        winGame();
                    }catch (Exception e){}
                }
            }
        }
    }
    public void blast(CherryBomb c){
        for(int i = 0; i<3; i++){
            for(int j = 0; j<zombies[c.getY()+i-1].size(); j++){
                if(zombies[c.getY()+i-1].get(j).getX()>=getLayoutGUI().getX(c.getX())-200 && zombies[c.getY()+i-1].get(j).getX()<=getLayoutGUI().getX(c.getX())+200){
                    zombies[c.getY()+i-1].get(j).die();
                }
            }
        }
    }
    public void blast(PotatoMine p){
        getNearestZombie(p.getY(),layoutGUI.getX(p.getX())).die();
    }
    public void setZombieActive(int i){
        if(i==1){
            getNearestZombie(zombieLoc[zombieCounter], -1).setActive(true);
            zombieCounter++;
        }
        else if(i==2){
            getNearestZombie(zombieLoc[zombieCounter], -1).setActive(true);
            if(zombieLoc[zombieCounter]==zombieLoc[zombieCounter+1])
                zombies[zombieLoc[zombieCounter]].get(1).setActive(true);
            else
                getNearestZombie(zombieLoc[zombieCounter+1], 0).setActive(true);
            zombieCounter+=2;
        }
        else if(i==3){
            getNearestZombie(zombieLoc[zombieCounter], -1).setActive(true);
            if(zombieLoc[zombieCounter]==zombieLoc[zombieCounter+1])
                zombies[zombieLoc[zombieCounter]].get(1).setActive(true);
            else
                getNearestZombie(zombieLoc[zombieCounter+1], -1).setActive(true);
            if(zombieLoc[zombieCounter+1]==zombieLoc[zombieCounter+2] && zombieLoc[zombieCounter]!=zombieLoc[zombieCounter+2])
                zombies[zombieLoc[zombieCounter+1]].get(1).setActive(true);
            else if(zombieLoc[zombieCounter]==zombieLoc[zombieCounter+2]  && zombieLoc[zombieCounter+1]!=zombieLoc[zombieCounter+2])
                zombies[zombieLoc[zombieCounter]].get(1).setActive(true);
            else if(zombieLoc[zombieCounter]==zombieLoc[zombieCounter+2]  && zombieLoc[zombieCounter+1]==zombieLoc[zombieCounter+2])
                zombies[zombieLoc[zombieCounter]].get(2).setActive(true);
            else
                getNearestZombie(zombieLoc[zombieCounter+2], -1).setActive(true);
            zombieCounter+=3;
        }
    }
    public void generateWave(int w){
        if(wave==null) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < zombies[i].size(); j++) {
                    zombies[i].get(j).setActive(true);
                    zombieCounter++;
                }
            }
        }
        else{
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < wave[w-1][i]; j++) {
                    zombies[i].get(j).setActive(true);
                    zombieCounter++;
                }
            }
        }
    }
    public Zombie getNearestZombie(int n, int x){
        if(zombies[n].isEmpty()){
            return null;
        }
        if(x==-1)
            return zombies[n].get(0);
        int i = 0;
        for(; i<zombies[n].size(); i++){
            if(x<=zombies[n].get(i).getX())
                break;
        }
        if(i==zombies[n].size())
            return null;
        return zombies[n].get(i);
    }

    public Plant getNearestPlant(Zombie z){
        Plant p = null;
        for(int i = 8; i>=0; i--){
            if(plants[z.getY()][i]!=null){
                if(getLayoutGUI().getX(plants[z.getY()][i].getX())<z.getX()){
                    p = plants[z.getY()][i];
                    break;
                }
            }
        }
        return p;
    }
    public void addZombie(int n, String type){
        try{
            Zombie z;
            if(type.equals("Basic")){
                z = new BasicZombie(this, n);
            }
            else if(type.equals("Cone")){
                z = new ConeZombie(this, n);
            }
            else{
                z = new BucketZombie(this, n);
            }
            int i = 0;
            for(; i<zombies[n].size(); i++){
                if(zombies[n].get(i).getX()>=z.getX()){
                    break;
                }
            }
            zombies[n].add(i, z);
        }catch (Exception e){System.out.println("Yo");}
    }
    public void removeZombie(Zombie z){
        for(int i = 0; i<5; i++){
            zombies[i].remove(z);
        }
    }
    public void removePlant(Plant p){
        plants[p.getY()][p.getX()] = null;
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(1),new TimeHandler());
        setUpTime = new Timeline(newKey);
        setUpTime.setCycleCount(Timeline.INDEFINITE);
        setUpTime.play();
        if(isPaused)
            setUpTime.pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            progressBar.setProgress(zombieCounter/((double) zombieLoc.length));
            totalSunLabel.setText(sun + " ");
            generateSun();
            generateZombies();
            rechargePlants();
        }
    }
}

class LawnMower implements Serializable{
    private static final long serialVersionUID = 40L;
    private transient ImageView image;
    private Level level;
    private transient Timeline setUpTime;
    private int X, Y;
    private Boolean isActive = false;
    public LawnMower(Level l, int p) throws Exception{
        level = l;
        Y = p;
        X = -40;
        level.addLawnMower(this);
        image = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/lawnMower.png"),138,96,false,false));
        image.setLayoutY(l.getLayoutGUI().getY(Y)+35);
        image.setLayoutX(X);
        l.levelLayout.getChildren().add(image);
        setupTimeLine();
    }
    public void load() throws Exception{
        image = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/lawnMower.png"),138,96,false,false));
        image.setLayoutY(level.getLayoutGUI().getY(Y)+35);
        image.setLayoutX(X);
        level.levelLayout.getChildren().add(image);
        setupTimeLine();
    }
    public Timeline getSetUpTime() {
        return setUpTime;
    }
    public int getX(){
        return X;
    }
    public void setX(int x){
        X = x;
        image.setX(x);
    }
    public int getY(){
        return Y;
    }
    public void activate(){
        isActive = true;
    }
    public Boolean isCollided(){
        try {
            if (Math.abs(getX() - level.getNearestZombie(getY(), -1).getX()) <= 20) {
                return true;
            }
            return false;
        }catch(NullPointerException e){
            if (getX()>1500){
                level.removeLawnMower(this);
                setUpTime.stop();
            }
            return false;
        }
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(3),new TimeHandler());
        setUpTime = new Timeline(newKey);
        setUpTime.setCycleCount(Timeline.INDEFINITE);
        setUpTime.play();
        if(level.isPaused())
            setUpTime.pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(isActive){
                setX(getX()+1);
                if(isCollided() && getX()<=1450){
                    level.getNearestZombie(getY(), -1).hit(1000);
                }
                if(getX()>=1480){
                    setUpTime.stop();
                }
            }
        }
    }
}
class Pea implements Serializable{
    private static final long serialVersionUID = 40L;
    private transient ImageView image;
    private transient Timeline setUpTime;
    private int value = 100,X,Y,speed=3;
    private Level level;
    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public Timeline getSetUpTime() {
        return setUpTime;
    }

    public void setSetUpTime(Timeline setUpTime) {
        this.setUpTime = setUpTime;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
        image.setX(x);
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
        image.setY(level.getLayoutGUI().getY(y)+55);
    }

    public int getSpeed() {
        return speed;
    }
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Boolean isCollided(){
        try {
            if ((getX() - level.getNearestZombie(getY(), getX()).getX()) >= -2 || (getX()-80 - level.getNearestZombie(getY(), getX()-80).getX()) >=-50) {
                return true;
            }
            return false;
        }catch(NullPointerException e){
            if (getX()>1500){
                level.removePea(this);
            }
            return false;
        }
    }

    public Pea(int x, int y, Level l){
        try {
            level = l;
            level.addPea(this);
            image = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/pea.png"),30,30,false,false));
            setX(level.getLayoutGUI().getX(x)+100);
            setY(y);
            level.levelLayout.getChildren().add(image);

            setupTimeLine();

        }catch (Exception e){}
    }
    public void load() throws Exception{
        image = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/pea.png"),30,30,false,false));
        setX(getX());
        setY(getY());
        level.levelLayout.getChildren().add(image);
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(speed),new TimeHandler());
        setUpTime = new Timeline(newKey);
        setUpTime.setCycleCount(Timeline.INDEFINITE);
        setUpTime.play();
        if(level.isPaused())
            setUpTime.pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            setX(getX()+1);
            if(isCollided()){
                level.getNearestZombie(getY(), getX()-30).hit(25);
                level.removePea(Pea.this);
                setUpTime.stop();
            }
        }
    }
}
class Sun implements Serializable{
    private static final long serialVersionUID = 40L;
    private transient ImageView image;
    private transient Timeline setUpTime;
    private int value = 25,X,Y,finalY,speed=10;
    private Level level;
    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public Timeline getSetUpTime() {
        return setUpTime;
    }

    public void setSetUpTime(Timeline setUpTime) {
        this.setUpTime = setUpTime;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Sun(Level l, SunFlower s){
        try {
            level = l;
            level.addSun(this);
            image = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/sun.png"),75,75,false,false));
            Random r = new Random();
            if(s==null) {
                X = r.nextInt(1000) + 65;
                Y = -100;
                finalY = r.nextInt(550) + 170;
                image.setX(X);
            }
            else{
                X = l.getLayoutGUI().getX(s.getX()) + r.nextInt(54) + 10;
                finalY = l.getLayoutGUI().getY(s.getY()) + 100;
                Y = finalY - 100;
                image.setX(X);
            }
            image.setX(X);
            image.setY(Y);
            l.levelLayout.getChildren().add(getImage());
            getImage().setOnMousePressed(e -> {
                if(!l.isPaused()){
                    getSetUpTime().pause();
                    l.removeSun(Sun.this);
                    double distance = Math.sqrt(Math.pow(12-getImage().getX(),2)+Math.pow(5-getImage().getY(),2));
                    TranslateTransition tt2 = new TranslateTransition();
                    tt2.setDuration(Duration.seconds(distance/700));
                    tt2.setNode(getImage());
                    tt2.setToX(12-getImage().getX());
                    tt2.setToY(5-getImage().getY());
                    tt2.setCycleCount(1);
                    tt2.play();
                    tt2.setOnFinished(e1->{
                        l.levelLayout.getChildren().remove(getImage());
                        l.setSun(l.getSun() + value);
                        value = 0;
                    });}
            });
            setupTimeLine();
        }catch (Exception e){}
    }
    public void load() throws Exception{
        image = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/sun.png"),75,75,false,false));
        image.setX(X);
        image.setY(Y);
        level.levelLayout.getChildren().add(getImage());
        getImage().setOnMousePressed(e -> {
            if(!level.isPaused()){
                getSetUpTime().pause();
                level.removeSun(Sun.this);
                double distance = Math.sqrt(Math.pow(12-getImage().getX(),2)+Math.pow(5-getImage().getY(),2));
                TranslateTransition tt2 = new TranslateTransition();
                tt2.setDuration(Duration.seconds(distance/700));
                tt2.setNode(getImage());
                tt2.setToX(12-getImage().getX());
                tt2.setToY(5-getImage().getY());
                tt2.setCycleCount(1);
                tt2.play();
                tt2.setOnFinished(e1->{
                    level.levelLayout.getChildren().remove(getImage());
                    level.setSun(level.getSun() + value);
                    value = 0;
                });}
        });
        setupTimeLine();
    }
    public void setupTimeLine(){
        KeyFrame newKey = new KeyFrame(Duration.millis(speed),new TimeHandler());
        setUpTime = new Timeline(newKey);
        setUpTime.setCycleCount(Timeline.INDEFINITE);
        setUpTime.play();
        if(level.isPaused())
            setUpTime.pause();
    }
    private class TimeHandler implements EventHandler<ActionEvent>{
        public void handle(ActionEvent event){
            if(image.getY() < finalY) {
                image.setY(image.getY() + 1);
                Y++;
            }
        }
    }
}

class SeedPacket extends Button{
    private ImageView plantImage;
    private Boolean bought = false;
    public SeedPacket(String plant, Level l){
        super();
        SeedPacket s = this;
        this.setPrefSize(68, 96);
        this.setLayoutY(5);
        if(plant.equals("PeaShooter")){
            this.setLayoutX(97);
            this.getStyleClass().add("peaShooter-seedPacket-button");
            this.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
                    s.setMouseTransparent(true);
                    event.setDragDetect(true);
                    if(l.getSun()>=PeaShooter.PeaShooter_Price){
                        try {
                            plantImage = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/PeaShooter/0.gif"), 150, 150, false, false));
                            l.levelLayout.getChildren().add(plantImage);
                        }catch (Exception e){}
                        plantImage.setLayoutX(event.getX()+15);
                        plantImage.setLayoutY(event.getY()-40);
                        bought = true;
                    }
                }
            });
            this.setOnMouseDragged(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event){
                    event.setDragDetect(false);
                    if(bought){
                        plantImage.setLayoutX(event.getX()+15);
                        plantImage.setLayoutY(event.getY()-40);
                    }
                }
            });
            this.setOnDragDetected(new EventHandler <MouseEvent>(){
                public void handle(MouseEvent event){
                    s.startFullDrag();
                    event.setDragDetect(false);
                }
            });
            this.setOnMouseReleased(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event) {
                    s.setMouseTransparent(false);
                    event.setDragDetect(false);
                    if(bought && l.getLayoutGUI().canPlace()) {
                        bought = false;
                        l.setSun(l.getSun()-100);
                        try{
                            PeaShooter p = new PeaShooter(l.getLayoutGUI().getCoOrdinates(), l);
                        }catch (Exception e){}

                    }
                    l.levelLayout.getChildren().remove(plantImage);
                    plantImage = null;
                }
            });
        }
        else if(plant.equals("SunFlower")){
            this.setLayoutX(165);
            this.getStyleClass().add("sunFlower-seedPacket-button");
            this.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
                    s.setMouseTransparent(true);
                    event.setDragDetect(true);
                    if(l.getSun()>=SunFlower.SunFlower_Price){
                        try {
                            plantImage = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/SunFlower/0.gif"), 150, 150, false, false));
                            l.levelLayout.getChildren().add(plantImage);
                        }catch (Exception e){}
                        plantImage.setLayoutX(event.getX()+100);
                        plantImage.setLayoutY(event.getY()-30);
                        bought = true;
                    }
                }
            });
            this.setOnMouseDragged(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event){
                    event.setDragDetect(false);
                    if(bought){
                        plantImage.setLayoutX(event.getX()+100);
                        plantImage.setLayoutY(event.getY()-30);
                    }
                }
            });
            this.setOnDragDetected(new EventHandler <MouseEvent>(){
                public void handle(MouseEvent event){
                    s.startFullDrag();
                    event.setDragDetect(false);
                }
            });
            this.setOnMouseReleased(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event) {
                    s.setMouseTransparent(false);
                    event.setDragDetect(false);
                    if(bought && l.getLayoutGUI().canPlace()) {
                        bought = false;
                        l.setSun(l.getSun()-50);
                        try{
                            SunFlower s = new SunFlower(l.getLayoutGUI().getCoOrdinates(), l);
                        }catch (Exception e){}
                    }
                    l.levelLayout.getChildren().remove(plantImage);
                    plantImage = null;
                }
            });
        }
        else if(plant.equals("CherryBomb")){
            this.setLayoutX(233);
            this.getStyleClass().add("cherryBomb-seedPacket-button");
            this.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
                    s.setMouseTransparent(true);
                    event.setDragDetect(true);
                    if(l.getSun()>=CherryBomb.Cherry_Price){
                        try {
                            plantImage = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/CherryBomb/0.gif"), 150, 150, false, false));
                            l.levelLayout.getChildren().add(plantImage);
                        }catch (Exception e){}
                        plantImage.setLayoutX(event.getX()+185);
                        plantImage.setLayoutY(event.getY()-20);
                        bought = true;
                    }
                }
            });
            this.setOnMouseDragged(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event){
                    event.setDragDetect(false);
                    if(bought){
                        plantImage.setLayoutX(event.getX()+185);
                        plantImage.setLayoutY(event.getY()-20);
                    }
                }
            });
            this.setOnDragDetected(new EventHandler <MouseEvent>(){
                public void handle(MouseEvent event){
                    s.startFullDrag();
                    event.setDragDetect(false);
                }
            });
            this.setOnMouseReleased(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event) {
                    s.setMouseTransparent(false);
                    event.setDragDetect(false);
                    if(bought && l.getLayoutGUI().canPlace()) {
                        bought = false;
                        l.setSun(l.getSun()-50);
                        try{
                            CherryBomb c = new CherryBomb(l.getLayoutGUI().getCoOrdinates(), l);
                        }catch (Exception e){}
                    }
                    l.levelLayout.getChildren().remove(plantImage);
                    plantImage = null;
                }
            });
        }
        else if(plant.equals("WallNut")){
            this.setLayoutX(301);
            this.getStyleClass().add("wallNut-seedPacket-button");
            this.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
                    s.setMouseTransparent(true);
                    event.setDragDetect(true);
                    if(l.getSun()>=WallNut.WallNut_Price){
                        try {
                            plantImage = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/WallNut/0.png"), 150, 150, false, false));
                            l.levelLayout.getChildren().add(plantImage);
                        }catch (Exception e){}
                        plantImage.setLayoutX(event.getX()+270);
                        plantImage.setLayoutY(event.getY()-30);
                        bought = true;
                    }
                }
            });
            this.setOnMouseDragged(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event){
                    event.setDragDetect(false);
                    if(bought){
                        plantImage.setLayoutX(event.getX()+270);
                        plantImage.setLayoutY(event.getY()-30);
                    }
                }
            });
            this.setOnDragDetected(new EventHandler <MouseEvent>(){
                public void handle(MouseEvent event){
                    s.startFullDrag();
                    event.setDragDetect(false);
                }
            });
            this.setOnMouseReleased(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event) {
                    s.setMouseTransparent(false);
                    event.setDragDetect(false);
                    if(bought && l.getLayoutGUI().canPlace()) {
                        bought = false;
                        l.setSun(l.getSun()-50);
                        try{
                            WallNut w = new WallNut(l.getLayoutGUI().getCoOrdinates(), l);
                        }catch (Exception e){}
                    }
                    l.levelLayout.getChildren().remove(plantImage);
                    plantImage = null;
                }
            });
        }
        else if(plant.equals("PotatoMine")){
            this.setLayoutX(369);
            this.getStyleClass().add("potatoMine-seedPacket-button");
            this.setOnMousePressed(new EventHandler<MouseEvent>(){
                public void handle(MouseEvent event){
                    s.setMouseTransparent(true);
                    event.setDragDetect(true);
                    if(l.getSun()>=PotatoMine.Potato_Price){
                        try {
                            plantImage = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/PotatoMine/1.png"), 150, 150, false, false));
                            l.levelLayout.getChildren().add(plantImage);
                        }catch (Exception e){}
                        plantImage.setLayoutX(event.getX()+300);
                        plantImage.setLayoutY(event.getY()-30);
                        bought = true;
                    }
                }
            });
            this.setOnMouseDragged(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event){
                    event.setDragDetect(false);
                    if(bought){
                        plantImage.setLayoutX(event.getX()+300);
                        plantImage.setLayoutY(event.getY()-30);
                    }
                }
            });
            this.setOnDragDetected(new EventHandler <MouseEvent>(){
                public void handle(MouseEvent event){
                    s.startFullDrag();
                    event.setDragDetect(false);
                }
            });
            this.setOnMouseReleased(new EventHandler <MouseEvent>() {
                public void handle(MouseEvent event) {
                    s.setMouseTransparent(false);
                    event.setDragDetect(false);
                    if(bought && l.getLayoutGUI().canPlace()) {
                        bought = false;
                        l.setSun(l.getSun()-25);
                        try{
                            PotatoMine p = new PotatoMine(l.getLayoutGUI().getCoOrdinates(), l);
                        }catch (Exception e){}
                    }
                    l.levelLayout.getChildren().remove(plantImage);
                    plantImage = null;
                }
            });
        }
    }
}
class Shovel extends Button{
    private ImageView shovelImage;
    public Shovel(Level l){
        super();
        Shovel s = this;
        this.setPrefSize(96, 96);
        this.setLayoutY(5);
        this.setLayoutX(660);
        this.getStyleClass().add("shovel-button");
        this.setOnMousePressed(new EventHandler<MouseEvent>(){
            public void handle(MouseEvent event){
                s.setMouseTransparent(true);
                event.setDragDetect(true);
                try {
                    shovelImage = new ImageView(new Image(new FileInputStream("/Users/abhinav/IdeaProjects/PVZ2/src/sprites/Shovel.png"), 100, 100, false, false));
                    l.levelLayout.getChildren().add(shovelImage);
                }catch (Exception e){}
                shovelImage.setLayoutX(event.getX()+590);
                shovelImage.setLayoutY(event.getY()-40);
            }
        });
        this.setOnMouseDragged(new EventHandler <MouseEvent>() {
            public void handle(MouseEvent event){
                event.setDragDetect(false);
                shovelImage.setLayoutX(event.getX()+590);
                shovelImage.setLayoutY(event.getY()-40);

            }
        });
        this.setOnDragDetected(new EventHandler <MouseEvent>(){
            public void handle(MouseEvent event){
                s.startFullDrag();
                event.setDragDetect(false);
            }
        });
        this.setOnMouseReleased(new EventHandler <MouseEvent>() {
            public void handle(MouseEvent event) {
                s.setMouseTransparent(false);
                event.setDragDetect(false);
                if(l.getLayoutGUI().canShovel()) {
                    try{
                        l.getPlants()[l.getLayoutGUI().getCoOrdinates()[1]][l.getLayoutGUI().getCoOrdinates()[0]].die();
                    }catch (Exception e){}

                }
                l.levelLayout.getChildren().remove(shovelImage);
                shovelImage = null;
            }
        });
    }
}
class User implements Serializable{
    private static final long serialVersionUID = 40L;
    private String name;
    private Boolean[] levelsUnlocked;
    private Level[] levelsSaved;
    public User(String n){
        name = n;
        levelsUnlocked = new Boolean[]{true, false, false, false, false};
        levelsSaved = new Level[5];
    }
    public String getName() {
        return name;
    }
    public Boolean[] getLevelsUnlocked() {
        return levelsUnlocked;
    }
    public Level[] getLevelsSaved() {
        return levelsSaved;
    }
}
class UsersList implements Serializable{
    private static final long serialVersionUID = 40L;
    private ArrayList<User> users;
    public UsersList(){
        users = new ArrayList<User>();
    }
    public ArrayList<User> getUsers() {
        return users;
    }
    public void addUser(User user) {
        users.add(user);
    }
}
public class Main extends Application {
    private static Boolean new_choose;
    private static String userName;
    private static Button ok_button, cancel_button;
    private static Button adv_button,choose_level_button,choose_user_button, minigames_button, puzzle_button, survival_button, quit_button, help_button, options_button;
    private static Button lvl1_button, lvl2_button, lvl3_button, lvl4_button, lvl5_button, back_button;
    private static Button almanac_button, cancel_a_button, peaShooter_button, sunFlower_button, cherryBomb_button, wallNut_button, potatoMine_button;
    private static TextArea description;
    private static Label userLabel, levelLabel, plantgif;
    private static TextField tf;
    private static Pane layout1, layout2, layout3, layout4;
    private static Scene newUserScene, mainMenuScene, chooseLevelScene, almanacScene;
    private static int levelNo = 1;
    private static Level[] levels;
    private static Stage stage;
    private static UsersList users;
    private static User activeUser;

    public static Scene getMainMenuScene() {
        return mainMenuScene;
    }

    public static Scene getChooseLevelScene() {
        return chooseLevelScene;
    }

    public static User getActiveUser() {
        return activeUser;
    }

    public static Stage getStage() {
        return stage;
    }
    public static void switchUser() throws Exception{
        int i = users.getUsers().indexOf(activeUser);
        i++;
        if(i==users.getUsers().size())
            i = 0;
        activeUser = users.getUsers().get(i);
        userLabel.setText(activeUser.getName());
        for(int j = 0; j<5; j++){
            deserialize(j);
        }
        int j = 1;
        for(;j<5;j++){
            if(!activeUser.getLevelsUnlocked()[j])
                break;
        }
        levelNo = j;

    }
    public static void setLevel(int n, Boolean isNew) throws Exception{
        saveUsers();
        if(levels[n-1]==null || isNew)
            levels[n-1] = new Level(n);
        stage.setScene(levels[n-1].getScene());
    }
    public static Level[] getLevels() {
        return levels;
    }
    public static void serialize(Level l) throws IOException{
        ObjectOutputStream out = null;
        try{
            out = new ObjectOutputStream(new FileOutputStream(activeUser.getName()+"_level" + l.getLevelno() + ".txt"));
            out.writeObject(l);
        }catch (Exception e){e.printStackTrace();}
        finally{
            out.close();
        }
    }
    public static void deserialize(int i) throws Exception{
        ObjectInputStream in = null;
        try{
            in = new ObjectInputStream(new FileInputStream(activeUser.getName()+"_level" + (i+1) + ".txt"));
            levels[i] = (Level) in.readObject();
            levels[i].load();
        }catch (Exception e){levels[i] = null;}
        finally{
            if(in!=null)
                in.close();
        }
    }
    public static void saveUsers() throws IOException{
        ObjectOutputStream out = null;
        try{
            out = new ObjectOutputStream(new FileOutputStream("users.txt"));
            out.writeObject(users);
        }catch (Exception e){}
        finally{
            out.close();
        }
    }
    public static void lockLevels(){
        lvl1_button.setDisable(!activeUser.getLevelsUnlocked()[0]);
        lvl2_button.setDisable(!activeUser.getLevelsUnlocked()[1]);
        lvl3_button.setDisable(!activeUser.getLevelsUnlocked()[2]);
        lvl4_button.setDisable(!activeUser.getLevelsUnlocked()[3]);
        lvl5_button.setDisable(!activeUser.getLevelsUnlocked()[4]);
    }
    public static void loadUsers() throws IOException{
        ObjectInputStream in = null;
        try{
            in = new ObjectInputStream(new FileInputStream("users.txt"));
            users = (UsersList) in.readObject();
            for(int i = 0; i<users.getUsers().size(); i++){
                for(int j = 0; j<5; j++){
                    if(users.getUsers().get(i).getLevelsSaved()[j]!=null)
                        users.getUsers().get(i).getLevelsSaved()[j].load();
                }
            }
        }catch (Exception e){e.printStackTrace();}
        finally{
            if(in!=null)
                in.close();
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        levels = new Level[5];
        new_choose = true;
        ok_button = new Button();
        ok_button.setPrefSize(292, 63);
        ok_button.setLayoutX(572);
        ok_button.setLayoutY(480);
        ok_button.getStyleClass().add("ok-button");
        ok_button.setOnAction(e -> {
            userName = tf.getText();
            User user = new User(userName);
            activeUser = user;
            users.addUser(user);
            levels = new Level[5];
            levelNo = 1;
            userLabel.setText(userName);
            try{
                saveUsers();
            }catch(Exception e1){}
            primaryStage.setScene(mainMenuScene);
        });
        cancel_button = new Button();
        cancel_button.setPrefSize(50, 50);
        cancel_button.setLayoutX(880);
        cancel_button.setLayoutY(490);
        cancel_button.getStyleClass().add("cancel-button");
        cancel_button.setOnAction(e -> {
            primaryStage.setScene(mainMenuScene);
        });
        tf = new TextField();
        tf.setPrefSize(373, 53);
        tf.setLayoutX(535);
        tf.setLayoutY(405);
        tf.setAlignment(Pos.CENTER);
        tf.getStyleClass().add("user-text");
        tf.setTextFormatter(new TextFormatter<>((change) -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
        layout1 = new Pane();
        layout1.getChildren().addAll(ok_button, tf);

        newUserScene = new Scene(layout1,1500, 800);
        newUserScene.getStylesheets().add("/sample/newUserStyle.css");

        lvl1_button = new Button();
        lvl1_button.setPrefSize(136, 195);
        lvl1_button.setLayoutX(400);
        lvl1_button.setLayoutY(300);
        lvl1_button.getStyleClass().add("lvl1-button");
        lvl1_button.setOnAction(e -> {
            try {
                setLevel(1, false);
            }catch(Exception e1){}
        });
        lvl2_button = new Button();
        lvl2_button.setPrefSize(136, 195);
        lvl2_button.setLayoutX(540);
        lvl2_button.setLayoutY(300);
        lvl2_button.getStyleClass().add("lvl2-button");
        lvl2_button.setOnAction(e -> {
            try {
                setLevel(2, false);
            }catch(Exception e1){

            }
        });
        lvl3_button = new Button();
        lvl3_button.setPrefSize(136, 195);
        lvl3_button.setLayoutX(680);
        lvl3_button.setLayoutY(300);
        lvl3_button.getStyleClass().add("lvl3-button");
        lvl3_button.setOnAction(e -> {
            try {
                setLevel(3, false);
            }catch(Exception e1){

            }
        });
        lvl4_button = new Button();
        lvl4_button.setPrefSize(136, 195);
        lvl4_button.setLayoutX(820);
        lvl4_button.setLayoutY(300);
        lvl4_button.getStyleClass().add("lvl4-button");
        lvl4_button.setOnAction(e -> {
            try {
                setLevel(4, false);
            }catch(Exception e1){

            }
        });
        lvl5_button = new Button();
        lvl5_button.setPrefSize(136, 195);
        lvl5_button.setLayoutX(960);
        lvl5_button.setLayoutY(300);
        lvl5_button.getStyleClass().add("lvl5-button");
        lvl5_button.setOnAction(e -> {
            try {
                setLevel(5, false);
            }catch(Exception e1){

            }
        });
        back_button = new Button();
        back_button.setPrefSize(50, 50);
        back_button.setLayoutX(675);
        back_button.setLayoutY(100);
        back_button.getStyleClass().add("cancel-button");
        back_button.setOnAction(e -> {
            primaryStage.setScene(mainMenuScene);
        });
        layout3 = new Pane();
        layout3.getChildren().addAll(lvl1_button,lvl2_button,lvl3_button,lvl4_button,lvl5_button, back_button);

        chooseLevelScene = new Scene(layout3,1500, 800);
        chooseLevelScene.getStylesheets().add("/sample/chooseLevelStyle.css");

        sunFlower_button = new Button();
        sunFlower_button.setPrefSize(186, 310);
        sunFlower_button.setLayoutX(236);
        sunFlower_button.setLayoutY(118);
        sunFlower_button.getStyleClass().add("sunFlower-seedPacket-button");
        sunFlower_button.setOnAction(e -> {
            plantgif.setStyle("-fx-background-image: url(\"/sprites/SunFlower.gif\");\n" +
                    "    -fx-background-size: 343px 190px;\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-color: transparent;");
            description.setText("Sunflower\n\nSunflowers are tactically the most important plant and the only sun producing plant, they give u an edge to have more sun to buy more plants. Go kill those zombies...!\n\nSun production: normal\n\nSunflower can't resist bouncing to the beat. Which beat is that? Why, the life-giving jazzy rhythm of the Earth itself, thumping at a frequency only Sunflower can hear.\n\nCost: 50\nRecharge: fast");
        });
        peaShooter_button = new Button();
        peaShooter_button.setPrefSize(186, 310);
        peaShooter_button.setLayoutX(46);
        peaShooter_button.setLayoutY(118);
        peaShooter_button.getStyleClass().add("peaShooter-seedPacket-button");
        peaShooter_button.setOnAction(e -> {
            plantgif.setStyle("-fx-background-image: url(\"/sprites/PeaShooter.gif\");\n" +
                    "    -fx-background-size: 343px 190px;\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-color: transparent;");
            description.setText("Peashooter\n\nPeashooters are the most basic attack type plant, who shoot pea as bullets so you don't need to touch the zombie to kill them, kill them from away snipe it ,kill it.\n\nDamage: normal\n\nHow can a single plant grow and shoot so many peas so quickly? Peashooter says,\"Hard work, commitment, and a healthy, well-balanced breakfast of sunlight and high-fiber carbon dioxide make it all possible.\"\n\nCost: 100\nRecharge: fast");
        });
        cherryBomb_button = new Button();
        cherryBomb_button.setPrefSize(186, 310);
        cherryBomb_button.setLayoutX(426);
        cherryBomb_button.setLayoutY(118);
        cherryBomb_button.getStyleClass().add("cherryBomb-seedPacket-button");
        cherryBomb_button.setOnAction(e -> {
            plantgif.setStyle("-fx-background-image: url(\"/sprites/CherryBomb.gif\");\n" +
                    "    -fx-background-size: 343px 190px;\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-color: transparent;");
            description.setText("Cherry Bomb\n\n"+"Cherry Bombs can blow up all zombies in an area. They have a short fuse so plant them near zombies.\n" +
                    "As sweet as he look, he explodes quiet atomically, he is desendent of the 1914 little boy, place him up so that he can blow. “Zombies are my friends” - he says\n\n" +
                    "Damage: massive\nRange: all zombies in a medium area\nUsage: single use, instant\n\n" +
                    "\"I wanna explode,\" says Cherry #1. \"No, let's detonate instead!\" says his brother, Cherry #2. After intense consultation they agree to explodonate.\n\n" +
                    "Cost: 150\n" +
                    "Recharge: very slow");
        });
        wallNut_button = new Button();
        wallNut_button.setPrefSize(186, 310);
        wallNut_button.setLayoutX(616);
        wallNut_button.setLayoutY(118);
        wallNut_button.getStyleClass().add("wallNut-seedPacket-button");
        wallNut_button.setOnAction(e -> {
            plantgif.setStyle("-fx-background-image: url(\"/sprites/WallNut.gif\");\n" +
                    "    -fx-background-size: 343px 190px;\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-color: transparent;");
            description.setText("Wall-nut\n\nWall-nuts are your first block of defense,they will sacrifice themselves to save your line,salute to them for this patriot act, hard shells; hard life.\n\nToughness: high\n\n\"People wonder how I feel about getting constantly chewed on by zombies,\" says Wall-nut. \"What they don't realize is that with my limited senses all I can feel is a kind of tingling, like a relaxing back rub.\"\n\nCost: 50\nRecharge: slow");
        });
        potatoMine_button = new Button();
        potatoMine_button.setPrefSize(186, 310);
        potatoMine_button.setLayoutX(46);
        potatoMine_button.setLayoutY(435);
        potatoMine_button.getStyleClass().add("potatoMine-seedPacket-button");
        potatoMine_button.setOnAction(e -> {
            plantgif.setStyle("-fx-background-image: url(\"/sprites/PotatoMine.gif\");\n" +
                    "    -fx-background-size: 343px 190px;\n" +
                    "    -fx-background-color: transparent;\n" +
                    "    -fx-border-color: transparent;");
            description.setText("Potato Mine\n\n" +
                    "Landmines are old fashioned. “grandpops were dumb, I am a smart mine I can pull off and zombie’s head.” Potato mines are sensitive little thing, if you touch them they will explode.But they are lazy things need time to buckle up.\n\n" +
                    "Damage: massive\nRange: all zombies in a small area\nUsage: single use, delayed activation\n\n" +
                    "Some folks say Potato Mine is lazy, that he leaves everything to the last minute. Potato Mine says nothing. He's too busy thinking about his investment strategy.\n\n" +
                    "Cost: 25\n" +
                    "Recharge: slow");
        });
        cancel_a_button = new Button();
        cancel_a_button.setPrefSize(50, 50);
        cancel_a_button.setLayoutX(40);
        cancel_a_button.setLayoutY(37);
        cancel_a_button.getStyleClass().add("cancel-button");
        cancel_a_button.setOnAction(e -> {
            primaryStage.setScene(mainMenuScene);
        });
        plantgif = new Label();
        plantgif.setPrefSize(343, 190);
        plantgif.setLayoutX(943);
        plantgif.setLayoutY(143);
        description = new TextArea();
        description.setPrefSize(490, 310);
        description.setLayoutX(870);
        description.setLayoutY(410);
        description.setWrapText(true);
        description.setDisable(true);
        description.getStyleClass().add("desc-text");
        layout4 = new Pane();
        layout4.getChildren().addAll(cancel_a_button, peaShooter_button, sunFlower_button, cherryBomb_button, wallNut_button,potatoMine_button, description, plantgif);

        almanacScene = new Scene(layout4,1500, 800);
        almanacScene.getStylesheets().add("/sample/AlmanacStyle.css");

        adv_button = new Button();
        adv_button.setPrefSize(620,158);
        adv_button.setLayoutX(720);
        adv_button.setLayoutY(107);
        adv_button.getStyleClass().add("adventure-button");
        adv_button.setOnAction(e -> {
            try {
                if (new_choose)
                    setLevel(levelNo, false);
                else {
                    primaryStage.setScene(chooseLevelScene);
                    lockLevels();
                }
            }catch(Exception e1){e1.printStackTrace();}
        });
        almanac_button = new Button();
        almanac_button.setPrefSize(173,127);
        almanac_button.setLayoutX(600);
        almanac_button.setLayoutY(572);
        almanac_button.getStyleClass().add("almanac-button");
        almanac_button.setOnAction(e -> {
            primaryStage.setScene(almanacScene);
        });
        choose_level_button = new Button();
        choose_level_button.setPrefSize(176,48);
        choose_level_button.setLayoutX(918);
        choose_level_button.setLayoutY(208);
        levelLabel = new Label("START GAME");
        levelLabel.setAlignment(Pos.CENTER);
        levelLabel.setRotate(5.6);
        levelLabel.getStyleClass().add("level-text");
        choose_level_button.setGraphic(new Group(levelLabel));
        choose_level_button.getStyleClass().add("chooseLevel-button");
        choose_level_button.setOnAction(e -> {
            new_choose = !new_choose;
            if(new_choose)
                levelLabel.setText("START GAME");
            else
                levelLabel.setText("CHOOSE LEVEL");
        });
        choose_user_button = new Button();
        choose_user_button.setPrefSize(518,68);
        choose_user_button.setLayoutX(46);
        choose_user_button.setLayoutY(169);
        choose_user_button.getStyleClass().add("choose-user-button");
        choose_user_button.setOnAction(e -> {
            try{
                switchUser();
            }catch (Exception e1){ }
        });
        survival_button = new Button();
        survival_button.setPrefSize(476,158);
        survival_button.setLayoutX(756);
        survival_button.setLayoutY(438);
        survival_button.getStyleClass().add("survival-button");
        puzzle_button = new Button();
        puzzle_button.setPrefSize(512,158);
        puzzle_button.setLayoutX(745);
        puzzle_button.setLayoutY(344);
        puzzle_button.getStyleClass().add("puzzle-button");
        minigames_button = new Button();
        minigames_button.setPrefSize(565,172);
        minigames_button.setLayoutX(738);
        minigames_button.setLayoutY(235);
        minigames_button.getStyleClass().add("minigames-button");
        quit_button = new Button();
        quit_button.setPrefSize(150,143);
        quit_button.setLayoutX(1274);
        quit_button.setLayoutY(592);
        quit_button.getStyleClass().add("quit-button");
        quit_button.setOnAction(e -> {
            try {
                saveUsers();
                System.exit(0);
            }catch (Exception e1){}finally {

            }
        });
        help_button = new Button();
        help_button.setPrefSize(138,190);
        help_button.setLayoutX(1157);
        help_button.setLayoutY(553);
        help_button.getStyleClass().add("help-button");
        options_button = new Button();
        options_button.setPrefSize(180,144);
        options_button.setLayoutX(997);
        options_button.setLayoutY(567);
        options_button.getStyleClass().add("options-button");
        userLabel = new Label();
        userLabel.setLayoutX(265);
        userLabel.setLayoutY(115);
        userLabel.setAlignment(Pos.CENTER);
        userLabel.getStyleClass().add("user-text");

        primaryStage.setTitle("Plants Vs Zombies");
        layout2 = new Pane();
        layout2.getChildren().addAll(adv_button,almanac_button, choose_level_button,choose_user_button , minigames_button, puzzle_button, survival_button, quit_button, options_button, help_button, userLabel);

        mainMenuScene = new Scene(layout2, 1500, 800);
        mainMenuScene.getStylesheets().add("/sample/mainMenuStyle.css");
        loadUsers();
        primaryStage.setScene(newUserScene);
        if(users==null || users.getUsers().isEmpty()) {
            users = new UsersList();
        }
        else{
            layout1.getChildren().addAll(cancel_button);
            userLabel.setText(users.getUsers().get(0).getName());
            activeUser = users.getUsers().get(0);
            for(int i = 0; i<5; i++){
                deserialize(i);
            }
            int j = 1;
            for(;j<5;j++){
                if(!activeUser.getLevelsUnlocked()[j])
                    break;
            }
            levelNo = j;
        }
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
