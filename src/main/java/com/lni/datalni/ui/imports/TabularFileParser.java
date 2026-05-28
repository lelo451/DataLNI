package com.lni.datalni.ui.imports;

import java.io.IOException;
import java.io.InputStream;

/** Reads a tabular file into a {@link ParsedFile}. One implementation per format. */
public interface TabularFileParser {

    ParsedFile parse(String fileName, InputStream content) throws IOException;
}
