package org.writer.csv;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvWriterTest {

    @Test
    void escapeCsvField_quotesAndCommas() {
        assertEquals("\"a,b\"", CsvWriter.escapeCsvField("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvWriter.escapeCsvField("say \"hi\""));
        assertEquals("\"line1\nline2\"", CsvWriter.escapeCsvField("line1\nline2"));
    }

    @Test
    void escapeCsvField_plain() {
        assertEquals("plain", CsvWriter.escapeCsvField("plain"));
        assertEquals("", CsvWriter.escapeCsvField(null));
    }

    @Test
    void writeToFile_personsFromFaker(@TempDir Path dir) throws Exception {
        Faker faker = new Faker(Locale.forLanguageTag("ru"));
        CsvWriter writer = new CsvWriter();

        List<Person> rows = IntStream.range(0, 5)
                .mapToObj(i -> Person.builder()
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .dayOfBirth(faker.number().numberBetween(1, 28))
                        .monthOfBirth(Months.values()[faker.number().numberBetween(0, Months.values().length)])
                        .yearOfBirth(faker.number().numberBetween(1990, 2010))
                        .build())
                .toList();

        Path file = dir.resolve("p.csv");
        writer.writeToFile(rows, file.toString());

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(1 + rows.size(), lines.size());
        assertTrue(lines.get(0).contains("First name"));
        assertTrue(lines.get(0).contains("Last name"));
    }

    @Test
    void writeToFile_studentScoresJoined(@TempDir Path dir) throws Exception {
        CsvWriter writer = new CsvWriter();
        List<Student> rows = List.of(
                Student.builder().name("A").score(List.of("10", "9")).build(),
                Student.builder().name("B").score(List.of()).build()
        );
        Path file = dir.resolve("s.csv");
        writer.writeToFile(rows, file.toString());

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertTrue(lines.get(1).contains("10;9"));
        assertEquals("B,", lines.get(2));
    }

    @Test
    void writeToFile_rejectsEmptyList() {
        CsvWriter writer = new CsvWriter();
        List<Object> emptyData = List.of();
        String fileName = "x.csv";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> writer.writeToFile(emptyData, fileName));

        assertEquals("data must not be empty (cannot infer row type)", exception.getMessage());
    }

    @Test
    void writeToFile_rejectsMixedRowTypes(@TempDir Path dir) {
        CsvWriter writer = new CsvWriter();
        Path file = dir.resolve("bad.csv");

        Person person = Person.builder()
                .firstName("a")
                .lastName("b")
                .dayOfBirth(1)
                .monthOfBirth(Months.JANUARY)
                .yearOfBirth(2000)
                .build();

        Student student = Student.builder()
                .name("x")
                .score(List.of("1"))
                .build();

        List<Object> mixed = List.of(person, student);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> writer.writeToFile(mixed, file.toString()));


        assertTrue(exception.getMessage().contains("All rows must be of type"));
    }
}
