package net.minestom.server.entity;

import net.kyori.adventure.sound.Sound.Source;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.Attributes;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.block.BlockIterator;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LivingEntity extends Entity implements EquipmentHandler {

    // ItemStack pickup
    protected boolean canPickupItem;
    protected Cooldown itemPickupCooldown = new Cooldown(new UpdateOption(5, TimeUnit.TICK));

    protected boolean isDead;

    protected DamageType lastDamageSource;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    protected BoundingBox expandedBoundingBox;

    private final Map<String, AttributeInstance> attributeModifiers = new ConcurrentHashMap<>(Attribute.values().length);

    // Abilities
    protected boolean invulnerable;

    /**
     * Time at which this entity must be extinguished
     */
    private long fireExtinguishTime;

    /**
     * Last time the fire damage was applied
     */
    private long lastFireDamageTime;

    /**
     * Period, in ms, between two fire damage applications
     */
    private long fireDamagePeriod = 1000L;

    private Team team;

    private int arrowCount;
    private float health = 1F;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    public LivingEntity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        this(entityType, uuid, new Position());
        setGravity(0.02f, 0.08f, 3.92f);
        initEquipments();
    }

    public LivingEntity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    /**
     * Constructor which allows to specify an UUID. Only use if you know what you are doing!
     */
    @Deprecated
    public LivingEntity(@NotNull EntityType entityType, @NotNull UUID uuid, @NotNull Position spawnPosition) {
        super(entityType, uuid, spawnPosition);
        setGravity(0.02f, 0.08f, 3.92f);
        initEquipments();
    }

    @Deprecated
    public LivingEntity(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        this(entityType, UUID.randomUUID(), spawnPosition);
    }

    private void initEquipments() {
        this.mainHandItem = ItemStack.AIR;
        this.offHandItem = ItemStack.AIR;

        this.helmet = ItemStack.AIR;
        this.chestplate = ItemStack.AIR;
        this.leggings = ItemStack.AIR;
        this.boots = ItemStack.AIR;
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        this.mainHandItem = getEquipmentItem(itemStack, EquipmentSlot.MAIN_HAND);
        syncEquipment(EquipmentSlot.MAIN_HAND);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        this.offHandItem = getEquipmentItem(itemStack, EquipmentSlot.OFF_HAND);
        syncEquipment(EquipmentSlot.OFF_HAND);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, EquipmentSlot.HELMET);
        syncEquipment(EquipmentSlot.HELMET);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, EquipmentSlot.CHESTPLATE);
        syncEquipment(EquipmentSlot.CHESTPLATE);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, EquipmentSlot.LEGGINGS);
        syncEquipment(EquipmentSlot.LEGGINGS);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, EquipmentSlot.BOOTS);
        syncEquipment(EquipmentSlot.BOOTS);
    }

    private ItemStack getEquipmentItem(@NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
        EntityEquipEvent entityEquipEvent = new EntityEquipEvent(this, itemStack, slot);
        EventDispatcher.call(entityEquipEvent);
        return entityEquipEvent.getEquippedItem();
    }

    @Override
    public void update(long time) {
        if (isOnFire()) {
            if (time > fireExtinguishTime) {
                setOnFire(false);
            } else {
                if (time - lastFireDamageTime > fireDamagePeriod) {
                    damage(DamageType.ON_FIRE, 1.0f);
                    lastFireDamageTime = time;
                }
            }
        }

        // Items picking
        if (canPickupItem() && itemPickupCooldown.isReady(time)) {
            itemPickupCooldown.refreshLastUpdate(time);

            final Chunk chunk = getChunk(); // TODO check surrounding chunks
            final Set<Entity> entities = instance.getChunkEntities(chunk);
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {

                    // Do not pickup if not visible
                    if (this instanceof Player && !entity.isViewer((Player) this))
                        continue;

                    final ItemEntity itemEntity = (ItemEntity) entity;
                    if (!itemEntity.isPickable())
                        continue;

                    final BoundingBox itemBoundingBox = itemEntity.getBoundingBox();
                    if (expandedBoundingBox.intersect(itemBoundingBox)) {
                        if (itemEntity.shouldRemove() || itemEntity.isRemoveScheduled())
                            continue;
                        PickupItemEvent pickupItemEvent = new PickupItemEvent(this, itemEntity);
                        EventDispatcher.callCancellable(pickupItemEvent, () -> {
                            final ItemStack item = itemEntity.getItemStack();

                            CollectItemPacket collectItemPacket = new CollectItemPacket();
                            collectItemPacket.collectedEntityId = itemEntity.getEntityId();
                            collectItemPacket.collectorEntityId = getEntityId();
                            collectItemPacket.pickupItemCount = item.getAmount();
                            sendPacketToViewersAndSelf(collectItemPacket);
                            entity.remove();
                        });
                    }
                }
            }
        }
    }

    /**
     * Gets the amount of arrows in the entity.
     *
     * @return the arrow count
     */
    public int getArrowCount() {
        return this.arrowCount;
    }

    /**
     * Changes the amount of arrow stuck in the entity.
     *
     * @param arrowCount the arrow count
     */
    public void setArrowCount(int arrowCount) {
        this.arrowCount = arrowCount;
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setArrowCount(arrowCount);
        }
    }

    /**
     * Gets if the entity is invulnerable.
     *
     * @return true if the entity is invulnerable
     */
    public boolean isInvulnerable() {
        return invulnerable;
    }

    /**
     * Makes the entity vulnerable or invulnerable.
     *
     * @param invulnerable should the entity be invulnerable
     */
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    /**
     * Kills the entity, trigger the {@link EntityDeathEvent} event.
     */
    public void kill() {
        refreshIsDead(true); // So the entity isn't killed over and over again
        triggerStatus((byte) 3); // Start death animation status
        setHealth(0);

        // Reset velocity
        velocity.zero();

        // Remove passengers if any
        if (hasPassenger()) {
            getPassengers().forEach(this::removePassenger);
        }

        EntityDeathEvent entityDeathEvent = new EntityDeathEvent(this);
        EventDispatcher.call(entityDeathEvent);
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration in ticks of the effect
     */
    public void setFireForDuration(int duration) {
        setFireForDuration(duration, TimeUnit.TICK);
    }

    /**
     * Sets fire to this entity for a given duration.
     *
     * @param duration duration of the effect
     * @param unit     unit used to express the duration
     * @see #setOnFire(boolean) if you want it to be permanent without any event callback
     */
    public void setFireForDuration(int duration, TimeUnit unit) {
        EntityFireEvent entityFireEvent = new EntityFireEvent(this, duration, unit);

        // Do not start fire event if the fire needs to be removed (< 0 duration)
        if (duration > 0) {
            EventDispatcher.callCancellable(entityFireEvent, () -> {
                final long fireTime = entityFireEvent.getFireTime(TimeUnit.MILLISECOND);
                setOnFire(true);
                fireExtinguishTime = System.currentTimeMillis() + fireTime;
            });
        } else {
            fireExtinguishTime = System.currentTimeMillis();
        }
    }

    /**
     * Damages the entity by a value, the type of the damage also has to be specified.
     *
     * @param type  the damage type
     * @param value the amount of damage
     * @return true if damage has been applied, false if it didn't
     */
    public boolean damage(@NotNull DamageType type, float value) {
        if (isDead())
            return false;
        if (isInvulnerable() || isImmune(type)) {
            return false;
        }

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(this, type, value);
        EventDispatcher.callCancellable(entityDamageEvent, () -> {
            // Set the last damage type since the event is not cancelled
            this.lastDamageSource = entityDamageEvent.getDamageType();

            float remainingDamage = entityDamageEvent.getDamage();

            EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
            entityAnimationPacket.entityId = getEntityId();
            entityAnimationPacket.animation = EntityAnimationPacket.Animation.TAKE_DAMAGE;
            sendPacketToViewersAndSelf(entityAnimationPacket);

            // Additional hearts support
            if (this instanceof Player) {
                final Player player = (Player) this;
                final float additionalHearts = player.getAdditionalHearts();
                if (additionalHearts > 0) {
                    if (remainingDamage > additionalHearts) {
                        remainingDamage -= additionalHearts;
                        player.setAdditionalHearts(0);
                    } else {
                        player.setAdditionalHearts(additionalHearts - remainingDamage);
                        remainingDamage = 0;
                    }
                }
            }

            // Set the final entity health
            setHealth(getHealth() - remainingDamage);

            // play damage sound
            final SoundEvent sound = type.getSound(this);
            if (sound != null) {
                Source soundCategory;
                if (this instanceof Player) {
                    soundCategory = Source.PLAYER;
                } else {
                    // TODO: separate living entity categories
                    soundCategory = Source.HOSTILE;
                }

                SoundEffectPacket damageSoundPacket =
                        SoundEffectPacket.create(soundCategory, sound,
                                getPosition(),
                                1.0f, 1.0f);
                sendPacketToViewersAndSelf(damageSoundPacket);
            }
        });

        return !entityDamageEvent.isCancelled();
    }

    /**
     * Is this entity immune to the given type of damage?
     *
     * @param type the type of damage
     * @return true if this entity is immune to the given type of damage
     */
    public boolean isImmune(@NotNull DamageType type) {
        return false;
    }

    /**
     * Gets the entity health.
     *
     * @return the entity health
     */
    public float getHealth() {
        return this.health;
    }

    /**
     * Changes the entity health, kill it if {@code health} is &gt;= 0 and is not dead yet.
     *
     * @param health the new entity health
     */
    public void setHealth(float health) {
        this.health = Math.min(health, getMaxHealth());
        if (this.health <= 0 && !isDead) {
            kill();
        }
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setHealth(this.health);
        }
    }

    /**
     * Gets the last damage source which damaged of this entity.
     *
     * @return the last damage source, null if not any
     */
    @Nullable
    public DamageType getLastDamageSource() {
        return lastDamageSource;
    }

    /**
     * Gets the entity max health from {@link #getAttributeValue(Attribute)} {@link Attributes#MAX_HEALTH}.
     *
     * @return the entity max health
     */
    public float getMaxHealth() {
        return getAttributeValue(Attribute.MAX_HEALTH);
    }

    /**
     * Sets the heal of the entity as its max health.
     * <p>
     * Retrieved from {@link #getAttributeValue(Attribute)} with the attribute {@link Attributes#MAX_HEALTH}.
     */
    public void heal() {
        setHealth(getAttributeValue(Attribute.MAX_HEALTH));
    }

    /**
     * Retrieves the attribute instance and its modifiers.
     *
     * @param attribute the attribute instance to get
     * @return the attribute instance
     */
    @NotNull
    public AttributeInstance getAttribute(@NotNull Attribute attribute) {
        return attributeModifiers.computeIfAbsent(attribute.getKey(),
                s -> new AttributeInstance(attribute, this::onAttributeChanged));
    }

    /**
     * Callback used when an attribute instance has been modified.
     *
     * @param attributeInstance the modified attribute instance
     */
    protected void onAttributeChanged(@NotNull AttributeInstance attributeInstance) {
        if (attributeInstance.getAttribute().isShared()) {
            boolean self = false;
            if (this instanceof Player) {
                Player player = (Player) this;
                PlayerConnection playerConnection = player.playerConnection;
                // connection null during Player initialization (due to #super call)
                self = playerConnection != null && playerConnection.getConnectionState() == ConnectionState.PLAY;
            }
            if (self) {
                sendPacketToViewersAndSelf(getPropertiesPacket());
            } else {
                sendPacketToViewers(getPropertiesPacket());
            }
        }
    }

    /**
     * Retrieves the attribute value.
     *
     * @param attribute the attribute value to get
     * @return the attribute value
     */
    public float getAttributeValue(@NotNull Attribute attribute) {
        AttributeInstance instance = attributeModifiers.get(attribute.getKey());
        return (instance != null) ? instance.getValue() : attribute.getDefaultValue();
    }

    /**
     * Gets if the entity is dead or not.
     *
     * @return true if the entity is dead
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Gets if the entity is able to pickup items.
     *
     * @return true if the entity is able to pickup items
     */
    public boolean canPickupItem() {
        return canPickupItem;
    }

    /**
     * When set to false, the entity will not be able to pick {@link ItemEntity} on the ground.
     *
     * @param canPickupItem can the entity pickup item
     */
    public void setCanPickupItem(boolean canPickupItem) {
        this.canPickupItem = canPickupItem;
    }

    @Override
    protected boolean addViewer0(@NotNull Player player) {
        if (!super.addViewer0(player)) {
            return false;
        }
        final PlayerConnection playerConnection = player.getPlayerConnection();
        playerConnection.sendPacket(getEquipmentsPacket());
        playerConnection.sendPacket(getPropertiesPacket());

        if (getTeam() != null) {
            playerConnection.sendPacket(getTeam().createTeamsCreationPacket());
        }

        return true;
    }

    @Override
    public void setBoundingBox(double x, double y, double z) {
        super.setBoundingBox(x, y, z);
        this.expandedBoundingBox = getBoundingBox().expand(1, 0.5f, 1);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the main hand
     * (can be used for attack animation).
     */
    public void swingMainHand() {
        EntityAnimationPacket animationPacket = new EntityAnimationPacket();
        animationPacket.entityId = getEntityId();
        animationPacket.animation = EntityAnimationPacket.Animation.SWING_MAIN_ARM;
        sendPacketToViewers(animationPacket);
    }

    /**
     * Sends a {@link EntityAnimationPacket} to swing the off hand
     * (can be used for attack animation).
     */
    public void swingOffHand() {
        EntityAnimationPacket animationPacket = new EntityAnimationPacket();
        animationPacket.entityId = getEntityId();
        animationPacket.animation = EntityAnimationPacket.Animation.SWING_OFF_HAND;
        sendPacketToViewers(animationPacket);
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        LivingEntityMeta meta = getLivingEntityMeta();
        if (meta != null) {
            meta.setNotifyAboutChanges(false);
            meta.setHandActive(isHandActive);
            meta.setActiveHand(offHand ? Player.Hand.OFF : Player.Hand.MAIN);
            meta.setInRiptideSpinAttack(riptideSpinAttack);
            meta.setNotifyAboutChanges(true);
        }
    }

    public boolean isFlyingWithElytra() {
        return this.entityMeta.isFlyingWithElytra();
    }

    public void setFlyingWithElytra(boolean isFlying) {
        this.entityMeta.setFlyingWithElytra(isFlying);
    }

    /**
     * Used to change the {@code isDead} internal field.
     *
     * @param isDead the new field value
     */
    protected void refreshIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    /**
     * Gets an {@link EntityPropertiesPacket} for this entity with all of its attributes values.
     *
     * @return an {@link EntityPropertiesPacket} linked to this entity
     */
    @NotNull
    protected EntityPropertiesPacket getPropertiesPacket() {
        // Get all the attributes which should be sent to the client
        final AttributeInstance[] instances = attributeModifiers.values().stream()
                .filter(i -> i.getAttribute().isShared())
                .toArray(AttributeInstance[]::new);


        EntityPropertiesPacket propertiesPacket = new EntityPropertiesPacket();
        propertiesPacket.entityId = getEntityId();

        EntityPropertiesPacket.Property[] properties = new EntityPropertiesPacket.Property[instances.length];
        for (int i = 0; i < properties.length; ++i) {
            EntityPropertiesPacket.Property property = new EntityPropertiesPacket.Property();

            final float value = instances[i].getBaseValue();

            property.instance = instances[i];
            property.attribute = instances[i].getAttribute();
            property.value = value;

            properties[i] = property;
        }

        propertiesPacket.properties = properties;
        return propertiesPacket;
    }

    @Override
    protected void handleVoid() {
        // Kill if in void
        if (getInstance().isInVoid(this.position)) {
            damage(DamageType.VOID, 10f);
        }
    }

    /**
     * Gets the time in ms between two fire damage applications.
     *
     * @return the time in ms
     * @see #setFireDamagePeriod(long, TimeUnit)
     */
    public long getFireDamagePeriod() {
        return fireDamagePeriod;
    }

    /**
     * Changes the delay between two fire damage applications.
     *
     * @param fireDamagePeriod the delay
     * @param timeUnit         the time unit
     */
    public void setFireDamagePeriod(long fireDamagePeriod, @NotNull TimeUnit timeUnit) {
        fireDamagePeriod = timeUnit.toMilliseconds(fireDamagePeriod);
        this.fireDamagePeriod = fireDamagePeriod;
    }

    /**
     * Changes the {@link Team} for the entity.
     *
     * @param team The new team
     */
    public void setTeam(Team team) {
        if (this.team == team) return;

        String member;

        if (this instanceof Player) {
            Player player = (Player) this;
            member = player.getUsername();
        } else {
            member = this.uuid.toString();
        }

        if (this.team != null) {
            this.team.removeMember(member);
        }

        this.team = team;
        if (team != null) {
            team.addMember(member);
        }
    }

    /**
     * Gets the {@link Team} of the entity.
     *
     * @return the {@link Team}
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Gets the line of sight in {@link BlockPosition} of the entity.
     *
     * @param maxDistance The max distance to scan
     * @return A list of {@link BlockPosition} in this entities line of sight
     */
    public List<BlockPosition> getLineOfSight(int maxDistance) {
        List<BlockPosition> blocks = new ArrayList<>();
        Iterator<BlockPosition> it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            BlockPosition position = it.next();
            if (Block.fromStateId(getInstance().getBlockStateId(position)) != Block.AIR) blocks.add(position);
        }
        return blocks;
    }

    /**
     * Checks whether the current entity has line of sight to the given one.
     * If so, it doesn't mean that the given entity is IN line of sight of the current,
     * but the current one can rotate so that it will be true.
     *
     * @param entity the entity to be checked.
     * @return if the current entity has line of sight to the given one.
     */
    public boolean hasLineOfSight(Entity entity) {
        Vector start = getPosition().toVector().add(0D, getEyeHeight(), 0D);
        Vector end = entity.getPosition().toVector().add(0D, getEyeHeight(), 0D);
        Vector direction = end.subtract(start);
        int maxDistance = (int) Math.ceil(direction.length());

        Iterator<BlockPosition> it = new BlockIterator(start, direction.normalize(), 0D, maxDistance);
        while (it.hasNext()) {
            Block block = Block.fromStateId(getInstance().getBlockStateId(it.next()));
            if (!block.isAir() && !block.isLiquid()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the target (not-air) {@link BlockPosition} of the entity.
     *
     * @param maxDistance The max distance to scan before returning null
     * @return The {@link BlockPosition} targeted by this entity, null if non are found
     */
    public BlockPosition getTargetBlockPosition(int maxDistance) {
        Iterator<BlockPosition> it = new BlockIterator(this, maxDistance);
        while (it.hasNext()) {
            BlockPosition position = it.next();
            if (Block.fromStateId(getInstance().getBlockStateId(position)) != Block.AIR) return position;
        }
        return null;
    }

    /**
     * Gets {@link net.minestom.server.entity.metadata.EntityMeta} of this entity casted to {@link LivingEntityMeta}.
     *
     * @return null if meta of this entity does not inherit {@link LivingEntityMeta}, casted value otherwise.
     */
    public LivingEntityMeta getLivingEntityMeta() {
        if (this.entityMeta instanceof LivingEntityMeta) {
            return (LivingEntityMeta) this.entityMeta;
        }
        return null;
    }

}
