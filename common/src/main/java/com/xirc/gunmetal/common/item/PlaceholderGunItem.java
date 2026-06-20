package com.xirc.gunmetal.common.item;

public class PlaceholderGunItem extends AbstractGunItem {
    public PlaceholderGunItem(Properties settings) {
        super(settings);
    }

    @Override
    protected int maxRounds() {
        return 6;
    }

    @Override
    protected float damage() {
        return 5.0f;
    }

    @Override
    protected float range() {
        return 80.0f;
    }

    @Override
    protected float knockback() {
        return 0.25f;
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
        return 10.0f;
    }

    @Override
    protected int stunTicks() {
        return 2;
    }

    @Override
    protected int inputCooldownTicks() {
        return 10;
    }

    @Override
    protected int refireCooldownTicks() {
        return 20;
    }

    @Override
    protected int reloadCooldownTicks() {
        return 60;
    }

    @Override
    protected int reloadStepTicks() {
        return 10;
    }
}
