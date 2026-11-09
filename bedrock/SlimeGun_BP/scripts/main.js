import { BlockPermutation, GameMode, LocationInUnloadedChunkError, LocationOutOfWorldBoundariesError, system, world } from "@minecraft/server";

const gun = "slimegun:slime_gun";
const cocoon = "slimegun:slime_cocoon";
// The API typings list vanilla states only. The behavior pack defines this state.
const age = /** @type {Parameters<BlockPermutation["getState"]>[0]} */ (
    /** @type {unknown} */ ("slimegun:remaining")
);
const permanent = /** @type {typeof age} */ (/** @type {unknown} */ ("slimegun:permanent"));
const ammo = "minecraft:slime_ball";
const projectile = "slimegun:slimeball_shot";
const range = 28;
const speed = 0.85;
const trapProperty = "slimegun:trap";
const trappedName = "\u2063";
const cooldowns = new Map();
const frozen = new Map();
const shots = new Map();

system.beforeEvents.startup.subscribe(({ itemComponentRegistry, blockComponentRegistry }) => {
    itemComponentRegistry.registerCustomComponent("slimegun:fire", {
        onUse: ({ source }) => fire(source),
        onUseOn: ({ source }) => fire(source),
    });
    blockComponentRegistry.registerCustomComponent("slimegun:decay", {
        onTick({ block }) {
            if (block.typeId !== cocoon || block.permutation.getState(permanent) === true) return;
            const remaining = Number(block.permutation.getState(age));
            if (remaining > 0) {
                block.setPermutation(block.permutation.withState(age, remaining - 1));
            } else {
                block.setType("minecraft:air");
            }
        },
    });
});

world.afterEvents.playerLeave.subscribe(({ playerId }) => cooldowns.delete(playerId));
world.afterEvents.entityLoad.subscribe(({ entity }) => restoreTrap(entity));
world.afterEvents.worldLoad.subscribe(() => {
    for (const dimension of ["overworld", "nether", "the_end"]) {
        for (const entity of world.getDimension(dimension).getEntities()) restoreTrap(entity);
    }
});

function restoreTrap(entity) {
    const saved = entity.getDynamicProperty(trapProperty);
    if (typeof saved !== "string") return;
    const trap = JSON.parse(saved);
    frozen.set(entity.id, { ...trap, entity, dimension: world.getDimension(trap.dimensionId) });
}

function release(entity, addedName) {
    if (addedName && entity.nameTag === trappedName) entity.nameTag = "";
    entity.setDynamicProperty(trapProperty, undefined);
    frozen.delete(entity.id);
}

function fire(player) {
    const now = system.currentTick;
    if ((cooldowns.get(player.id) ?? 0) > now) return;

    const inventory = player.getComponent("minecraft:inventory")?.container;
    const slot = player.selectedSlotIndex;
    const weapon = inventory?.getItem(slot);
    if (!inventory || weapon?.typeId !== gun) return;

    const creative = player.getGameMode() === GameMode.Creative;
    if (!creative && player.getGameMode() !== GameMode.Survival && player.getGameMode() !== GameMode.Adventure) return;
    let ammoSlot = -1;
    let slimeball;
    if (!creative) {
        for (let i = 0; i < inventory.size; i++) {
            const stack = inventory.getItem(i);
            if (stack?.typeId !== ammo) continue;
            ammoSlot = i;
            slimeball = stack;
            break;
        }
        if (!slimeball) {
            cooldowns.set(player.id, now + 10);
            player.onScreenDisplay.setActionBar("You need a slimeball to fire.");
            return;
        }
    }

    const position = player.getHeadLocation();
    const direction = player.getViewDirection();
    const dimension = player.dimension;
    const visual = dimension.spawnEntity(projectile, position);
    shots.set(visual.id, {
        visual, position, direction, dimension,
        ownerId: player.id, remaining: range,
    });

    cooldowns.set(player.id, now + 20);
    if (slimeball) {
        if (slimeball.amount === 1) inventory.setItem(ammoSlot, undefined);
        else {
            slimeball.amount--;
            inventory.setItem(ammoSlot, slimeball);
        }
        const durability = weapon.getComponent("minecraft:durability");
        if (durability) {
            if (durability.damage + 1 >= durability.maxDurability) inventory.setItem(slot, undefined);
            else {
                durability.damage++;
                inventory.setItem(slot, weapon);
            }
        }
    }

}

function pointAlong(position, direction, distance) {
    return {
        x: position.x + direction.x * distance,
        y: position.y + direction.y * distance,
        z: position.z + direction.z * distance,
    };
}

function advanceShot(shot) {
    const { visual, position, direction, dimension, ownerId } = shot;
    const step = Math.min(speed, shot.remaining);
    const next = pointAlong(position, direction, step);
    if (!visual.isValid || !dimension.isChunkLoaded(next) || !loadedBlock(dimension, next)) return false;

    // Sweep only this tick's flight segment. The target can move into or out
    // of the shot before it arrives. Our own passable slime lets shots refresh it.
    const options = { maxDistance: Math.ceil(step), includePassableBlocks: false };
    const wall = dimension.getBlockFromRay(position, direction, { ...options, excludeTypes: [cocoon] });
    const wallDistance = wall ? Math.hypot(
        wall.block.x + wall.faceLocation.x - position.x,
        wall.block.y + wall.faceLocation.y - position.y,
        wall.block.z + wall.faceLocation.z - position.z,
    ) : Infinity;
    const hit = dimension.getEntitiesFromRay(position, direction, { ...options, ignoreBlockCollision: true })
        .filter(({ entity, distance }) => distance <= step && distance < wallDistance &&
            entity.id !== ownerId && entity.isValid && entity.typeId !== "minecraft:player" &&
            entity.typeId !== "minecraft:armor_stand" && entity.typeId !== projectile &&
            (entity.getComponent("minecraft:health")?.currentValue ?? 0) > 0)
        .sort((first, second) => first.distance - second.distance)[0];
    if (hit) {
        encase(hit.entity);
        return false;
    }
    if (wallDistance <= step) return false;
    if (!visual.tryTeleport(next, { dimension, checkForBlocks: false })) return false;
    shot.position = next;
    shot.remaining -= step;
    return shot.remaining > 0;
}

function encase(entity) {
    const dimension = entity.dimension;
    const anchor = entity.location;
    const { center, extent } = entity.getAABB();
    // Dragons have multipart geometry that can extend beyond the main hitbox.
    const horizontalPadding = entity.typeId === "minecraft:ender_dragon" ? 3 : 0.18;
    const minX = Math.floor(center.x - extent.x - horizontalPadding);
    const maxX = Math.floor(center.x + extent.x + horizontalPadding);
    const minZ = Math.floor(center.z - extent.z - horizontalPadding);
    const maxZ = Math.floor(center.z + extent.z + horizontalPadding);
    const bottom = Math.floor(center.y - extent.y + 0.0001);
    const top = Math.floor(center.y + extent.y + 0.18);
    const bounds = { minX, maxX, bottom, top, minZ, maxZ };
    const fresh = BlockPermutation.resolve(cocoon, { [permanent]: true });
    let placed = false;
    for (let x = minX; x <= maxX; x++) {
        for (let z = minZ; z <= maxZ; z++) {
            for (let y = bottom; y <= top; y++) {
                const location = { x, y, z };
                const block = loadedBlock(dimension, location);
                if (!block || (!block.isAir && block.typeId !== cocoon)) continue;
                block.setPermutation(fresh);
                placed = true;
            }
        }
    }
    if (!placed) return false;

    const needsName = entity.nameTag === "";
    const addedName = needsName || (frozen.get(entity.id)?.addedName && entity.nameTag === trappedName) || false;
    const trap = { anchor, rotation: entity.getRotation(), bounds, dimensionId: dimension.id, addedName };
    entity.setDynamicProperty(trapProperty, JSON.stringify(trap));
    // Keep unnamed captives from ordinary distance despawning. Preserve custom names.
    if (needsName) entity.nameTag = trappedName;
    frozen.set(entity.id, { ...trap, entity, dimension });
    entity.clearVelocity();
    return true;
}

function loadedBlock(dimension, location) {
    try {
        return dimension.getBlock(location);
    } catch (error) {
        if (error instanceof LocationInUnloadedChunkError ||
            error instanceof LocationOutOfWorldBoundariesError) return undefined;
        throw error;
    }
}

function cocoonStillPresent(dimension, bounds) {
    const { minX, maxX, bottom, top, minZ, maxZ } = bounds;
    let unloaded = false;
    for (let x = minX; x <= maxX; x++) {
        for (let z = minZ; z <= maxZ; z++) {
            if (!dimension.isChunkLoaded({ x, y: bottom, z })) {
                unloaded = true;
                continue;
            }
            for (let y = bottom; y <= top; y++) {
                if (loadedBlock(dimension, { x, y, z })?.typeId === cocoon) return true;
            }
        }
    }
    // The absent part of an unloaded shell cannot be checked until it loads.
    return unloaded;
}

system.runInterval(() => {
    for (const [id, shot] of shots) {
        if (advanceShot(shot)) continue;
        shots.delete(id);
        if (shot.visual.isValid) shot.visual.remove();
    }
    for (const [id, trap] of frozen) {
        const { entity, dimension, anchor, rotation, bounds, addedName } = trap;
        if (!entity.isValid) {
            frozen.delete(id);
            continue;
        }
        if (entity.dimension.id !== dimension.id ||
            (system.currentTick % 5 === 0 && !cocoonStillPresent(dimension, bounds))) {
            release(entity, addedName);
            continue;
        }
        // Bedrock's public scripting API exposes movement and effects, but no
        // general-purpose pause for vanilla mob AI.
        entity.clearVelocity();
        if (!entity.tryTeleport(anchor, { dimension, rotation, checkForBlocks: false })) continue;
        if (system.currentTick % 5 === 0) {
            entity.addEffect("minecraft:slowness", 8, { amplifier: 6, showParticles: false });
            entity.addEffect("minecraft:weakness", 8, { amplifier: 4, showParticles: false });
        }
    }
}, 1);
