package org.writer.csv;

import org.writer.Writable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Записывает список объектов Java в CSV-файл, используя отражение и метаданные {@link CsvColumn}.
 * <p>
 * Тип строк берётся из первого объекта в списке (data.get(0).getClass()). Все остальные элементы должны
 * быть совместимы с этим типом (того же класса или наследники). Значения полей читаются через Reflection
 * {@link Field#get(Object)}), поэтому можно читать даже private поля.
 * Файл пишется в кодировке UTF-8.
 * Если в значении есть спецсимволы CSV (запятая, кавычки, перенос строки), применяется стандартное
 * экранирование в стиле RFC 4180: значение оборачивается в " и внутренние " удваиваются (" -> "").
 * </p>
 */
public class CsvWriter implements Writable {

    private static final String COLLECTION_SEPARATOR = ";";

    /**
     * {@inheritDoc}
     *
     * @param data     непустой список объектов-строк одного и того же конкретного типа
     * @param fileName путь к файлу, который нужно создать или перезаписать
     * @throws IllegalArgumentException если значение {@code data} равно null или пусто
     * @throws UncheckedIOException     если файл не может быть записан
     */
    @Override
    public void writeToFile(List<?> data, String fileName) {
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(fileName, "fileName");
        if (data.isEmpty()) {
            throw new IllegalArgumentException("data must not be empty (cannot infer row type)");
        }

        Object first = data.get(0);
        Class<?> rowClass = first.getClass();
        List<Field> columns = orderedCsvFields(rowClass);

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("No @CsvColumn fields on " + rowClass.getName());
        }

        List<String> lines = new ArrayList<>(1 + data.size());
        lines.add(buildHeaderLine(columns));

        for (Object row : data) {
            if (row == null) {
                throw new IllegalArgumentException("Row must not be null");
            }
            if (!rowClass.isInstance(row)) {
                throw new IllegalArgumentException(
                        "All rows must be of type " + rowClass.getName() + ", got " + row.getClass().getName());
            }
            lines.add(buildDataLine(row, columns));
        }

        Path path = Path.of(fileName);
        try {
            Files.writeString(path, String.join("\n", lines), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Field> orderedCsvFields(Class<?> rowClass) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = rowClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(f);
                }
            }
        }
        fields.sort(Comparator
                .comparingInt((Field f) -> f.getAnnotation(CsvColumn.class).order())
                .thenComparing(Field::getName));
        return fields;
    }

    private static String buildHeaderLine(List<Field> columns) {
        return columns.stream()
                .map(f -> {
                    CsvColumn meta = f.getAnnotation(CsvColumn.class);
                    String name = meta.name();
                    return escapeCsvField(name.isBlank() ? f.getName() : name);
                })
                .collect(Collectors.joining(","));
    }

    private static String buildDataLine(Object row, List<Field> columns) {
        List<String> cells = new ArrayList<>(columns.size());
        for (Field field : columns) {
            Object value = getValueViaGetter(row, field);
            cells.add(escapeCsvField(formatCell(value)));
        }
        return String.join(",", cells);
    }

    private static String formatCell(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(v -> v == null ? "" : String.valueOf(v))
                    .collect(Collectors.joining(COLLECTION_SEPARATOR));
        }

        return String.valueOf(value);
    }

    /**
     * Экранирует одно поле CSV в соответствии с общими правилами: заключает в двойные кавычки, если значение содержит
     * запятую, кавычку, CR или LF; удваивает все встроенные кавычки.
     *
     * @param raw неэкранированный текст ячейки (может быть пустым)
     * @return экранированное содержимое поля без окружающих разделителей
     */
    static String escapeCsvField(String raw) {
        String s = raw == null ? "" : raw;
        if (s.contains(",") || s.contains("\"") || s.contains("\r") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static Object getValueViaGetter(Object obj, Field field) {
        String fieldName = field.getName();
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

        try {
            Method getter = obj.getClass().getMethod(getterName);
            return getter.invoke(obj);
        } catch (NoSuchMethodException e) {
            // Пробуем is-геттер для boolean
            if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                String isGetterName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                try {
                    Method isGetter = obj.getClass().getMethod(isGetterName);
                    return isGetter.invoke(obj);
                } catch (NoSuchMethodException ex) {
                    throw new IllegalStateException(
                            String.format("No getter found for field '%s' (tried %s and %s)",
                                    fieldName, getterName, isGetterName), ex);
                } catch (InvocationTargetException | IllegalAccessException ex) {
                    throw new IllegalStateException("Cannot invoke is-getter for field: " + fieldName, ex);
                }
            }
            throw new IllegalStateException("No getter found for field: " + fieldName, e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke getter for field: " + fieldName, e);
        }
    }
}
