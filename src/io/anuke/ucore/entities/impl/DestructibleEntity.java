package io.anuke.ucore.entities.impl;

import io.anuke.ucore.entities.component.DamageTrait;
import io.anuke.ucore.entities.component.HealthTrait;
import io.anuke.ucore.entities.component.SolidTrait;

public abstract class DestructibleEntity extends SolidEntity implements HealthTrait {
	public transient boolean dead;
	public float health;

	@Override
	public boolean collides(SolidTrait other){
		return other instanceof DamageTrait;
	}

	@Override
	public void collision(SolidTrait other, float x, float y){
		if(other instanceof DamageTrait){
			onHit(other);
			damage(((DamageTrait)other).getDamage());
		}
	}

	@Override
	public void health(float health) {
		this.health = health;
	}

	@Override
	public float health() {
		return health;
	}

	@Override
	public boolean isDead() {
		return dead;
	}

	@Override
	public void setDeath() {
		this.dead = true;
	}
}
