package com.rnmd.cursedquest;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class QuestManager {
    public static final int MAX_STAGE = 12;
    public static final String STAGE_KEY = "cursedquest_v2_stage";
    public static final String COUNT_KEY = "cursedquest_v2_count";
    public static final String DISTANCE_KEY = "cursedquest_v2_distance";
    public static final String LAST_X_KEY = "cursedquest_v2_last_x";
    public static final String LAST_Y_KEY = "cursedquest_v2_last_y";
    public static final String LAST_Z_KEY = "cursedquest_v2_last_z";
    public static final String HAS_LAST_POS_KEY = "cursedquest_v2_has_last_pos";
    public static final String NETHER_SURVIVED_KEY = "cursedquest_v2_nether_survived";

    private static final String[] TASKS = {
            "Убей 8 враждебных мобов ночью. Комиссия по безопасности хочет поговорить.",
            "Принеси 12 угля и 8 костей. Не спрашивай, почему бухгалтерия ест уголь.",
            "Пройди пешком 512 блоков. Телепортация считается попыткой мошенничества.",
            "Проведи 40 секунд непрерывно под водой. Подписка на воздух временно отменена.",
            "Позвони в колокол 5 раз после наступления ночи. Лучше не спрашивай, кто ответит.",
            "Сдай 4 жемчуга Края. Геометрия требует залог за нарушение пространства.",
            "Проведи 60 секунд в Незере, вернись в обычный мир и принеси 4 стержня ифрита.",
            "На глубине Y≤0 сломай 16 блоков глубинного сланца. Геологи уже плачут.",
            "Убей 2 эндерменов, надев тыкву на голову. Не смотри на них. Они тоже не хотят.",
            "Сдай 3 аметистовых осколка и 1 алмаз. Компенсация принимается только блестящим.",
            "Ниже Y=20 и в темноте напиши в чат: я не слышал. Это не признание. Пока.",
            "Финальный протокол: вместе с другом стойте рядом и крадитесь 10 секунд. В одиночке — часы в руке, глубоко под землёй и тоже 10 секунд."
    };

    private static final String[] HINTS = {
            "Срабатывают только убийства, совершённые ночью игроком.",
            "Нужные вещи заберут автоматически, когда обе стопки будут в инвентаре.",
            "Считается горизонтальное перемещение без транспорта и телепортаций.",
            "Если вынырнуть хоть на миг, счётчик обнулится. Да, это подло.",
            "Колокол можно найти в деревне. Ночь — это время мира 13000–23000.",
            "Жемчуг заберут автоматически после сбора всех четырёх.",
            "Сначала переживи минуту в Незере. Затем вернись в Верхний мир со стержнями.",
            "Подойдут обычный и булыжный глубинный сланец, но высота должна быть 0 или ниже.",
            "Надень вырезанную тыкву именно в слот головы до убийства.",
            "Алмаз и аметисты исчезнут при сдаче. Это называется аудит.",
            "Условие проверяется на сервере; сообщение в обычном чате не отменяется.",
            "В мультиплеере оба игрока должны красться. В одиночке держи часы в инвентаре."
    };

    private QuestManager() {
    }

    public static int getStage(Player player) {
        return Math.max(0, Math.min(MAX_STAGE, player.getPersistentData().getInt(STAGE_KEY)));
    }

    public static int getCount(Player player) {
        return Math.max(0, player.getPersistentData().getInt(COUNT_KEY));
    }

    public static void setCount(Player player, int count) {
        player.getPersistentData().putInt(COUNT_KEY, Math.max(0, count));
    }

    public static int addCount(ServerPlayer player, int amount, int goal, String progressText) {
        if (amount <= 0) return getCount(player);
        int count = getCount(player) + amount;
        setCount(player, count);
        if (count >= goal) {
            completeStage(player);
        } else if (progressText != null && !progressText.isBlank()) {
            questMessage(player, progressText + " " + count + "/" + goal + ".");
        }
        return count;
    }

    public static void showCurrentQuest(ServerPlayer player) {
        int stage = getStage(player);
        if (stage >= MAX_STAGE) {
            questMessage(player, "Глава II пройдена. Если на экране появится титр, не верь ему.");
            player.sendSystemMessage(Component.literal("/cq scare <0-11> — тест экранов ужаса (только для развлечения).")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }
        questMessage(player, "ГЛАВА II — испытание " + (stage + 1) + "/" + MAX_STAGE + ": " + TASKS[stage]);
        player.sendSystemMessage(Component.literal("Подсказка: " + HINTS[stage]).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        player.sendSystemMessage(Component.literal("/cq — задание | /cq reset — сброс | /cq scare <0-11> — проверить скример")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (stage == 0) {
            player.sendSystemMessage(Component.literal("Прогресс: " + getCount(player) + "/8 ночных убийств").withStyle(ChatFormatting.GRAY));
        } else if (stage == 2) {
            player.sendSystemMessage(Component.literal("Прогресс: " + (int) player.getPersistentData().getDouble(DISTANCE_KEY) + "/512 блоков")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stage == 3) {
            player.sendSystemMessage(Component.literal("Под водой: " + getCount(player) / 20 + "/40 секунд")
                    .withStyle(ChatFormatting.GRAY));
        } else if (stage == 6) {
            player.sendSystemMessage(Component.literal("В Незере: " + (getCount(player) / 20) + "/60 секунд; выжил: "
                    + player.getPersistentData().getBoolean(NETHER_SURVIVED_KEY)).withStyle(ChatFormatting.GRAY));
        } else if (stage == 7) {
            player.sendSystemMessage(Component.literal("Глубинный сланец: " + getCount(player) + "/16").withStyle(ChatFormatting.GRAY));
        } else if (stage == 8) {
            player.sendSystemMessage(Component.literal("Эндермены: " + getCount(player) + "/2").withStyle(ChatFormatting.GRAY));
        } else if (stage == 11) {
            player.sendSystemMessage(Component.literal("Финальный протокол: " + getCount(player) / 20 + "/10 секунд")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static void questMessage(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("[Квест II] ").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    public static boolean completeStage(ServerPlayer player) {
        int completedStage = getStage(player);
        if (completedStage >= MAX_STAGE) return false;

        player.getPersistentData().putInt(STAGE_KEY, completedStage + 1);
        resetStageCounters(player);
        reward(player, completedStage);

        if (completedStage == 5) {
            CursedEvents.scheduleScareForOtherPlayer(player, 20 * (20 + CursedEvents.randomInt(16)));
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("[АУДИОСИСТЕМА] Обнаружен неизвестный источник шума. Диагностика не рекомендуется.")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
            }
        }

        if (completedStage == MAX_STAGE - 1) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("[Квест II] Протокол завершён. Пожалуйста, не смотрите за спину.")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
                int variant = 0;
                for (ServerPlayer online : server.getPlayerList().getPlayers()) {
                    ModNetworking.sendScare(online, variant++ % 12);
                    CursedEvents.playScareAudio(online, (variant + 6) % 12);
                }
            }
            questMessage(player, "Финальная выплата: тотем, незерит и право больше никогда не заполнять форму 404.");
        } else {
            showCurrentQuest(player);
        }
        return true;
    }

    public static void reset(ServerPlayer player) {
        player.getPersistentData().putInt(STAGE_KEY, 0);
        resetStageCounters(player);
        player.getPersistentData().putBoolean(NETHER_SURVIVED_KEY, false);
        player.getPersistentData().putBoolean(HAS_LAST_POS_KEY, false);
        questMessage(player, "Прогресс сброшен. Ваша анкета была сожжена, но копия почему-то осталась.");
        showCurrentQuest(player);
    }

    public static void setStage(ServerPlayer player, int stage) {
        int clamped = Math.max(0, Math.min(MAX_STAGE, stage));
        player.getPersistentData().putInt(STAGE_KEY, clamped);
        resetStageCounters(player);
        player.getPersistentData().putBoolean(NETHER_SURVIVED_KEY, false);
        player.getPersistentData().putBoolean(HAS_LAST_POS_KEY, false);
        questMessage(player, "Администратор установил этап " + clamped + "/" + MAX_STAGE + ". Реальность внесла это в протокол.");
        showCurrentQuest(player);
    }

    private static void resetStageCounters(Player player) {
        player.getPersistentData().putInt(COUNT_KEY, 0);
        player.getPersistentData().putDouble(DISTANCE_KEY, 0.0D);
        player.getPersistentData().putBoolean(HAS_LAST_POS_KEY, false);
        player.getPersistentData().putDouble(LAST_X_KEY, player.getX());
        player.getPersistentData().putDouble(LAST_Y_KEY, player.getY());
        player.getPersistentData().putDouble(LAST_Z_KEY, player.getZ());
    }

    public static boolean hasItems(Player player, Item item, int amount) {
        return countItems(player, item) >= amount;
    }

    public static int countItems(Player player, Item item) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    public static void consumeItems(Player player, Item item, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (!stack.is(item)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        player.getInventory().setChanged();
    }

    private static ItemStack named(Item item, int count, String name, ChatFormatting color) {
        ItemStack stack = new ItemStack(item, count);
        stack.setHoverName(Component.literal(name).withStyle(color, ChatFormatting.BOLD));
        return stack;
    }

    private static void reward(ServerPlayer player, int completedStage) {
        switch (completedStage) {
            case 0 -> give(player, named(Items.IRON_SWORD, 1, "Кадровый ликвидатор", ChatFormatting.GOLD));
            case 1 -> {
                give(player, named(Items.GOLDEN_CARROT, 4, "Паёк за травму", ChatFormatting.GOLD));
                give(player, named(Items.BONE, 1, "Квитанция от бухгалтерии", ChatFormatting.GRAY));
            }
            case 2 -> give(player, named(Items.LEATHER_BOOTS, 1, "Сертификат о перемещении", ChatFormatting.AQUA));
            case 3 -> give(player, named(Items.PUFFERFISH, 2, "Аквариумный посттравматический опыт", ChatFormatting.YELLOW));
            case 4 -> give(player, named(Items.BELL, 1, "Кнопка вызова ужаса", ChatFormatting.DARK_PURPLE));
            case 5 -> give(player, named(Items.ENDER_EYE, 2, "Контракт с геометрией", ChatFormatting.DARK_GREEN));
            case 6 -> give(player, named(Items.GOLD_INGOT, 5, "Компенсация за командировку в ад", ChatFormatting.GOLD));
            case 7 -> give(player, named(Items.IRON_PICKAXE, 1, "Дробитель архитектурной психики", ChatFormatting.GRAY));
            case 8 -> give(player, named(Items.CARVED_PUMPKIN, 1, "Лицо второго пилота", ChatFormatting.GOLD));
            case 9 -> give(player, named(Items.DIAMOND, 2, "Компенсация морального ущерба II", ChatFormatting.AQUA));
            case 10 -> give(player, named(Items.WRITABLE_BOOK, 1, "Протокол отрицания", ChatFormatting.DARK_RED));
            case 11 -> {
                give(player, named(Items.TOTEM_OF_UNDYING, 1, "Тотем от последствий", ChatFormatting.LIGHT_PURPLE));
                give(player, named(Items.NETHERITE_INGOT, 1, "Последний взнос в фонд страха", ChatFormatting.DARK_RED));
                give(player, named(Items.DIAMOND, 4, "Премия за психологическую устойчивость", ChatFormatting.AQUA));
            }
            default -> { }
        }
        questMessage(player, "Испытание " + (completedStage + 1) + "/" + MAX_STAGE + " закрыто. Награда доставлена — возможно, это худшая часть.");
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack.copy();
        if (!player.getInventory().add(remaining)) player.drop(remaining, false);
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal("Получено: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(stack.getHoverName().getString()).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" × " + stack.getCount()).withStyle(ChatFormatting.GRAY)));
    }
}
