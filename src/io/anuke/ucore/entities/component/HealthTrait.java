package io.anuke.ucore.entities.component;

import io.anuke.ucore.util.Mathf;

public interface HealthTrait {

    void health(float health);
    float health();
    float maxHealth();

    boolean isDead();
    void setDead(boolean dead);

    default void onHit(SolidTrait entity){}
    default void onDeath(){}

    default void damage(float amount){
        health(health() - amount);
        if(health() <= 0 && !isDead()){
            onDeath();
            setDead(true);
        }
    }

    default void clampHealth(){
        health(Mathf.clamp(health(), 0, maxHealth()));
    }

    default float healthf(){
        return health() / maxHealth();
    }

    default void heal(){
        health(maxHealth());
        setDead(false);
    }
}
