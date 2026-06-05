package org.writer;

import java.util.List;

/**
 * Интерфейс записи данных в файл
 *
 * @see org.writer.csv.CsvWriter
 */
public interface Writable {

    /**
     * Сериализует заданные строки в новый файл, заменяя любой существующий файл по указанному пути.
     *
     * @param data     строки для экспорта; семантика (например, непустой, единообразный тип) зависит от реализации
     * @param fileName destination path
     */
    void writeToFile(List<?> data, String fileName);

}
