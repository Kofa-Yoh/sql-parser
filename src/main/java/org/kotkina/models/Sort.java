package org.kotkina.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.MessageFormat;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sort {
    private String expression;
    private boolean desc;

    public Sort(String expression) {
        this.expression = expression;
        this.desc = false;
    }

    @Override
    public String toString() {
        return MessageFormat.format("\n'\t{' \"expression\": \"{0}\", \"sort\": \"{1}\"' }'",
                expression, ((desc) ? "DESC" : "ASC"));
    }
}
