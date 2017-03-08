package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Game;
import DurakGame.Player;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by HP on 19.02.2017.
 */
public class RLFileReader  {

    public class StateAction{
        State state;
        ArrayList<Card> action;
        State nextState;
        long gameID;

        private StateAction(){}

        @Override
        public String toString() {
            return "StateAction{" +
                    "state=" + state +
                    ", action=" + action +
                    ", nextState=" + nextState +
                    "}\n";
        }

        public StateAction(State state, ArrayList<Card> action,long gameID) {
            this.state = state;
            this.action = action;
            this.gameID=gameID;
        }
    }

    public ArrayList<StateAction> readStateActionsFromTxd(String path){
        ArrayList<StateAction> stateActions=new ArrayList<>();
        int linesCount=0;
        long gameId=0;
        String prevLine="     ";
        String line;
        ArrayList<Card> hand0=new ArrayList<>();
        ArrayList<Card> hand1=new ArrayList<>();
        ArrayList<Card> attack0=new ArrayList<>();
        ArrayList<Card> attack1=new ArrayList<>();
        ArrayList<Card> defence0=new ArrayList<>();
        ArrayList<Card> defence1=new ArrayList<>();
        ArrayList<Card> cardsOnTable=new ArrayList<>();
        ArrayList<Card> outOfTheGame=new ArrayList<>();
        ArrayList<Card> enemyKnownCards0=new ArrayList<>();
        ArrayList<Card> enemyKnownCards1=new ArrayList<>();
        Card newCard;
        
        ArrayList<StateAction> stateActions0=new ArrayList<>();
        ArrayList<StateAction> stateActions1=new ArrayList<>();
        Player player0=new ReadingSimulationPlayer();
        Player player1=new ReadingSimulationPlayer();
        ArrayList<Player> players=new ArrayList<>();
        players.add(player0);
        players.add(player1);
        Game.players=players;
        player0.state.hand=hand0;
        player1.state.hand=hand1;
        int attacker=-1;
        int roundNumber=0;
        try {
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            ArrayList<Card> oldHandAttack=new ArrayList<>();
            ArrayList<Card> oldHandDefence=new ArrayList<>();
            ArrayList<Card> defaultEnemyAttack=new ArrayList<>();
            defaultEnemyAttack.add(new Card(6));


            while ((line = reader.readLine()) != null /*&& linesCount<1000*/) {
                if (line.substring(0,4).equals("game") && attacker==0){
                    gameId=Long.parseLong(line.substring(6,line.length()-1));
                    stateActions.addAll(stateActions0);
                    stateActions0.clear();
                    stateActions1.clear();
                    roundNumber=0;
                    Game.deck=Card.createDeck();
                }
                else if (line.substring(0,4).equals("game") && attacker==1){
                    gameId=Long.parseLong(line.substring(6,line.length()-1));
                    stateActions.addAll(stateActions1);
                    stateActions0.clear();
                    stateActions1.clear();
                    roundNumber=0;
                    Game.deck=Card.createDeck();
                }
                else if (line.substring(0,4).equals("hand")&&line.charAt(4)=='0') {
                    hand0.clear();
                    hand0.add(new Card(line.substring(6,8)));
                    hand0.add(new Card(line.substring(8,10)));
                    hand0.add(new Card(line.substring(10,12)));
                    hand0.add(new Card(line.substring(12,14)));
                    hand0.add(new Card(line.substring(14,16)));
                    hand0.add(new Card(line.substring(16)));
                    Game.deck.removeAll(hand0);
                }
                else if (line.substring(0,4).equals("hand")&&line.charAt(4)=='1') {
                    hand1.clear();
                    hand1.add(new Card(line.substring(6,8)));
                    hand1.add(new Card(line.substring(8,10)));
                    hand1.add(new Card(line.substring(10,12)));
                    hand1.add(new Card(line.substring(12,14)));
                    hand1.add(new Card(line.substring(14,16)));
                    hand1.add(new Card(line.substring(16)));
                    Game.deck.removeAll(hand1);
                }
                else if (line.substring(0,4).equals("vskr")) {
                    Game.setTrumpCard(new Card(line.substring(10,12)));
                    attack0.clear();
                    attack1.clear();
                    defence0.clear();
                    defence1.clear();
                    outOfTheGame.clear();
                }
                else if (((line.charAt(2)!='+'&& prevLine.charAt(2)=='+')||prevLine.substring(0,3).equals("tru") )&& line.charAt(0)=='0') {
                    attacker=0;
                    attack0.add(new Card(line.substring(2,4)));
                    cardsOnTable.add(new Card(line.substring(2,4)));
                }
                else if (((line.charAt(2)!='+'&& prevLine.charAt(2)=='+')||prevLine.substring(0,3).equals("tru") ) && line.charAt(0)=='1') {
                    attacker=1;
                    attack1.add(new Card(line.substring(2,4)));
                    cardsOnTable.add(new Card(line.substring(2,4)));

                }
                else if (line.substring(2,4).equals("hv") && line.charAt(0)=='0'){
                    if (!(prevLine.substring(2,4).equals("be")|| prevLine.charAt(0)=='0')) {attacker=1;}
                    //write attack & hand for 0 & change hand for 0 & set attack0=null
                    oldHandAttack.clear();
                    oldHandAttack.addAll(hand0);
                    stateActions0.add(new StateAction(new State(new ArrayList(hand0),new ArrayList(outOfTheGame),
                            new ArrayList(enemyKnownCards0), State.ActionType.ATTACK,
                            new ArrayList<>(),new ArrayList(cardsOnTable),roundNumber),new ArrayList(attack0),gameId));
                    hand0.removeAll(attack0);
                    outOfTheGame.addAll(attack0);
                    attack0.clear();
                    stateActions0.get(stateActions0.size()-1).nextState=new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards0),attacker==0? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    //write defence & hand for 1 & change hand for 1 & set defence1=null
                    oldHandDefence.clear();
                    oldHandDefence.addAll(hand1);
                    stateActions1.add(new StateAction(new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards1), State.ActionType.DEFENCE,
                            new ArrayList(stateActions0.get(stateActions0.size()-1).action),new ArrayList(cardsOnTable),roundNumber)
                            ,new ArrayList(defence1),gameId));
                    outOfTheGame.addAll(defence1);
                    defence1.clear();
                    cardsOnTable.clear();
                    stateActions1.get(stateActions1.size()-1).nextState=new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards1), attacker==1? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    roundNumber++;
                }
                else if (line.substring(2,4).equals("hv") && line.charAt(0)=='1'){
                    if (!(prevLine.substring(2,4).equals("be")|| prevLine.charAt(0)=='1')) {attacker=0;}
                    //write attack & hand for 1 & change hand for 1 & set attack1=null
                    oldHandAttack.clear();
                    oldHandAttack.addAll(hand1);
                    stateActions1.add(new StateAction(new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards1), State.ActionType.ATTACK,
                            new ArrayList<>(),new ArrayList(cardsOnTable),roundNumber),new ArrayList(attack1),gameId));
                    hand1.removeAll(attack1);
                    outOfTheGame.addAll(attack1);
                    attack1.clear();
                    stateActions1.get(stateActions1.size()-1).nextState=new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards1), attacker==1? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    //write defence & hand for 0 & change hand for 0 & set attack0=null
                    oldHandDefence.clear();
                    oldHandDefence.addAll(hand0);
                    stateActions0.add(new StateAction(new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards0), State.ActionType.DEFENCE,
                            new ArrayList(stateActions1.get(stateActions1.size()-1).action),new ArrayList(cardsOnTable),roundNumber)
                            ,new ArrayList(defence0),gameId));
                    hand0.removeAll(defence0);
                    outOfTheGame.addAll(defence0);
                    defence0.clear();
                    cardsOnTable.clear();
                    stateActions0.get(stateActions0.size()-1).nextState=new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards0),attacker==0? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    roundNumber++;
                }
                else if ((prevLine.length()==4||prevLine.substring(2,4).equals("be") ||prevLine.substring(2,4).equals("hv"))&& line.charAt(0)=='0' && attacker==0 && line.length()==4){
                    if (attack0.size()==0 || attack0.get(attack0.size()-1).value==line.charAt(3)) attack0.add(new Card(line.substring(2,4)));
                    else {//write attack & hand for 0 & change hand for 0 & set attack0=null & .add(line.substring(2,3))
                        oldHandAttack.clear();
                        oldHandAttack.addAll(hand0);
                        stateActions0.add(new StateAction(new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards0), State.ActionType.ATTACK,
                                new ArrayList<>(),new ArrayList(cardsOnTable),roundNumber),new ArrayList(attack0),gameId));
                        hand0.removeAll(attack0);
                        outOfTheGame.addAll(attack0);
                        attack0.clear();
                        attack0.add(new Card(line.substring(2,4)));
                        stateActions0.get(stateActions0.size()-1).nextState=new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards0), attacker==0? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                                new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    }
                }
                else if ((prevLine.length()==4||prevLine.substring(2,4).equals("be") ||prevLine.substring(2,4).equals("hv"))&& line.charAt(0)=='1' && attacker==1 && line.length()==4){
                    if (attack1.size()==0 || attack1.get(attack1.size()-1).value==line.charAt(3)) attack1.add(new Card(line.substring(2,4)));
                    else {//write attack & hand for 1 & change hand for 1 & set attack1=null & .add(line.substring(2,3))
                        oldHandAttack.clear();
                        oldHandAttack.addAll(hand1);
                        stateActions1.add(new StateAction(new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards1), State.ActionType.ATTACK,
                                new ArrayList<>(),new ArrayList(cardsOnTable),roundNumber),new ArrayList(attack1),gameId));
                        hand1.removeAll(attack1);
                        outOfTheGame.addAll(attack1);
                        attack1.clear();
                        attack1.add(new Card(line.substring(2,4)));
                        cardsOnTable.add(new Card(line.substring(2,4)));
                        stateActions1.get(stateActions1.size()-1).nextState=new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards1), attacker==1? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                                new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    }
                }
                else if (line.substring(2,4).equals("be")&& line.charAt(0)=='0') {
                    if (!defence0.isEmpty() && attack1.isEmpty()){
                        stateActions0.add(new StateAction(new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards0), State.ActionType.DEFENCE,
                                new ArrayList(stateActions1.get(stateActions1.size()-1).action),new ArrayList(cardsOnTable),roundNumber)
                                ,new ArrayList(defence0),gameId));
                    }
                    else if(!attack1.isEmpty()){
                        stateActions0.add(new StateAction(new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards0), State.ActionType.DEFENCE,
                                new ArrayList(attack1),new ArrayList(cardsOnTable),roundNumber),new ArrayList(defence0),gameId));
                    }
                    defence0.clear();
                    hand0.addAll(cardsOnTable);
                    stateActions0.get(stateActions0.size()-1).nextState=new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards0),attacker==0? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                }
                else if (line.substring(2,4).equals("be")&& line.charAt(0)=='1') {
                    if (!defence1.isEmpty() && attack0.isEmpty()){
                        stateActions1.add(new StateAction(new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards1), State.ActionType.DEFENCE,
                                new ArrayList(stateActions0.get(stateActions0.size()-1).action),new ArrayList(cardsOnTable),roundNumber)
                                ,new ArrayList(defence1),gameId));
                    }
                    else if(!attack0.isEmpty()){
                        stateActions1.add(new StateAction(new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards1), State.ActionType.DEFENCE,
                                new ArrayList(attack0),new ArrayList(cardsOnTable),roundNumber),new ArrayList(defence1),gameId));
                    }
                    defence1.clear();
                    hand1.addAll(cardsOnTable);
                    stateActions1.get(stateActions1.size()-1).nextState=new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards1), attacker==1? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                }
                else if (line.charAt(2)=='+'&& line.charAt(0)=='0'){
                    for (int i = 2; i <line.length()-1 ; i++) {
                        newCard=new Card(line.substring(i+1,i+3));
                        hand0.add(newCard);
                        Game.deck.remove(newCard);
                        i+=2;
                    }
                    stateActions0.get(stateActions0.size()-1).nextState=new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards0),attacker==0? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);

                }
                else if (line.charAt(2)=='+'&& line.charAt(0)=='1'){
                    for (int i = 2; i <line.length()-1 ; i++) {
                        newCard=new Card(line.substring(i+1,i+3));
                        hand1.add(newCard);
                        i+=2;
                    }
                    stateActions1.get(stateActions1.size()-1).nextState=new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                            ,new ArrayList(enemyKnownCards1), attacker==1? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                            new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                }
                else if (prevLine.length()==4 && line.length()==4 && line.charAt(0)=='0' && attacker==1){
                    if (defence0.size()==0|| !attack1.isEmpty()) defence0.add(new Card(line.substring(2,4)));
                    else {
                        oldHandDefence.clear();
                        oldHandDefence.addAll(hand0);
                        stateActions0.add(new StateAction(new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards0), State.ActionType.DEFENCE,
                                new ArrayList(stateActions1.get(stateActions1.size()-1).action)
                                ,new ArrayList(cardsOnTable),roundNumber),new ArrayList(defence0),gameId));
                        hand0.removeAll(defence0);
                        outOfTheGame.addAll(defence0);
                        defence0.clear();
                        defence0.add(new Card(line.substring(2,4)));
                        cardsOnTable.add(new Card(line.substring(2,4)));
                        stateActions0.get(stateActions0.size()-1).nextState=new State(new ArrayList(hand0),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards0), attacker==0? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                                new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    }
                }
                else if (prevLine.length()==4 && line.length()==4 && line.charAt(0)=='1' && attacker==0){
                    if (defence1.size()==0|| !attack0.isEmpty()) defence1.add(new Card(line.substring(2,4)));
                    else {
                        oldHandDefence.clear();
                        oldHandDefence.addAll(hand1);
                        stateActions1.add(new StateAction(new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards1), State.ActionType.DEFENCE
                                ,new ArrayList(stateActions0.get(stateActions0.size()-1).action),new ArrayList(cardsOnTable),roundNumber)
                                ,new ArrayList(defence1),gameId));
                        hand0.removeAll(defence1);
                        outOfTheGame.addAll(defence1);
                        defence1.clear();
                        defence1.add(new Card(line.substring(2,4)));
                        cardsOnTable.add(new Card(line.substring(2,4)));
                        stateActions1.get(stateActions1.size()-1).nextState=new State(new ArrayList(hand1),new ArrayList(outOfTheGame)
                                ,new ArrayList(enemyKnownCards1), attacker==1? State.ActionType.ATTACK: State.ActionType.DEFENCE,
                                new ArrayList(defaultEnemyAttack),new ArrayList(cardsOnTable),roundNumber);
                    }
                }
                prevLine=line;
                linesCount++;
            }
        }
        catch (Exception e) {
            System.out.println(linesCount+"  "+prevLine);
            System.out.println("def1= "+defence1+"at0"+attack0);
            e.printStackTrace();

        }

        return stateActions;
    }

}
