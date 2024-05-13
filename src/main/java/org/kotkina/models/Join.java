package org.kotkina.models;

import lombok.*;

import java.text.MessageFormat;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Join {
    private String type;
    private Source source;
    private List<Clause> conditions;

    @Override
    public String toString() {
        return MessageFormat.format("\n\t'{'\n\t\"type\": \"{0}\",\n\t\"source\": {1},\n\t\"conditions\": {2}\n\t'}'", type, source, conditions);
    }
}
