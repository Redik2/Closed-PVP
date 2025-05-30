package Modules;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import arc.files.Fi;
import arc.util.Log;
import mindustry.Vars;
import mindustry.gen.Player;

public class LocalizationManager {
    private static Map<String, Map<String, String>> translations = new HashMap<>();
    private static final Gson gson = new Gson();

    private static final String server_info_prefix = "[accent]\uE837[standard]";
    private static final String server_warn_prefix = "[orange]\uE810";
    private static final String server_err_prefix = "[red]⚠";
    private static final String standart_color = "[#bfbfbf]";
    private static final String accent_color = "[#ffd37f]";

    public static void init() {
        // Загрузка языков при инициализации
        Fi modDir = Vars.dataDirectory;
        loadLanguage("en", modDir.child("lang/en.json"));
        loadLanguage("ru", modDir.child("lang/ru.json"));
    }

    @SuppressWarnings("UseSpecificCatch")
    private static void loadLanguage(String lang, Fi file) {
        try {
            // 1. Проверка существования файла
            if (!file.exists()) {
                Log.err("File not found: @", file.path());
                return;
            }

            // 2. Чтение с проверкой содержимого
            String json = file.readString();
            if (json == null || json.trim().isEmpty()) {
                Log.err("Empty file: @", file.path());
                return;
            }

            // 3. Парсинг с явным указанием типа
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> data = gson.fromJson(json, type);
            
            if (data == null) {
                Log.err("Parsed data is null for: @", file.path());
                return;
            }

            // 4. Проверка непустых данных
            if (data.isEmpty()) {
                Log.err("No translations found in: @", file.path());
                return;
            }

            translations.put(lang, data);
            Log.info("Loaded language '@' with @ entries", lang, data.size());
        } catch (Exception e) {
            Log.err("Failed to load language '@': @\nFile: @", lang, e, file.path());
        }
    }

    public static String get(String key, String lang) {
        return translations.getOrDefault(lang, translations.get("en")).getOrDefault(key, key);
    }

    public static String getFormatted(String key, Player player, Object... args) {
        // Получаем базовую строку по ключу и языку
        String pattern = get(key, player.locale());
        
        try {
            // Форматируем строку, подставляя аргументы в плейсхолдеры {0}, {1} и т.д.
            String formattedMessage = MessageFormat.format(pattern, args);
            // Замена тегов на цвета и символы для стандартизации типовых сообщений
            return formattedMessage.replace("[error]", server_err_prefix).replace("[warn]", server_warn_prefix).replace("[info]", server_info_prefix).replace("[standart]", standart_color).replace("[accent]", accent_color);
        } catch (IllegalArgumentException e) {
            Log.err("Failed to format string for key '@' (lang: @): @", key, player.locale(), e.getMessage());
            return pattern; // Возвращаем неформатированную строку в случае ошибки
        }
    }
}