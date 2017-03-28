package project;

import java.io.IOException;

public interface TupleReader {

/**
 * read next tuple from file
 */
public Tuple readNext() throws IOException;

/**
 * reset the reader to the starting position
 */
public void reset() throws IOException;
}
