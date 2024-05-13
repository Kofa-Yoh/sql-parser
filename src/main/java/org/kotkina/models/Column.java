package org.kotkina.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    String name;
    String alias;

    @Override
    public String toString() {
        return "\n\t{ \"column\": \"" + name + "\"" + (alias == null ? "" : ", \"alias\": \"" + alias + "\"") + " }";
    }
}
