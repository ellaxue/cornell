package project;

import java.io.IOException;

import javax.swing.tree.ExpandVetoException;

public interface TupleReader {

/**
 * read next tuple from file
 */
public Tuple readNext() throws IOException;

/**
 * reset the reader to the starting position
 */
public void reset() throws IOException;

/**
 * reset the reader to a specific tuple index
 */
public void reset(int index) throws IOException; }