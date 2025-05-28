package digital.ozna.javabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RandomNumberResponse {
    public String id;
    public int value;
    public String dt;

    public String toJsonString() {
        return "{\"id\":\"%s\",\"value\":%d,\"dt\":\"%s\"}".formatted(id, value, dt);
    }
}
