package com.xirc.gunmetal.common.item;

public class BerettaItem extends AbstractGunItem {
    public BerettaItem(Properties settings) {
        super(settings);
    }

    @Override
    protected int maxRounds() {
        return 15;
    }

    @Override
    protected float damage() {
        return 4.0f;
    }

    @Override
    protected float range() {
        return 60.0f;
    }

    @Override
    protected float knockback() {
        return 0.15f;
    }

    @Override
    protected int barrels() {
        return 1;
    }

    @Override
    protected float caliber() {
        return 9.0f;
    }

    @Override
    protected float bulletLength() {
        return 19.0f;
    }

    @Override
    protected int stunTicks() {
        return 1;
    }

    @Override
    protected int inputCooldownTicks() {
        return 4;
    }

    @Override
    protected int refireCooldownTicks() {
        return 6;
    }

    @Override
    protected int reloadCooldownTicks() {
        return 35;
    }

    @Override
    protected int reloadStepTicks() {
        return 3;
    }
}
