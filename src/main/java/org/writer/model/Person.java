package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.writer.csv.CsvColumn;

/**
 * Сущность Персоны
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @CsvColumn(order = 1, name = "First name")
    private String firstName;

    @CsvColumn(order = 2, name = "Last name")
    private String lastName;

    @CsvColumn(order = 3, name = "Day")
    private int dayOfBirth;

    @CsvColumn(order = 4, name = "Month")
    private Months monthOfBirth;

    @CsvColumn(order = 5, name = "Year")
    private int yearOfBirth;

}
