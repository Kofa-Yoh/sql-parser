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
public class Source {
    String table;
    String alias;

    @Override
    public String toString() {
        return "\n\t{ \"table\": \"" + table + "\"" + (alias == null ? "" : ", \"alias\": \"" + alias + "\"") + " }";
    }
}
