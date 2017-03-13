package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.util.ArrayList;
import java.util.logging.Level;

import static DurakGame.Game.logger;

/**
 * Created by igor on 11.10.16.
 */
public abstract class Player {
    protected String name;
    public State state=new State();


    public abstract ArrayList<Card> attack() throws Card.TrumpIsNotDefinedException;

    public abstract ArrayList<Card> defend(ArrayList<Card> attackCards) throws Card.TrumpIsNotDefinedException;

    public boolean canAttack() {
        this.state.enemyAttack.clear();
        if (Game.cardsOnTable.size()==0) return true;
        else {
            for (Card cardTable: Game.cardsOnTable
                    ) {
                for (Card cardHand: this.state.hand
                        ) {
                    if (cardHand.value==cardTable.value) return true;
                }
            }
        }
        return false;
    }

    public static boolean canAttack(State currentState) {
        currentState.enemyAttack.clear();
        if (currentState.cardsOnTable.size()==0) return true;
        else {
            for (Card cardTable: currentState.cardsOnTable
                    ) {
                for (Card cardHand: currentState.hand
                        ) {
                    if (cardHand.value==cardTable.value) return true;
                }
            }
        }
        return false;
    }

    public boolean canDefend(ArrayList<Card> attack) throws Card.TrumpIsNotDefinedException {
        this.state.enemyKnownCards.removeAll(attack);
        this.state.hiddenCards.removeAll(attack);
        this.state.enemyAttack.clear();
        this.state.enemyAttack.addAll(attack);
        ArrayList<ArrayList<Card>> possibleDefences = new ArrayList<>();
        ArrayList<Card> availableHand0=new ArrayList<>();
        ArrayList<Card> availableHand1=new ArrayList<>();
        ArrayList<Card> availableHand2=new ArrayList<>();
        ArrayList<Card> tmp=new ArrayList<>();
        for (int i = 0; i <this.state.hand.size() ; i++) {
            tmp.clear();
            availableHand0.clear();
            availableHand0.addAll(state.hand);
            if (state.hand.get(i).beats(attack.get(0))) {
                tmp.add(state.hand.get(i));
                availableHand0.remove(state.hand.get(i));
                if (attack.size() > 1) {
                    availableHand1.clear();
                    availableHand1.addAll(availableHand0);
                    for (Card card :
                            availableHand0) {
                        if (card.beats(attack.get(1))) {
                            availableHand1.remove(card);
                            tmp.add(card);
                            if (attack.size() > 2) {
                                availableHand2.clear();
                                availableHand2.addAll(availableHand1);
                                for (Card card1 :
                                        availableHand1) {
                                    if (card1.beats(attack.get(2))) {
                                        availableHand2.remove(card1);
                                        tmp.add(card1);
                                        if (attack.size() > 3) {
                                            for (Card card2 :
                                                    availableHand2) {
                                                if (card2.beats(attack.get(3))) {
                                                    tmp.add(card2);
                                                    possibleDefences.add(new ArrayList(tmp));
                                                    tmp.clear();
                                                }
                                            }
                                        }
                                        else {
                                            possibleDefences.add(new ArrayList(tmp));
                                            tmp.remove(2);
                                        }
                                    }
                                }
                            }
                            else {
                                possibleDefences.add(new ArrayList(tmp));
                                tmp.remove(1);
                            }

                        }
                    }

                }
                else {
                    possibleDefences.add(new ArrayList(tmp));
                    tmp.remove(0);
                }


            }

        }
        return !(possibleDefences.size()==0);
    }

    public static ArrayList<ArrayList<Card>> possibleActions(State currentState) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleActions;
        if (currentState.actionType== State.ActionType.ATTACK){
            possibleActions=possibleAttacks(currentState.hand,currentState.cardsOnTable);
        }
        else {
            possibleActions=possibleDefences(currentState.hand,currentState.enemyAttack);
        }

        return possibleActions;
    }

    public static ArrayList<ArrayList<Card>> possibleDefences(ArrayList<Card> hand, ArrayList<Card> enemyAttack) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleActions=new ArrayList<>(new ArrayList<>());
            possibleActions.add(new ArrayList<>());
            ArrayList<Card> availableHand0 = new ArrayList<>();
            ArrayList<Card> availableHand1 = new ArrayList<>();
            ArrayList<Card> availableHand2 = new ArrayList<>();
            ArrayList<Card> tmp = new ArrayList<>();
            for (int i = 0; i < hand.size(); i++) {
                tmp.clear();
                availableHand0.clear();
                availableHand0.addAll(hand);
                if (hand.get(i).beats(enemyAttack.get(0))) {
                    tmp.add(hand.get(i));
                    availableHand0.remove(hand.get(i));
                    if (enemyAttack.size() > 1) {
                        availableHand1.clear();
                        availableHand1.addAll(availableHand0);
                        for (Card card :
                                availableHand0) {
                            if (card.beats(enemyAttack.get(1))) {
                                availableHand1.remove(card);
                                tmp.add(card);
                                if (enemyAttack.size() > 2) {
                                    availableHand2.clear();
                                    availableHand2.addAll(availableHand1);
                                    for (Card card1 :
                                            availableHand1) {
                                        if (card1.beats(enemyAttack.get(2))) {
                                            availableHand2.remove(card1);
                                            tmp.add(card1);
                                            if (enemyAttack.size() > 3) {
                                                for (Card card2 :
                                                        availableHand2) {
                                                    if (card2.beats(enemyAttack.get(3))) {
                                                        tmp.add(card2);
                                                        possibleActions.add(new ArrayList<>(tmp));
                                                        tmp.clear();
                                                    }
                                                }
                                            } else {
                                                possibleActions.add(new ArrayList<>(tmp));
                                                tmp.remove(2);
                                            }
                                        }
                                    }
                                } else {
                                    possibleActions.add(new ArrayList<>(tmp));
                                    tmp.remove(1);
                                }

                            }
                        }

                    } else {
                        possibleActions.add(new ArrayList<>(tmp));
                        tmp.remove(0);
                    }


                }

            }
            ArrayList<ArrayList<Card>> possibleActionsCopy=new ArrayList<>(possibleActions);
            for (int i = 0; i <possibleActionsCopy.size() ; i++) {
                for (int j = i+1; j <possibleActionsCopy.size() ; j++) {
                    ArrayList<Card> l1=possibleActionsCopy.get(i);
                    ArrayList<Card> l2=possibleActionsCopy.get(j);
                    if (l1.containsAll(l2)&&l2.containsAll(l1)) possibleActions.remove(l2);
                }
            }
        return possibleActions;
    }

    public static ArrayList<ArrayList<Card>> possibleAttacks(ArrayList<Card> hand, ArrayList<Card> cardsOnTable) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleActions=new ArrayList<>(new ArrayList<>());
            ArrayList<Card> tmp=new ArrayList<>();
            ArrayList<Card> retainedHand=new ArrayList<>();
            retainedHand.addAll(hand);

            int in=0;
            if (!(cardsOnTable.isEmpty())){
                possibleActions.add(new ArrayList<>());
                for (Card cardOnHand:
                        hand) {
                    for (Card cardOnTable:
                            cardsOnTable) {
                        if (cardOnHand.value==cardOnTable.value) in=1;
                    }
                    if (in==0) retainedHand.remove(cardOnHand);
                    else in=0;
                }
            }

            for (int i = 0; i <retainedHand.size() ; i++) {
                tmp.add(retainedHand.get(i));
                possibleActions.add(new ArrayList<>(tmp));
                for (int j = i+1; j < retainedHand.size(); j++) {
                    if (retainedHand.get(i).value==retainedHand.get(j).value) {
                        tmp.add(retainedHand.get(j));
                        possibleActions.add(new ArrayList<>(tmp));
                        for (int k = j+1; k <retainedHand.size(); k++) {
                            if (retainedHand.get(j).value==retainedHand.get(k).value){
                                tmp.add(retainedHand.get(k));
                                possibleActions.add(new ArrayList<>(tmp));
                                for (int l = k+1; l <retainedHand.size() ; l++) {
                                    if (retainedHand.get(k).value==retainedHand.get(l).value) {
                                        tmp.add(retainedHand.get(l));
                                        possibleActions.add(new ArrayList<>(tmp));
                                    }
                                }
                                tmp.clear();
                            }
                        }
                        tmp.clear();
                    }
                }
                tmp.clear();
            }

        return possibleActions;
    }

    public static boolean canDefend(State currentState,ArrayList<Card> attack) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleDefences = new ArrayList<>();
        ArrayList<Card> availableHand0=new ArrayList<>();
        ArrayList<Card> availableHand1=new ArrayList<>();
        ArrayList<Card> availableHand2=new ArrayList<>();
        ArrayList<Card> tmp=new ArrayList<>();
        for (int i = 0; i <currentState.hand.size() ; i++) {
            tmp.clear();
            availableHand0.clear();
            availableHand0.addAll(currentState.hand);
            if (currentState.hand.get(i).beats(attack.get(0))) {
                tmp.add(currentState.hand.get(i));
                availableHand0.remove(currentState.hand.get(i));
                if (attack.size() > 1) {
                    availableHand1.clear();
                    availableHand1.addAll(availableHand0);
                    for (Card card :
                            availableHand0) {
                        if (card.beats(attack.get(1))) {
                            availableHand1.remove(card);
                            tmp.add(card);
                            if (attack.size() > 2) {
                                availableHand2.clear();
                                availableHand2.addAll(availableHand1);
                                for (Card card1 :
                                        availableHand1) {
                                    if (card1.beats(attack.get(2))) {
                                        availableHand2.remove(card1);
                                        tmp.add(card1);
                                        if (attack.size() > 3) {
                                            for (Card card2 :
                                                    availableHand2) {
                                                if (card2.beats(attack.get(3))) {
                                                    tmp.add(card2);
                                                    possibleDefences.add(new ArrayList(tmp));
                                                    tmp.clear();
                                                }
                                            }
                                        }
                                        else {
                                            possibleDefences.add(new ArrayList(tmp));
                                            tmp.remove(2);
                                        }
                                    }
                                }
                            }
                            else {
                                possibleDefences.add(new ArrayList(tmp));
                                tmp.remove(1);
                            }

                        }
                    }

                }
                else {
                    possibleDefences.add(new ArrayList(tmp));
                    tmp.remove(0);
                }


            }

        }
        return !(possibleDefences.size()==0);
    }

    public static boolean canDefend(ArrayList<Card> hand,ArrayList<Card> attack) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleDefences = new ArrayList<>();
        ArrayList<Card> availableHand0=new ArrayList<>();
        ArrayList<Card> availableHand1=new ArrayList<>();
        ArrayList<Card> availableHand2=new ArrayList<>();
        ArrayList<Card> tmp=new ArrayList<>();
        for (int i = 0; i <hand.size() ; i++) {
            tmp.clear();
            availableHand0.clear();
            availableHand0.addAll(hand);
            if (hand.get(i).beats(attack.get(0))) {
                tmp.add(hand.get(i));
                availableHand0.remove(hand.get(i));
                if (attack.size() > 1) {
                    availableHand1.clear();
                    availableHand1.addAll(availableHand0);
                    for (Card card :
                            availableHand0) {
                        if (card.beats(attack.get(1))) {
                            availableHand1.remove(card);
                            tmp.add(card);
                            if (attack.size() > 2) {
                                availableHand2.clear();
                                availableHand2.addAll(availableHand1);
                                for (Card card1 :
                                        availableHand1) {
                                    if (card1.beats(attack.get(2))) {
                                        availableHand2.remove(card1);
                                        tmp.add(card1);
                                        if (attack.size() > 3) {
                                            for (Card card2 :
                                                    availableHand2) {
                                                if (card2.beats(attack.get(3))) {
                                                    tmp.add(card2);
                                                    possibleDefences.add(new ArrayList(tmp));
                                                    tmp.clear();
                                                }
                                            }
                                        }
                                        else {
                                            possibleDefences.add(new ArrayList(tmp));
                                            tmp.remove(2);
                                        }
                                    }
                                }
                            }
                            else {
                                possibleDefences.add(new ArrayList(tmp));
                                tmp.remove(1);
                            }

                        }
                    }

                }
                else {
                    possibleDefences.add(new ArrayList(tmp));
                    tmp.remove(0);
                }


            }

        }
        return !(possibleDefences.size()==0);
    }

    public void takeCardFromDeck(){
        Card card=Game.deck.get(Game.deck.size()-1);
        Game.deck.remove(Game.deck.size()-1);
        this.state.hand.add(card);
        this.state.hiddenCards.remove(card);
        logger.log(Level.INFO,this.name+" takes ["+card+"] from deck");
    }

    private void takeCard(Card card){
        this.state.hand.add(card);
    }

    void takeCards(ArrayList<Card> cards){
        for (Card card:
             cards) {
            takeCard(card);
        }
    }

    public abstract int getCardValue(Card card);
}
