package DurakGame.ReinforcementLearningPlayer;

import static DurakGame.Conn.conn;
import static DurakGame.Conn.resSet;
import static DurakGame.Conn.statmt;

/**
 * Created by HP on 04.02.2017.
 */
public class RLDBReader {
    public RLDBReader(){
        try {
            new DurakGame.Conn("ReinforcementLearningDB.db");
            statmt = conn.createStatement();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void readFromDB(){}
    public void finalize(){
        try{
            conn.close();
            statmt.close();
            resSet.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
