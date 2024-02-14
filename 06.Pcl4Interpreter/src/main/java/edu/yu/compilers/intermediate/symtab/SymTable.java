/**
 * Adapted from
 * Symbol table class for a simple interpreter.
 * (c) 2020 by Ronald Mak
 */
package edu.yu.compilers.intermediate.symtab;

import java.util.HashMap;

public class SymTable
{
    private final HashMap<String, SymTableEntry> contents = new HashMap<>();
    
    /**
     * Make an entry.
     * @param name the entry's name.
     */
    public SymTableEntry enter(String name)
    { 
        SymTableEntry entry = new SymTableEntry(name);
        contents.put(name, entry);
        
        return entry;
    }
    
    /**
     * Look up an entry.
     * @param name the entry's name.
     * @return the entry or null if it's not in the symbol table.
     */
    public SymTableEntry lookup(String name) { return contents.get(name); }
}
