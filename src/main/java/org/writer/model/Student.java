package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.writer.csv.CsvColumn;

import java.util.List;

/**
 * Сущность Студента
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @CsvColumn(order = 1, name = "Name")
    private String name;

    @CsvColumn(order = 2, name = "Scores")
    private List<String> score;
}