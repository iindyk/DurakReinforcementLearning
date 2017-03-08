package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by igor on 11.10.16.
 */
public abstract class Player {
    public String name;
    public State state;

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

    public void takeCardFromDeck(){
        Card card=Game.deck.get(Game.deck.size()-1);
        Game.deck.remove(Game.deck.size()-1);
        this.state.hand.add(card);
        this.state.hiddenCards.remove(card);
    }

    public void takeCard(Card card){
        this.state.hand.add(card);
    }

    public void takeCards(ArrayList<Card> cards){
        for (Card card:
             cards) {
            takeCard(card);
        }
    }

    public abstract int getCardValue(Card card);
}
