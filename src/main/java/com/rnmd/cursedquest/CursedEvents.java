package com.rnmd.cursedquest;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CursedQuest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CursedEvents {
    private static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();
    private static final List<PendingScare> PENDING_SCARES = new ArrayList<>();
    private static int ambientCountdown = 20 * 180;

    private CursedEvents() {
    }

    public static int randomInt(int bound) {
        return bound <= 0 ? 0 : RANDOM.nextInt(bound);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        int stage = QuestManager.getStage(player);
        LivingEntity victim = event.getEntity();
        if (stage == 0 && victim instanceof Monster && isNight(level)) {
            QuestManager.addCount(player, 1, 8, "Ночные устранения:");
            return;
        }
        if (stage == 8 && victim instanceof EnderMan
                && player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN)) {
            QuestManager.addCount(player, 1, 2, "Эндермены в тыквенной бухгалтерии:");
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (QuestManager.getStage(player) != 7 || event.getPos().getY() > 0) return;
        if (event.getState().is(net.minecraft.world.level.block.Blocks.DEEPSLATE)
                || event.getState().is(net.minecraft.world.level.block.Blocks.COBBLED_DEEPSLATE)) {
            QuestManager.addCount(player, 1, 16, "Добыча глубинного сланца:");
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) return;
        if (QuestManager.getStage(player) != 4) return;
        if (!event.getLevel().getBlockState(event.getPos()).is(net.minecraft.world.level.block.Blocks.BELL)) return;
        if (!isNight(player.serverLevel())) {
            player.sendSystemMessage(Component.literal("[Квест II] Колокол молчит. Он работает только после заката.")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            return;
        }
        long gameTick = player.serverLevel().getGameTime();
        if (player.getPersistentData().getLong("cursedquest_v2_last_bell_tick") == gameTick) return;
        player.getPersistentData().putLong("cursedquest_v2_last_bell_tick", gameTick);
        QuestManager.addCount(player, 1, 5, "Ночные звонки:");
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (QuestManager.getStage(player) != 10) return;
        if (!"я не слышал".equalsIgnoreCase(event.getRawText().trim())) return;

        BlockPos pos = player.blockPosition();
        int blockLight = player.serverLevel().getBrightness(LightLayer.BLOCK, pos);
        if (pos.getY() < 20 && blockLight <= 3) {
            QuestManager.completeStage(player);
        } else {
            player.sendSystemMessage(Component.literal("[ПРОТОКОЛ] Показания отклонены: слишком светло или слишком высоко.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            if (randomInt(3) == 0) {
                player.sendSystemMessage(Component.literal("%%VOICE_CAPTURED%% НЕ ОГЛЯДЫВАЙСЯ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.OBFUSCATED));
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            QuestManager.showCurrentQuest(player);
            player.sendSystemMessage(Component.literal("[Квест II] Новая глава активна. Старую версию мода нужно заменить этой сборкой.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        var oldData = oldPlayer.getPersistentData();
        var newData = newPlayer.getPersistentData();
        int copiedStage = oldData.getInt(QuestManager.STAGE_KEY);
        newData.putInt(QuestManager.STAGE_KEY, copiedStage);
        int copiedCount = oldData.getInt(QuestManager.COUNT_KEY);
        // Continuous-survival challenges restart if the player dies mid-attempt.
        if (copiedStage == 3 || copiedStage == 11
                || (copiedStage == 6 && !oldData.getBoolean(QuestManager.NETHER_SURVIVED_KEY))) {
            copiedCount = 0;
        }
        newData.putInt(QuestManager.COUNT_KEY, copiedCount);
        newData.putDouble(QuestManager.DISTANCE_KEY, oldData.getDouble(QuestManager.DISTANCE_KEY));
        newData.putDouble(QuestManager.LAST_X_KEY, oldData.getDouble(QuestManager.LAST_X_KEY));
        newData.putDouble(QuestManager.LAST_Y_KEY, oldData.getDouble(QuestManager.LAST_Y_KEY));
        newData.putDouble(QuestManager.LAST_Z_KEY, oldData.getDouble(QuestManager.LAST_Z_KEY));
        newData.putBoolean(QuestManager.HAS_LAST_POS_KEY, oldData.getBoolean(QuestManager.HAS_LAST_POS_KEY));
        newData.putBoolean(QuestManager.NETHER_SURVIVED_KEY, oldData.getBoolean(QuestManager.NETHER_SURVIVED_KEY));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        int stage = QuestManager.getStage(player);
        if (stage >= QuestManager.MAX_STAGE) return;

        switch (stage) {
            case 1 -> {
                if (QuestManager.hasItems(player, Items.COAL, 12) && QuestManager.hasItems(player, Items.BONE, 8)) {
                    QuestManager.consumeItems(player, Items.COAL, 12);
                    QuestManager.consumeItems(player, Items.BONE, 8);
                    QuestManager.completeStage(player);
                }
            }
            case 2 -> tickDistanceQuest(player);
            case 3 -> {
                if (player.isUnderWater() || player.isEyeInFluid(FluidTags.WATER)) {
                    int ticks = QuestManager.getCount(player) + 1;
                    QuestManager.setCount(player, ticks);
                    if (ticks >= 40 * 20) {
                        QuestManager.completeStage(player);
                    } else if (ticks % 200 == 0) {
                        QuestManager.questMessage(player, "Подводная сертификация: " + (ticks / 20) + "/40 секунд.");
                    }
                } else {
                    QuestManager.setCount(player, 0);
                }
            }
            case 6 -> tickNetherQuest(player);
            case 9 -> {
                if (QuestManager.hasItems(player, Items.AMETHYST_SHARD, 3) && QuestManager.hasItems(player, Items.DIAMOND, 1)) {
                    QuestManager.consumeItems(player, Items.AMETHYST_SHARD, 3);
                    QuestManager.consumeItems(player, Items.DIAMOND, 1);
                    QuestManager.completeStage(player);
                }
            }
            case 11 -> tickFinalProtocol(player);
            default -> { }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        Iterator<PendingScare> iterator = PENDING_SCARES.iterator();
        while (iterator.hasNext()) {
            PendingScare pending = iterator.next();
            pending.ticksRemaining--;
            if (pending.ticksRemaining <= 0) {
                ServerPlayer target = server.getPlayerList().getPlayer(pending.target);
                if (target != null) {
                    sendScare(target, pending.type);
                    server.getPlayerList().broadcastSystemMessage(Component.literal("Ты слышал это?..")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
                }
                iterator.remove();
            }
        }

        ambientCountdown--;
        if (ambientCountdown <= 0) {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                ServerPlayer selected = players.get(RANDOM.nextInt(players.size()));
                runAmbientEvent(selected);
            }
            ambientCountdown = 20 * (180 + RANDOM.nextInt(300));
        }
    }

    public static void scheduleScareForOtherPlayer(ServerPlayer owner, int delayTicks) {
        MinecraftServer server = owner.getServer();
        if (server == null) return;
        List<ServerPlayer> otherPlayers = server.getPlayerList().getPlayers().stream()
                .filter(candidate -> !candidate.getUUID().equals(owner.getUUID()))
                .toList();
        if (otherPlayers.isEmpty()) {
            QuestManager.questMessage(owner, "Скример ждёт второго игрока. Он не хочет портить атмосферу в одиночку.");
            return;
        }
        ServerPlayer target = otherPlayers.get(randomInt(otherPlayers.size()));
        PENDING_SCARES.add(new PendingScare(target.getUUID(), Math.max(20, delayTicks), randomInt(12)));
        QuestManager.questMessage(owner, "Техническая проверка назначена другому участнику. Не сообщай ему детали.");
    }

    private static void tickDistanceQuest(ServerPlayer player) {
        var data = player.getPersistentData();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (!data.getBoolean(QuestManager.HAS_LAST_POS_KEY)) {
            savePosition(player, x, y, z);
            return;
        }

        double oldX = data.getDouble(QuestManager.LAST_X_KEY);
        double oldY = data.getDouble(QuestManager.LAST_Y_KEY);
        double oldZ = data.getDouble(QuestManager.LAST_Z_KEY);
        double dx = x - oldX;
        double dz = z - oldZ;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (!player.isPassenger() && horizontalDistance <= 1.5D) {
            double distance = data.getDouble(QuestManager.DISTANCE_KEY) + horizontalDistance;
            data.putDouble(QuestManager.DISTANCE_KEY, distance);
            if ((int) distance >= 512) {
                QuestManager.completeStage(player);
                return;
            }
            if ((int) distance > 0 && (int) distance % 128 == 0 && (int) distance != (int) (distance - horizontalDistance)) {
                QuestManager.questMessage(player, "Пешком пройдено: " + (int) distance + "/512 блоков. Транспортная комиссия недовольна.");
            }
        }
        savePosition(player, x, y, z);
    }

    private static void savePosition(ServerPlayer player, double x, double y, double z) {
        var data = player.getPersistentData();
        data.putDouble(QuestManager.LAST_X_KEY, x);
        data.putDouble(QuestManager.LAST_Y_KEY, y);
        data.putDouble(QuestManager.LAST_Z_KEY, z);
        data.putBoolean(QuestManager.HAS_LAST_POS_KEY, true);
    }

    private static void tickNetherQuest(ServerPlayer player) {
        var data = player.getPersistentData();
        boolean survived = data.getBoolean(QuestManager.NETHER_SURVIVED_KEY);
        if (!survived) {
            if (player.level().dimension().equals(Level.NETHER)) {
                int ticks = QuestManager.getCount(player) + 1;
                QuestManager.setCount(player, ticks);
                if (ticks >= 60 * 20) {
                    data.putBoolean(QuestManager.NETHER_SURVIVED_KEY, true);
                    QuestManager.questMessage(player, "Минута в Незере подтверждена. Теперь вернись в Верхний мир с 4 стержнями ифрита.");
                } else if (ticks % 200 == 0) {
                    QuestManager.questMessage(player, "Адовое дежурство: " + (ticks / 20) + "/60 секунд.");
                }
            } else {
                QuestManager.setCount(player, 0);
            }
            return;
        }

        if (player.level().dimension().equals(Level.OVERWORLD) && QuestManager.hasItems(player, Items.BLAZE_ROD, 4)) {
            QuestManager.consumeItems(player, Items.BLAZE_ROD, 4);
            QuestManager.completeStage(player);
        }
    }

    private static void tickFinalProtocol(ServerPlayer player) {
        boolean valid = false;
        List<ServerPlayer> online = player.getServer().getPlayerList().getPlayers();
        if (online.size() >= 2) {
            for (ServerPlayer other : online) {
                if (other == player || other.level() != player.level()) continue;
                if (other.distanceToSqr(player) <= 36.0D && other.isCrouching() && player.isCrouching()) {
                    valid = true;
                    break;
                }
            }
        } else {
            valid = player.isCrouching()
                    && player.getY() <= 0.0D
                    && player.serverLevel().getBrightness(LightLayer.BLOCK, player.blockPosition()) <= 4
                    && player.getInventory().items.stream().anyMatch(stack -> stack.is(Items.CLOCK));
        }

        int ticks = valid ? QuestManager.getCount(player) + 1 : 0;
        QuestManager.setCount(player, ticks);
        if (ticks >= 10 * 20) {
            QuestManager.completeStage(player);
        } else if (valid && ticks > 0 && ticks % 100 == 0) {
            QuestManager.questMessage(player, "Протокол синхронизации: " + (ticks / 20) + "/10 секунд. Не двигайся.");
        }
    }

    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time <= 23000L;
    }

    private static void runAmbientEvent(ServerPlayer player) {
        int roll = RANDOM.nextInt(100);
        if (roll < 23) {
            int[] sudden = {0, 1, 5, 6, 7, 8, 10};
            sendScare(player, sudden[RANDOM.nextInt(sudden.length)]); // Deliberately no warning.
            return;
        }
        if (roll < 35) {
            player.sendSystemMessage(Component.literal("[СИСТЕМА] Вы умерли. Повторная проверка: пока нет.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH));
        } else if (roll < 46) {
            spawnWitnessChicken(player);
        } else if (roll < 57) {
            whisperBehindPlayer(player);
        } else if (roll < 68) {
            player.sendSystemMessage(Component.literal("[АУДИОСИСТЕМА] Ложная тревога. Источник шума не найден.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            PENDING_SCARES.add(new PendingScare(player.getUUID(), 20 * (8 + RANDOM.nextInt(18)), 3 + RANDOM.nextInt(9)));
        } else if (roll < 78) {
            ItemStack receipt = new ItemStack(Items.PAPER);
            receipt.setHoverName(Component.literal("Квитанция за воздух №404").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            if (!player.getInventory().add(receipt)) player.drop(receipt, false);
            QuestManager.questMessage(player, "Вам выставлен счёт за использование атмосферы. Оплатить нельзя.");
        } else if (roll < 89) {
            player.sendSystemMessage(Component.literal("Достижение получено: НЕ ОБОРАЧИВАЙСЯ")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT, SoundSource.AMBIENT, 0.8F, 0.65F);
        } else {
            sendScare(player, 4);
            player.sendSystemMessage(Component.literal("Internal Exception: java.net.SocketException: Connection reset (это симуляция)")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static void spawnWitnessChicken(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Chicken chicken = EntityType.CHICKEN.create(level);
        if (chicken == null) return;
        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        chicken.moveTo(player.getX() + Math.cos(angle) * 2.5D, player.getY(),
                player.getZ() + Math.sin(angle) * 2.5D, RANDOM.nextFloat() * 360.0F, 0.0F);
        chicken.setCustomName(Component.literal("Свидетель №404").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        chicken.setCustomNameVisible(true);
        chicken.setNoAi(true);
        chicken.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 20 * 45, 0, false, false));
        level.addFreshEntity(chicken);
        QuestManager.questMessage(player, "Свидетель №404 прибыл. Не задавай ему вопросов: он отвечает только в суде.");
    }

    private static void whisperBehindPlayer(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double angle = player.getYRot() * (Math.PI / 180.0D);
        double x = player.getX() - Math.sin(angle) * 2.7D;
        double z = player.getZ() + Math.cos(angle) * 2.7D;
        BlockPos position = BlockPos.containing(x, player.getY() + 1.0D, z);
        level.sendParticles(ParticleTypes.SMOKE, x, player.getY() + 1.0D, z, 18, 0.25D, 0.4D, 0.25D, 0.01D);
        level.playSound(null, position, SoundEvents.ENDERMAN_STARE, SoundSource.AMBIENT, 0.8F, 0.65F);
        if (randomInt(3) == 0) {
            level.playSound(null, position, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.AMBIENT, 0.3F, 1.1F);
        }
    }

    private static void sendScare(ServerPlayer player, int type) {
        int safeType = Math.max(0, Math.min(11, type));
        ModNetworking.sendScare(player, safeType);
        playScareAudio(player, safeType);
    }

    public static void playScareAudio(ServerPlayer player, int type) {
        ServerLevel level = player.serverLevel();
        BlockPos at = player.blockPosition();
        SoundEvent first;
        SoundEvent second;
        float volume = 0.95F;
        float pitch = 0.8F + RANDOM.nextFloat() * 0.45F;
        switch (Math.floorMod(type, 12)) {
            case 0 -> { first = SoundEvents.ENDERMAN_SCREAM; second = SoundEvents.GHAST_SCREAM; }
            case 1 -> { first = SoundEvents.ELDER_GUARDIAN_AMBIENT; second = SoundEvents.SCULK_SHRIEKER_SHRIEK; }
            case 2 -> { first = SoundEvents.DROWNED_HURT; second = SoundEvents.GUARDIAN_HURT; }
            case 3 -> { first = SoundEvents.WITHER_SPAWN; second = SoundEvents.ENDERMAN_STARE; volume = 0.55F; }
            case 4 -> { first = SoundEvents.ENDERMAN_TELEPORT; second = SoundEvents.PHANTOM_SWOOP; volume = 0.7F; }
            case 5 -> { first = SoundEvents.PHANTOM_SWOOP; second = SoundEvents.ENDERMAN_STARE; }
            case 6 -> { first = SoundEvents.WARDEN_SONIC_BOOM; second = SoundEvents.GHAST_SCREAM; }
            case 7 -> { first = SoundEvents.ENDERMAN_SCREAM; second = SoundEvents.WARDEN_ROAR; }
            case 8 -> { first = SoundEvents.SCULK_SHRIEKER_SHRIEK; second = SoundEvents.ELDER_GUARDIAN_CURSE; }
            case 9 -> { first = SoundEvents.WITHER_SPAWN; second = SoundEvents.WARDEN_ROAR; }
            case 10 -> { first = SoundEvents.ELDER_GUARDIAN_AMBIENT; second = SoundEvents.ENDERMAN_STARE; volume = 0.65F; }
            default -> { first = SoundEvents.GHAST_SCREAM; second = SoundEvents.SCULK_SHRIEKER_SHRIEK; }
        }
        level.playSound(null, at, first, SoundSource.PLAYERS, volume, pitch);
        level.playSound(null, at, second, SoundSource.AMBIENT, Math.min(1.0F, volume * 0.72F), 0.65F + RANDOM.nextFloat() * 0.3F);
    }

    private static final class PendingScare {
        private final UUID target;
        private int ticksRemaining;
        private final int type;

        private PendingScare(UUID target, int ticksRemaining, int type) {
            this.target = target;
            this.ticksRemaining = ticksRemaining;
            this.type = Math.max(0, Math.min(11, type));
        }
    }
}
