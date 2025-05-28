package digital.ozna.javabackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RandomNumberRequest {
    public String id;
    public int max;
    public int min;
    public boolean run;
    public int frequency;
}
