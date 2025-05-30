package Modules;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Call;
import mindustry.gen.Groups;

public class ZoneManager {
    static float safeSize;
    static float maxSize;
    static Timer.Task zoneClosingTask;
    static Timer.Task zoneDamagingTask;
    static int closingTimeMinutes;
    // Центр карты
    static float centerX = Vars.world.width() / 2f;
    static float centerY = Vars.world.height() / 2f;
    public static void init()
    {
        safeSize = 0;
        closingTimeMinutes = 0;
    }

    public static void resetZone()
    {
        maxSize = Vars.world.width() / 2;
        safeSize = maxSize;
        centerX = Vars.world.width() / 2f;
        centerY = Vars.world.height() / 2f;
    }

    public static void stopZoneClosing()
    {
        if (zoneClosingTask != null)
        {
            zoneClosingTask.cancel();
        }
        if (zoneDamagingTask != null)
        {
            zoneDamagingTask.cancel();
        }
    }

    public static void startZoneClosing(int newClosingTimeMinutes)
    {
        closingTimeMinutes = newClosingTimeMinutes;
        zoneClosingTask = Timer.schedule(() -> {
            if (safeSize > 0) safeSize -= (float)Vars.world.width() / (float)closingTimeMinutes / 1200f;
            else {
                safeSize = 0;
                Events.fire(new CustomEvents.ZoneClosed());
            }
        },
        0f, 0.1f);

        zoneDamagingTask = Timer.schedule(() -> {
            damage();
        }, 0f, 1f);
    }

    public static void damage() {
        Groups.build.forEach(build -> {
            // Расстояние от центра по осям X и Y (абсолютное значение)
            float distX = Math.abs(build.tileX() - centerX);
            float distY = Math.abs(build.tileY() - centerY);
            
            // Находим максимальное отклонение (чтобы учитывать квадратную зону)
            float dist = Math.max(distX, distY);
            
            // Если строение вне безопасной зоны
            if (dist > safeSize) {
                // Нормализованное расстояние (0..1) от safeSize до size
                float normalizedDist = Math.min((dist - safeSize) / maxSize, 1f);
                
                // Урон пропорционален расстоянию (можно изменить формулу)
                float damage = build.maxHealth() * (float)Math.pow(normalizedDist, 1.5);
                
                // Наносим урон (но не больше текущего HP)
                Call.effect(Fx.lightBlock, build.x, build.y, build.block().size, Color.valueOf("ff757540"));
                build.damage(Math.min(damage, build.health));
                
            }
        });
        Groups.unit.forEach(unit -> {
            // Расстояние от центра по осям X и Y (абсолютное значение)
            float distX = Math.abs(unit.tileX() - centerX);
            float distY = Math.abs(unit.tileY() - centerY);
            
            // Находим максимальное отклонение (чтобы учитывать квадратную зону)
            float dist = Math.max(distX, distY);
            
            // Если строение вне безопасной зоны
            if (dist > safeSize) {
                // Нормализованное расстояние (0..1) от safeSize до size
                float normalizedDist = Math.min((dist - safeSize) / maxSize, 1f);
                
                // Урон пропорционален расстоянию (можно изменить формулу)
                float damage = unit.maxHealth() * (float)Math.pow(normalizedDist, 1.5);
                
                // Наносим урон (но не больше текущего HP)
                unit.damage(Math.min(damage, unit.health));
            }
        });
    }

    // public static void draw() {
        
    //     float safeSize = size;
    //     for (int i = 0; i < size * 10; i++) {
    //         // Определяем, по какой стороне спавнить молнию
    //         int side = Mathf.random(3);
    //         float x, y;
            
    //         switch(side) {
    //             case 0: // Верхняя граница
    //                 x = centerX + Mathf.range(safeSize + 1);
    //                 y = centerY + safeSize;
    //                 break;
    //             case 1: // Нижняя граница
    //                 x = centerX + Mathf.range(safeSize + 1);
    //                 y = centerY - safeSize;
    //                 break;
    //             case 2: // Левая граница
    //                 x = centerX - safeSize;
    //                 y = centerY + Mathf.range(safeSize + 1);
    //                 break;
    //             default: // Правая граница
    //                 x = centerX + safeSize;
    //                 y = centerY + Mathf.range(safeSize + 1);
    //         }
    //         spawnParticle(x, y);
    //     }
    // }

    public static void draw()
    {
        // Рассчитываем границы зоны
        float left = centerX - safeSize;
        float right = centerX + safeSize;
        float bottom = centerY - safeSize;
        float top = centerY + safeSize;
        
        // Шаг между эффектами (в тайлах)
        float step = 1f;
        
        // Спавним эффекты по границам
        for(float x = left; x <= right; x += step) {
            spawnParticle(x, top);
            spawnParticle(x, bottom);
        }
        
        for(float y = bottom; y <= top; y += step) {
            spawnParticle(left, y);
            spawnParticle(right, y);
        }
    }

    private static void spawnParticle(float x, float y)
    {

        // Спавн эффекта молнии
        float dx = x - centerX;
        float dy = y - centerY;
        float rotation = Mathf.angle(dx, dy); // Угол от центра к точке
        
        // Спавн эффекта молнии с правильным поворотом
        Call.effect(
            Fx.hitFlameBeam, 
            x * 8, 
            y * 8, 
            rotation, // Поворот наружу от центра
            Color.valueOf("ff5555")
        );
    }
}
