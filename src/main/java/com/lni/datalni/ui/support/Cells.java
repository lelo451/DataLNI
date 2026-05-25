package com.lni.datalni.ui.support;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

import java.util.function.Function;

/** Helpers for building {@code TableColumn} cell value factories. */
public final class Cells {

    private Cells() {
    }

    /**
     * A cell value factory that reads a property via {@code getter}, returning an empty
     * value when the row item is {@code null}. JavaFX may invoke the factory with a null
     * item for empty/virtualized cells, so the guard avoids a {@link NullPointerException}.
     */
    public static <S, T> Callback<CellDataFeatures<S, T>, ObservableValue<T>> of(Function<S, T> getter) {
        return cd -> new ReadOnlyObjectWrapper<>(
                cd.getValue() == null ? null : getter.apply(cd.getValue()));
    }
}
