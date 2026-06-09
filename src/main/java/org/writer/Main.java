package org.writer;

import org.writer.csv.CsvWriter;
import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Демонстрирует экспорт объектов модели в CSV с помощью {@link Writable}.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(System.getProperty("user.dir"), "output");
        java.nio.file.Files.createDirectories(outDir);

        Writable csv = new CsvWriter();

        List<Person> people = List.of(
                Person.builder()
                        .firstName("Anna \"Hi\" ")
                        .lastName("Ivanova, test")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(2001)
                        .build(),
                Person.builder()
                        .firstName("Boris")
                        .lastName("Petrov")
                        .dayOfBirth(3)
                        .monthOfBirth(Months.NOVEMBER)
                        .yearOfBirth(1998)
                        .build()
        );
        String peopleFile = outDir.resolve("people.csv").toString();
        csv.writeToFile(people, peopleFile);

        List<Student> students = List.of(
                Student.builder().name("Elena Smirnova").score(List.of("5", "4", "5")).build(),
                Student.builder().name("Dmitry Kozlov").score(List.of("3", "4", "4", "5")).build()
        );
        String studentsFile = outDir.resolve("students.csv").toString();
        csv.writeToFile(students, studentsFile);
    }
}
