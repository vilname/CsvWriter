package org.writer.csv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Помечает поле класса модели, которое должно быть записано в строку CSV файла.
 * <p>
 * Экспортируются только поля, помеченные {@code CsvColumn}. Заголовок столбца взят из
 * {@link #name()}, если оно не пустое; в противном случае используется имя поля Java. {@link #order()}
 * управляет порядком расположения столбцов слева направо (сначала идут меньшие значения;
 * по умолчанию столбец располагается после всех явно упорядоченных столбцов).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {

    /**
     * Текст заголовка в формате CSV для этого столбца. Если поле пустое, используется название поля.
     *
     * @return header label
     */
    String name() default "";

    /**
     * Ключ сортировки для определения позиции столбца. Меньшие значения отображаются ранее в
     * заголовке и в каждой строке.
     *
     * @return ordering index
     */
    int order() default Integer.MAX_VALUE;
}
