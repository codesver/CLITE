package self;

import java.util.*;

public class TypeMap extends HashMap<Variable, Type> { 

// self.TypeMap is implemented as a Java HashMap.
// Plus a 'display' method to facilitate experimentation.
    public void display() {
        System.out.println(this.entrySet());
    }
}
