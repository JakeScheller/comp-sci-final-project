import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;

class Card implements Comparable<Card> {
	private int value;
	private String suit;

	public Card(int value, String suit) {
		this.value = value;
		this.suit = suit;
	}

	public int getValue() {
		return this.value;
	}

	public String getSuit() {
		return this.suit;
	}

	public String toString() {
		if (this.suit.equals("JOKER")) {
			return this.suit;
		}
		String valueString = Integer.toString(this.value);
		if (this.value > 10) {
			String[] names = {"JACK", "QUEEN", "KING", "ACE", "JOKER"};
			valueString = names[this.value-11];
		}
		return valueString + " of " + this.suit;
	}

	public int compareTo(Card otherCard) {
		return this.value - otherCard.getValue();
	}
}

class CardDeck {
	private ArrayList<Card> cards;
	private int curCardIdx;

	public CardDeck(int numDecks) {
		this.cards = new ArrayList<Card>();
		this.curCardIdx = 0;

		String[] suits = {"HEARTS", "DIAMONDS", "CLUBS", "SPADES"};
		int[] values = {2,3,4,5,6,7,8,9,10,11,12,13,14};
		for (int ii = 0; ii < numDecks; ii++) {
			for (String suit : suits) {
				for (int value : values) {
					this.cards.add(new Card(value, suit));
				}
			}
			this.cards.add(new Card(15, "JOKER"));
			this.cards.add(new Card(15, "JOKER"));
		}
	}

	public Card drawCard() {
		if (this.curCardIdx < this.cards.size()) {
			int idx = (int)(Math.random() * getNumCards()) + this.curCardIdx;
			Card selectedCard = this.cards.get(idx);
			this.cards.set(idx, this.cards.get(this.curCardIdx));
			this.cards.set(this.curCardIdx, selectedCard);
			this.curCardIdx++;
			return selectedCard;
		}
		else {
			return null;
		}
	}

	public void addCard(Card card) {
		this.cards.add(card);
	}

	public int getNumCards() {
		return Math.max(this.cards.size() - this.curCardIdx, 0);
	}
}

class Player implements Comparable<Player> {
	private Game game;
	private String name;
	private ArrayList<Card> attackCards;
	private ArrayList<Card> defenseCards;
	private int health;
	private int speed;
	private boolean isDead;

	public Player(String name, Game game) {
		this.name = name;
		this.game = game;
		this.attackCards = new ArrayList<Card>();
		this.defenseCards = new ArrayList<Card>();
		this.health = 15;
		this.speed = 0;
		this.isDead = false;
	}

	private int getAttackSum() {
		int sum = 0;
		for (Card card : this.attackCards) {
			sum += card.getValue();
		}
		return sum;
	}

	private int getDefenseSum() {
		int sum = 0;
		for (Card card : this.defenseCards) {
			sum += card.getValue();
		}
		return sum;
	}

	public void setDead() {
		this.isDead = true;
	}

	public boolean getDead() {
		return this.isDead;
	}

	public String getName() {
		return this.name;
	}

	public int getSpeed() {
		return this.speed;
	}

	public int getHealth() {
		return this.health;
	}

	public ArrayList<Card> getAttackCards() {
		return this.attackCards;
	}

	public ArrayList<Card> getDefenseCards() {
		return this.defenseCards;
	}

	public void loseHealth(int amount) {
		this.health = Math.max(this.health - amount, 0);
	}

	public int compareTo(Player otherPlayer) {
		if (this.speed == otherPlayer.getSpeed()) {
			return otherPlayer.getTotalResources() - getTotalResources();
		}
		return otherPlayer.getSpeed() - this.speed;
	}

	public void clearScreen() {
		// Note: On Linux, printing this clears the terminal screen.
		System.out.println(((char)27) + "[2J" + ((char)27) + "[0;0H");
	}

	public int getTotalResources() {
		return this.health + this.speed + getAttackSum() + getDefenseSum();
	}

	public int getNumStealableCards() {
		return this.attackCards.size() + this.defenseCards.size();
	}

	public void printStealableCards() {
		System.out.println(this.name + "'s cards:");
		System.out.println("\tCLUBS (attack):");
		System.out.print("\t\t");
		if (this.attackCards.size() > 0) {
			Collections.sort(this.attackCards);
			for (int idx = 0; idx < this.attackCards.size(); idx++) {
				System.out.print(this.attackCards.get(idx).getValue() + "");
				if (idx != this.attackCards.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println();
		}
		else {
			System.out.println("No cards of this suit.");
		}
		System.out.println("\tDIAMONDS (defense):");
		System.out.print("\t\t");
		if (this.defenseCards.size() > 0) {
			Collections.sort(this.defenseCards);
			for (int idx = 0; idx < this.defenseCards.size(); idx++) {
				System.out.print(this.defenseCards.get(idx).getValue() + "");
				if (idx != this.defenseCards.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println();
		}
		else {
			System.out.println("No cards of this suit.");
		}
	}

	public void drawInitialCards() {
		Scanner scan = new Scanner(System.in);
		clearScreen();
		System.out.println("You are " + this.name + ".");
		System.out.println("You will now draw six cards and add each card to your stat values");
		System.out.println("You start with no attack cards, no defense cards, 15 health, and 0 speed.");
		drawCards(6);
		System.out.println("You are finished drawing cards.");
		System.out.println("You will now discard your HEARTS and SPADES cards because their values have already been added to your stats.");
		System.out.println();
		printStats();
		System.out.print("Press enter to finish your turn.");
		scan.nextLine();
		clearScreen();
	}

	public void drawCards(int numCards) {
		while (numCards > 0) {
			Card card = this.game.getDeck().drawCard();
			if (card == null) {
				System.out.println("\tOh no! The deck is out of cards!");
				System.out.println("\tYou don't get to draw any more cards.");
				numCards = 0;
			}
			else {
				if (card.getSuit().equals("JOKER")) {
					System.out.println("\tYou drew a JOKER, so you get to draw an extra card. (Jokers are not playable cards.)");
					numCards += 1;
				}
				else {
					System.out.print("\tYou drew a " + card + ".");
					if (card.getSuit().equals("HEARTS")) {
						System.out.println(" This adds " + card.getValue() + " points to your health.");
					}
					else if (card.getSuit().equals("SPADES")) {
						System.out.println(" This adds " + card.getValue() + " points to your speed.");
					}
					else {
						System.out.println();
					}
					this.addCardToStats(card);
					numCards -= 1;
				}
			}
		}
	}

	public void printStats() {
		System.out.println(this.name + "'s cards:");
		System.out.println("\tCLUBS (attack):");
		System.out.print("\t\t");
		if (this.attackCards.size() > 0) {
			Collections.sort(this.attackCards);
			for (int idx = 0; idx < this.attackCards.size(); idx++) {
				System.out.print(this.attackCards.get(idx).getValue() + "");
				if (idx != this.attackCards.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println();
		}
		else {
			System.out.println("No cards of this suit.");
		}
		System.out.println("\t\tTotal: " + getAttackSum());
		System.out.println("\tDIAMONDS (defense):");
		System.out.print("\t\t");
		if (this.defenseCards.size() > 0) {
			Collections.sort(this.defenseCards);
			for (int idx = 0; idx < this.defenseCards.size(); idx++) {
				System.out.print(this.defenseCards.get(idx).getValue() + "");
				if (idx != this.defenseCards.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println();
		}
		else {
			System.out.println("No cards of this suit.");
		}
		System.out.println("\t\tTotal: " + getDefenseSum());
		System.out.println("\tHEARTS (health):");
		System.out.println("\t\tTotal: " + this.health);
		System.out.println("\tSPADES (speed):");
		System.out.println("\t\tTotal: " + this.speed);
	}

	public void addCardToStats(Card card) {
		if (card.getSuit().equals("HEARTS")) {
			this.health += card.getValue();
		}
		else if (card.getSuit().equals("DIAMONDS")) {
			this.defenseCards.add(card);
		}
		else if (card.getSuit().equals("CLUBS")) {
			this.attackCards.add(card);
		}
		else if (card.getSuit().equals("SPADES")) {
			this.speed += card.getValue();
		}
	}

	public void doTurn() {
		Scanner scan = new Scanner(System.in);
		clearScreen();
		System.out.println("You are " + this.name + ".");
		this.printStats();
		System.out.println();

		String inp = "";
		boolean turnFinished = false;
		while (!turnFinished) {
			System.out.println("What would you like to do?");
			System.out.println("1. Attack a player");
			System.out.println("2. Draw a card");
			System.out.println("3. Finish turn");

			boolean haveValidInput = false;
			while (!haveValidInput) {
				System.out.print("?> ");
				inp = scan.nextLine().trim().toLowerCase();
				if (inp.equals("1")) {
					haveValidInput = true;
					if (this.attackCards.size() == 0) {
						System.out.println("You have no attack cards. You can not attack.");
						System.out.println();
					}
					else {
						turnFinished = doAttack();
					}
				}
				else if (inp.equals("2")) {
					haveValidInput = true;
					if (this.game.getDeck().getNumCards() == 0) {
						System.out.println("The deck has no cards, so you cannot draw one.");
						System.out.println();
					}
					else {
						turnFinished = true;
						doDrawCard();
					}
				}
				else if (inp.equals("3")) {
					haveValidInput = true;
					turnFinished = true;
					System.out.println("Finishing turn.");
				}
				else if (!inp.equals("")) {
					System.out.println("Invalid input.");
				}
			}
		}

		if (!inp.equals("1")) {
			System.out.print("Press enter to finish your turn.");
			scan.nextLine();
		}
		clearScreen();
	}

	public void doDrawCard() {
		System.out.println("You will now draw one card.");
		drawCards(1);
		System.out.println("You are finished drawing cards.");
		System.out.println();
		printStats();
	}

	public boolean doAttack() {
		Scanner scan = new Scanner(System.in);
		System.out.print("You have these attack cards: ");
		Collections.sort(this.attackCards);
		for (int idx = 0; idx < this.attackCards.size(); idx++) {
			System.out.print(this.attackCards.get(idx).getValue() + "");
			if (idx != this.attackCards.size() - 1) {
				System.out.print(", ");
			}
		}
		System.out.println();
		System.out.println("Which card would you like to attack with? (enter 'q' to cancel)");

		Card attackCard = null;
		while (attackCard == null) {
			System.out.print("?> ");
			String inp = scan.nextLine().trim().toLowerCase();
			if (inp.equals("q")) {
				System.out.println();
				return false;
			}
			else if (!inp.equals("")) {
				try {
					int attackValue = Integer.parseInt(inp);
					for (Card card : this.attackCards) {
						if (card.getValue() == attackValue) {
							attackCard = card;
							break;
						}
					}
					if (attackCard == null) {
						System.out.println("You don't have that card.");
					}
				}
				catch (NumberFormatException ex) {
					System.out.println("Invalid input.");
				}
			}
		}
		System.out.println("You are attacking with a value of " + attackCard.getValue() + ".");

		Player enemy = null;
		System.out.println("Who would you like to attack? (enter 'q' to cancel)");
		ArrayList<Player> enemies = new ArrayList<Player>();
		for (int idx = 0; idx < this.game.getAlivePlayers().size(); idx++) {
			Player player = this.game.getAlivePlayers().get(idx);
			if (player != this) {
				enemies.add(player);
				System.out.println((enemies.size()) + ". " + player.getName());
			}
		}
		while (enemy == null) {
			System.out.print("?> ");
			String inp = scan.nextLine().trim().toLowerCase();
			if (inp.equals("q")) {
				System.out.println();
				return false;
			}
			else if (!inp.equals("")) {
				int inpNum = -1;
				try {
					inpNum = Integer.parseInt(inp);
				}
				catch (NumberFormatException ex) {}
				if (inpNum > 0 && inpNum <= enemies.size()) {
					enemy = enemies.get(inpNum-1);
				}
				else {
					System.out.println("Invalid input.");
				}
			}
		}
		System.out.println("You are attacking " + enemy.getName() + " with a value of " + attackCard.getValue() + ".");

		this.attackCards.remove(attackCard);
		this.game.getDeck().addCard(attackCard);
		System.out.print("Press enter to perform the attack.");
		scan.nextLine();
		clearScreen();
		this.game.performAttack(this, enemy, attackCard.getValue());
		return true;
	}

	public void doWasAttacked(Player attacker, int attackValue) {
		Scanner scan = new Scanner(System.in);
		System.out.println("You are " + this.name + ".");
		System.out.println(attacker.getName() + " is attacking you!");
		printStats();
		System.out.println();

		Card defenseCard = null;
		if (this.defenseCards.size() == 0) {
			System.out.println("You have no defense cards. You must endure the attack.");
		}
		else {
			System.out.print("You have these defense cards: ");
			Collections.sort(this.defenseCards);
			for (int idx = 0; idx < this.defenseCards.size(); idx++) {
				System.out.print(this.defenseCards.get(idx).getValue() + "");
				if (idx != this.defenseCards.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println();
			System.out.println("Which card would you like to defend with? (enter 'x' to use no defense card)");
			while (defenseCard == null) {
				System.out.print("?> ");
				String inp = scan.nextLine().trim().toLowerCase();
				if (inp.equals("x")) {
					break;
				}
				else if (!inp.equals("")) {
					try {
						int defenseValue = Integer.parseInt(inp);
						for (Card card : this.defenseCards) {
							if (card.getValue() == defenseValue) {
								defenseCard = card;
								break;
							}
						}
						if (defenseCard == null) {
							System.out.println("You don't have that card.");
						}
					}
					catch (NumberFormatException ex) {
						System.out.println("Invalid input.");
					}
				}
			}

			if (defenseCard == null) {
				System.out.println("You will not use a defense card.");
			}
			else {
				System.out.println("You are defending with a value of " + defenseCard.getValue() + ".");
				this.defenseCards.remove(defenseCard);
			}
			System.out.print("Press enter to continue.");
			scan.nextLine();
			clearScreen();

			if (defenseCard == null) {
				this.game.completeAttack(attacker, this, attackValue, 0);
			}
			else {
				this.game.completeAttack(attacker, this, attackValue, defenseCard.getValue());
			}
		}
	}

	public void doStealCardsFromPlayer(Player victim, int numCards) {
		Scanner scan = new Scanner(System.in);
		int numCardsChosen = 0;
		int numStealableCards = victim.getNumStealableCards();
		if (numStealableCards < numCards) {
			String pluralized = (numStealableCards > 1) ? "cards" : "card";
			System.out.println(victim.getName() + " only has " + numStealableCards + " " + pluralized + " so " + this.name + " only gets to steal " + numStealableCards + " " + pluralized + ".");
			numCards = numStealableCards;
		}
		while (numCards > 0) {
			if (numCards == 1 && numCardsChosen == 0) {
				System.out.println(this.name + ", choose the suit of your card:");
			}
			else if (numCardsChosen == 0) {
				System.out.println(this.name + ", choose the suit of your first card:");
			}
			else if (numCardsChosen == 1) {
				System.out.println(this.name + ", choose the suit of your second card:");
			}
			else if (numCardsChosen == 2) {
				System.out.println(this.name + ", choose the suit of your third card:");
			}
			System.out.println("1. CLUBS (attack)");
			System.out.println("2. DIAMONDS (defense)");
			String suit = "";
			while (suit.equals("")) {
				System.out.print("?> ");
				String inp = scan.nextLine().trim().toLowerCase();
				if (inp.equals("1")) {
					if (victim.getAttackCards().size() == 0) {
						System.out.println(victim.getName() + " has no attack cards. Choose another suit.");
					}
					else {
						suit = "CLUBS";
					}
				}
				else if (inp.equals("2")) {
					if (victim.getDefenseCards().size() == 0) {
						System.out.println(victim.getName() + " has no defense cards. Choose another suit.");
					}
					else {
						suit = "DIAMONDS";
					}
				}
				else if (!inp.equals("")) {
					System.out.println("Invalid input.");
				}
			}

			ArrayList<Card> possibleCards = null;
			if (suit.equals("CLUBS")) {
				possibleCards = victim.getAttackCards();
				System.out.println(victim.getName() + " has these attack cards: ");
			}
			else if (suit.equals("DIAMONDS")) {
				possibleCards = victim.getDefenseCards();
				System.out.println(victim.getName() + " has these defense cards: ");
			}
			Collections.sort(possibleCards);
			for (int idx = 0; idx < possibleCards.size(); idx++) {
				System.out.print(possibleCards.get(idx).getValue() + "");
				if (idx != possibleCards.size() - 1) {
					System.out.print(", ");
				}
			}
			System.out.println();

			System.out.println("Which value will you steal?");
			Card stolenCard = null;
			while (stolenCard == null) {
				System.out.println("?> ");
				String inp = scan.nextLine().trim().toLowerCase();
				if (!inp.equals("")) {
					try {
						int value = Integer.parseInt(inp);
						for (Card card : possibleCards) {
							if (card.getValue() == value) {
								stolenCard = card;
								break;
							}
						}
						if (stolenCard == null) {
							System.out.println(victim.getName() + " doesn't have that card.");
						}
					}
					catch (NumberFormatException ex) {
						System.out.println("Invalid input.");
					}
				}
			}

			if (suit.equals("CLUBS")) {
				victim.removeCard(stolenCard);
				this.attackCards.add(stolenCard);
			}
			else if (suit.equals("DIAMONDS")) {
				victim.removeCard(stolenCard);
				this.defenseCards.add(stolenCard);
			}

			System.out.println(this.name + " stole the " + stolenCard.getValue() + " of " + suit + " from " + victim.getName() + ".");
			System.out.println();
			victim.printStealableCards();
			System.out.println();

			numCards--;
			numCardsChosen++;
		}
	}

	public void removeCard(Card card) {
		if (card.getSuit().equals("CLUBS")) {
			this.attackCards.remove(card);
		}
		else if (card.getSuit().equals("DIAMONDS")) {
			this.defenseCards.remove(card);
		}
	}
}

public class Game {
	private ArrayList<Player> players;
	private CardDeck deck;

	public Game() {
		this.players = new ArrayList<Player>();
		this.deck = new CardDeck(2);
	}

	public ArrayList<Player> getPlayers() {
		return this.players;
	}

	public ArrayList<String> getPlayerNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (Player player : getAlivePlayers()) {
			names.add(player.getName());
		}
		return names;
	}

	public ArrayList<Player> getAlivePlayers() {
		ArrayList<Player> alivePlayers = new ArrayList<Player>();
		for (Player player : this.players) {
			if (!player.getDead()) {
				alivePlayers.add(player);
			}
		}
		return alivePlayers;
	}

	public static void main(String[] args) {
		Game game = new Game();
		game.doAddPlayersPhase();
	}

	public CardDeck getDeck() {
		return this.deck;
	}

	public void clearScreen() {
		System.out.println(((char)27) + "[2J" + ((char)27) + "[0;0H");
	}

	public void doAddPlayersPhase() {
		Scanner scan = new Scanner(System.in);
		clearScreen();
		System.out.println("Add at least 2 players.");
		System.out.println();

		boolean doneAddingPlayers = false;
		while (!doneAddingPlayers) {
			if (this.players.size() == 0) {
				System.out.println("No players have been added.");
			}
			else {
				System.out.println("Players: " + String.join(", ", getPlayerNames()));

			}
			System.out.println("Pick an action.");
			System.out.println("1. Add a new player.");
			System.out.println("2. Finish adding players.");

			boolean haveValidInput = false;
			while (!haveValidInput) {
				System.out.print("?> ");
				String inp = scan.nextLine().trim().toLowerCase();
				if (inp.equals("1")) {
					haveValidInput = true;
					System.out.print("Player's name? ");
					String name = scan.nextLine();
					this.players.add(new Player(name, this));
					System.out.println();
				}
				else if (inp.equals("2")) {
					if (this.players.size() < 2) {
						System.out.println("You must add at least 2 players.");
					}
					else {
						haveValidInput = true;
						doneAddingPlayers = true;
					}
				}
				else if (!inp.equals("")) {
					System.out.println("Invalid input.");
				}
			}
		}

		System.out.println();
		System.out.println("You have added " + this.players.size() + " players: " + String.join(", ", getPlayerNames()));
		System.out.print("Press enter to continue to the setup phase.");
		scan.nextLine();
		doSetupPhase();
	}

	public void doSetupPhase() {
		Scanner scan = new Scanner(System.in);
		clearScreen();
		System.out.println("This game uses 2 decks of cards. That means there are 102 cards in total.");
		System.out.println("Each player will now draw six cards to determine their initial stats.");
		System.out.println("If a player draws a JOKER, he or she gets to draw an extra card.");
		System.out.println();

		for (Player player : this.players) {
			System.out.println("It is " + player.getName() + "'s turn. Only " + player.getName() + " may look at the screen.");
			System.out.print("Press enter to start " + player.getName() + "'s turn.");
			scan.nextLine();
			player.drawInitialCards();
		}

		System.out.println("Everyone has drawn their initial cards.");
		System.out.print("Press enter to continue to the play phase.");
		scan.nextLine();
		this.doPlayPhase();
	}

	public void doPlayPhase() {
		Scanner scan = new Scanner(System.in);
		clearScreen();
		System.out.println("Starting play phase. Have fun!");
		System.out.println();

		boolean gameOver = false;
		while (!gameOver) {
			System.out.println("The turn order is based on the speed stat. The turn order this round is:");
			Collections.sort(this.players);
			System.out.println("\t" + String.join(", ", getPlayerNames()));
			System.out.println();

			ArrayList<Player> deadPlayers = new ArrayList<Player>();
			for (Player player : this.players) {
				if (player.getDead()) {
					deadPlayers.add(player);
					continue;
				}
				System.out.println("It is " + player.getName() + "'s turn. Only " + player.getName() + " may look at the screen.");
				System.out.print("Press enter to start " + player.getName() + "'s turn.");
				scan.nextLine();
				player.doTurn();
				gameOver = (getAlivePlayers().size() == 1);
				if (gameOver) {
					break;
				}
			}

			for (Player player : deadPlayers) {
				this.players.remove(player);
			}

			if (!gameOver) {
				System.out.println("The round is over.");
				System.out.print("Press enter to start the next round.");
				scan.nextLine();
			}
			clearScreen();
		}

		System.out.println("The game is over.");
		System.out.println("The winner is: " + this.players.get(0).getName());
		System.out.print("Press enter to quit.");
		scan.nextLine();
	}

	public void performAttack(Player attacker, Player victim, int attackValue) {
		Scanner scan = new Scanner(System.in);
		System.out.println(attacker.getName() + " is attacking " + victim.getName() + ". Only " + victim.getName() + " may look at the screen.");
		System.out.print("Press enter to continue.");
		scan.nextLine();
		clearScreen();
		victim.doWasAttacked(attacker, attackValue);
	}

	public void completeAttack(Player attacker, Player victim, int attackValue, int defenseValue) {
		Scanner scan = new Scanner(System.in);
		System.out.println(attacker.getName() + " attacked with a value of " + attackValue + ".");
		if (defenseValue == 0) {
			System.out.println(victim.getName() + " did not defend.");
		}
		else {
			System.out.println(victim.getName() + " defended with a value of " + defenseValue + ".");
		}

		if (attackValue > defenseValue) {
			System.out.println(attacker.getName() + "'s attack was successful.");
			int numHealthLost = attackValue - defenseValue;
			if (numHealthLost > 1) {
				System.out.println(victim.getName() + " lost " + numHealthLost + " health points.");
			}
			else {
				System.out.println(victim.getName() + " lost " + numHealthLost + " health point.");
			}
			victim.loseHealth(numHealthLost);
			if (victim.getHealth() == 0) {
				playerDied(attacker, victim);
			}
		}
		else {
			System.out.println(victim.getName() + "'s defense was successful.");
		}
		System.out.print("Press enter to end " + attacker.getName() + "'s turn.");
		scan.nextLine();
	}

	public void playerDied(Player attacker, Player victim) {
		victim.setDead();
		System.out.println(victim.getName() + " died!");
		System.out.println();
		victim.printStealableCards();
		System.out.println();
		if (victim.getNumStealableCards() > 0) {
			System.out.println(attacker.getName() + " gets to steal 3 attack or defense cards from " + victim.getName() + ".");
			attacker.doStealCardsFromPlayer(victim, 3);
		}
		else {
			System.out.println(victim.getName() + " has no cards to steal.");
			return;
		}

		if (victim.getNumStealableCards() == 0) {
			System.out.println(victim.getName() + " has no more cards.");
			return;
		}

		if (getAlivePlayers().size() > 1) {
			System.out.println("Each other player, ordered by their speed stat, gets to steal 1 attack or defense card from " + victim.getName() + ".");
			System.out.println();
			victim.printStealableCards();
			System.out.println();
			Collections.sort(this.players);
			for (Player player : getAlivePlayers()) {
				if (player != attacker) {
					player.doStealCardsFromPlayer(victim, 1);
					if (victim.getNumStealableCards() == 0) {
						System.out.println(victim.getName() + " has no more cards.");
						return;
					}
				}
			}
		}

		for (Card card : victim.getAttackCards()) {
			this.deck.addCard(card);
		}
		for (Card card : victim.getDefenseCards()) {
			this.deck.addCard(card);
		}

		System.out.println("Everyone has stolen their cards.");
	}
}
