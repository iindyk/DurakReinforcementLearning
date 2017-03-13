package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.util.ArrayList;

/**
 * Created by igor on 09.10.16.
 */

//attacks and defends with smallest possible cards
public class SimpleAgentPlayer extends Player {
    private static int count;
    public SimpleAgentPlayer(){
        this.name="SimpleAgent"+count;
        this.state=new State();
        SimpleAgentPlayer.count++;
    }

    @Override
    public ArrayList<Card> attack() {
        Card result=this.state.hand.get(0);
        if (Game.cardsOnTable.size()==0) {
            for (Card card:this.state.hand
                 ) {
                if (result.valueIntWithTrump>card.valueIntWithTrump) result=card;
            }
        }
        else {
            for (Card cardTable: Game.cardsOnTable
                 ) {
                for (Card cardHand: this.state.hand
                     ) {
                    if (cardTable.value==cardHand.value && cardHand.valueIntWithTrump<result.valueIntWithTrump) result=cardHand;
                }
            }
        }
        this.state.hand.remove(result);
        ArrayList<Card> res=new ArrayList<>();
        res.add(result);
        return res;
    }

    @Override
    public ArrayList<Card> defend(ArrayList<Card> attack) throws Card.TrumpIsNotDefinedException {
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
        ArrayList<Card> bestDefence=null;
        int bestDefenceValue=1000;
        for (int i = 0; i <possibleDefences.size(); i++) {
            int sumDefenceValue=0;
            for (Card card:
                 possibleDefences.get(i)) {
                sumDefenceValue+=card.valueIntWithTrump;
            }
            if (sumDefenceValue <bestDefenceValue) {
                bestDefenceValue=sumDefenceValue;
                bestDefence=possibleDefences.get(i);
            }
        }
        ArrayList<Card> handDuplicate=new ArrayList<>();
        handDuplicate.addAll(state.hand);
        for (Card att:
                bestDefence) {
            for (Card card:
                    handDuplicate) {
                if (att.value==card.value&&att.suit==card.suit) this.state.hand.remove(card);
            }
        }
        return bestDefence;
    }

    @Override
    public int getCardValue(Card card) {
        if (card==null) return 100;
        else return card.valueInt;
    }

}
