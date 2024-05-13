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
public class Clause {
    private String condition;
    private String operator;

    @Override
    public String toString() {
        return "\n\t{ \"condition\": \"" + condition + "\"" + (operator == null ? "" : ", \"operator\": \"" + operator + "\"") + " }";
    }
}
