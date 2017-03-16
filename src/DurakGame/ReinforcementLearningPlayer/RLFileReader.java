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

    public ArrayList<State.StateAction> readStateActionsFromTxd(String path) throws Card.UnknownSuitException {
        ArrayList<State.StateAction> stateActions=new ArrayList<>();
        int linesCount=0;
        long gameId=0;
        String prevLine="     ";
        String line;
        Card attack0=null;
        Card attack1=null;
        Card defence0=null;
        Card defence1=null;
        
        ArrayList<State.StateAction> stateActions0=new ArrayList<>();
        ArrayList<State.StateAction> stateActions1=new ArrayList<>();
        Player player0=new ReadingSimulationPlayer();
        Player player1=new ReadingSimulationPlayer();
        Player[] players=new Player[]{player0,player1};
        Game.players=players;
        Game.deck=Card.createDeck();
        for (Player player: players
                ) {
            player.state.cardsOnTable=Game.cardsOnTable;
            player.state.outOfTheGame=Game.outOfTheGame;
        }
        int attacker=-1;
        try {
            InputStream is = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            while ((line = reader.readLine()) != null /*&& linesCount<1000*/) {
                if (line.substring(0,4).equals("game") && attacker==0){
                    gameId=Long.parseLong(line.substring(6,line.length()-1));
                    stateActions.addAll(stateActions0);
                    stateActions0.clear();
                    stateActions1.clear();
                    Game.roundNumber=0;
                    Game.deck=Card.createDeck();
                    for (Player player: players
                            ) {
                        player.state.hiddenCards.clear();
                        player.state.cardsOnTable=Game.cardsOnTable;
                        player.state.outOfTheGame=Game.outOfTheGame;
                        player.state.enemyKnownCards.clear();
                        player.state.enemyAttack=null;
                        player.state.roundNumber=Game.roundNumber;
                    }
                }
                else if (line.substring(0,4).equals("game") && attacker==1){
                    gameId=Long.parseLong(line.substring(6,line.length()-1));
                    stateActions.addAll(stateActions1);
                    stateActions0.clear();
                    stateActions1.clear();
                    Game.roundNumber=0;
                    Game.deck=Card.createDeck();
                    for (Player player: players
                            ) {
                        player.state.hiddenCards.clear();
                        player.state.cardsOnTable=Game.cardsOnTable;
                        player.state.outOfTheGame=Game.outOfTheGame;
                        player.state.enemyKnownCards.clear();
                        player.state.enemyAttack=null;
                        player.state.roundNumber=Game.roundNumber;
                    }
                }
                else if (line.substring(0,4).equals("hand")&&line.charAt(4)=='0') {
                    player0.state.hand.clear();
                    for (int i = 6; i <17 ; i+=2) {
                        Card newCard=new Card(line.substring(i,i+2));
                        player0.state.hand.add(newCard);
                        Game.deck.remove(newCard);
                    }
                }
                else if (line.substring(0,4).equals("hand")&&line.charAt(4)=='1') {
                    player1.state.hand.clear();
                    for (int i = 6; i <17 ; i+=2) {
                        Card newCard=new Card(line.substring(i,i+2));
                        player1.state.hand.add(newCard);
                        Game.deck.remove(newCard);
                    }
                    //always defined second
                    player0.state.hiddenCards.addAll(Game.deck);
                    player0.state.hiddenCards.addAll(player1.state.hand);
                    player1.state.hiddenCards.addAll(Game.deck);
                    player1.state.hiddenCards.addAll(player0.state.hand);
                }
                else if (line.substring(0,4).equals("vskr")) {
                    Game.setTrumpCard(new Card(line.substring(10,12)));
                    attack0=null;
                    attack1=null;
                    defence0=null;
                    defence1=null;
                    Game.outOfTheGame.clear();
                }
                else if (((line.charAt(2)!='+'&& prevLine.charAt(2)=='+')||prevLine.substring(0,3).equals("tru") )&& line.charAt(0)=='0') {
                    attacker=0;
                    player0.state.actionType= State.ActionType.ATTACK;
                    player1.state.actionType= State.ActionType.DEFENCE;
                    Card newCard=new Card(line.substring(2,4));
                    attack0=newCard;
                    Game.cardsOnTable.add(newCard);
                    stateActions0.add(new State.StateAction(new State(player0.state),attack0,gameId));
                }
                else if (((line.charAt(2)!='+'&& prevLine.charAt(2)=='+')||prevLine.substring(0,3).equals("tru") ) && line.charAt(0)=='1') {
                    attacker=1;
                    player1.state.actionType= State.ActionType.ATTACK;
                    player0.state.actionType= State.ActionType.DEFENCE;
                    Card newCard=new Card(line.substring(2,4));
                    attack1=newCard;
                    Game.cardsOnTable.add(newCard);
                    stateActions1.add(new State.StateAction(new State(player1.state),attack1,gameId));

                }
                else if (line.substring(2,4).equals("hv") && line.charAt(0)=='0'){
                    if (prevLine.charAt(0)=='0'){
                        //stateActions1.add(new State.StateAction(new State(player1.state),new ArrayList<>(),gameId));
                        player1.state.hand.addAll(Game.cardsOnTable);
                        Game.cardsOnTable.clear();
                        attack0=null;
                    }
                    else {
                        //write attack & hand for 0 & change hand for 0 & set attack0=null
                        player0.state.hiddenCards.remove(defence1);
                        //stateActions0.add(new State.StateAction(new State(player0.state),new ArrayList<>(attack0)));
                        player1.state.enemyAttack=attack0;
                        //write defence & hand for 1 & change hand for 1 & set defence1=null
                        player1.state.hiddenCards.remove(attack0);
                        stateActions1.add(new State.StateAction(new State(player1.state), defence1, gameId));
                        player0.state.hand.remove(attack0);
                        player1.state.hand.remove(defence1);
                        Game.outOfTheGame.add(defence1);
                        Game.outOfTheGame.add(attack0);
                        attack0=null;
                        defence1=null;
                        Game.cardsOnTable.clear();
                        Game.roundNumber++;
                        player0.state.roundNumber = Game.roundNumber;
                        player1.state.roundNumber = Game.roundNumber;
                        if (!(prevLine.substring(2, 4).equals("be") || prevLine.charAt(0) == '0')) {
                            attacker = 1;
                            player1.state.actionType = State.ActionType.ATTACK;
                            player1.state.enemyAttack=null;
                            player0.state.actionType = State.ActionType.DEFENCE;
                        }
                    }
                }
                else if (line.substring(2,4).equals("hv") && line.charAt(0)=='1'){
                    if (prevLine.charAt(0)=='1'){
                        //stateActions1.add(new State.StateAction(new State(player1.state),new ArrayList<>(),gameId));
                        player0.state.hand.addAll(Game.cardsOnTable);
                        Game.cardsOnTable.clear();
                        attack1=null;
                    }
                    else {
                        //write attack & hand for 1 & change hand for 1 & set attack1=null
                        player1.state.hiddenCards.remove(defence0);
                        //stateActions1.add(new State.StateAction(new State(player1.state),new ArrayList<>(attack1)));
                        player0.state.enemyAttack=attack1;
                        //write defence & hand for 0 & change hand for 0 & set attack0=null
                        player0.state.hiddenCards.remove(attack1);
                        stateActions0.add(new State.StateAction(new State(player0.state),defence0,gameId));
                        player1.state.hand.remove(attack1);
                        player0.state.hand.remove(defence0);
                        Game.outOfTheGame.add(defence0);
                        Game.outOfTheGame.add(attack1);
                        attack1=null;
                        defence0=null;
                        Game.cardsOnTable.clear();
                        Game.roundNumber++;
                        player0.state.roundNumber=Game.roundNumber;
                        player1.state.roundNumber=Game.roundNumber;
                        if (!(prevLine.substring(2,4).equals("be")|| prevLine.charAt(0)=='1')) {
                            attacker=0;
                            player0.state.actionType= State.ActionType.ATTACK;
                            player0.state.enemyAttack=null;
                            player1.state.actionType= State.ActionType.DEFENCE;
                        }
                    }
                }
                else if ((prevLine.length()==4||prevLine.substring(2,4).equals("be") ||prevLine.substring(2,4).equals("hv"))&& line.charAt(0)=='0' && attacker==0 && line.length()==4){
                    //write attack & hand for 0 & change hand for 0 & set attack0=null & .add(line.substring(2,3))
                    player0.state.hiddenCards.remove(defence1);
                    Card newCard=new Card(line.substring(2,4));
                    Game.cardsOnTable.add(newCard);
                    attack0=newCard;
                    if (defence1!=null) Game.cardsOnTable.add(defence1);
                    stateActions0.add(new State.StateAction(new State(player0.state),attack0,gameId));
                    player0.state.hand.remove(attack0);
                }
                else if ((prevLine.length()==4||prevLine.substring(2,4).equals("be") ||prevLine.substring(2,4).equals("hv"))&& line.charAt(0)=='1' && attacker==1 && line.length()==4){
                    //write attack & hand for 1 & change hand for 1 & set attack1=null & .add(line.substring(2,3))
                    player1.state.hiddenCards.remove(defence0);
                    Card newCard=new Card(line.substring(2,4));
                    Game.cardsOnTable.add(newCard);
                    attack1=newCard;
                    if (defence0!=null) Game.cardsOnTable.add(defence0);
                    stateActions1.add(new State.StateAction(new State(player1.state),attack1,gameId));
                    player1.state.hand.remove(attack1);
                        //Game.outOfTheGame.addAll(attack1);

                        //
                }
                else if (line.substring(2,4).equals("be")&& line.charAt(0)=='0') {
                    if (defence0!=null && attack1==null){
                        player0.state.enemyAttack=stateActions1.get(stateActions1.size()-1).action;
                        stateActions0.add(new State.StateAction(new State(player0.state),defence0,gameId));
                    }
                    else if(attack1!=null){
                        player0.state.enemyAttack=attack1;
                        player0.state.hiddenCards.remove(attack1);
                        stateActions0.add(new State.StateAction(new State(player0.state),defence0,gameId));
                    }
                    defence0=null;
                    player0.state.hand.addAll(Game.cardsOnTable);
                }
                else if (line.substring(2,4).equals("be")&& line.charAt(0)=='1') {
                    if (defence1!=null && attack0==null){
                        player1.state.enemyAttack=stateActions0.get(stateActions0.size()-1).action;
                        stateActions1.add(new State.StateAction(new State(player1.state),defence1,gameId));
                    }
                    else if(attack0!=null){
                        player1.state.enemyAttack=attack0;
                        player0.state.hiddenCards.remove(attack0);
                        stateActions1.add(new State.StateAction(new State(player1.state),defence1,gameId));
                    }
                    defence1=null;
                    player1.state.hand.addAll(Game.cardsOnTable);
                }
                else if (line.charAt(2)=='+'&& line.charAt(0)=='0'){
                    for (int i = 2; i <line.length()-1 ; i++) {
                        Card newCard=new Card(line.substring(i+1,i+3));
                        player0.state.hand.add(newCard);
                        Game.deck.remove(newCard);
                        player0.state.hiddenCards.remove(newCard);
                        i+=2;
                    }
                }
                else if (line.charAt(2)=='+'&& line.charAt(0)=='1'){
                    for (int i = 2; i <line.length()-1 ; i++) {
                        Card newCard=new Card(line.substring(i+1,i+3));
                        player1.state.hand.add(newCard);
                        Game.deck.remove(newCard);
                        player1.state.hiddenCards.remove(newCard);
                        i+=2;
                    }
                }
                else if (prevLine.length()==4 && line.length()==4 && line.charAt(0)=='0' && attacker==1){
                    player0.state.enemyAttack=stateActions1.get(stateActions1.size()-1).action;
                    player0.state.hiddenCards.remove(stateActions1.get(stateActions1.size()-1).action);
                    Card newCard=new Card(line.substring(2,4));
                    defence0=newCard;
                    Game.cardsOnTable.add(newCard);
                    stateActions0.add(new State.StateAction(new State(player0.state),defence0,gameId));
                    player0.state.hand.remove(defence0);
                    defence0=null;
                }
                else if (prevLine.length()==4 && line.length()==4 && line.charAt(0)=='1' && attacker==0){
                    player1.state.enemyAttack=stateActions0.get(stateActions0.size()-1).action;
                    player1.state.hiddenCards.remove(stateActions0.get(stateActions0.size()-1).action);
                    Card newCard=new Card(line.substring(2,4));
                    defence1=newCard;
                    Game.cardsOnTable.add(newCard);
                    stateActions1.add(new State.StateAction(new State(player1.state),defence1,gameId));
                    player1.state.hand.remove(defence1);
                    defence1=null;

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
