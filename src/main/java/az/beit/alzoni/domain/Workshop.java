package az.beit.alzoni.domain;

import java.util.ArrayList;
import java.util.List;

public record Workshop(int no, String name, List<String> processNames) {

    public List<ProcessRef> processes() {
        List<ProcessRef> refs = new ArrayList<>();
        for (int i = 0; i < processNames.size(); i++) {
            refs.add(new ProcessRef(no + "-" + (i + 1), name, processNames.get(i)));
        }
        return refs;
    }
}
