package com.lni.datalni.ui.imports;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** One row failure, surfaced in the Failures tab. */
@RequiredArgsConstructor
@Getter
public class RowError {

    /** 1-based row number including the header. */
    private final int row;
    private final String message;
}
