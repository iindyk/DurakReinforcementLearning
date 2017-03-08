package DurakGame.ReinforcementLearningPlayer;

import static DurakGame.Conn.conn;
import static DurakGame.Conn.statmt;


/**
 * Created by HP on 04.02.2017.
 */
public class RLDBWriter {
    public RLDBWriter(){
        try {
            new DurakGame.Conn("ReinforcementLearningDB.db");
            statmt = conn.createStatement();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void writeToDB(){}
    public void finalize(){
        try{
            conn.close();
            statmt.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
