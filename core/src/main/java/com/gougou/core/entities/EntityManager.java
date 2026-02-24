package com.gougou.core.entities;

import com.gougou.core.world.World;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager {
    private final Map<Integer, Entity> entities = new ConcurrentHashMap<>();
    private int nextEntityId = 1;
    private World world;

    public EntityManager() {}

    public void setWorld(World world) {
        this.world = world;
    }

    public int addEntity(Entity entity) {
        entities.put(entity.getEntityId(), entity);
        return entity.getEntityId();
    }

    public Player spawnPlayer(String name, float x, float y) {
        int id = nextEntityId++;
        Player player = new Player(id, name, x, y);
        player.setWorld(world);
        entities.put(id, player);
        return player;
    }

    public Mob spawnMob(Mob.MobType type, float x, float y) {
        int id = nextEntityId++;
        Mob mob = new Mob(id, type, x, y);
        mob.setWorld(world);
        entities.put(id, mob);
        return mob;
    }

    public void removeEntity(int entityId) {
        entities.remove(entityId);
    }

    public Entity getEntity(int entityId) {
        return entities.get(entityId);
    }

    public Collection<Entity> getAllEntities() {
        return entities.values();
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> getEntitiesOfType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Entity e : entities.values()) {
            if (type.isInstance(e)) result.add((T) e);
        }
        return result;
    }

    public void update(float delta) {
        List<Integer> dead = new ArrayList<>();
        for (Entity e : entities.values()) {
            e.update(delta);
            if (!e.isAlive()) dead.add(e.getEntityId());
        }
        for (int id : dead) {
            entities.remove(id);
        }
    }

    public int getEntityCount() {
        return entities.size();
    }

    public int getNextEntityId() {
        return nextEntityId++;
    }
}
