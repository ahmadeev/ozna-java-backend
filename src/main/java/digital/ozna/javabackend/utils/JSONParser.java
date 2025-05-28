package digital.ozna.javabackend.utils;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class JSONParser {
    public static <T> T parseJSON(String jsonString, Class<T> type) {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            return jsonb.fromJson(jsonString, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
