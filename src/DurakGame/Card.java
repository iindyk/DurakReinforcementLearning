package DurakGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.lang.StrictMath.abs;

/**
 * Created by igor on 28.09.16.
 */
/* suits:
'h' - heart
'd' - diamond
'c' - club
's' - spade
'\0' - unknown
'j j' - hidden card
 */
public class Card {
    public char value;
    char suit;
    int valueInt;
    public int valueIntWithTrump;
    private Card(){}

    public Card(char value, char suit) throws UnknownSuitException {
        if (suit!='h' && suit!='d' && suit!='c' && suit!='s') throw new UnknownSuitException();
        this.value=value;
        this.suit=suit;
        if (Character.isDigit(value)) {
            this.valueInt=Character.getNumericValue(value);
        }
        else{
            switch (value){
                case 't':
                    this.valueInt=10;
                    break;
                case 'j':
                    this.valueInt=11;
                    break;
                case 'q':
                    this.valueInt=12;
                    break;
                case 'k':
                    this.valueInt=13;
                    break;
                case 'a':
                    this.valueInt=14;
                    break;
            }
        }
    }

    public Card(String cardString) {
        this.value=cardString.charAt(1);
        this.suit=cardString.charAt(0);
        if (Character.isDigit(this.value)) {
            this.valueInt=Character.getNumericValue(value);
        }
        else{
            switch (value){
                case 't':
                    this.valueInt=10;
                    break;
                case 'j':
                    this.valueInt=11;
                    break;
                case 'q':
                    this.valueInt=12;
                    break;
                case 'k':
                    this.valueInt=13;
                    break;
                case 'a':
                    this.valueInt=14;
                    break;
            }
        }
        if (Game.getTrumpCard() != null && Game.getTrumpCard().suit==this.suit) this.valueIntWithTrump=this.valueInt+9;
    }

    public Card(int valueIntWithTrump){
        this.valueIntWithTrump=valueIntWithTrump;
        if (valueIntWithTrump==24) {
            this.value='j';//joker - hidden card
            this.suit='j';
            return;
        }
        if (valueIntWithTrump>14) {
            this.valueInt=valueIntWithTrump-9;
            this.suit=Game.getTrumpCard().suit;
        }
        else {
            this.valueInt=valueIntWithTrump;
        }
        if (this.valueInt<10) this.value=(char)('0'+this.valueInt);
        else if (this.valueInt==10) this.value='t';
        else if (this.valueInt==11) this.value='j';
        else if (this.valueInt==12) this.value='q';
        else if (this.valueInt==13) this.value='k';
        else this.value='a';
    }

    public static void defineValuesWithTrump(ArrayList<Card> deck){
        for (Card card:
             deck) {
            if (card.suit==Game.getTrumpCard().suit) card.valueIntWithTrump=card.valueInt+9;
            else card.valueIntWithTrump=card.valueInt;
        }
    }

    public boolean beats(Card card2,char trumpSuit){
        if ((this.suit==card2.suit)&&(this.valueInt>card2.valueInt)) return true;
        else if ((this.suit!=card2.suit)&&(this.suit==trumpSuit)) return true;
        else return false;
    }

    public boolean beats(Card card) throws TrumpIsNotDefinedException {
        if (Game.getTrumpCard()==null) throw new TrumpIsNotDefinedException();
        return beats(card, Game.getTrumpCard().suit);
    }

    public static ArrayList<Card> createDeck() throws UnknownSuitException {
        ArrayList<Card> result=new ArrayList<>();
        Card[] sortedDeck={new Card('6','h'),new Card('7','h'),new Card('8','h'),new Card('9','h'),new Card('t','h'),new Card('j','h'),
                new Card('q','h'), new Card('k','h'), new Card('a','h'),
                new Card('6','d'),new Card('7','d'),new Card('8','d'),new Card('9','d'),new Card('t','d'),new Card('j','d'),
                new Card('q','d'), new Card('k','d'), new Card('a','d'),
                new Card('6','c'),new Card('7','c'),new Card('8','c'),new Card('9','c'),new Card('t','c'),new Card('j','c'),
                new Card('q','c'), new Card('k','c'), new Card('a','c'),
                new Card('6','s'),new Card('7','s'),new Card('8','s'),new Card('9','s'),new Card('t','s'),new Card('j','s'),
                new Card('q','s'), new Card('k','s'), new Card('a','s')};
        Collections.addAll(result,sortedDeck);
        Random random=new Random();
        for (int i = 2; i <result.size() ; i++) {
            int j=random.nextInt(i);
            Card temp;
            temp=result.get(i);
            result.set(i,result.get(j));
            result.set(j,temp);
        }
        return result;
    }

    @Override
    public String toString(){
        return this.value+" "+this.suit;
    }

    @Override
    public int hashCode() {
        int result = (int) value;
        result = 31 * result + (int) suit;
        return result;
    }

    @Override
    public boolean equals(Object o){
        Card card;
        if (!(o instanceof Card)) return false;
        else card=(Card) o;
        if (this.value==card.value && this.suit==card.suit) return true;
        else return false;
    }

    public static ArrayList<Card> makeCardsList(ArrayList<String> stringList) {
        ArrayList<Card> result=new ArrayList<>();
        for (String strCard:
             stringList) {
            result.add(new Card(strCard));
        }
        Card temp;
        for (int i = 0; i <result.size() ; i++) {
            for (int j = i+1; j <result.size() ; j++) {
                if (result.get(i).valueIntWithTrump>result.get(j).valueIntWithTrump) {
                    temp=result.get(i);
                    result.set(i,result.get(j));
                    result.set(j,temp);
                }
            }
        }
        return result;
    }

    public int distTo(Card card){
        if (card==null) return card.valueIntWithTrump;
        int r;
        if (this.suit==card.suit) r=abs(this.valueIntWithTrump-card.valueIntWithTrump);
        else r=abs(this.valueIntWithTrump-card.valueIntWithTrump)+5;//todo why 5?
        return r;
    }

    public static int distTo(ArrayList<Card> cardList1, ArrayList<Card> cardList2){
        int result=0;
        ArrayList<Card> ordcl1=getSorted(cardList1);
        ArrayList<Card> ordcl2=getSorted(cardList2);
        for (int i = 0; i <ordcl1.size() && i<ordcl2.size(); i++) {
            result+=ordcl1.get(i).distTo(ordcl2.get(i));
        }
        if (ordcl1.size()>ordcl2.size()){
            for (int i = ordcl2.size(); i <ordcl1.size() ; i++) {
                result+=ordcl1.get(i).distTo(null);
            }
        }
        else {
            for (int i = ordcl1.size(); i <ordcl2.size() ; i++) {
                result+=ordcl2.get(i).distTo(null);
            }
        }
        return result;
    }

    public static ArrayList<Card> getSorted(ArrayList<Card> cards){
        ArrayList<Card> result=new ArrayList<>(cards.size());
        result.addAll(cards);
        Card temp;
        //sorting by suit
        for (int i = 0; i <result.size() ; i++) {
            for (int j = i+1; j <result.size() ; j++) {
                if ((result.get(i).suit=='s' && result.get(j).suit=='c') ||(result.get(i).suit=='s' && result.get(j).suit=='d')||
                        (result.get(i).suit=='s' && result.get(j).suit=='h')||(result.get(i).suit=='c' && result.get(j).suit=='d')||
                        (result.get(i).suit=='c' && result.get(j).suit=='h')||(result.get(i).suit=='d' && result.get(j).suit=='h')) {
                    temp=result.get(i);
                    result.set(i,result.get(j));
                    result.set(j,temp);
                }
            }
        }
        //sorting by value
        for (int i = 0; i <result.size() ; i++) {
            for (int j = i+1; j <result.size() ; j++) {
                if (result.get(i).valueIntWithTrump>result.get(j).valueIntWithTrump) {
                    temp=result.get(i);
                    result.set(i,result.get(j));
                    result.set(j,temp);
                }
            }
        }
        
        return result;
    }

    public class TrumpIsNotDefinedException extends Exception{}

    public class UnknownSuitException extends Exception{}
}
