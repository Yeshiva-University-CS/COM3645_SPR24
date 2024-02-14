/**
 * Adapted from
 * Symbol table entry class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.intermediate.symtab;

public class SymTableEntry
{
    private final String name;
    private Double value;
    
    /**
     * Constructor.
     * @param name the entry's name.
     */
    public SymTableEntry(String name)
    {
        this.name  = name;
        this.value = 0.0;
    }
    
    /**
     * Getter.
     * @return the entry's name.
     */
    public String getName()  { return name;  }

    /**
     * Getter.
     * @return the entry's value
     */
    public Double getValue() { return value; }
    
    /**
     * Set the entry's value.
     * @param value the value to set.
     */
    public void setValue(Double value) { this.value = value; }
}
