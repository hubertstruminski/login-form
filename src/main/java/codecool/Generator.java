package codecool;

import java.util.UUID;

public class Generator {

    public String generateUniqueSessionID(){
        return UUID.randomUUID().toString();
    }
}
